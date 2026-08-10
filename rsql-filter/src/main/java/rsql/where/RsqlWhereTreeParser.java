package rsql.where;

import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.Token;
import rsql.antlr.where.RsqlWhereLexer;
import rsql.antlr.where.RsqlWhereParser;
import rsql.exceptions.SyntaxErrorException;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;

/**
 * RsqlWhereParser is a class that parses a string into a ParseTree.
 *
 */
public class RsqlWhereTreeParser {

    /**
     * How deeply grouping parentheses may nest.
     * <p>
     * The parser descends one JVM frame per level, so past a point it overflows the stack. Measured on this
     * code base, one fresh JVM per data point: about 1 237 levels on a 512k stack and 2 801 on a 1M one. The
     * default leaves an order of magnitude of headroom below the smaller figure - hand-written filters nest a
     * handful of levels, machine-generated ones rarely more than a dozen.
     */
    public static final int DEFAULT_MAX_NESTING_DEPTH = 100;

    /**
     * How deep the parse tree may be.
     * <p>
     * This is what bounds the <em>visitors</em>, which walk the tree recursively. It is not the same thing as
     * the number of conditions: {@code a==1;a==1;...} builds a left-deep tree one level per condition, while a
     * balanced filter with 8 192 conditions is only about 32 levels deep. Measured overflow: about 1 630 levels
     * on a 512k stack, 3 750 on a 1M one.
     */
    public static final int DEFAULT_MAX_TREE_DEPTH = 500;

    private static volatile int maxNestingDepth = DEFAULT_MAX_NESTING_DEPTH;
    private static volatile int maxTreeDepth = DEFAULT_MAX_TREE_DEPTH;

    public static int getMaxNestingDepth() {
        return maxNestingDepth;
    }

    /**
     * Raise or lower the nesting limit. Sizing it above the measured overflow point trades a clean
     * {@link SyntaxErrorException} for a {@link StackOverflowError}, which callers cannot catch.
     *
     * @param value The new limit
     */
    public static void setMaxNestingDepth(int value) {
        maxNestingDepth = value;
    }

    public static int getMaxTreeDepth() {
        return maxTreeDepth;
    }

    /**
     * Raise or lower the parse tree depth limit. Same trade-off as {@link #setMaxNestingDepth(int)}.
     *
     * @param value The new limit
     */
    public static void setMaxTreeDepth(int value) {
        maxTreeDepth = value;
    }

    public ParseTree parseStream(CharStream inputStream) {
        RsqlWhereLexer lexer = new BailRsqlWhereLexer(inputStream);
        lexer.removeErrorListeners();
        lexer.addErrorListener(new RsqlWhereErrorListener());

        CommonTokenStream tokens = new CommonTokenStream(lexer);

        verifyNestingIsWithinLimit(tokens);

        RsqlWhereParser parser = new RsqlWhereParser(tokens);
        parser.removeErrorListeners();
        parser.addErrorListener(new RsqlWhereErrorListener());
        parser.setErrorHandler(new CustomErrorStrategy());

        ParseTree tree = parser.where();
        verifyWholeInputWasUsed(tokens, tree);
        verifyTreeDepthIsWithinLimit(tree);

        return tree;
    }

    /**
     * Bound the recursion the <em>parser</em> is about to do, before it does it.
     * <p>
     * Only grouping parentheses count. The ones that delimit an {@code IN} / {@code NIN} / {@code BT} /
     * {@code NBT} argument list do not nest the {@code condition} rule, so counting them would reject filters
     * such as {@code a=in=(1,2);b=in=(3,4)} that parse in microseconds. Parentheses inside a string literal
     * never reach this loop at all - the lexer has already folded them into a single token.
     *
     * @param tokens The token stream, which this method fills
     */
    private void verifyNestingIsWithinLimit(CommonTokenStream tokens) {
        // fill() is required: getTokens() returns an empty list on an unfilled stream, which would make this
        // check silently do nothing while every valid filter still parsed
        tokens.fill();
        List<Token> all = tokens.getTokens();

        Deque<Boolean> argumentList = new ArrayDeque<>();
        int depth = 0;

        for (int i = 0; i < all.size(); i++) {
            int type = all.get(i).getType();
            if (type == RsqlWhereLexer.LR_BRACKET) {
                // operatorIN is '=' IN '=', three tokens, so the operator sits two places back - not one
                int operator = i >= 2 ? all.get(i - 2).getType() : Token.INVALID_TYPE;
                boolean isArgumentList = operator == RsqlWhereLexer.IN
                    || operator == RsqlWhereLexer.NIN
                    || operator == RsqlWhereLexer.BT
                    || operator == RsqlWhereLexer.NBT;
                argumentList.push(isArgumentList);
                if (!isArgumentList && ++depth > maxNestingDepth) {
                    throw new SyntaxErrorException(
                        "Filter is nested too deeply at position " + all.get(i).getStartIndex()
                            + " - at most " + maxNestingDepth + " levels of parentheses are allowed"
                    );
                }
            } else if (type == RsqlWhereLexer.RR_BRACKET) {
                if (!argumentList.isEmpty() && !argumentList.pop()) {
                    depth--;
                }
            }
        }
    }

    /**
     * Bound the recursion the <em>visitors</em> are about to do.
     * <p>
     * Walks the tree iteratively, so measuring the depth cannot itself overflow. Runs after parsing because
     * the depth is a property of the tree, not of the token stream: the number of logical operators is a poor
     * proxy for it, since a balanced filter with thousands of conditions is only tens of levels deep.
     *
     * @param tree The parse tree returned by the start rule
     */
    private void verifyTreeDepthIsWithinLimit(ParseTree tree) {
        Deque<ParseTree> nodes = new ArrayDeque<>();
        Deque<Integer> depths = new ArrayDeque<>();
        nodes.push(tree);
        depths.push(1);

        while (!nodes.isEmpty()) {
            ParseTree node = nodes.pop();
            int depth = depths.pop();
            if (depth > maxTreeDepth) {
                throw new SyntaxErrorException(
                    "Filter is structured too deeply - at most " + maxTreeDepth
                        + " levels of nested conditions are allowed"
                );
            }
            for (int i = 0; i < node.getChildCount(); i++) {
                nodes.push(node.getChild(i));
                depths.push(depth + 1);
            }
        }
    }

    /**
     * Reject input that the parser did not fully turn into a single filter expression.
     * <p>
     * The start rule is {@code where: condition+}, which is not anchored to {@code EOF} and which accepts
     * several conditions next to each other without a logical operator. Without this check both cases pass
     * silently: leftover tokens are discarded, and of several juxtaposed conditions the visitor keeps only
     * the last one - so the executed query is wider than the filter that was written.
     *
     * @param tokens The token stream the parser consumed from
     * @param tree   The parse tree returned by the start rule
     */
    private void verifyWholeInputWasUsed(CommonTokenStream tokens, ParseTree tree) {
        // trailing NEWLINE tokens are not consumed by any parser rule, so they are not an error
        int i = 1;
        while (tokens.LA(i) == RsqlWhereLexer.NEWLINE) {
            i++;
        }
        if (tokens.LA(i) != Token.EOF) {
            throw new SyntaxErrorException(
                "Unexpected input after the filter expression at position " + tokens.LT(i).getStartIndex()
            );
        }

        if (tree.getChildCount() > 1) {
            throw new SyntaxErrorException("Missing logical operator between conditions");
        }
    }
}

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
     * Every parenthesis counts. An earlier version exempted {@code IN} / {@code NIN} / {@code BT} /
     * {@code NBT} argument lists, on the theory that they do not nest the {@code condition} rule - but the
     * exemption was both unnecessary and harmful. Unnecessary because the depth is decremented on the closing
     * token, so {@code a=in=(1,2);b=in=(3,4)} never exceeds one level however many conditions follow. Harmful
     * because the same reasoning, applied to the aggregate calls of SELECT and HAVING, hid a genuinely
     * recursive path: {@code functionArg} may itself be a {@code functionCall}, so {@code SUM(SUM(SUM(...)))}
     * recursed in the parser while the check saw nothing.
     * <p>
     * Parentheses inside a string literal never reach this loop - the lexer has already folded them into a
     * single token.
     *
     * @param tokens The token stream, which this method fills
     */
    private void verifyNestingIsWithinLimit(CommonTokenStream tokens) {
        // fill() is required: getTokens() returns an empty list on an unfilled stream, which would make this
        // check silently do nothing while every valid filter still parsed
        tokens.fill();
        verifyNestingIsWithinLimit(tokens.getTokens(), maxNestingDepth);
    }

    /**
     * Package-private so a test can drive it directly. Measuring it through the public facade would mix its
     * cost with the parser's, and the parser is not linear on every shape - an {@code IN} list of n elements,
     * for one, costs roughly O(n^2) to parse.
     *
     * @param all             The filled token list
     * @param maxNestingDepth The limit to enforce
     */
    static void verifyNestingIsWithinLimit(List<Token> all, int maxNestingDepth) {
        int depth = 0;
        for (Token token : all) {
            int type = token.getType();
            if (type == RsqlWhereLexer.LR_BRACKET) {
                if (++depth > maxNestingDepth) {
                    throw new SyntaxErrorException(
                        "Filter is nested too deeply at position " + token.getStartIndex()
                            + " - at most " + maxNestingDepth + " levels of parentheses are allowed"
                    );
                }
            } else if (type == RsqlWhereLexer.RR_BRACKET) {
                depth--;
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

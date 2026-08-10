package rsql.having;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.ParseTree;
import rsql.antlr.having.RsqlHavingLexer;
import rsql.antlr.having.RsqlHavingParser;
import rsql.exceptions.SyntaxErrorException;
import rsql.where.CustomErrorStrategy;
import rsql.where.RsqlWhereTreeParser;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;

/**
 * Parser for HAVING clause that creates a ParseTree from input string.
 * Uses BailRsqlHavingLexer for fail-fast lexing and HavingErrorListener for error handling.
 */
public class HavingTreeParser {

    /**
     * Parses a CharStream into a ParseTree for HAVING clause.
     *
     * @param inputStream The CharStream containing the HAVING filter string
     * @return ParseTree representing the parsed HAVING clause
     */
    public ParseTree parseStream(CharStream inputStream) {
        // Create bail-out lexer
        RsqlHavingLexer lexer = new BailRsqlHavingLexer(inputStream);
        lexer.removeErrorListeners();
        lexer.addErrorListener(new HavingErrorListener());

        // Create token stream
        CommonTokenStream tokens = new CommonTokenStream(lexer);

        verifyNestingIsWithinLimit(tokens);

        // Create parser
        RsqlHavingParser parser = new RsqlHavingParser(tokens);
        parser.removeErrorListeners();
        parser.addErrorListener(new HavingErrorListener());
        parser.setErrorHandler(new CustomErrorStrategy());

        // Parse the 'having' rule (entry point)
        ParseTree tree = parser.having();
        verifyWholeInputWasUsed(tokens, tree);
        verifyTreeDepthIsWithinLimit(tree);

        return tree;
    }

    /**
     * Bound the recursion the parser and the visitors are about to do. Same defect and same reasoning as in
     * {@link RsqlWhereTreeParser}: measured on a 1M stack, HAVING overflows at about 5 000 nested
     * parentheses. The limits are shared with the WHERE side, since a caller tuning one would mean the other.
     * <p>
     * Two kinds of parenthesis do not nest {@code havingCondition} and so must not count: the argument list
     * of {@code IN} / {@code NIN} / {@code BT} / {@code NBT}, and the argument list of an aggregate function
     * such as {@code SUM(price)}.
     *
     * @param tokens The token stream, which this method fills
     */
    private void verifyNestingIsWithinLimit(CommonTokenStream tokens) {
        // fill() is required: getTokens() returns an empty list on an unfilled stream, which would leave
        // this check silently doing nothing
        tokens.fill();
        List<Token> all = tokens.getTokens();

        int maxNestingDepth = RsqlWhereTreeParser.getMaxNestingDepth();
        Deque<Boolean> argumentList = new ArrayDeque<>();
        int depth = 0;

        for (int i = 0; i < all.size(); i++) {
            int type = all.get(i).getType();
            if (type == RsqlHavingLexer.LR_BRACKET) {
                if (!isGroupingParenthesis(all, i)) {
                    argumentList.push(true);
                } else {
                    argumentList.push(false);
                    if (++depth > maxNestingDepth) {
                        throw new SyntaxErrorException(
                            "HAVING clause is nested too deeply at position " + all.get(i).getStartIndex()
                                + " - at most " + maxNestingDepth + " levels of parentheses are allowed"
                        );
                    }
                }
            } else if (type == RsqlHavingLexer.RR_BRACKET) {
                if (!argumentList.isEmpty() && !argumentList.pop()) {
                    depth--;
                }
            }
        }
    }

    private boolean isGroupingParenthesis(List<Token> all, int index) {
        // an aggregate function puts its name immediately before the '('
        int previous = index >= 1 ? all.get(index - 1).getType() : Token.INVALID_TYPE;
        if (previous == RsqlHavingLexer.SUM || previous == RsqlHavingLexer.AVG
            || previous == RsqlHavingLexer.COUNT || previous == RsqlHavingLexer.MIN
            || previous == RsqlHavingLexer.MAX || previous == RsqlHavingLexer.GRP) {
            return false;
        }
        // operatorIN is '=' IN '=', three tokens, so its keyword sits two places back
        int operator = index >= 2 ? all.get(index - 2).getType() : Token.INVALID_TYPE;
        return operator != RsqlHavingLexer.IN && operator != RsqlHavingLexer.NIN
            && operator != RsqlHavingLexer.BT && operator != RsqlHavingLexer.NBT;
    }

    /**
     * Bound the recursion the visitors are about to do. Walks iteratively, so measuring cannot itself
     * overflow.
     *
     * @param tree The parse tree returned by the start rule
     */
    private void verifyTreeDepthIsWithinLimit(ParseTree tree) {
        int maxTreeDepth = RsqlWhereTreeParser.getMaxTreeDepth();
        Deque<ParseTree> nodes = new ArrayDeque<>();
        Deque<Integer> depths = new ArrayDeque<>();
        nodes.push(tree);
        depths.push(1);

        while (!nodes.isEmpty()) {
            ParseTree node = nodes.pop();
            int depth = depths.pop();
            if (depth > maxTreeDepth) {
                throw new SyntaxErrorException(
                    "HAVING clause is structured too deeply - at most " + maxTreeDepth
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
     * Reject input that the parser did not fully turn into a single HAVING expression.
     * <p>
     * The start rule is {@code having: havingCondition+}, which is not anchored to {@code EOF} and which
     * accepts several conditions next to each other without a logical operator. Without this check both
     * cases pass silently: leftover tokens are discarded, and of several juxtaposed conditions the visitor
     * keeps only the last one.
     *
     * @param tokens The token stream the parser consumed from
     * @param tree   The parse tree returned by the start rule
     */
    private void verifyWholeInputWasUsed(CommonTokenStream tokens, ParseTree tree) {
        // trailing NEWLINE tokens are not consumed by any parser rule, so they are not an error
        int i = 1;
        while (tokens.LA(i) == RsqlHavingLexer.NEWLINE) {
            i++;
        }
        if (tokens.LA(i) != Token.EOF) {
            throw new SyntaxErrorException(
                "Unexpected input after the having expression at position " + tokens.LT(i).getStartIndex()
            );
        }

        if (tree.getChildCount() > 1) {
            throw new SyntaxErrorException("Missing logical operator between conditions");
        }
    }
}

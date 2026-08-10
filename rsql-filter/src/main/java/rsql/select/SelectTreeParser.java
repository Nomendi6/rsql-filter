package rsql.select;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.ParseTree;
import rsql.antlr.select.RsqlSelectLexer;
import rsql.antlr.select.RsqlSelectParser;
import rsql.exceptions.SyntaxErrorException;
import rsql.where.CustomErrorStrategy;
import rsql.where.RsqlWhereTreeParser;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;

/**
 * SelectTreeParser is a class that parses a SELECT string into a ParseTree.
 * It uses ANTLR-generated RsqlSelectLexer and RsqlSelectParser.
 */
public class SelectTreeParser {

    /*
     * RsqlSelect.g4 writes its parentheses as inline literals, so ANTLR names them T__n and renumbers them
     * whenever the grammar gains or loses a literal. Resolving them from the vocabulary keeps this code
     * correct across such edits.
     */
    private static final int LEFT_PARENTHESIS = tokenTypeOf("'('");
    private static final int RIGHT_PARENTHESIS = tokenTypeOf("')'");

    private static int tokenTypeOf(String literal) {
        for (int type = 0; type <= RsqlSelectLexer.VOCABULARY.getMaxTokenType(); type++) {
            if (literal.equals(RsqlSelectLexer.VOCABULARY.getLiteralName(type))) {
                return type;
            }
        }
        throw new IllegalStateException("RsqlSelect.g4 no longer defines the " + literal + " token");
    }

    /**
     * Parses a CharStream input into a ParseTree.
     *
     * @param inputStream The CharStream containing the SELECT clause to parse
     * @return ParseTree representing the parsed SELECT clause
     * @throws rsql.exceptions.SyntaxErrorException if syntax error is encountered
     */
    public ParseTree parseStream(CharStream inputStream) {
        // Create lexer with bail-out behavior
        RsqlSelectLexer lexer = new BailRsqlSelectLexer(inputStream);
        lexer.removeErrorListeners();
        lexer.addErrorListener(new SelectErrorListener());

        // Create token stream
        CommonTokenStream tokens = new CommonTokenStream(lexer);

        verifyNestingIsWithinLimit(tokens);

        // Create parser with custom error handling
        RsqlSelectParser parser = new RsqlSelectParser(tokens);
        parser.removeErrorListeners();
        parser.addErrorListener(new SelectErrorListener());
        parser.setErrorHandler(new CustomErrorStrategy());

        // Parse the 'select' rule (entry point)
        ParseTree tree = parser.select();
        verifyWholeInputWasUsed(tokens);
        verifyTreeDepthIsWithinLimit(tree);

        return tree;
    }

    /**
     * The parentheses of {@code '(' expression ')'} are the only ones that nest a rule, and the parser
     * descends one JVM frame per level. An aggregate call such as {@code SUM(price)} does not nest, so its
     * parentheses must not count - otherwise a clause of a few hundred aggregates would be rejected.
     * <p>
     * Shares its limits with {@link RsqlWhereTreeParser}: a caller tuning one would mean the other.
     *
     * @param tokens The token stream, which this method fills
     */
    private void verifyNestingIsWithinLimit(CommonTokenStream tokens) {
        // fill() is required: getTokens() returns an empty list on an unfilled stream, which would leave
        // this check silently doing nothing while every valid clause still parsed
        tokens.fill();
        List<Token> all = tokens.getTokens();

        int maxNestingDepth = RsqlWhereTreeParser.getMaxNestingDepth();
        Deque<Boolean> functionCall = new ArrayDeque<>();
        int depth = 0;

        for (int i = 0; i < all.size(); i++) {
            int type = all.get(i).getType();
            if (type == LEFT_PARENTHESIS) {
                int previous = i >= 1 ? all.get(i - 1).getType() : Token.INVALID_TYPE;
                boolean isFunctionCall = previous == RsqlSelectLexer.AVG || previous == RsqlSelectLexer.MAX
                    || previous == RsqlSelectLexer.MIN || previous == RsqlSelectLexer.SUM
                    || previous == RsqlSelectLexer.COUNT || previous == RsqlSelectLexer.GRP;
                functionCall.push(isFunctionCall);
                if (!isFunctionCall && ++depth > maxNestingDepth) {
                    throw new SyntaxErrorException(
                        "Select expression is nested too deeply at position " + all.get(i).getStartIndex()
                            + " - at most " + maxNestingDepth + " levels of parentheses are allowed"
                    );
                }
            } else if (type == RIGHT_PARENTHESIS) {
                if (!functionCall.isEmpty() && !functionCall.pop()) {
                    depth--;
                }
            }
        }
    }

    /**
     * Bound the recursion the select visitors are about to do. Walks iteratively, so measuring cannot
     * itself overflow.
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
                    "Select expression is structured too deeply - at most " + maxTreeDepth
                        + " levels of nesting are allowed"
                );
            }
            for (int i = 0; i < node.getChildCount(); i++) {
                nodes.push(node.getChild(i));
                depths.push(depth + 1);
            }
        }
    }

    /**
     * Reject input that the parser did not fully consume.
     * <p>
     * The start rule {@code select: selectElements+} is not anchored to {@code EOF}, so without this check
     * leftover tokens are discarded silently - {@code name)} parses as {@code name}.
     *
     * @param tokens The token stream the parser consumed from
     */
    private void verifyWholeInputWasUsed(CommonTokenStream tokens) {
        // unlike the WHERE/HAVING grammars, RsqlSelect.g4 skips all whitespace (WS rule),
        // so there is no trailing token to tolerate here
        if (tokens.LA(1) != Token.EOF) {
            throw new SyntaxErrorException(
                "Unexpected input after the select expression at position " + tokens.LT(1).getStartIndex()
            );
        }
    }
}

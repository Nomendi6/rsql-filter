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
     * Every parenthesis counts, including an aggregate call such as {@code SUM(price)}. {@code functionArg}
     * may itself be a {@code functionCall} (RsqlSelect.g4:64-67), so {@code SUM(SUM(SUM(...)))} recurses
     * through the parser - exempting those parentheses hid it, and 1 000 levels overflowed the stack before
     * the tree depth check could run. Exempting them was never necessary: the depth is decremented on the
     * closing token, so a clause of 300 aggregates never exceeds one level.
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
        int depth = 0;
        for (Token token : all) {
            int type = token.getType();
            if (type == LEFT_PARENTHESIS) {
                if (++depth > maxNestingDepth) {
                    throw new SyntaxErrorException(
                        "Select expression is nested too deeply at position " + token.getStartIndex()
                            + " - at most " + maxNestingDepth + " levels of parentheses are allowed"
                    );
                }
            } else if (type == RIGHT_PARENTHESIS) {
                depth--;
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

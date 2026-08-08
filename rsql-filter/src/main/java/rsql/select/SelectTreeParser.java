package rsql.select;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.ParseTree;
import rsql.antlr.select.RsqlSelectLexer;
import rsql.antlr.select.RsqlSelectParser;
import rsql.exceptions.SyntaxErrorException;
import rsql.where.CustomErrorStrategy;

/**
 * SelectTreeParser is a class that parses a SELECT string into a ParseTree.
 * It uses ANTLR-generated RsqlSelectLexer and RsqlSelectParser.
 */
public class SelectTreeParser {

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

        // Create parser with custom error handling
        RsqlSelectParser parser = new RsqlSelectParser(tokens);
        parser.removeErrorListeners();
        parser.addErrorListener(new SelectErrorListener());
        parser.setErrorHandler(new CustomErrorStrategy());

        // Parse the 'select' rule (entry point)
        ParseTree tree = parser.select();
        verifyWholeInputWasUsed(tokens);

        return tree;
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

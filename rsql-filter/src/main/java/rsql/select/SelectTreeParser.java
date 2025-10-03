package rsql.select;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import rsql.antlr.select.RsqlSelectLexer;
import rsql.antlr.select.RsqlSelectParser;
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

        // Parse and return the parse tree (entry point is 'select' rule)
        return parser.select();
    }
}

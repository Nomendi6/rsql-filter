package rsql.having;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import rsql.antlr.having.RsqlHavingLexer;
import rsql.antlr.having.RsqlHavingParser;
import rsql.where.CustomErrorStrategy;

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

        // Create parser
        RsqlHavingParser parser = new RsqlHavingParser(tokens);
        parser.removeErrorListeners();
        parser.addErrorListener(new HavingErrorListener());
        parser.setErrorHandler(new CustomErrorStrategy());

        // Parse and return the 'having' rule (entry point)
        return parser.having();
    }
}

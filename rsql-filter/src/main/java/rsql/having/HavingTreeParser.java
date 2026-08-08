package rsql.having;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.ParseTree;
import rsql.antlr.having.RsqlHavingLexer;
import rsql.antlr.having.RsqlHavingParser;
import rsql.exceptions.SyntaxErrorException;
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

        // Parse the 'having' rule (entry point)
        ParseTree tree = parser.having();
        verifyWholeInputWasUsed(tokens, tree);

        return tree;
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

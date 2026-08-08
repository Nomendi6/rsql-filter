package rsql.where;

import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.Token;
import rsql.antlr.where.RsqlWhereLexer;
import rsql.antlr.where.RsqlWhereParser;
import rsql.exceptions.SyntaxErrorException;

/**
 * RsqlWhereParser is a class that parses a string into a ParseTree.
 *
 */
public class RsqlWhereTreeParser {

    public ParseTree parseStream(CharStream inputStream) {
        RsqlWhereLexer lexer = new BailRsqlWhereLexer(inputStream);
        lexer.removeErrorListeners();
        lexer.addErrorListener(new RsqlWhereErrorListener());

        CommonTokenStream tokens = new CommonTokenStream(lexer);

        RsqlWhereParser parser = new RsqlWhereParser(tokens);
        parser.removeErrorListeners();
        parser.addErrorListener(new RsqlWhereErrorListener());
        parser.setErrorHandler(new CustomErrorStrategy());

        ParseTree tree = parser.where();
        verifyWholeInputWasUsed(tokens, tree);

        return tree;
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

package rsql.where;

import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CommonTokenStream;
import rsql.antlr.where.RsqlWhereLexer;
import rsql.antlr.where.RsqlWhereParser;

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
        parser.setErrorHandler(new BailErrorStrategy());

        return parser.where();
    }
}

package rsql.where;

import rsql.antlr.where.RsqlWhereLexer;
import rsql.antlr.where.RsqlWhereParser;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;

import java.io.IOException;

/**
 * RsqlWhereString convert rsql expression to string.
 * <p>
 * It DOES NOT interpret the content of rsql and to map it to the related entity.
 * </p>
 */
public class RsqlWhereString {

    private RsqlWhereTreeParser treeParser;
    public RsqlWhereString() {
        treeParser = new RsqlWhereTreeParser();
    }

    /**
     * Parse a file containing an rsql expression.
     *
     * @param inputFile     The file that contains an expression.
     * @return              Where expression.
     * @throws IOException  If something happens during the file read operation.
     */
    public String parseFile(String inputFile) throws IOException {
        CharStream inputStream = CharStreams.fromFileName(inputFile);
        return processStream(inputStream);
    }

    /**
     * Parse a string containing an rsql expression.
     *
     * @param inputString   The string that contains an expression
     * @return              Where expression.
     */
    public String parseString(String inputString) {
        CharStream inputStream = CharStreams.fromString(inputString);
        return processStream(inputStream);
    }

    /**
     * A method that is called to process the input stream to convert an rsql expression to a where clause.
     *
     * @param inputStream Stream that contains a rsql expression.
     * @return            Where expression.
     */
    private String processStream(CharStream inputStream) {
        ParseTree tree = this.treeParser.parseStream(inputStream);
        WhereStringVisitor visitor = new WhereStringVisitor();
        return visitor.visit(tree);
    }
}

package rsql.where;

import org.antlr.v4.runtime.CharStream;
import rsql.antlr.where.RsqlWhereLexer;
import rsql.exceptions.SyntaxErrorException;

public class BailRsqlWhereLexer extends RsqlWhereLexer {
    public BailRsqlWhereLexer(CharStream input) {
        super(input);
    }

    public void recover(org.antlr.v4.runtime.RecognitionException e) {
        throw new SyntaxErrorException("Error parsing query:" + e.getMessage());
    }
}

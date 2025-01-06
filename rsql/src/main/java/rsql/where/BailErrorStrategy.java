package rsql.where;

import org.antlr.v4.runtime.DefaultErrorStrategy;
import org.antlr.v4.runtime.Token;
import rsql.exceptions.SyntaxErrorException;

public class BailErrorStrategy extends DefaultErrorStrategy {

    @Override
    public void recover(org.antlr.v4.runtime.Parser recognizer, org.antlr.v4.runtime.RecognitionException e) {
        throw new SyntaxErrorException("Error parsing query:" + e.getMessage());
    }

    @Override
    public Token recoverInline(org.antlr.v4.runtime.Parser recognizer) throws org.antlr.v4.runtime.InputMismatchException {
        throw new SyntaxErrorException("Error parsing query:" + recognizer.getCurrentToken().getText());
    }

    @Override
    public void sync(org.antlr.v4.runtime.Parser recognizer) {
    }
}

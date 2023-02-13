package rsql.where;

import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import rsql.exceptions.SyntaxErrorException;

public class RsqlWhereErrorListener extends BaseErrorListener {
    @Override
    public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line, int charPositionInLine, String msg, RecognitionException e) {
        throw new SyntaxErrorException("line " + line + ":" + charPositionInLine + " " + msg);
    }
}

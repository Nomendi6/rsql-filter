package rsql.select;

import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import rsql.exceptions.SyntaxErrorException;

/**
 * Error listener for SELECT clause parsing.
 * Throws SyntaxErrorException when a syntax error is encountered during parsing.
 */
public class SelectErrorListener extends BaseErrorListener {
    @Override
    public void syntaxError(
        Recognizer<?, ?> recognizer,
        Object offendingSymbol,
        int line,
        int charPositionInLine,
        String msg,
        RecognitionException e
    ) {
        throw new SyntaxErrorException(
            "Syntax error in SELECT clause at position " + charPositionInLine + ": " + msg
        );
    }
}

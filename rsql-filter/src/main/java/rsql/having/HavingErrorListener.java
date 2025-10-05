package rsql.having;

import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import rsql.exceptions.SyntaxErrorException;

/**
 * Error listener for HAVING clause parsing.
 * Catches ANTLR syntax errors and throws SyntaxErrorException.
 */
public class HavingErrorListener extends BaseErrorListener {

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
            "Syntax error in HAVING clause at position " + charPositionInLine + ": " + msg
        );
    }
}

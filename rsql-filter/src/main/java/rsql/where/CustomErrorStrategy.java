package rsql.where;

import org.antlr.v4.runtime.DefaultErrorStrategy;
import org.antlr.v4.runtime.Token;
import rsql.exceptions.SyntaxErrorException;

/**
 * This class extends the DefaultErrorStrategy class and overrides the recover methods.
 * It is used to handle RecognitionException and throw a custom SyntaxErrorException.
 */
public class CustomErrorStrategy extends DefaultErrorStrategy {

    /**
     * This method is used to handle RecognitionException.
     * It overrides the recover method of the super class.
     * When a RecognitionException occurs, it throws a custom SyntaxErrorException with a message.
     *
     * @param recognizer The Parser that is currently in use.
     * @param e The RecognitionException that occurred.
     * @throws SyntaxErrorException when a RecognitionException occurs.
     */
    @Override
    public void recover(org.antlr.v4.runtime.Parser recognizer, org.antlr.v4.runtime.RecognitionException e) {
        throw new SyntaxErrorException("Error parsing query:" + e.getMessage());
    }

    /**
     * This method is used to handle InputMismatchException.
     * It overrides the recoverInline method of the super class.
     * When an InputMismatchException occurs, it throws a custom SyntaxErrorException with a message.
     *
     * @param recognizer The Parser that is currently in use.
     * @throws SyntaxErrorException when an InputMismatchException occurs.
     */
    @Override
    public Token recoverInline(org.antlr.v4.runtime.Parser recognizer) throws org.antlr.v4.runtime.InputMismatchException {
        throw new SyntaxErrorException("Error parsing query:" + recognizer.getCurrentToken().getText());
    }

    /**
     * This method is used to synchronize the Parser.
     * It overrides the sync method of the super class.
     * In this implementation, the method is empty.
     *
     * @param recognizer The Parser that is currently in use.
     */
    @Override
    public void sync(org.antlr.v4.runtime.Parser recognizer) {
    }
}

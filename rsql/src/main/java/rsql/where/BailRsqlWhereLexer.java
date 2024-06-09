package rsql.where;

import org.antlr.v4.runtime.CharStream;
import rsql.antlr.where.RsqlWhereLexer;
import rsql.exceptions.SyntaxErrorException;

/**
 * This class extends the RsqlWhereLexer class and overrides the recover method.
 * It is used to handle RecognitionException and throw a custom SyntaxErrorException.
 */

public class BailRsqlWhereLexer extends RsqlWhereLexer {

    /**
     * Constructor for the BailRsqlWhereLexer class.
     * It calls the super constructor of RsqlWhereLexer with the provided input.
     *
     * @param input The CharStream input that is passed to the super constructor of RsqlWhereLexer.
     */
    public BailRsqlWhereLexer(CharStream input) {
        super(input);
    }

    /**
     * This method is used to handle RecognitionException.
     * It overrides the recover method of the super class.
     * When a RecognitionException occurs, it throws a custom SyntaxErrorException with a message.
     *
     * @param e The RecognitionException that occurred.
     * @throws SyntaxErrorException when a RecognitionException occurs.
     */
    public void recover(org.antlr.v4.runtime.RecognitionException e) {
        throw new SyntaxErrorException("Error parsing query:" + e.getMessage());
    }
}

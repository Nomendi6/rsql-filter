package rsql.select;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.LexerNoViableAltException;
import rsql.antlr.select.RsqlSelectLexer;
import rsql.exceptions.SyntaxErrorException;

/**
 * This class extends the RsqlSelectLexer class and overrides the recover method.
 * It is used to handle LexerNoViableAltException and throw a custom SyntaxErrorException,
 * ensuring the parser fails fast on the first lexer error.
 */
public class BailRsqlSelectLexer extends RsqlSelectLexer {

    /**
     * Constructor for the BailRsqlSelectLexer class.
     * It calls the super constructor of RsqlSelectLexer with the provided input.
     *
     * @param input The CharStream input that is passed to the super constructor of RsqlSelectLexer.
     */
    public BailRsqlSelectLexer(CharStream input) {
        super(input);
    }

    /**
     * This method is used to handle LexerNoViableAltException.
     * It overrides the recover method of the super class.
     * When a LexerNoViableAltException occurs, it throws a custom SyntaxErrorException with a message.
     *
     * @param e The LexerNoViableAltException that occurred.
     * @throws SyntaxErrorException when a LexerNoViableAltException occurs.
     */
    @Override
    public void recover(LexerNoViableAltException e) {
        throw new SyntaxErrorException("Error parsing SELECT clause: " + e.getMessage());
    }
}

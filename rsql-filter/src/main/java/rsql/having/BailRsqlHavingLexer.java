package rsql.having;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.LexerNoViableAltException;
import rsql.antlr.having.RsqlHavingLexer;

/**
 * Bail-out lexer for HAVING clause parsing.
 * Extends RsqlHavingLexer and overrides the recover method to fail fast on lexer errors.
 */
public class BailRsqlHavingLexer extends RsqlHavingLexer {

    /**
     * Constructor for BailRsqlHavingLexer.
     * Calls the super constructor of RsqlHavingLexer with the provided input.
     *
     * @param input The CharStream input passed to the super constructor.
     */
    public BailRsqlHavingLexer(CharStream input) {
        super(input);
    }

    /**
     * Overrides the recover method to fail fast on lexer errors.
     * Instead of trying to recover from errors, this method immediately throws a RuntimeException.
     *
     * @param e The LexerNoViableAltException that occurred.
     * @throws RuntimeException wrapping the original exception to bail out immediately.
     */
    @Override
    public void recover(LexerNoViableAltException e) {
        throw new RuntimeException(e);
    }
}

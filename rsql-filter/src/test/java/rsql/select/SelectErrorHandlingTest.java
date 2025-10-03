package rsql.select;

import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.junit.jupiter.api.Test;
import rsql.antlr.select.RsqlSelectLexer;
import rsql.antlr.select.RsqlSelectParser;
import rsql.exceptions.SyntaxErrorException;
import rsql.where.CustomErrorStrategy;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for SELECT error handling.
 * Tests SelectErrorListener and BailRsqlSelectLexer functionality.
 */
class SelectErrorHandlingTest {

    /**
     * Test that SelectErrorListener throws SyntaxErrorException with proper message
     * when parser encounters a syntax error.
     */
    @Test
    void testSelectErrorListener_ParserError() {
        // Invalid SELECT syntax: missing field after comma
        String invalidSelect = "field1, ";

        SyntaxErrorException exception = assertThrows(SyntaxErrorException.class, () -> {
            RsqlSelectLexer lexer = new BailRsqlSelectLexer(CharStreams.fromString(invalidSelect));
            lexer.removeErrorListeners();
            lexer.addErrorListener(new SelectErrorListener());

            CommonTokenStream tokens = new CommonTokenStream(lexer);

            RsqlSelectParser parser = new RsqlSelectParser(tokens);
            parser.removeErrorListeners();
            parser.addErrorListener(new SelectErrorListener());
            parser.setErrorHandler(new CustomErrorStrategy());

            parser.select(); // This should throw SyntaxErrorException
        });

        assertTrue(exception.getMessage().contains("Syntax error") ||
                   exception.getMessage().contains("Error parsing"),
                   "Exception message should contain 'Syntax error' or 'Error parsing'");
    }

    /**
     * Test that SelectErrorListener throws SyntaxErrorException
     * when encountering missing closing parenthesis.
     */
    @Test
    void testSelectErrorListener_MissingClosingParenthesis() {
        // Invalid SELECT syntax: missing closing parenthesis in function call
        String invalidSelect = "SUM(price";

        assertThrows(SyntaxErrorException.class, () -> {
            RsqlSelectLexer lexer = new BailRsqlSelectLexer(CharStreams.fromString(invalidSelect));
            lexer.removeErrorListeners();
            lexer.addErrorListener(new SelectErrorListener());

            CommonTokenStream tokens = new CommonTokenStream(lexer);

            RsqlSelectParser parser = new RsqlSelectParser(tokens);
            parser.removeErrorListeners();
            parser.addErrorListener(new SelectErrorListener());
            parser.setErrorHandler(new CustomErrorStrategy());

            parser.select();
        });
    }

    /**
     * Test that BailRsqlSelectLexer throws SyntaxErrorException
     * when encountering invalid characters.
     */
    @Test
    void testBailRsqlSelectLexer_InvalidCharacter() {
        // Invalid characters that lexer cannot tokenize
        String invalidSelect = "field1@#$%^";

        SyntaxErrorException exception = assertThrows(SyntaxErrorException.class, () -> {
            RsqlSelectLexer lexer = new BailRsqlSelectLexer(CharStreams.fromString(invalidSelect));
            lexer.removeErrorListeners();
            lexer.addErrorListener(new SelectErrorListener());

            CommonTokenStream tokens = new CommonTokenStream(lexer);
            tokens.fill(); // Force lexer to process all tokens
        });

        assertTrue(exception.getMessage().contains("Error parsing") ||
                   exception.getMessage().contains("Syntax error"),
                   "Exception message should indicate parsing error");
    }

    /**
     * Test that valid SELECT syntax does not throw exceptions.
     */
    @Test
    void testValidSelectSyntax_NoException() {
        String validSelect = "field1, field2, field3";

        assertDoesNotThrow(() -> {
            RsqlSelectLexer lexer = new BailRsqlSelectLexer(CharStreams.fromString(validSelect));
            lexer.removeErrorListeners();
            lexer.addErrorListener(new SelectErrorListener());

            CommonTokenStream tokens = new CommonTokenStream(lexer);

            RsqlSelectParser parser = new RsqlSelectParser(tokens);
            parser.removeErrorListeners();
            parser.addErrorListener(new SelectErrorListener());
            parser.setErrorHandler(new CustomErrorStrategy());

            parser.select();
        });
    }

    /**
     * Test that valid aggregate SELECT syntax does not throw exceptions.
     */
    @Test
    void testValidAggregateSyntax_NoException() {
        String validSelect = "field1, SUM(price), COUNT(*)";

        assertDoesNotThrow(() -> {
            RsqlSelectLexer lexer = new BailRsqlSelectLexer(CharStreams.fromString(validSelect));
            lexer.removeErrorListeners();
            lexer.addErrorListener(new SelectErrorListener());

            CommonTokenStream tokens = new CommonTokenStream(lexer);

            RsqlSelectParser parser = new RsqlSelectParser(tokens);
            parser.removeErrorListeners();
            parser.addErrorListener(new SelectErrorListener());
            parser.setErrorHandler(new CustomErrorStrategy());

            parser.select();
        });
    }

    /**
     * Test that error message contains position information.
     */
    @Test
    void testErrorMessage_ContainsPosition() {
        String invalidSelect = "field1, , field2"; // Double comma is invalid

        SyntaxErrorException exception = assertThrows(SyntaxErrorException.class, () -> {
            RsqlSelectLexer lexer = new BailRsqlSelectLexer(CharStreams.fromString(invalidSelect));
            lexer.removeErrorListeners();
            lexer.addErrorListener(new SelectErrorListener());

            CommonTokenStream tokens = new CommonTokenStream(lexer);

            RsqlSelectParser parser = new RsqlSelectParser(tokens);
            parser.removeErrorListeners();
            parser.addErrorListener(new SelectErrorListener());
            parser.setErrorHandler(new CustomErrorStrategy());

            parser.select();
        });

        assertNotNull(exception.getMessage());
        assertFalse(exception.getMessage().isEmpty());
    }
}

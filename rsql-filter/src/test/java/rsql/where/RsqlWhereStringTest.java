package rsql.where;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import rsql.exceptions.SyntaxErrorException;

import static org.junit.jupiter.api.Assertions.*;

class RsqlWhereStringTest {

    @BeforeEach
    void setUp() {
    }

    @Test
    void fieldSeqEq1() {
        RsqlWhereString parser = new RsqlWhereString();
        String result = parser.parseString("seq==1");
        assertEquals("seq=1", result);
    }

    @Test
    void fieldEqNum() {
        RsqlWhereString parser = new RsqlWhereString();
        String result = parser.parseString("field1==1");
        assertEquals("field1=1", result);
    }

    @Test
    void fieldEqNegNum() {
        RsqlWhereString parser = new RsqlWhereString();
        String result = parser.parseString("field1==-1");
        assertEquals("field1=-1", result);
    }

    @Test
    void errorMissingClosingParentheses() {
        assertThrows(SyntaxErrorException.class, () -> {
            RsqlWhereString parser = new RsqlWhereString();
            parser.parseString("(field1==1 and field2==2");
        });
    }

    @Test
    void errorMissingOpeningParentheses() {
        // the surplus ')' is rejected by verifyWholeInputWasUsed, not by the grammar - the two error
        // alternatives that used to report "Missing opening parenthesis" were removed because they made
        // every ')' ambiguous (see the note in RsqlWhere.g4). The replacement message carries the position.
        SyntaxErrorException thrown = assertThrows(SyntaxErrorException.class, () -> {
            RsqlWhereString parser = new RsqlWhereString();
            parser.parseString("(field1==1 or field2==2)) and field3==3");
        });
        assertTrue(thrown.getMessage().contains("Unexpected input after the filter expression"));
    }

    @Test
    void errorStringMissingQuote1() {
        assertThrows(SyntaxErrorException.class, () -> {
            RsqlWhereString parser = new RsqlWhereString();
            parser.parseString("field1=='text");
        });
    }

    @Test
    void fieldNotEqNum() {
        RsqlWhereString parser = new RsqlWhereString();
        String result = parser.parseString("field1!=1");
        assertEquals("field1!=1", result);
    }

    @Test
    void fieldEqString() {
        RsqlWhereString parser = new RsqlWhereString();
        String result = parser.parseString("field1=='a'");
        assertEquals("field1='a'", result);
    }
    @Test
    void fieldEqReal() {
        RsqlWhereString parser = new RsqlWhereString();
        String result = parser.parseString("field1==1.0");
        assertEquals("field1=1.0", result);
    }

    @Test
    void fieldEqNegReal() {
        RsqlWhereString parser = new RsqlWhereString();
        String result = parser.parseString("field1==-1.0");
        assertEquals("field1=-1.0", result);
    }

    @Test
    void fieldEqDate() {
        RsqlWhereString parser = new RsqlWhereString();
        String result = parser.parseString("field1==#2020-01-01#");
        assertEquals("field1='2020-01-01'", result);
    }

    @Test
    void errorDateMissingHash() {
        assertThrows(SyntaxErrorException.class, () -> {
            RsqlWhereString parser = new RsqlWhereString();
            parser.parseString("field1==#2020-01-01");
        });
    }

    @Test
    void fieldEqDatetime() {
        RsqlWhereString parser = new RsqlWhereString();
        String result = parser.parseString("field1==#2020-01-01T12:01:01Z#");
        assertEquals("field1='2020-01-01T12:01:01Z'", result);
    }

    @Test
    void fieldsAnd1() {
        RsqlWhereString parser = new RsqlWhereString();
        String result = parser.parseString("field1==1;field2==2");
        assertEquals("field1=1 and field2=2", result);
    }

    @Test
    void fieldsAnd2() {
        RsqlWhereString parser = new RsqlWhereString();
        String result = parser.parseString("field1==1 and field2==2");
        assertEquals("field1=1 and field2=2", result);
    }

    @Test
    void fieldsAnd3() {
        RsqlWhereString parser = new RsqlWhereString();
        String result = parser.parseString("(field1==1);(field2==2)");
        assertEquals("(field1=1) and (field2=2)", result);
    }

    @Test
    void fieldsAnd4() {
        RsqlWhereString parser = new RsqlWhereString();
        String result = parser.parseString("(field1==1)and(field2==2)");
        assertEquals("(field1=1) and (field2=2)", result);
    }

    @Test
    void fieldsAnd5() {
        RsqlWhereString parser = new RsqlWhereString();
        String result = parser.parseString("(((field1==1)and(field2==2))and(field3==3))");
        assertEquals("(((field1=1) and (field2=2)) and (field3=3))", result);
    }

    @Test
    void fieldsOr1() {
        RsqlWhereString parser = new RsqlWhereString();
        String result = parser.parseString("field1==1,field2==2");
        assertEquals("field1=1 or field2=2", result);
    }

    @Test
    void fieldsOr2() {
        RsqlWhereString parser = new RsqlWhereString();
        String result = parser.parseString("field1==1 or field2==2");
        assertEquals("field1=1 or field2=2", result);
    }

    @Test
    void fieldsOr3() {
        RsqlWhereString parser = new RsqlWhereString();
        String result = parser.parseString("(field1==1),(field2==2)");
        assertEquals("(field1=1) or (field2=2)", result);
    }

    @Test
    void fieldsOr4() {
        RsqlWhereString parser = new RsqlWhereString();
        String result = parser.parseString("(field1==1)or(field2==2)");
        assertEquals("(field1=1) or (field2=2)", result);
    }

    @Test
    void fieldEqField() {
        RsqlWhereString parser = new RsqlWhereString();
        String result = parser.parseString("field1==field2");
        assertEquals("field1=field2", result);
    }

    @Test
    void fieldWithDotsEqField() {
        RsqlWhereString parser = new RsqlWhereString();
        String result = parser.parseString("field1.field1a.field1aa==field2");
        assertEquals("field1.field1a.field1aa=field2", result);
    }

    @Test
    void fieldGeField() {
        RsqlWhereString parser = new RsqlWhereString();
        String result = parser.parseString("field1=ge=field2");
        assertEquals("field1>=field2", result);
    }
    @Test
    void fieldGtField() {
        RsqlWhereString parser = new RsqlWhereString();
        String result = parser.parseString("field1=gt=field2");
        assertEquals("field1>field2", result);
    }
    @Test
    void fieldLtField() {
        RsqlWhereString parser = new RsqlWhereString();
        String result = parser.parseString("field1=lt=field2");
        assertEquals("field1<field2", result);
    }
    @Test
    void fieldLeField() {
        RsqlWhereString parser = new RsqlWhereString();
        String result = parser.parseString("field1=le=field2");
        assertEquals("field1<=field2", result);
    }
    @Test
    void fieldIsNull() {
        RsqlWhereString parser = new RsqlWhereString();
        String result = parser.parseString("field1==NULL");
        assertEquals("field1 is null", result);
    }

    @Test
    void fieldIsNotNull() {
        RsqlWhereString parser = new RsqlWhereString();
        String result = parser.parseString("field1!=NULL");
        assertEquals("field1 is not null", result);
    }

    @Test
    void fieldEqEnum() {
        RsqlWhereString parser = new RsqlWhereString();
        String result = parser.parseString("field1==#ENUM#");
        assertEquals("field1='ENUM'", result);
    }

    @Test
    void fieldNotEqEnum() {
        RsqlWhereString parser = new RsqlWhereString();
        String result = parser.parseString("field1!=#ENUM#");
        assertEquals("field1!='ENUM'", result);
    }

    @Test
    void errorEnumMissingHash1() {
        assertThrows(SyntaxErrorException.class, () -> {
            RsqlWhereString parser = new RsqlWhereString();
            parser.parseString("field1==ENUM#");
        });
    }

    @Test
    void errorEnumMissingHash2() {
        assertThrows(SyntaxErrorException.class, () -> {
            RsqlWhereString parser = new RsqlWhereString();
            parser.parseString("field1==#ENUM");
        });
    }

    @Test
    void fieldEqTrue() {
        RsqlWhereString parser = new RsqlWhereString();
        String result = parser.parseString("field1==true");
        assertEquals("field1=true", result);
    }

    @Test
    void errorGtTrue() {
        assertThrows(SyntaxErrorException.class, () -> {
            RsqlWhereString parser = new RsqlWhereString();
            parser.parseString("field1=gt=true");
        });
    }

    @Test
    void errorGtNull() {
        assertThrows(SyntaxErrorException.class, () -> {
            RsqlWhereString parser = new RsqlWhereString();
            parser.parseString("field1=gt=null");
        });
    }

    @Test
    void errorGtEnum() {
        assertThrows(SyntaxErrorException.class, () -> {
            RsqlWhereString parser = new RsqlWhereString();
            parser.parseString("field1=gt=#ENUM#");
        });
    }
    @Test
    void fieldNotEqTrue() {
        RsqlWhereString parser = new RsqlWhereString();
        String result = parser.parseString("field1!=true");
        assertEquals("field1!=true", result);
    }

    @Test
    void fieldEqFalse() {
        RsqlWhereString parser = new RsqlWhereString();
        String result = parser.parseString("field1==false");
        assertEquals("field1=false", result);
    }

    @Test
    void fieldNotEqFalse() {
        RsqlWhereString parser = new RsqlWhereString();
        String result = parser.parseString("field1!=false");
        assertEquals("field1!=false", result);
    }

    @Test
    void fieldEqParam() {
        RsqlWhereString parser = new RsqlWhereString();
        String result = parser.parseString("field1==:param1");
        assertEquals("field1=:param1", result);
    }

    @Test
    void fieldBetweenInt() {
        RsqlWhereString parser = new RsqlWhereString();
        String result = parser.parseString("field1=bt=(1,2)");
        assertEquals("field1 between 1 and 2", result);
    }

    @Test
    void fieldBetweenReal() {
        RsqlWhereString parser = new RsqlWhereString();
        String result = parser.parseString("field1=bt=(1.0,2.0)");
        assertEquals("field1 between 1.0 and 2.0", result);
    }
    @Test
    void fieldBetweenString() {
        RsqlWhereString parser = new RsqlWhereString();
        String result = parser.parseString("field1=bt=('Aa','Bb')");
        assertEquals("field1 between 'Aa' and 'Bb'", result);
    }

    @Test
    void fieldBetweenDatetime() {
        RsqlWhereString parser = new RsqlWhereString();
        String result = parser.parseString("field1=bt=(#2020-01-01T12:01:01Z#,#2020-01-02T12:01:01Z#)");
        assertEquals("field1 between '2020-01-01T12:01:01Z' and '2020-01-02T12:01:01Z'", result);
    }

    @Test
    void fieldBetweenDate() {
        RsqlWhereString parser = new RsqlWhereString();
        String result = parser.parseString("field1=bt=(#2020-01-01#,#2020-01-02#)");
        assertEquals("field1 between '2020-01-01' and '2020-01-02'", result);
    }

    @Test
    void errorBtClauseMissingParentheses1() {
        assertThrows(RuntimeException.class, () -> {
            RsqlWhereString parser = new RsqlWhereString();
            parser.parseString("field1=bt=(1,2");
        });
    }

    @Test
    void errorBtClauseMissingParentheses2() {
        assertThrows(RuntimeException.class, () -> {
            RsqlWhereString parser = new RsqlWhereString();
            parser.parseString("field1=bt=1,2)");
        });
    }

    @Test
    void fieldInNumbers() {
        RsqlWhereString parser = new RsqlWhereString();
        String result = parser.parseString("field1=in=(1,2,3)");
        assertEquals("field1 in (1,2,3)", result);
    }
    @Test
    void errorInClauseMissingParentheses1() {
        assertThrows(RuntimeException.class, () -> {
            RsqlWhereString parser = new RsqlWhereString();
            parser.parseString("field1=in=(1,2,3");
        });
    }

    @Test
    void errorInClauseMissingParentheses2() {
        assertThrows(RuntimeException.class, () -> {
            RsqlWhereString parser = new RsqlWhereString();
            parser.parseString("field1=in=1,2,3)");
        });
    }

    @Test
    void fieldNotInNumbers() {
        RsqlWhereString parser = new RsqlWhereString();
        String result = parser.parseString("field1=nin=(1,2,3)");
        assertEquals("field1 not in (1,2,3)", result);
    }

    @Test
    void fieldLikeString() {
        RsqlWhereString parser = new RsqlWhereString();
        String result = parser.parseString("field1=*'A*'");
        assertEquals("lower(field1) like 'a%' escape '\\'", result);
    }

    @Test
    void fieldLikeString2() {
        RsqlWhereString parser = new RsqlWhereString();
        String result = parser.parseString("field1=like='A*'");
        assertEquals("lower(field1) like 'a%' escape '\\'", result);
    }

    // ---- case-sensitive LIKE (=clike= / =^*) : no lower(), pattern keeps its case ----

    @Test
    void fieldCLikeKeyword() {
        RsqlWhereString parser = new RsqlWhereString();
        String result = parser.parseString("field1=clike='A*'");
        assertEquals("field1 like 'A%' escape '\\'", result);
    }

    @Test
    void fieldCLikeSymbolic() {
        RsqlWhereString parser = new RsqlWhereString();
        String result = parser.parseString("field1=^*'A*'");
        assertEquals("field1 like 'A%' escape '\\'", result);
    }

    @Test
    void fieldCNLikeKeyword() {
        RsqlWhereString parser = new RsqlWhereString();
        String result = parser.parseString("field1=cnlike='A*'");
        assertEquals("field1 not like 'A%' escape '\\'", result);
    }

    @Test
    void fieldCNLikeSymbolic() {
        RsqlWhereString parser = new RsqlWhereString();
        assertEquals("field1 not like 'A%' escape '\\'", parser.parseString("field1=!^*'A*'"));
        assertEquals("field1 not like 'A%' escape '\\'", parser.parseString("field1!=^*'A*'"));
    }

    // ---- §3: un-escaping of a doubled delimiter ----

    @Test
    void unescapeDoubledDelimiterSingleQuote() {
        RsqlWhereString parser = new RsqlWhereString();
        assertEquals("field1='it''s'", parser.parseString("field1=='it''s'"));
    }

    @Test
    void unescapeDoubledDelimiterDoubleQuote() {
        RsqlWhereString parser = new RsqlWhereString();
        // "say ""hi""" -> value: say "hi" -> re-emitted as a valid JPQL literal
        assertEquals("field1='say \"hi\"'", parser.parseString("field1==\"say \"\"hi\"\"\""));
    }

    @Test
    void unescapeDoubledDelimiterBacktick() {
        RsqlWhereString parser = new RsqlWhereString();
        assertEquals("field1='a`b'", parser.parseString("field1==`a``b`"));
    }

    @Test
    void otherDelimiterStaysLiteral() {
        RsqlWhereString parser = new RsqlWhereString();
        // a double quote inside a single-quoted literal is an ordinary character
        assertEquals("field1='a\"\"b'", parser.parseString("field1=='a\"\"b'"));
    }

    @Test
    void embeddedSingleQuoteIsDoubledOnOutput() {
        RsqlWhereString parser = new RsqlWhereString();
        // value it's must be emitted as 'it''s', otherwise the JPQL cannot be parsed
        assertEquals("field1='it''s'", parser.parseString("field1==\"it's\""));
        assertEquals("lower(field1) like 'it''s%' escape '\\'", parser.parseString("field1=*\"it's*\""));
    }

    @Test
    void unescapeInLikePattern() {
        RsqlWhereString parser = new RsqlWhereString();
        assertEquals("lower(field1) like 'say \"hi\"%' escape '\\'", parser.parseString("field1=*\"say \"\"hi\"\"*\""));
    }

    @Test
    void emptyStringLiteralIsUnchanged() {
        RsqlWhereString parser = new RsqlWhereString();
        assertEquals("field1=''", parser.parseString("field1==''"));
        assertEquals("lower(field1) like '' escape '\\'", parser.parseString("field1=*''"));
    }

    // ---- missing NLIKE rendering (operator used to render as the literal text "null") ----

    @Test
    void fieldNotLikeString() {
        RsqlWhereString parser = new RsqlWhereString();
        assertEquals("lower(field1) not like 'a%' escape '\\'", parser.parseString("field1=!*'A*'"));
        assertEquals("lower(field1) not like 'a%' escape '\\'", parser.parseString("field1=nlike='A*'"));
        assertEquals("lower(field1) not like 'a%' escape '\\'", parser.parseString("field1!=*'A*'"));
    }

    @Test
    void notLikeOperatorInNonStringContexts() {
        RsqlWhereString parser = new RsqlWhereString();
        assertEquals("field1 not like field2", parser.parseString("field1=!*field2"));
        assertEquals("field1 not like 1", parser.parseString("field1=!*1"));
        assertEquals("field1 not like 1.5", parser.parseString("field1=!*1.5"));
        assertEquals("field1 not like :p1", parser.parseString("field1=!*:p1"));
    }

    // ---- missing NOT BETWEEN rendering (used to return null) ----

    @Test
    void fieldNotBetweenString() {
        RsqlWhereString parser = new RsqlWhereString();
        assertEquals("field1 not between 'Aa' and 'Bb'", parser.parseString("field1=nbt=('Aa','Bb')"));
    }

    @Test
    void fieldNotBetweenInt() {
        RsqlWhereString parser = new RsqlWhereString();
        assertEquals("field1 not between 1 and 2", parser.parseString("field1=nbt=(1,2)"));
    }

    // ---- type-aware rendering of in-list and between elements ----

    @Test
    void inListStringElementIsUnescaped() {
        RsqlWhereString parser = new RsqlWhereString();
        assertEquals("field1 in ('a\"b')", parser.parseString("field1=in=(\"a\"\"b\")"));
        assertEquals("field1 not in ('a\"b')", parser.parseString("field1=nin=(\"a\"\"b\")"));
    }

    @Test
    void inListNormalizesDatesAndEnums() {
        RsqlWhereString parser = new RsqlWhereString();
        assertEquals("field1 in ('2020-01-01','a')", parser.parseString("field1=in=(#2020-01-01#,'a')"));
        assertEquals("field1 in ('ENUM1','ENUM2')", parser.parseString("field1=in=(#ENUM1#,#ENUM2#)"));
    }

    @Test
    void betweenStringElementIsUnescaped() {
        RsqlWhereString parser = new RsqlWhereString();
        assertEquals("field1 between 'a\"b' and 'c'", parser.parseString("field1=bt=(\"a\"\"b\",'c')"));
    }

    @Test
    void betweenNonStringElementsAreUnchanged() {
        RsqlWhereString parser = new RsqlWhereString();
        // regression guard: these branches already rendered correctly and must not change
        assertEquals("field1 between 1 and 10", parser.parseString("field1=bt=(1,10)"));
        assertEquals("field1 between 1.5 and 10.5", parser.parseString("field1=bt=(1.5,10.5)"));
        assertEquals("field1 between :p1 and :p2", parser.parseString("field1=bt=(:p1,:p2)"));
        assertEquals("field1 between field2 and field3", parser.parseString("field1=bt=(field2,field3)"));
    }

    @Test
    void inListNumbersAreUnchanged() {
        RsqlWhereString parser = new RsqlWhereString();
        assertEquals("field1 in (1,2,3)", parser.parseString("field1=in=(1,2,3)"));
    }

    // ---- a backslash is an ordinary character: a value may end with one ----

    @Test
    void valueEndingWithBackslash() {
        RsqlWhereString parser = new RsqlWhereString();
        assertEquals("field1='abc\\'", parser.parseString("field1==\"abc\\\""));
        assertEquals("field1='abc\\'", parser.parseString("field1=='abc\\'"));
        assertEquals("field1='abc\\'", parser.parseString("field1==`abc\\`"));
    }

    @Test
    void windowsPathEndingWithBackslash() {
        RsqlWhereString parser = new RsqlWhereString();
        assertEquals("field1='C:\\dir\\'", parser.parseString("field1==\"C:\\dir\\\""));
    }

    @Test
    void backslashBeforeDelimiterInsideValue() {
        RsqlWhereString parser = new RsqlWhereString();
        // the delimiter is escaped by doubling it; the backslash stays literal
        assertEquals("field1='a\\\"b'", parser.parseString("field1==\"a\\\"\"b\""));
    }

    @Test
    void allThreeDelimitersInOneValue() {
        RsqlWhereString parser = new RsqlWhereString();
        assertEquals("field1='d\" s''s t`'", parser.parseString("field1==\"d\"\" s's t`\""));
    }

    @Test
    void backslashInTheMiddleIsUnchanged() {
        RsqlWhereString parser = new RsqlWhereString();
        assertEquals("field1='C:\\dir\\file'", parser.parseString("field1==\"C:\\dir\\file\""));
        assertEquals("field1='a\\\\'", parser.parseString("field1==\"a\\\\\""));
    }

    @Test
    void backslashNoLongerProtectsTheDelimiter() {
        // breaking change: this used to parse and yield a\"b, now the delimiter closes the literal
        RsqlWhereString parser = new RsqlWhereString();
        assertThrows(RuntimeException.class, () -> parser.parseString("field1==\"a\\\"b\""));
        assertThrows(RuntimeException.class, () -> parser.parseString("field1=='it\\'s'"));
    }

    // ---- the whole input must be turned into one filter expression ----

    @Test
    void conditionsWithoutLogicalOperatorAreRejected() {
        RsqlWhereString parser = new RsqlWhereString();
        // used to silently keep only the last condition
        assertThrows(RuntimeException.class, () -> parser.parseString("field1=='a' field2==1"));
        assertThrows(RuntimeException.class, () -> parser.parseString("field1=='a' field2==1 field3==2"));
        assertThrows(RuntimeException.class, () -> parser.parseString("(field1=='a') (field2==1)"));
    }

    @Test
    void trailingInputIsRejected() {
        RsqlWhereString parser = new RsqlWhereString();
        // used to be discarded silently
        assertThrows(RuntimeException.class, () -> parser.parseString("field1=='a' 123"));
    }

    @Test
    void logicalOperatorsStillWork() {
        RsqlWhereString parser = new RsqlWhereString();
        assertEquals("field1='a' and field2=1", parser.parseString("field1=='a' and field2==1"));
        assertEquals("field1='a' and field2=1", parser.parseString("field1=='a';field2==1"));
        assertEquals("field1='a' or field2=1", parser.parseString("field1=='a',field2==1"));
        assertEquals("(field1='a') and (field2=1)", parser.parseString("(field1=='a') and (field2==1)"));
    }

    @Test
    void trailingNewlineIsTolerated() {
        RsqlWhereString parser = new RsqlWhereString();
        assertEquals("field1='a'", parser.parseString("field1=='a'\n"));
        assertEquals("field1='a'", parser.parseString("field1=='a'\n\n"));
    }

    @Test
    void parseFileWithTrailingNewline() throws java.io.IOException {
        // guard: a file normally ends with a newline, and NEWLINE is a real token here -
        // a naive "stream fully consumed" check would reject every such file
        java.nio.file.Path f = java.nio.file.Files.createTempFile("rsql", ".txt");
        try {
            java.nio.file.Files.writeString(f, "field1=='a'\n");
            assertEquals("field1='a'", new RsqlWhereString().parseFile(f.toString()));
        } finally {
            java.nio.file.Files.deleteIfExists(f);
        }
    }

    /**
     * Up to 0.6.20 a trailing newline after a GROUPING ')' was rejected with
     * "Missing opening parenthesis", even though no parenthesis was missing: the inner condition
     * swallowed the ')' through the {@code condition ')'} error alternative, leaving the outer
     * {@code '(' condition ')'} unclosed, and error recovery then consumed the NEWLINE.
     * <p>
     * The two tests above missed it only because their fixture ends in {@code 'a'}, not in ')'.
     */
    @Test
    void trailingNewlineAfterClosingParenthesisIsTolerated() {
        RsqlWhereString parser = new RsqlWhereString();
        assertEquals("(field1='a')", parser.parseString("(field1=='a')\n"));
        assertEquals("(field1='a')", parser.parseString("(field1=='a')\r\n"));
        assertEquals("(field1='a')", parser.parseString("(field1=='a') \n"));
        assertEquals("((field1='a'))", parser.parseString("((field1=='a'))\n"));
        assertEquals("(field1='a') and (field2=1)", parser.parseString("(field1=='a');(field2==1)\n"));
        assertEquals("(field1 in ('a','b'))", parser.parseString("(field1=in=('a','b'))\n"));
    }

    @Test
    void parseFileEndingWithClosingParenthesis() throws java.io.IOException {
        java.nio.file.Path f = java.nio.file.Files.createTempFile("rsql", ".txt");
        try {
            java.nio.file.Files.writeString(f, "(field1=='a');(field2==1)\n");
            assertEquals("(field1='a') and (field2=1)", new RsqlWhereString().parseFile(f.toString()));
        } finally {
            java.nio.file.Files.deleteIfExists(f);
        }
    }

    /**
     * Leading and inner newlines were rejected before this change and still are - NEWLINE is a real
     * token that only the trailing-token check in RsqlWhereTreeParser tolerates. Pinned here so the
     * scope of the fix stays honest.
     */
    @Test
    void leadingAndInnerNewlineAreStillRejected() {
        RsqlWhereString parser = new RsqlWhereString();
        assertThrows(SyntaxErrorException.class, () -> parser.parseString("\nfield1=='a'"));
        assertThrows(SyntaxErrorException.class, () -> parser.parseString("field1=='a';\nfield2==1"));
    }

    /**
     * The parentheses used to be parsed in exponential time: 26 nested levels (56 characters) took
     * roughly 13 seconds, and 200 flat groups roughly 9 seconds. Both are linear now.
     * <p>
     * Runs on a thread with an explicit stack size so the outcome does not depend on the CI default,
     * and rethrows from the worker - join() alone would swallow the failure.
     */
    @Test
    void deeplyNestedAndFlatParenthesesParseInLinearTime() throws InterruptedException {
        // capped at the nesting limit from RsqlWhereTreeParser - still a decisive regression test, since
        // the old grammar took 12.7 s at 26 levels and doubled with every further one
        int nesting = RsqlWhereTreeParser.getMaxNestingDepth();
        assertParsesWithin("(".repeat(nesting) + "field1==1" + ")".repeat(nesting));
        // flat groups nest one level deep however many there are, so the limit does not apply
        assertParsesWithin(String.join(";", java.util.Collections.nCopies(400, "(field1==1)")));
    }


    /**
     * {@code ctx.inListElement()} and {@code ctx.DOT_ID()} are {@code getRuleContexts} calls: each one
     * rebuilds its list by scanning every child. Calling them once per loop iteration made rendering
     * quadratic - an IN list of 16 000 elements took about 5 s, and a 16 000-segment field path about 2 s,
     * while the parser handled both in tens of milliseconds. Both are hoisted now.
     */
    @Test
    void longInListsAndFieldPathsRenderInLinearTime() {
        RsqlWhereString parser = new RsqlWhereString();
        for (int i = 0; i < 5; i++) parser.parseString("a=in=(1,2,3)");   // warm up

        String longInList = "a=in=(" + String.join(",", java.util.Collections.nCopies(16_000, "1")) + ")";
        assertUnder(1000, parser, longInList, "IN list of 16 000 elements");

        String deepPath = "a" + ".b".repeat(16_000) + "==1";
        assertUnder(1000, parser, deepPath, "field path of 16 000 segments");
    }

    private void assertUnder(long budgetMs, RsqlWhereString parser, String filter, String what) {
        long start = System.nanoTime();
        parser.parseString(filter);
        long elapsedMs = (System.nanoTime() - start) / 1_000_000;
        assertTrue(elapsedMs < budgetMs, what + " took " + elapsedMs + " ms");
    }

    private void assertParsesWithin(String filter) throws InterruptedException {
        final Throwable[] failure = new Throwable[1];
        final long[] elapsedMs = new long[1];
        Thread worker = new Thread(null, () -> {
            long start = System.nanoTime();
            try {
                new RsqlWhereString().parseString(filter);
            } catch (Throwable t) {
                failure[0] = t;
            }
            elapsedMs[0] = (System.nanoTime() - start) / 1_000_000;
        }, "rsql-parse", 8L << 20);
        worker.start();
        worker.join();
        if (failure[0] != null) {
            throw new AssertionError("parsing failed for a " + filter.length() + " character filter", failure[0]);
        }
        assertTrue(
            elapsedMs[0] < 2000,
            "parsing a " + filter.length() + " character filter took " + elapsedMs[0] + " ms"
        );
    }

    @Test
    void backslashBeforeClosingDelimiterIsRejected() {
        // the silent branch: this used to parse as a\" with the surplus quote quietly discarded
        RsqlWhereString parser = new RsqlWhereString();
        assertThrows(RuntimeException.class, () -> parser.parseString("field1==\"a\\\"\""));
        assertThrows(RuntimeException.class, () -> parser.parseString("field1==\"a\\\"\";field2==1"));
    }
}

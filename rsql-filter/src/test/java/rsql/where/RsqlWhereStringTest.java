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
        SyntaxErrorException thrown = assertThrows(SyntaxErrorException.class, () -> {
            RsqlWhereString parser = new RsqlWhereString();
            parser.parseString("(field1==1 or field2==2)) and field3==3");
        });
        assertTrue(thrown.getMessage().contains("Missing opening parenthesis"));
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
        assertEquals("lower(field1) like 'a%'", result);
    }

    @Test
    void fieldLikeString2() {
        RsqlWhereString parser = new RsqlWhereString();
        String result = parser.parseString("field1=like='A*'");
        assertEquals("lower(field1) like 'a%'", result);
    }
}

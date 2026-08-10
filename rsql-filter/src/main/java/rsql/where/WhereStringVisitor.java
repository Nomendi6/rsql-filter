package rsql.where;

import java.util.List;

import org.antlr.v4.runtime.tree.TerminalNode;

import org.antlr.v4.runtime.tree.ErrorNode;
import org.antlr.v4.runtime.tree.ErrorNodeImpl;
import rsql.antlr.where.RsqlWhereBaseVisitor;
import rsql.antlr.where.RsqlWhereParser;
import org.antlr.v4.runtime.tree.ParseTree;
import rsql.exceptions.SyntaxErrorException;

import java.util.Locale;

import static rsql.where.RsqlWhereHelper.*;

public class WhereStringVisitor extends RsqlWhereBaseVisitor<String> {

    /** JPQL {@code ESCAPE} clause; two backslashes here is a single one in the emitted text. */
    private static final String LIKE_ESCAPE_CLAUSE = " escape '\\'";


    public WhereStringVisitor() {}

    @Override
    public String visitConditionAnd(RsqlWhereParser.ConditionAndContext ctx) {
        String left = super.visit(ctx.condition(0));
        String right = super.visit(ctx.condition(1));
        return left + " and " + right;
    }

    @Override
    public String visitConditionOr(RsqlWhereParser.ConditionOrContext ctx) {
        String left = super.visit(ctx.condition(0));
        String right = super.visit(ctx.condition(1));
        return left + " or " + right;
    }

    @Override
    public String visitConditionParens(RsqlWhereParser.ConditionParensContext ctx) {
        String condition = super.visit(ctx.condition());

        return "(" + condition + ")";
    }

    @Override
    public String visitConditionSingle(RsqlWhereParser.ConditionSingleContext ctx) {
        return super.visitConditionSingle(ctx);
    }

    @Override
    public String visitErrorNode(ErrorNode node) {
        final int charPositionInLine = ((RsqlWhereParser.SingleConditionContext) ((ErrorNodeImpl) node).parent).exception.getOffendingToken().getCharPositionInLine();
        throw new SyntaxErrorException("Error in RSQL syntax: " + node.getText() + " at position " + charPositionInLine);
    }

    @Override
    public String visitField(RsqlWhereParser.FieldContext ctx) {
        StringBuilder field = new StringBuilder(ctx.ID().getText());
        // hoisted: ctx.DOT_ID() is getRuleContexts(), which rebuilds the list by scanning every child,
        // so calling it per iteration makes this O(n^2) in the length of the field path
        for (TerminalNode segment : ctx.DOT_ID()) {
            field.append(segment.getText());
        }

        return field.toString();
    }

    /**
     * Render a value as a JPQL string literal, doubling an embedded single quote.
     */
    private static String quote(String value) {
        return "'" + value.replace("'", "''") + "'";
    }

    /**
     * Render a single in-list element according to its type.
     * <p>
     * String and enum literals go through the shared helper (so a doubled delimiter is un-escaped) and are
     * re-emitted as valid JPQL string literals; date and datetime literals are normalized to quoted values;
     * numbers, parameters and field references are passed through unchanged.
     */
    private String renderInListElement(RsqlWhereParser.InListElementContext ctx) {
        if (ctx.STRING_LITERAL() != null) {
            return quote(getStringFromStringLiteral(ctx.STRING_LITERAL()));
        } else if (ctx.ENUM_LITERAL() != null) {
            return quote(getStringFromStringLiteral(ctx.ENUM_LITERAL()));
        } else if (ctx.DATE_LITERAL() != null) {
            return getStringFromDateLiteral(ctx.DATE_LITERAL());
        } else if (ctx.DATETIME_LITERAL() != null) {
            return getStringFromDatetimeLiteral(ctx.DATETIME_LITERAL());
        }
        // DECIMAL_LITERAL, REAL_LITERAL, PARAM_LITERAL and field are rendered as-is
        return ctx.getText();
    }

    @Override
    public String visitInList(RsqlWhereParser.InListContext ctx) {
        // hoisted: ctx.inListElement() is getRuleContexts(), which rebuilds the list by scanning every
        // child. Calling it per iteration made this O(n^2) - an IN list of 16 000 elements took ~5 s.
        List<RsqlWhereParser.InListElementContext> elements = ctx.inListElement();

        StringBuilder l = new StringBuilder(renderInListElement(elements.get(0)));
        for (int i = 1; i < elements.size(); i++) {
            l.append(',').append(renderInListElement(elements.get(i)));
        }
        return l.toString();
    }

    @Override
    public String visitOperator(RsqlWhereParser.OperatorContext ctx) {
        return super.visitOperator(ctx);
    }

    @Override
    public String visitOperatorEQ(RsqlWhereParser.OperatorEQContext ctx) {
        return "=";
    }

    @Override
    public String visitOperatorNEQ(RsqlWhereParser.OperatorNEQContext ctx) {
        return "!=";
    }

    @Override
    public String visitOperatorGT(RsqlWhereParser.OperatorGTContext ctx) {
        return ">";
    }

    @Override
    public String visitOperatorLT(RsqlWhereParser.OperatorLTContext ctx) {
        return "<";
    }

    @Override
    public String visitOperatorGE(RsqlWhereParser.OperatorGEContext ctx) {
        return ">=";
    }

    @Override
    public String visitOperatorLE(RsqlWhereParser.OperatorLEContext ctx) {
        return "<=";
    }

    @Override
    public String visitOperatorLIKE(RsqlWhereParser.OperatorLIKEContext ctx) {
        return " like ";
    }

    // Non-null renderings so the new operators never yield a null operator in the
    // non-string visitSingleCondition* methods. The case-sensitive string rendering
    // is handled directly in visitSingleConditionString (subcontext branch), so these
    // are only reached for non-STRING_LITERAL contexts.
    @Override
    public String visitOperatorCLIKE(RsqlWhereParser.OperatorCLIKEContext ctx) {
        return " like ";
    }

    @Override
    public String visitOperatorCNLIKE(RsqlWhereParser.OperatorCNLIKEContext ctx) {
        return " not like ";
    }

    @Override
    public String visitOperatorNLIKE(RsqlWhereParser.OperatorNLIKEContext ctx) {
        return " not like ";
    }

    @Override
    public String visitSingleConditionBetween(RsqlWhereParser.SingleConditionBetweenContext ctx) {
        String field = visitField(ctx.field());
        String first = renderInListElement(ctx.inListElement(0));
        String second = renderInListElement(ctx.inListElement(1));
        return field + " between " + first + " and " + second;
    }

    @Override
    public String visitSingleConditionNotBetween(RsqlWhereParser.SingleConditionNotBetweenContext ctx) {
        String field = visitField(ctx.field());
        String first = renderInListElement(ctx.inListElement(0));
        String second = renderInListElement(ctx.inListElement(1));
        return field + " not between " + first + " and " + second;
    }

    @Override
    public String visitSingleConditionDate(RsqlWhereParser.SingleConditionDateContext ctx) {
        String field = visitField(ctx.field());
        String operator = visitOperator(ctx.operator());
        String text = getStringFromDateLiteral(ctx.DATE_LITERAL());
        return field + operator + text;
    }

    @Override
    public String visitSingleConditionDatetime(RsqlWhereParser.SingleConditionDatetimeContext ctx) {
        String field = visitField(ctx.field());
        String operator = visitOperator(ctx.operator());
        String text = getStringFromDatetimeLiteral(ctx.DATETIME_LITERAL());
        return field + operator + text;
    }

    @Override
    public String visitSingleConditionDecimal(RsqlWhereParser.SingleConditionDecimalContext ctx) {
        String field = visitField(ctx.field());
        String operator = visitOperator(ctx.operator());
        String text = ctx.DECIMAL_LITERAL().getText();
        return field + operator + text;
    }

    @Override
    public String visitSingleConditionEnum(RsqlWhereParser.SingleConditionEnumContext ctx) {
        String field = visitField(ctx.field());
        String value = getStringFromStringLiteral((ctx.ENUM_LITERAL()));
        RsqlWhereParser.OperatorBasicContext operator = ctx.operatorBasic();
        if (operator.operatorEQ() != null)
            return field + "='" + value + "'";
        else if (operator.operatorNEQ() != null)
            return field + "!='" + value + "'";

        throw new SyntaxErrorException("Invalid operator for enum condition: " + operator.getText());
    }

    @Override
    public String visitSingleConditionFalse(RsqlWhereParser.SingleConditionFalseContext ctx) {
        String field = visitField(ctx.field());
        RsqlWhereParser.OperatorBasicContext operator = ctx.operatorBasic();

        if (operator.operatorEQ() != null)
            return field + "=false";
        else if (operator.operatorNEQ() != null)
            return field + "!=false";
        // this is not possible, because the grammar does not allow it
        throw new SyntaxErrorException("Invalid operator for false condition: " + operator.getText());
    }

    @Override
    public String visitSingleConditionIn(RsqlWhereParser.SingleConditionInContext ctx) {
        String field = visitField(ctx.field());
        String operator = " in ";
        String text = super.visit(ctx.inList());
        return field + operator + "(" + text + ")";
    }

    @Override
    public String visitSingleConditionNotIn(RsqlWhereParser.SingleConditionNotInContext ctx) {
        String field = visitField(ctx.field());
        String operator = " not in ";
        String text = super.visit(ctx.inList());
        return field + operator + "(" + text + ")";
    }

    @Override
    public String visitSingleConditionNull(RsqlWhereParser.SingleConditionNullContext ctx) {
        String field = visitField(ctx.field());
        RsqlWhereParser.OperatorBasicContext operator = ctx.operatorBasic();

        if (operator.operatorEQ() != null)
            return field + " is null";
        else if (operator.operatorNEQ() != null)
            return field + " is not null";
        throw new SyntaxErrorException("Invalid operator for null condition: " + operator.getText());
    }

    @Override
    public String visitSingleConditionOtherField(RsqlWhereParser.SingleConditionOtherFieldContext ctx) {
        String fieldName1 = visitField(ctx.field(0));
        String operator = visitOperator(ctx.operator());
        String fieldName2 = visitField(ctx.field(1));
        return fieldName1 + operator + fieldName2;
    }

    @Override
    public String visitSingleConditionReal(RsqlWhereParser.SingleConditionRealContext ctx) {
        String field = visitField(ctx.field());
        String operator = visitOperator(ctx.operator());
        String text = ctx.REAL_LITERAL().getText();
        return field + operator + text;
    }

    @Override
    public String visitSingleConditionParam(RsqlWhereParser.SingleConditionParamContext ctx) {
        String field = visitField(ctx.field());
        String operator = visitOperator(ctx.operator());
        String text = ctx.PARAM_LITERAL().getText();
        return field + operator + text;
    }

    @Override
    public String visitSingleConditionString(RsqlWhereParser.SingleConditionStringContext ctx) {
        String field = visitField(ctx.field());
        // the value goes through the shared helper, so a doubled delimiter is un-escaped,
        // and is re-emitted as a valid JPQL string literal regardless of the original delimiter
        String value = getStringFromStringLiteral(ctx.STRING_LITERAL());
        RsqlWhereParser.OperatorContext op = ctx.operator();

        if (op.operatorLIKE() != null) {
            // case-insensitive LIKE: lower(field) like 'lowercased-pattern' escape '\'
            return "lower(".concat(field).concat(") like ")
                .concat(quote(toLikePattern(value).toLowerCase(Locale.ROOT))).concat(LIKE_ESCAPE_CLAUSE);
        } else if (op.operatorNLIKE() != null) {
            return "lower(".concat(field).concat(") not like ")
                .concat(quote(toLikePattern(value).toLowerCase(Locale.ROOT))).concat(LIKE_ESCAPE_CLAUSE);
        } else if (op.operatorCLIKE() != null) {
            // case-sensitive LIKE: no lower(), pattern keeps its original case
            return field + " like " + quote(toLikePattern(value)) + LIKE_ESCAPE_CLAUSE;
        } else if (op.operatorCNLIKE() != null) {
            return field + " not like " + quote(toLikePattern(value)) + LIKE_ESCAPE_CLAUSE;
        } else {
            // all other operators (==, !=, <, >, ...) rendered via visitOperator
            return field + visitOperator(op) + quote(value);
        }
    }

    @Override
    public String visitSingleConditionTrue(RsqlWhereParser.SingleConditionTrueContext ctx) {
        String field = visitField(ctx.field());
        RsqlWhereParser.OperatorBasicContext operator = ctx.operatorBasic();

        if (operator.operatorEQ() != null)
            return field + "=true";
        else if (operator.operatorNEQ() != null)
            return field + "!=true";
        // this is not possible, because the grammar does not allow it
        throw new SyntaxErrorException("Invalid operator for true condition: " + operator.getText());
    }

    @Override
    public String visitWhere(RsqlWhereParser.WhereContext ctx) {
        return super.visitWhere(ctx);
    }

}

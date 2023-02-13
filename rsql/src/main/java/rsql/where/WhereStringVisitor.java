package rsql.where;

import org.antlr.v4.runtime.tree.ErrorNode;
import org.antlr.v4.runtime.tree.ErrorNodeImpl;
import rsql.antlr.where.RsqlWhereBaseVisitor;
import rsql.antlr.where.RsqlWhereParser;
import org.antlr.v4.runtime.tree.ParseTree;
import rsql.exceptions.SyntaxErrorException;

import java.util.Locale;
import java.util.Objects;

import static rsql.where.RsqlWhereHelper.*;

public class WhereStringVisitor extends RsqlWhereBaseVisitor<String> {

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
        for (int i = 0; i < ctx.DOT_ID().size(); i++) {
            field.append(ctx.DOT_ID(i).getText());
        }

        return field.toString();
    }

    @Override
    public String visitInList(RsqlWhereParser.InListContext ctx) {
        StringBuilder l = new StringBuilder(ctx.inListElement(0).getText());

        if (ctx.getChildCount() > 1) {
            for (int i = 1; i < ctx.inListElement().size(); i++) {
                l.append(',').append(ctx.inListElement(i).getText());
            }
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

    @Override
    public String visitSingleConditionBetween(RsqlWhereParser.SingleConditionBetweenContext ctx) {
        String field = visitField(ctx.field());
        String operator = " between ";
        if (ctx.inListElement(0).DATETIME_LITERAL() != null && ctx.inListElement(1).DATETIME_LITERAL() != null) {
            String first = getStringFromDatetimeLiteral(ctx.inListElement(0).DATETIME_LITERAL());
            String second = getStringFromDatetimeLiteral(ctx.inListElement(1).DATETIME_LITERAL());
            return field + operator + first + " and " + second;
        } else if (ctx.inListElement(0).DATE_LITERAL() != null && ctx.inListElement(1).DATE_LITERAL() != null) {
            String first = getStringFromDateLiteral(ctx.inListElement(0).DATE_LITERAL());
            String second = getStringFromDateLiteral(ctx.inListElement(1).DATE_LITERAL());
            return field + operator + first + " and " + second;
        } else {
            String first = ctx.inListElement(0).getText();
            String second = ctx.inListElement(1).getText();
            return field + operator + first + " and " + second;
        }
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
        String operator = visitOperator(ctx.operator());
        String text = ctx.STRING_LITERAL().getText();

        if (Objects.equals(operator, " like ")) {
            text = text.replace('*', '%').toLowerCase(Locale.ROOT);
            text = "lower(".concat(field).concat(") like ").concat(text);
            return text;
        } else {
            return field + operator + text;
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

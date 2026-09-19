package rsql.where;

import org.antlr.v4.runtime.tree.TerminalNode;
import rsql.antlr.where.RsqlWhereBaseVisitor;
import rsql.antlr.where.RsqlWhereParser;
import rsql.describe.FilterCondition;
import rsql.describe.FilterGroup;
import rsql.describe.FilterNode;
import rsql.describe.FilterOperator;
import rsql.describe.ListItem;
import rsql.describe.Operand;
import rsql.describe.PatternShape;
import rsql.describe.RightSide;
import rsql.exceptions.SyntaxErrorException;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Turns a WHERE parse tree into a {@link FilterNode} tree.
 * <p>
 * Purely syntactic: no {@code RsqlContext}, no {@code EntityManager}, no {@code Root}. Resolving a field path
 * against the JPA metamodel would create LEFT JOINs and fill {@code rsqlContext.joinsMap} as a side effect, so
 * describing a filter would change the query it describes. {@code WhereStringVisitor} shows the whole tree can
 * be walked without any of that.
 * <p>
 * Lives in this package because the literal helpers in {@code RsqlWhereHelper} are package-private.
 * <p>
 * <strong>Every composite method is overridden.</strong> The default {@code aggregateResult} returns the
 * result of the <em>last</em> child, so a missing override loses part of the filter silently - which is
 * exactly the defect that was fixed in 0.7.4, where {@code compileToRsqlQuery("name=='a' code=='b'")} returned
 * a filter on {@code code} alone.
 */
public class WhereDescriptionVisitor extends RsqlWhereBaseVisitor<FilterNode> {

    // ------------------------------------------------------------------ composite rules

    @Override
    public FilterNode visitWhere(RsqlWhereParser.WhereContext ctx) {
        // the tree parser has already rejected several juxtaposed conditions, so there is exactly one
        return visit(ctx.condition(0));
    }

    @Override
    public FilterNode visitConditionSingle(RsqlWhereParser.ConditionSingleContext ctx) {
        return visit(ctx.singleCondition());
    }

    /**
     * Parentheses do not produce a node of their own - a group node <em>is</em> the parenthesis. Redundant
     * ones therefore disappear, which is what makes {@code a;(b;c)} and {@code a;b;c} describe identically.
     */
    @Override
    public FilterNode visitConditionParens(RsqlWhereParser.ConditionParensContext ctx) {
        return visit(ctx.condition());
    }

    @Override
    public FilterNode visitConditionAnd(RsqlWhereParser.ConditionAndContext ctx) {
        return group(FilterGroup.Junction.AND, ctx.condition(0), ctx.condition(1));
    }

    @Override
    public FilterNode visitConditionOr(RsqlWhereParser.ConditionOrContext ctx) {
        return group(FilterGroup.Junction.OR, ctx.condition(0), ctx.condition(1));
    }

    /**
     * Builds a group and flattens both children that are already groups of the same junction.
     * <p>
     * The parse tree is strictly binary - {@code conditionAnd} and {@code conditionOr} are left-recursive - so
     * {@code a;b;c;d} arrives as three nested nodes. Without flattening here, {@link FilterGroup} would reject
     * it, and the rules that depend on normalisation (junctions alternate by depth; every non-root group needs
     * parentheses) would not hold.
     */
    private FilterNode group(FilterGroup.Junction junction,
                             RsqlWhereParser.ConditionContext left,
                             RsqlWhereParser.ConditionContext right) {
        List<FilterNode> children = new ArrayList<>();
        addFlattened(children, visit(left), junction);
        addFlattened(children, visit(right), junction);
        return new FilterGroup(junction, children);
    }

    private void addFlattened(List<FilterNode> target, FilterNode node, FilterGroup.Junction junction) {
        if (node instanceof FilterGroup group && group.junction() == junction) {
            target.addAll(group.children());
        } else {
            target.add(node);
        }
    }

    // ------------------------------------------------------------------ single conditions

    @Override
    public FilterNode visitSingleConditionString(RsqlWhereParser.SingleConditionStringContext ctx) {
        String value = RsqlWhereHelper.getStringFromStringLiteral(ctx.STRING_LITERAL());
        FilterOperator operator = operatorOf(ctx.operator());
        return pattern(field(ctx.field()), operator, new RightSide.SingleValue(value), value);
    }

    @Override
    public FilterNode visitSingleConditionDecimal(RsqlWhereParser.SingleConditionDecimalContext ctx) {
        // no helper exists for this one - the execution path parses it inline too
        return condition(ctx.field(), ctx.operator(),
            new RightSide.SingleValue(Long.valueOf(ctx.DECIMAL_LITERAL().getText())));
    }

    @Override
    public FilterNode visitSingleConditionReal(RsqlWhereParser.SingleConditionRealContext ctx) {
        return condition(ctx.field(), ctx.operator(),
            new RightSide.SingleValue(new BigDecimal(ctx.REAL_LITERAL().getText())));
    }

    @Override
    public FilterNode visitSingleConditionDate(RsqlWhereParser.SingleConditionDateContext ctx) {
        return condition(ctx.field(), ctx.operator(),
            new RightSide.SingleValue(RsqlWhereHelper.getLocalDateFromDateLiteral(ctx.DATE_LITERAL())));
    }

    @Override
    public FilterNode visitSingleConditionDatetime(RsqlWhereParser.SingleConditionDatetimeContext ctx) {
        return condition(ctx.field(), ctx.operator(),
            new RightSide.SingleValue(RsqlWhereHelper.getDatetimeLiteral(ctx.DATETIME_LITERAL()).toNeutralValue()));
    }

    /**
     * {@code status==ACTIVE} compares two columns - see {@link RightSide.FieldRef}. The LIKE family is legal
     * here grammatically, but no execution path supports it, so no pattern is derived.
     */
    @Override
    public FilterNode visitSingleConditionOtherField(RsqlWhereParser.SingleConditionOtherFieldContext ctx) {
        return condition(ctx.field(0), ctx.operator(),
            new RightSide.FieldRef(RsqlWhereHelper.getFieldName(ctx.field(1))));
    }

    @Override
    public FilterNode visitSingleConditionParam(RsqlWhereParser.SingleConditionParamContext ctx) {
        return condition(ctx.field(), ctx.operator(),
            new RightSide.Parameter(RsqlWhereHelper.getParamFromLiteral(ctx.PARAM_LITERAL())));
    }

    @Override
    public FilterNode visitSingleConditionEnum(RsqlWhereParser.SingleConditionEnumContext ctx) {
        String value = RsqlWhereHelper.getStringFromStringLiteral(ctx.ENUM_LITERAL());
        return new FilterCondition(field(ctx.field()), basicOperatorOf(ctx.operatorBasic()),
            new RightSide.SingleValue(value), PatternShape.NONE, null);
    }

    @Override
    public FilterNode visitSingleConditionNull(RsqlWhereParser.SingleConditionNullContext ctx) {
        FilterOperator operator = ctx.operatorBasic().operatorEQ() != null
            ? FilterOperator.IS_NULL
            : FilterOperator.IS_NOT_NULL;
        return new FilterCondition(field(ctx.field()), operator, new RightSide.NoValue(),
            PatternShape.NONE, null);
    }

    @Override
    public FilterNode visitSingleConditionTrue(RsqlWhereParser.SingleConditionTrueContext ctx) {
        return booleanCondition(ctx.field(), ctx.operatorBasic(), Boolean.TRUE);
    }

    @Override
    public FilterNode visitSingleConditionFalse(RsqlWhereParser.SingleConditionFalseContext ctx) {
        return booleanCondition(ctx.field(), ctx.operatorBasic(), Boolean.FALSE);
    }

    @Override
    public FilterNode visitSingleConditionIn(RsqlWhereParser.SingleConditionInContext ctx) {
        return new FilterCondition(field(ctx.field()), FilterOperator.IN,
            new RightSide.ValueList(items(ctx.inList())), PatternShape.NONE, null);
    }

    @Override
    public FilterNode visitSingleConditionNotIn(RsqlWhereParser.SingleConditionNotInContext ctx) {
        return new FilterCondition(field(ctx.field()), FilterOperator.NIN,
            new RightSide.ValueList(items(ctx.inList())), PatternShape.NONE, null);
    }

    @Override
    public FilterNode visitSingleConditionBetween(RsqlWhereParser.SingleConditionBetweenContext ctx) {
        return range(ctx.field(), FilterOperator.BT, ctx.inListElement(0), ctx.inListElement(1));
    }

    @Override
    public FilterNode visitSingleConditionNotBetween(RsqlWhereParser.SingleConditionNotBetweenContext ctx) {
        return range(ctx.field(), FilterOperator.NBT, ctx.inListElement(0), ctx.inListElement(1));
    }

    // ------------------------------------------------------------------ helpers

    private FilterNode booleanCondition(RsqlWhereParser.FieldContext field,
                                        RsqlWhereParser.OperatorBasicContext operator,
                                        Boolean value) {
        return new FilterCondition(field(field), basicOperatorOf(operator),
            new RightSide.SingleValue(value), PatternShape.NONE, null);
    }

    private FilterNode range(RsqlWhereParser.FieldContext field, FilterOperator operator,
                             RsqlWhereParser.InListElementContext from,
                             RsqlWhereParser.InListElementContext to) {
        return new FilterCondition(field(field), operator,
            new RightSide.Range(item(from), item(to)), PatternShape.NONE, null);
    }

    private FilterNode condition(RsqlWhereParser.FieldContext field,
                                 RsqlWhereParser.OperatorContext operator,
                                 RightSide rightSide) {
        return new FilterCondition(field(field), operatorOf(operator), rightSide, PatternShape.NONE, null);
    }

    /** Attaches the derived pattern, but only where a pattern can honestly be derived. */
    private FilterNode pattern(Operand left, FilterOperator operator, RightSide rightSide, String rawValue) {
        if (!isLikeFamily(operator)) {
            return new FilterCondition(left, operator, rightSide, PatternShape.NONE, null);
        }
        // the derivation lives in PatternShape, so this and FilterCondition's validation cannot disagree
        PatternShape shape = PatternShape.of(rawValue);
        return new FilterCondition(left, operator, rightSide, shape, shape.needleOf(rawValue));
    }

    private static boolean isLikeFamily(FilterOperator operator) {
        return operator == FilterOperator.LIKE || operator == FilterOperator.NLIKE
            || operator == FilterOperator.CLIKE || operator == FilterOperator.CNLIKE;
    }

    private static Operand field(RsqlWhereParser.FieldContext ctx) {
        return new Operand.FieldOperand(RsqlWhereHelper.getFieldName(ctx));
    }

    private static List<ListItem> items(RsqlWhereParser.InListContext ctx) {
        // hoisted: ctx.inListElement() rebuilds its list on every call
        List<RsqlWhereParser.InListElementContext> elements = ctx.inListElement();
        List<ListItem> items = new ArrayList<>(elements.size());
        for (RsqlWhereParser.InListElementContext element : elements) {
            items.add(item(element));
        }
        return items;
    }

    /**
     * {@code RsqlWhereHelper.getInListLiteral} throws for two of the eight alternatives - a parameter and a
     * field - so those are handled here before it is called.
     */
    private static ListItem item(RsqlWhereParser.InListElementContext ctx) {
        if (ctx.PARAM_LITERAL() != null) {
            return new ListItem.ItemParam(RsqlWhereHelper.getParamFromLiteral(ctx.PARAM_LITERAL()));
        }
        if (ctx.field() != null) {
            return new ListItem.ItemField(RsqlWhereHelper.getFieldName(ctx.field()));
        }
        Object value = RsqlWhereHelper.getInListLiteral(ctx);
        // A description has no attribute to take a type from, so a datetime is shown as what it names.
        return new ListItem.ItemValue(value instanceof DatetimeLiteral literal ? literal.toNeutralValue() : value);
    }

    private static FilterOperator basicOperatorOf(RsqlWhereParser.OperatorBasicContext ctx) {
        if (ctx.operatorEQ() != null) {
            return FilterOperator.EQ;
        }
        if (ctx.operatorNEQ() != null) {
            return FilterOperator.NEQ;
        }
        throw new SyntaxErrorException("Unknown operator: " + ctx.getText());
    }

    /** Normalises to the enum rather than keeping the text - keywords are case-insensitive and aliased. */
    private static FilterOperator operatorOf(RsqlWhereParser.OperatorContext ctx) {
        if (ctx.operatorEQ() != null) {
            return FilterOperator.EQ;
        }
        if (ctx.operatorNEQ() != null) {
            return FilterOperator.NEQ;
        }
        if (ctx.operatorGT() != null) {
            return FilterOperator.GT;
        }
        if (ctx.operatorGE() != null) {
            return FilterOperator.GE;
        }
        if (ctx.operatorLT() != null) {
            return FilterOperator.LT;
        }
        if (ctx.operatorLE() != null) {
            return FilterOperator.LE;
        }
        if (ctx.operatorLIKE() != null) {
            return FilterOperator.LIKE;
        }
        if (ctx.operatorNLIKE() != null) {
            return FilterOperator.NLIKE;
        }
        if (ctx.operatorCLIKE() != null) {
            return FilterOperator.CLIKE;
        }
        if (ctx.operatorCNLIKE() != null) {
            return FilterOperator.CNLIKE;
        }
        throw new SyntaxErrorException("Unknown operator: " + ctx.getText());
    }
}

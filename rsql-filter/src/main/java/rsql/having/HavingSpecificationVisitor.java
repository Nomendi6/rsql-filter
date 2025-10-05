package rsql.having;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.From;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.metamodel.ManagedType;
import jakarta.persistence.metamodel.Metamodel;
import org.antlr.v4.runtime.tree.TerminalNode;
import rsql.where.RsqlContext;
import rsql.antlr.having.RsqlHavingBaseVisitor;
import rsql.antlr.having.RsqlHavingParser.*;
import rsql.exceptions.SyntaxErrorException;
import rsql.helper.AggregateField.AggregateFunction;
import rsql.where.RsqlWhereHelper;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Visitor that converts HAVING clause ParseTree into JPA Criteria Predicate.
 * Extends RsqlHavingBaseVisitor to implement visitor pattern for all HAVING grammar rules.
 */
public class HavingSpecificationVisitor<ENTITY> extends RsqlHavingBaseVisitor<Object> {

    private HavingContext<ENTITY> havingContext;
    private RsqlContext<ENTITY> rsqlContext;
    private List<String> groupByFields;
    private CriteriaBuilder builder;
    private Root<ENTITY> root;
    private Map<String, Path<?>> joinsMap;
    private Map<String, ManagedType<?>> classMetadataMap;

    /**
     * Sets the context for the visitor.
     *
     * @param havingContext HAVING context with alias mappings
     * @param rsqlContext RSQL context with EntityManager
     * @param groupByFields List of GROUP BY fields for validation
     */
    public void setContext(
        HavingContext<ENTITY> havingContext,
        RsqlContext<ENTITY> rsqlContext,
        List<String> groupByFields
    ) {
        this.havingContext = havingContext;
        this.rsqlContext = rsqlContext;
        this.groupByFields = groupByFields != null ? groupByFields : new ArrayList<>();
        this.builder = havingContext.getBuilder();
        this.root = havingContext.getRoot();
        this.joinsMap = havingContext.getJoinsMap();
        this.classMetadataMap = havingContext.getClassMetadataMap();
    }

    // ==================== LOGICAL CONDITIONS ====================

    @Override
    public Predicate visitHavingConditionSingle(HavingConditionSingleContext ctx) {
        return (Predicate) visit(ctx.singleHavingCondition());
    }

    @Override
    public Predicate visitHavingConditionParens(HavingConditionParensContext ctx) {
        return (Predicate) visit(ctx.havingCondition());
    }

    @Override
    public Predicate visitHavingConditionAnd(HavingConditionAndContext ctx) {
        Predicate left = (Predicate) visit(ctx.havingCondition(0));
        Predicate right = (Predicate) visit(ctx.havingCondition(1));
        return builder.and(left, right);
    }

    @Override
    public Predicate visitHavingConditionOr(HavingConditionOrContext ctx) {
        Predicate left = (Predicate) visit(ctx.havingCondition(0));
        Predicate right = (Predicate) visit(ctx.havingCondition(1));
        return builder.or(left, right);
    }

    // ==================== COMPARISON CONDITIONS ====================

    @Override
    public Predicate visitHavingConditionComparison(HavingConditionComparisonContext ctx) {
        // Comparison of two expressions: SUM(debit) > SUM(credit)
        Expression<?> left = (Expression<?>) visit(ctx.havingExpression(0));
        Expression<?> right = (Expression<?>) visit(ctx.havingExpression(1));
        String operator = ctx.operator().getText();

        return buildComparisonPredicate(left, right, operator);
    }

    @Override
    public Predicate visitHavingConditionLiteral(HavingConditionLiteralContext ctx) {
        // Comparison with literal: totalPrice > 1000
        Expression<?> expression = (Expression<?>) visit(ctx.havingExpression());
        Object literalValue = extractLiteralValue(ctx.literal());
        String operator = ctx.operator().getText();

        return buildComparisonPredicate(expression, literalValue, operator);
    }

    @Override
    public Predicate visitHavingConditionNull(HavingConditionNullContext ctx) {
        Expression<?> expression = (Expression<?>) visit(ctx.havingExpression());
        String operator = ctx.operatorBasic().getText();

        if ("==".equals(operator)) {
            return builder.isNull(expression);
        } else { // "!=" or "=!"
            return builder.isNotNull(expression);
        }
    }

    @Override
    public Predicate visitHavingConditionTrue(HavingConditionTrueContext ctx) {
        Expression<?> expression = (Expression<?>) visit(ctx.havingExpression());
        String operator = ctx.operatorBasic().getText();

        if ("==".equals(operator)) {
            return builder.isTrue((Expression<Boolean>) expression);
        } else {
            return builder.isFalse((Expression<Boolean>) expression);
        }
    }

    @Override
    public Predicate visitHavingConditionFalse(HavingConditionFalseContext ctx) {
        Expression<?> expression = (Expression<?>) visit(ctx.havingExpression());
        String operator = ctx.operatorBasic().getText();

        if ("==".equals(operator)) {
            return builder.isFalse((Expression<Boolean>) expression);
        } else {
            return builder.isTrue((Expression<Boolean>) expression);
        }
    }

    @Override
    public Predicate visitHavingConditionIn(HavingConditionInContext ctx) {
        Expression<?> expression = (Expression<?>) visit(ctx.havingExpression());
        List<Object> values = extractLiteralList(ctx.literalList());

        CriteriaBuilder.In<Object> inPredicate = builder.in(expression);
        for (Object value : values) {
            inPredicate.value(value);
        }
        return inPredicate;
    }

    @Override
    public Predicate visitHavingConditionNotIn(HavingConditionNotInContext ctx) {
        Expression<?> expression = (Expression<?>) visit(ctx.havingExpression());
        List<Object> values = extractLiteralList(ctx.literalList());

        CriteriaBuilder.In<Object> inPredicate = builder.in(expression);
        for (Object value : values) {
            inPredicate.value(value);
        }
        return builder.not(inPredicate);
    }

    @Override
    public Predicate visitHavingConditionBetween(HavingConditionBetweenContext ctx) {
        Expression<?> expression = (Expression<?>) visit(ctx.havingExpression());
        Object from = extractLiteralValue(ctx.literal(0));
        Object to = extractLiteralValue(ctx.literal(1));

        return builder.between((Expression<Comparable>) expression, (Comparable) from, (Comparable) to);
    }

    @Override
    public Predicate visitHavingConditionNotBetween(HavingConditionNotBetweenContext ctx) {
        Expression<?> expression = (Expression<?>) visit(ctx.havingExpression());
        Object from = extractLiteralValue(ctx.literal(0));
        Object to = extractLiteralValue(ctx.literal(1));

        Predicate between = builder.between((Expression<Comparable>) expression, (Comparable) from, (Comparable) to);
        return builder.not(between);
    }

    // ==================== HAVING EXPRESSIONS ====================

    @Override
    public Expression<?> visitHavingExprField(HavingExprFieldContext ctx) {
        String fieldText = ctx.field().getText();

        // First try to get as alias
        if (havingContext.hasAlias(fieldText)) {
            return havingContext.getExpressionForAlias(fieldText);
        }

        // Otherwise treat as GROUP BY field (must be in groupByFields)
        validateGroupByField(fieldText);
        return getPropertyPathRecursive(fieldText, root, rsqlContext, joinsMap, classMetadataMap);
    }

    @Override
    public Expression<?> visitHavingExprFunctionCall(HavingExprFunctionCallContext ctx) {
        // Delegate to functionCall visitor
        return (Expression<?>) visit(ctx.functionCall());
    }

    // ==================== FUNCTION CALLS ====================

    @Override
    public Expression<?> visitFunctionCall(FunctionCallContext ctx) {
        // Delegate to aggregateFunction visitor
        return (Expression<?>) visit(ctx.aggregateFunction());
    }

    @Override
    public Expression<?> visitFuncCall(FuncCallContext ctx) {
        // Detect function type
        AggregateFunction function;
        if (ctx.AVG() != null) function = AggregateFunction.AVG;
        else if (ctx.MAX() != null) function = AggregateFunction.MAX;
        else if (ctx.MIN() != null) function = AggregateFunction.MIN;
        else if (ctx.SUM() != null) function = AggregateFunction.SUM;
        else if (ctx.GRP() != null) function = AggregateFunction.NONE;
        else throw new SyntaxErrorException("Unknown aggregate function");

        // Extract field path from functionArg
        String fieldPath = visitFunctionArgAsString(ctx.functionArg());
        Path<?> path = getPropertyPathRecursive(fieldPath, root, rsqlContext, joinsMap, classMetadataMap);

        // Create Expression depending on function
        return switch (function) {
            case SUM -> builder.sum((Expression<Number>) path);
            case AVG -> builder.avg((Expression<Number>) path);
            case MIN -> builder.least((Expression) path);
            case MAX -> builder.greatest((Expression) path);
            case NONE -> path;  // GRP() function → GROUP BY without aggregation
            default -> throw new SyntaxErrorException("Unsupported function: " + function);
        };
    }

    @Override
    public Expression<?> visitCountAll(CountAllContext ctx) {
        String fieldPath;

        // COUNT(*) or COUNT(field)
        if (ctx.starArg != null) {
            // COUNT(*) - use default field (e.g., "id" or first PK)
            fieldPath = "id";  // TODO: Could fetch from Metamodel
        } else {
            // COUNT(field)
            fieldPath = visitFunctionArgAsString(ctx.functionArg());
        }

        Path<?> path = getPropertyPathRecursive(fieldPath, root, rsqlContext, joinsMap, classMetadataMap);
        return builder.count(path);
    }

    @Override
    public Expression<?> visitCountDist(CountDistContext ctx) {
        // COUNT(DIST field1, field2, ...)
        // JPA Criteria supports only one field for COUNT DISTINCT
        List<String> fieldPaths = visitFunctionArgsAsStrings(ctx.functionArgs());

        if (fieldPaths.size() > 1) {
            throw new SyntaxErrorException(
                "COUNT(DIST ...) supports only one field in HAVING clause. " +
                "Found: " + fieldPaths.size() + " fields"
            );
        }

        String fieldPath = fieldPaths.get(0);
        Path<?> path = getPropertyPathRecursive(fieldPath, root, rsqlContext, joinsMap, classMetadataMap);
        return builder.countDistinct(path);
    }

    // ==================== HELPER METHODS ====================

    private Predicate buildComparisonPredicate(Expression<?> left, Object right, String operator) {
        return switch (operator) {
            case "==" -> builder.equal(left, right);
            case "!=", "=!" -> builder.notEqual(left, right);
            case "=gt=" -> builder.greaterThan((Expression<Comparable>) left, (Comparable) right);
            case "=lt=" -> builder.lessThan((Expression<Comparable>) left, (Comparable) right);
            case "=ge=" -> builder.greaterThanOrEqualTo((Expression<Comparable>) left, (Comparable) right);
            case "=le=" -> builder.lessThanOrEqualTo((Expression<Comparable>) left, (Comparable) right);
            case "=*", "=like=" -> builder.like((Expression<String>) left, (String) right);
            case "=!*", "!=*", "=nlike=" -> builder.notLike((Expression<String>) left, (String) right);
            default -> throw new SyntaxErrorException("Unknown operator: " + operator);
        };
    }

    private void validateGroupByField(String fieldPath) {
        if (!groupByFields.contains(fieldPath)) {
            throw new IllegalArgumentException(
                "Field '" + fieldPath + "' must be in GROUP BY or be a SELECT alias. " +
                "Current GROUP BY fields: " + groupByFields + ". " +
                "Available SELECT aliases: " + havingContext.getAvailableAliases()
            );
        }
    }

    private String visitFunctionArgAsString(FunctionArgContext ctx) {
        if (ctx.field() != null) {
            return ctx.field().getText();
        } else if (ctx.functionCall() != null) {
            throw new SyntaxErrorException(
                "Nested function calls are not supported in HAVING clause"
            );
        }
        throw new SyntaxErrorException("Invalid function argument");
    }

    private List<String> visitFunctionArgsAsStrings(FunctionArgsContext ctx) {
        List<String> fieldPaths = new ArrayList<>();
        for (FunctionArgContext argCtx : ctx.functionArg()) {
            fieldPaths.add(visitFunctionArgAsString(argCtx));
        }
        return fieldPaths;
    }

    private Object extractLiteralValue(LiteralContext ctx) {
        if (ctx.STRING_LITERAL() != null) {
            return getStringFromStringLiteral(ctx.STRING_LITERAL());
        } else if (ctx.DECIMAL_LITERAL() != null) {
            return Long.valueOf(ctx.DECIMAL_LITERAL().getText());
        } else if (ctx.REAL_LITERAL() != null) {
            return new BigDecimal(ctx.REAL_LITERAL().getText());
        } else if (ctx.DATE_LITERAL() != null) {
            return RsqlWhereHelper.getLocalDateFromDateLiteral(ctx.DATE_LITERAL());
        } else if (ctx.DATETIME_LITERAL() != null) {
            return RsqlWhereHelper.getInstantFromDatetimeLiteral(ctx.DATETIME_LITERAL());
        } else if (ctx.TRUE() != null) {
            return Boolean.TRUE;
        } else if (ctx.FALSE() != null) {
            return Boolean.FALSE;
        }
        throw new SyntaxErrorException("Unknown literal type");
    }

    private List<Object> extractLiteralList(LiteralListContext ctx) {
        List<Object> values = new ArrayList<>();
        for (LiteralContext literalCtx : ctx.literal()) {
            values.add(extractLiteralValue(literalCtx));
        }
        return values;
    }

    private String getStringFromStringLiteral(TerminalNode node) {
        String text = node.getText();
        // Remove quotes and handle escape sequences
        if (text.startsWith("'") && text.endsWith("'")) {
            return text.substring(1, text.length() - 1).replace("''", "'");
        } else if (text.startsWith("\"") && text.endsWith("\"")) {
            return text.substring(1, text.length() - 1).replace("\"\"", "\"");
        } else if (text.startsWith("`") && text.endsWith("`")) {
            return text.substring(1, text.length() - 1).replace("``", "`");
        }
        return text;
    }

    private static <ENTITY> Path<?> getPropertyPathRecursive(
        String fieldName,
        Path<?> startRoot,
        RsqlContext<ENTITY> rsqlContext,
        Map<String, Path<?>> joinsMap,
        Map<String, ManagedType<?>> classMetadataMap
    ) {
        String[] graph = fieldName.split("\\.");
        Metamodel metamodel = rsqlContext.entityManager.getMetamodel();
        ManagedType<?> classMetadata = metamodel.managedType(startRoot.getJavaType());
        Path<?> root = startRoot;

        String pathKey = "";
        if (graph.length > 1) {
            pathKey = joinArrayItems(graph, graph.length - 1, ".");
            if (joinsMap.containsKey(pathKey)) {
                Path<?> pathRoot = joinsMap.get(pathKey);
                ManagedType<?> pathClassMetadata = classMetadataMap.get(pathKey);
                String property = graph[graph.length - 1];
                root = pathRoot.get(property);
                if (isEmbeddedType(property, pathClassMetadata)) {
                    Class<?> embeddedType = findPropertyType(property, pathClassMetadata);
                    classMetadata = metamodel.managedType(embeddedType);
                }
                return root;
            }
        }

        // try to build a property path
        pathKey = "";
        for (String property : graph) {
            if (!hasPropertyName(property, classMetadata)) {
                throw new IllegalArgumentException(
                    "Unknown property: " + property + " from entity " + classMetadata.getJavaType().getName()
                );
            }

            if (isAssociationType(property, classMetadata)) {
                if (pathKey.length() > 0) {
                    pathKey = pathKey.concat(".").concat(property);
                } else {
                    pathKey = property;
                }
                if (joinsMap.containsKey(pathKey)) {
                    classMetadata = classMetadataMap.get(pathKey);
                    root = joinsMap.get(pathKey);
                } else {
                    Class<?> associationType = findPropertyType(property, classMetadata);
                    classMetadata = metamodel.managedType(associationType);

                    root = ((From) root).join(property, JoinType.LEFT);
                    joinsMap.put(pathKey, root);
                    classMetadataMap.put(pathKey, classMetadata);
                }
            } else {
                root = root.get(property);
                if (isEmbeddedType(property, classMetadata)) {
                    Class<?> embeddedType = findPropertyType(property, classMetadata);
                    classMetadata = metamodel.managedType(embeddedType);
                }
            }
        }
        return root;
    }

    private static String joinArrayItems(String[] graph, int len, String delimiter) {
        String key = graph[0];
        for (int i = 1; i < len; i++) {
            key = key.concat(delimiter).concat(graph[i]);
        }
        return key;
    }

    private static boolean hasPropertyName(String property, ManagedType<?> classMetadata) {
        return classMetadata.getAttributes().stream()
            .anyMatch(attr -> attr.getName().equals(property));
    }

    private static boolean isAssociationType(String property, ManagedType<?> classMetadata) {
        return classMetadata.getAttributes().stream()
            .filter(attr -> attr.getName().equals(property))
            .anyMatch(attr -> attr.getPersistentAttributeType() == jakarta.persistence.metamodel.Attribute.PersistentAttributeType.MANY_TO_ONE
                || attr.getPersistentAttributeType() == jakarta.persistence.metamodel.Attribute.PersistentAttributeType.ONE_TO_ONE
                || attr.getPersistentAttributeType() == jakarta.persistence.metamodel.Attribute.PersistentAttributeType.ONE_TO_MANY
                || attr.getPersistentAttributeType() == jakarta.persistence.metamodel.Attribute.PersistentAttributeType.MANY_TO_MANY);
    }

    private static boolean isEmbeddedType(String property, ManagedType<?> classMetadata) {
        return classMetadata.getAttributes().stream()
            .filter(attr -> attr.getName().equals(property))
            .anyMatch(attr -> attr.getPersistentAttributeType() == jakarta.persistence.metamodel.Attribute.PersistentAttributeType.EMBEDDED);
    }

    private static Class<?> findPropertyType(String property, ManagedType<?> classMetadata) {
        return classMetadata.getAttributes().stream()
            .filter(attr -> attr.getName().equals(property))
            .findFirst()
            .map(attr -> attr.getJavaType())
            .orElseThrow(() -> new IllegalArgumentException("Property not found: " + property));
    }
}

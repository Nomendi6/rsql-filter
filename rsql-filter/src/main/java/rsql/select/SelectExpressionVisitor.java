package rsql.select;

import jakarta.persistence.metamodel.Attribute;
import jakarta.persistence.metamodel.ManagedType;
import jakarta.persistence.metamodel.Metamodel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rsql.antlr.select.RsqlSelectBaseVisitor;
import rsql.antlr.select.RsqlSelectParser;
import rsql.exceptions.SyntaxErrorException;
import rsql.helper.*;
import rsql.helper.AggregateField.AggregateFunction;
import rsql.where.RsqlContext;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Visitor for parsing SELECT clauses with expressions into List&lt;SelectExpression&gt;.
 * Supports fields, aggregate functions, arithmetic operations, and numeric literals.
 *
 * <p>Example usage:</p>
 * <pre>
 * SelectExpressionVisitor visitor = new SelectExpressionVisitor();
 * visitor.setContext(rsqlContext);
 * List&lt;SelectExpression&gt; expressions = visitor.visit(parseTree);
 * </pre>
 */
public class SelectExpressionVisitor extends RsqlSelectBaseVisitor<List<SelectExpression>> {

    private static final Logger log = LoggerFactory.getLogger(SelectExpressionVisitor.class);

    private RsqlContext<?> rsqlContext;

    /**
     * Sets the RSQL context for JPA metamodel access.
     *
     * @param rsqlContext The RSQL context
     */
    public void setContext(RsqlContext<?> rsqlContext) {
        this.rsqlContext = rsqlContext;
    }

    @Override
    public List<SelectExpression> visitSelect(RsqlSelectParser.SelectContext ctx) {
        List<SelectExpression> allExpressions = new ArrayList<>();

        for (RsqlSelectParser.SelectElementsContext elementsCtx : ctx.selectElements()) {
            List<SelectExpression> expressions = visit(elementsCtx);
            if (expressions != null) {
                allExpressions.addAll(expressions);
            }
        }

        return allExpressions;
    }

    @Override
    public List<SelectExpression> visitSelectElements(RsqlSelectParser.SelectElementsContext ctx) {
        // If SELECT *, return all fields from root entity
        if (ctx.star != null) {
            return getAllFieldExpressionsFromRootEntity();
        }

        List<SelectExpression> expressions = new ArrayList<>();
        for (RsqlSelectParser.SelectElementContext elemCtx : ctx.selectElement()) {
            List<SelectExpression> elemExpressions = visit(elemCtx);
            if (elemExpressions != null) {
                expressions.addAll(elemExpressions);
            }
        }

        return expressions;
    }

    @Override
    public List<SelectExpression> visitSeAll(RsqlSelectParser.SeAllContext ctx) {
        String entityPath = getFieldPath(ctx.field());
        return getAllFieldExpressionsFromEntity(entityPath);
    }

    @Override
    public List<SelectExpression> visitSeField(RsqlSelectParser.SeFieldContext ctx) {
        // Convert simple field to FieldExpression
        String fieldPath = getFieldPath(ctx.field());
        validateFieldPath(fieldPath);

        FieldExpression expr = new FieldExpression(fieldPath);

        // Apply alias if present
        if (ctx.simpleField() != null) {
            String alias = ctx.simpleField().getText();
            expr.setAlias(alias);
        }

        return Arrays.asList(expr);
    }

    @Override
    public List<SelectExpression> visitSeFuncCall(RsqlSelectParser.SeFuncCallContext ctx) {
        // Convert function call to FunctionExpression
        SelectExpression expr = visitFunctionCallAsExpression(ctx.functionCall());

        // Apply alias if present
        if (ctx.simpleField() != null) {
            String alias = ctx.simpleField().getText();
            expr.setAlias(alias);
        }

        return Arrays.asList(expr);
    }

    @Override
    public List<SelectExpression> visitSeExpression(RsqlSelectParser.SeExpressionContext ctx) {
        // Visit the expression (single SelectExpression)
        SelectExpression expr = visitExpressionSingle(ctx.expression());

        // Apply alias if present
        if (ctx.simpleField() != null) {
            String alias = ctx.simpleField().getText();
            expr.setAlias(alias);
        }

        return Arrays.asList(expr);
    }

    // ==================== Expression Visitors ====================

    /**
     * Visits an expression and returns a single SelectExpression.
     */
    private SelectExpression visitExpressionSingle(RsqlSelectParser.ExpressionContext ctx) {
        if (ctx instanceof RsqlSelectParser.ParenExpressionContext) {
            return processParenExpression((RsqlSelectParser.ParenExpressionContext) ctx);
        } else if (ctx instanceof RsqlSelectParser.MulDivExpressionContext) {
            return processMulDivExpression((RsqlSelectParser.MulDivExpressionContext) ctx);
        } else if (ctx instanceof RsqlSelectParser.AddSubExpressionContext) {
            return processAddSubExpression((RsqlSelectParser.AddSubExpressionContext) ctx);
        } else if (ctx instanceof RsqlSelectParser.FuncExpressionContext) {
            return processFuncExpression((RsqlSelectParser.FuncExpressionContext) ctx);
        } else if (ctx instanceof RsqlSelectParser.FieldExpressionContext) {
            return processFieldExpression((RsqlSelectParser.FieldExpressionContext) ctx);
        } else if (ctx instanceof RsqlSelectParser.NumberExpressionContext) {
            return processNumberExpression((RsqlSelectParser.NumberExpressionContext) ctx);
        } else {
            throw new SyntaxErrorException("Unknown expression type: " + ctx.getClass().getSimpleName());
        }
    }

    private SelectExpression processParenExpression(RsqlSelectParser.ParenExpressionContext ctx) {
        // Parentheses don't create a new node, just pass through the inner expression
        return visitExpressionSingle(ctx.expression());
    }

    private SelectExpression processMulDivExpression(RsqlSelectParser.MulDivExpressionContext ctx) {
        SelectExpression left = visitExpressionSingle(ctx.expression(0));
        SelectExpression right = visitExpressionSingle(ctx.expression(1));
        BinaryOperator op = BinaryOperator.fromSymbol(ctx.op.getText());
        return new BinaryOpExpression(left, op, right);
    }

    private SelectExpression processAddSubExpression(RsqlSelectParser.AddSubExpressionContext ctx) {
        SelectExpression left = visitExpressionSingle(ctx.expression(0));
        SelectExpression right = visitExpressionSingle(ctx.expression(1));
        BinaryOperator op = BinaryOperator.fromSymbol(ctx.op.getText());
        return new BinaryOpExpression(left, op, right);
    }

    private SelectExpression processFuncExpression(RsqlSelectParser.FuncExpressionContext ctx) {
        return visitFunctionCallAsExpression(ctx.functionCall());
    }

    private SelectExpression processFieldExpression(RsqlSelectParser.FieldExpressionContext ctx) {
        String fieldPath = getFieldPath(ctx.field());
        validateFieldPath(fieldPath);
        return new FieldExpression(fieldPath);
    }

    private SelectExpression processNumberExpression(RsqlSelectParser.NumberExpressionContext ctx) {
        String numberText = ctx.NUMBER().getText();
        return new LiteralExpression(numberText);
    }

    // ==================== Function Call Processing ====================

    private SelectExpression visitFunctionCallAsExpression(RsqlSelectParser.FunctionCallContext ctx) {
        RsqlSelectParser.AggregateFunctionContext aggCtx = ctx.aggregateFunction();

        if (aggCtx instanceof RsqlSelectParser.FuncCallContext) {
            return processFuncCall((RsqlSelectParser.FuncCallContext) aggCtx);
        } else if (aggCtx instanceof RsqlSelectParser.CountAllContext) {
            return processCountAll((RsqlSelectParser.CountAllContext) aggCtx);
        } else if (aggCtx instanceof RsqlSelectParser.CountDistContext) {
            return processCountDist((RsqlSelectParser.CountDistContext) aggCtx);
        } else {
            throw new SyntaxErrorException("Unknown aggregate function type");
        }
    }

    private SelectExpression processFuncCall(RsqlSelectParser.FuncCallContext ctx) {
        // Detect function type
        AggregateFunction function;
        if (ctx.AVG() != null) function = AggregateFunction.AVG;
        else if (ctx.MAX() != null) function = AggregateFunction.MAX;
        else if (ctx.MIN() != null) function = AggregateFunction.MIN;
        else if (ctx.SUM() != null) function = AggregateFunction.SUM;
        else if (ctx.GRP() != null) function = AggregateFunction.NONE; // GRP() → GROUP BY
        else throw new SyntaxErrorException("Unknown aggregate function");

        // Check for DIST modifier (not supported for non-COUNT functions)
        if (ctx.aggregator != null && ctx.aggregator.getText().equalsIgnoreCase("DIST")) {
            throw new SyntaxErrorException(
                "DISTINCT modifier is only supported for COUNT function, not for " +
                (ctx.AVG() != null ? "AVG" : ctx.MAX() != null ? "MAX" : ctx.MIN() != null ? "MIN" : "SUM")
            );
        }

        // Extract field path from function argument
        String fieldPath = visitFunctionArgAsString(ctx.functionArg());
        validateFieldPath(fieldPath);

        return new FunctionExpression(fieldPath, function);
    }

    private SelectExpression processCountAll(RsqlSelectParser.CountAllContext ctx) {
        String fieldPath;

        // COUNT(*) or COUNT(field)
        if (ctx.starArg != null) {
            // COUNT(*) - use primary key or id field
            fieldPath = getPrimaryKeyField();
        } else {
            // COUNT(field)
            fieldPath = visitFunctionArgAsString(ctx.functionArg());
            validateFieldPath(fieldPath);
        }

        return new FunctionExpression(fieldPath, AggregateFunction.COUNT);
    }

    private SelectExpression processCountDist(RsqlSelectParser.CountDistContext ctx) {
        // COUNT(DIST field) → COUNT DISTINCT
        // Note: Multiple fields in COUNT DISTINCT are handled as separate expressions
        List<String> fieldPaths = visitFunctionArgsAsStrings(ctx.functionArgs());

        if (fieldPaths.isEmpty()) {
            throw new SyntaxErrorException("COUNT(DIST ...) requires at least one field");
        }

        // For simplicity, if multiple fields, we only support the first one
        // (JPA CriteriaBuilder.countDistinct() takes a single expression)
        if (fieldPaths.size() > 1) {
            log.warn("COUNT(DIST field1, field2, ...) with multiple fields: only first field will be used");
        }

        String fieldPath = fieldPaths.get(0);
        validateFieldPath(fieldPath);

        return new FunctionExpression(fieldPath, AggregateFunction.COUNT_DISTINCT);
    }

    // ==================== Helper Methods ====================

    /**
     * Extracts field path from FieldContext.
     */
    private String getFieldPath(RsqlSelectParser.FieldContext ctx) {
        StringBuilder sb = new StringBuilder(ctx.ID().getText());

        for (int i = 0; i < ctx.DOT_ID().size(); i++) {
            sb.append(ctx.DOT_ID(i).getText());
        }

        return sb.toString();
    }

    /**
     * Extracts field path string from FunctionArgContext.
     */
    private String visitFunctionArgAsString(RsqlSelectParser.FunctionArgContext ctx) {
        if (ctx.field() != null) {
            return getFieldPath(ctx.field());
        } else if (ctx.functionCall() != null) {
            throw new SyntaxErrorException(
                "Nested function calls are not currently supported"
            );
        }
        throw new SyntaxErrorException("Invalid function argument");
    }

    /**
     * Extracts list of field path strings from FunctionArgsContext.
     */
    private List<String> visitFunctionArgsAsStrings(RsqlSelectParser.FunctionArgsContext ctx) {
        List<String> fieldPaths = new ArrayList<>();
        for (RsqlSelectParser.FunctionArgContext argCtx : ctx.functionArg()) {
            fieldPaths.add(visitFunctionArgAsString(argCtx));
        }
        return fieldPaths;
    }

    /**
     * Gets all field expressions from root entity.
     */
    private List<SelectExpression> getAllFieldExpressionsFromRootEntity() {
        return getAllFieldExpressionsFromEntity("");
    }

    /**
     * Gets all field expressions from a specific entity.
     */
    private List<SelectExpression> getAllFieldExpressionsFromEntity(String entityPath) {
        List<String> fieldPaths = getAllFieldPathsFromEntity(entityPath);
        List<SelectExpression> expressions = new ArrayList<>();
        for (String fieldPath : fieldPaths) {
            expressions.add(new FieldExpression(fieldPath));
        }
        return expressions;
    }

    /**
     * Gets all field paths from a specific entity.
     */
    private List<String> getAllFieldPathsFromEntity(String entityPath) {
        if (rsqlContext == null || rsqlContext.entityManager == null) {
            throw new IllegalStateException("RsqlContext with EntityManager must be set before parsing");
        }

        Metamodel metamodel = rsqlContext.entityManager.getMetamodel();
        ManagedType<?> managedType;

        if (entityPath == null || entityPath.isEmpty()) {
            // Root entity
            managedType = metamodel.managedType(rsqlContext.entityClass);
        } else {
            // Navigate to nested entity
            managedType = getManagedTypeForPath(entityPath, metamodel);
        }

        // Get all non-association attributes (basic fields only)
        return managedType.getAttributes().stream()
            .filter(attr -> attr.getPersistentAttributeType() == Attribute.PersistentAttributeType.BASIC)
            .map(attr -> {
                if (entityPath == null || entityPath.isEmpty()) {
                    return attr.getName();
                } else {
                    return entityPath + "." + attr.getName();
                }
            })
            .collect(Collectors.toList());
    }

    /**
     * Gets the ManagedType for a given field path.
     */
    private ManagedType<?> getManagedTypeForPath(String entityPath, Metamodel metamodel) {
        String[] parts = entityPath.split("\\.");
        ManagedType<?> currentType = metamodel.managedType(rsqlContext.entityClass);

        for (String part : parts) {
            Attribute<?, ?> attribute = currentType.getAttribute(part);

            if (attribute.getPersistentAttributeType() == Attribute.PersistentAttributeType.MANY_TO_ONE ||
                attribute.getPersistentAttributeType() == Attribute.PersistentAttributeType.ONE_TO_ONE ||
                attribute.getPersistentAttributeType() == Attribute.PersistentAttributeType.EMBEDDED) {

                Class<?> javaType = attribute.getJavaType();
                currentType = metamodel.managedType(javaType);
            } else {
                throw new SyntaxErrorException(
                    "Field '" + part + "' in path '" + entityPath + "' is not an entity or embeddable"
                );
            }
        }

        return currentType;
    }

    /**
     * Validates that a field path exists in the entity metamodel.
     */
    private void validateFieldPath(String fieldPath) {
        if (rsqlContext == null || rsqlContext.entityManager == null) {
            throw new IllegalStateException("RsqlContext with EntityManager must be set before parsing");
        }

        String[] parts = fieldPath.split("\\.");
        Metamodel metamodel = rsqlContext.entityManager.getMetamodel();
        ManagedType<?> currentType = metamodel.managedType(rsqlContext.entityClass);

        for (int i = 0; i < parts.length; i++) {
            String part = parts[i];

            try {
                Attribute<?, ?> attribute = currentType.getAttribute(part);

                // If not the last part, navigate to the next type
                if (i < parts.length - 1) {
                    if (attribute.getPersistentAttributeType() == Attribute.PersistentAttributeType.MANY_TO_ONE ||
                        attribute.getPersistentAttributeType() == Attribute.PersistentAttributeType.ONE_TO_ONE ||
                        attribute.getPersistentAttributeType() == Attribute.PersistentAttributeType.EMBEDDED) {

                        Class<?> javaType = attribute.getJavaType();
                        currentType = metamodel.managedType(javaType);
                    } else {
                        throw new SyntaxErrorException(
                            "Field '" + part + "' in path '" + fieldPath + "' is not an entity or embeddable"
                        );
                    }
                }
            } catch (IllegalArgumentException e) {
                throw new SyntaxErrorException(
                    "Unknown field: '" + part + "' in path '" + fieldPath + "' for entity " +
                    currentType.getJavaType().getName()
                );
            }
        }
    }

    /**
     * Gets the primary key field name for COUNT(*) operations.
     * Returns "id" as default, which is common convention.
     */
    private String getPrimaryKeyField() {
        // TODO: Could use metamodel to get actual PK field
        // For now, use "id" as convention
        return "id";
    }
}

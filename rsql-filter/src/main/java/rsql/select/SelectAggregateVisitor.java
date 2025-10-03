package rsql.select;

import jakarta.persistence.metamodel.Attribute;
import jakarta.persistence.metamodel.ManagedType;
import jakarta.persistence.metamodel.Metamodel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rsql.antlr.select.RsqlSelectBaseVisitor;
import rsql.antlr.select.RsqlSelectParser;
import rsql.exceptions.SyntaxErrorException;
import rsql.helper.AggregateField;
import rsql.helper.AggregateField.AggregateFunction;
import rsql.where.RsqlContext;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Visitor for parsing aggregate SELECT clauses into List<AggregateField>.
 * Supports both simple fields (as GROUP BY) and aggregate functions (SUM, AVG, COUNT, etc.).
 */
public class SelectAggregateVisitor extends RsqlSelectBaseVisitor<List<AggregateField>> {

    private static final Logger log = LoggerFactory.getLogger(SelectAggregateVisitor.class);

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
    public List<AggregateField> visitSelect(RsqlSelectParser.SelectContext ctx) {
        List<AggregateField> allFields = new ArrayList<>();

        for (RsqlSelectParser.SelectElementsContext elementsCtx : ctx.selectElements()) {
            List<AggregateField> fields = visit(elementsCtx);
            if (fields != null) {
                allFields.addAll(fields);
            }
        }

        return allFields;
    }

    @Override
    public List<AggregateField> visitSelectElements(RsqlSelectParser.SelectElementsContext ctx) {
        // If SELECT *, return all fields as GROUP BY
        if (ctx.star != null) {
            return getAllFieldsAsGroupBy("");
        }

        List<AggregateField> fields = new ArrayList<>();
        for (RsqlSelectParser.SelectElementContext elemCtx : ctx.selectElement()) {
            List<AggregateField> elemFields = visit(elemCtx);
            if (elemFields != null) {
                fields.addAll(elemFields);
            }
        }

        return fields;
    }

    @Override
    public List<AggregateField> visitSeField(RsqlSelectParser.SeFieldContext ctx) {
        // Plain field → GROUP BY
        String fieldPath = getFieldPath(ctx.field());
        String alias = ctx.simpleField() != null ? ctx.simpleField().getText() : null;

        validateFieldPath(fieldPath);

        AggregateField field = AggregateField.groupBy(fieldPath, alias);
        return Arrays.asList(field);
    }

    @Override
    public List<AggregateField> visitSeAll(RsqlSelectParser.SeAllContext ctx) {
        String entityPath = getFieldPath(ctx.field());
        return getAllFieldsAsGroupBy(entityPath);
    }

    @Override
    public List<AggregateField> visitSeFuncCall(RsqlSelectParser.SeFuncCallContext ctx) {
        // Aggregate function with optional alias
        List<AggregateField> fields = visit(ctx.functionCall());

        // If alias is present, create new AggregateField with the alias
        if (ctx.simpleField() != null && !fields.isEmpty()) {
            String alias = ctx.simpleField().getText();
            AggregateField originalField = fields.get(0);
            // Create new AggregateField with the same fieldPath and function, but with alias
            fields = Arrays.asList(
                new AggregateField(originalField.getFieldPath(), originalField.getFunction(), alias)
            );
        }

        return fields;
    }

    @Override
    public List<AggregateField> visitFuncCall(RsqlSelectParser.FuncCallContext ctx) {
        // Detect function type
        AggregateFunction function;
        if (ctx.AVG() != null) function = AggregateFunction.AVG;
        else if (ctx.MAX() != null) function = AggregateFunction.MAX;
        else if (ctx.MIN() != null) function = AggregateFunction.MIN;
        else if (ctx.SUM() != null) function = AggregateFunction.SUM;
        else if (ctx.GRP() != null) function = AggregateFunction.NONE; // GRP() → GROUP BY
        else throw new SyntaxErrorException("Unknown aggregate function");

        // Check for DIST modifier
        if (ctx.aggregator != null && ctx.aggregator.getText().equalsIgnoreCase("DIST")) {
            // DIST is only valid for COUNT, not for other functions
            throw new SyntaxErrorException(
                "DISTINCT modifier is only supported for COUNT function, not for " +
                (ctx.AVG() != null ? "AVG" : ctx.MAX() != null ? "MAX" : ctx.MIN() != null ? "MIN" : "SUM")
            );
        }

        // Extract field path from function argument
        String fieldPath = visitFunctionArgAsString(ctx.functionArg());

        validateFieldPath(fieldPath);

        AggregateField field = AggregateField.of(fieldPath, function, null);
        return Arrays.asList(field);
    }

    @Override
    public List<AggregateField> visitCountAll(RsqlSelectParser.CountAllContext ctx) {
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

        AggregateField field = AggregateField.of(fieldPath, AggregateFunction.COUNT, null);
        return Arrays.asList(field);
    }

    @Override
    public List<AggregateField> visitCountDist(RsqlSelectParser.CountDistContext ctx) {
        // COUNT(DIST field1, field2, ...) → COUNT DISTINCT
        List<String> fieldPaths = visitFunctionArgsAsStrings(ctx.functionArgs());

        List<AggregateField> fields = new ArrayList<>();
        for (String fieldPath : fieldPaths) {
            validateFieldPath(fieldPath);
            fields.add(AggregateField.of(fieldPath, AggregateFunction.COUNT_DISTINCT, null));
        }

        return fields;
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
     * Gets all fields from entity as AggregateField objects with NONE function (GROUP BY).
     */
    private List<AggregateField> getAllFieldsAsGroupBy(String entityPath) {
        List<String> fieldPaths = getAllFieldPathsFromEntity(entityPath);
        List<AggregateField> fields = new ArrayList<>();
        for (String fieldPath : fieldPaths) {
            fields.add(AggregateField.groupBy(fieldPath, null));
        }
        return fields;
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
        // TODO: Could use metamodel to get actual PK field:
        // EntityType<?> entityType = metamodel.entity(rsqlContext.entityClass);
        // Set<SingularAttribute<?, ?>> idAttributes = entityType.getIdClassAttributes();
        // For now, use "id" as convention
        return "id";
    }
}

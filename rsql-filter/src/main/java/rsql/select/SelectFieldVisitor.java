package rsql.select;

import jakarta.persistence.metamodel.Attribute;
import jakarta.persistence.metamodel.ManagedType;
import jakarta.persistence.metamodel.Metamodel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rsql.antlr.select.RsqlSelectBaseVisitor;
import rsql.antlr.select.RsqlSelectParser;
import rsql.exceptions.SyntaxErrorException;
import rsql.where.RsqlContext;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Visitor for parsing simple (non-aggregate) SELECT clauses into List<SelectField>.
 * Does not support aggregate functions - throws SyntaxErrorException if aggregate function is encountered.
 */
public class SelectFieldVisitor extends RsqlSelectBaseVisitor<List<SelectField>> {

    private static final Logger log = LoggerFactory.getLogger(SelectFieldVisitor.class);

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
    public List<SelectField> visitSelect(RsqlSelectParser.SelectContext ctx) {
        List<SelectField> allFields = new ArrayList<>();

        for (RsqlSelectParser.SelectElementsContext elementsCtx : ctx.selectElements()) {
            List<SelectField> fields = visit(elementsCtx);
            if (fields != null) {
                allFields.addAll(fields);
            }
        }

        return allFields;
    }

    @Override
    public List<SelectField> visitSelectElements(RsqlSelectParser.SelectElementsContext ctx) {
        // If SELECT *, return all fields from root entity
        if (ctx.star != null) {
            return getAllFieldsFromRootEntity();
        }

        List<SelectField> fields = new ArrayList<>();
        for (RsqlSelectParser.SelectElementContext elemCtx : ctx.selectElement()) {
            List<SelectField> elemFields = visit(elemCtx);
            if (elemFields != null) {
                fields.addAll(elemFields);
            }
        }

        return fields;
    }

    @Override
    public List<SelectField> visitSeField(RsqlSelectParser.SeFieldContext ctx) {
        String fieldPath = getFieldPath(ctx.field());
        String alias = ctx.simpleField() != null ? ctx.simpleField().getText() : null;

        // Validate field path exists in entity
        validateFieldPath(fieldPath);

        return Arrays.asList(new SelectField(fieldPath, alias));
    }

    @Override
    public List<SelectField> visitSeAll(RsqlSelectParser.SeAllContext ctx) {
        String entityPath = getFieldPath(ctx.field());
        return getAllFieldsFromEntity(entityPath);
    }

    @Override
    public List<SelectField> visitSeFuncCall(RsqlSelectParser.SeFuncCallContext ctx) {
        throw new SyntaxErrorException(
            "Aggregate functions are not supported in simple SELECT queries. " +
            "Use SelectAggregateVisitor for aggregate queries."
        );
    }

    @Override
    public List<SelectField> visitSeExpression(RsqlSelectParser.SeExpressionContext ctx) {
        // This visitor doesn't support arithmetic expressions
        // But due to grammar rule ordering, simple fields are now parsed as expressions
        // So we need to check if it's a simple field and handle it

        RsqlSelectParser.ExpressionContext exprCtx = ctx.expression();

        // If it's a simple field expression (without operators), handle it as seField
        if (exprCtx instanceof RsqlSelectParser.FieldExpressionContext) {
            RsqlSelectParser.FieldExpressionContext fieldCtx = (RsqlSelectParser.FieldExpressionContext) exprCtx;
            String fieldPath = getFieldPath(fieldCtx.field());
            String alias = ctx.simpleField() != null ? ctx.simpleField().getText() : null;

            validateFieldPath(fieldPath);
            return Arrays.asList(new SelectField(fieldPath, alias));
        }

        // If it's a function expression, throw error (no aggregates in simple SELECT)
        if (exprCtx instanceof RsqlSelectParser.FuncExpressionContext) {
            throw new SyntaxErrorException(
                "Aggregate functions are not supported in simple SELECT queries. " +
                "Use SelectAggregateVisitor for aggregate queries."
            );
        }

        // For any other expression type (with operators), throw error
        throw new SyntaxErrorException(
            "Arithmetic expressions are not supported in this query type. " +
            "Use SelectExpressionVisitor for queries with arithmetic expressions."
        );
    }

    /**
     * Extracts field path from FieldContext.
     * Example: field context "productType.name" -> "productType.name"
     */
    private String getFieldPath(RsqlSelectParser.FieldContext ctx) {
        StringBuilder sb = new StringBuilder(ctx.ID().getText());

        for (int i = 0; i < ctx.DOT_ID().size(); i++) {
            sb.append(ctx.DOT_ID(i).getText());
        }

        return sb.toString();
    }

    /**
     * Gets all fields from root entity as SelectField objects without aliases.
     */
    private List<SelectField> getAllFieldsFromRootEntity() {
        return getAllFieldsFromEntity("");
    }

    /**
     * Gets all fields from a specific entity or nested entity as SelectField objects without aliases.
     *
     * @param entityPath The path to the entity (empty string for root, or "productType" for nested)
     * @return List of SelectField objects representing all fields
     */
    private List<SelectField> getAllFieldsFromEntity(String entityPath) {
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

        // Get all non-association attributes (basic fields only, not relations)
        List<String> fieldPaths = managedType.getAttributes().stream()
            .filter(attr -> attr.getPersistentAttributeType() == Attribute.PersistentAttributeType.BASIC)
            .map(attr -> {
                if (entityPath == null || entityPath.isEmpty()) {
                    return attr.getName();
                } else {
                    return entityPath + "." + attr.getName();
                }
            })
            .collect(Collectors.toList());

        return fieldPaths.stream()
            .map(fp -> new SelectField(fp, null))
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
     *
     * @param fieldPath The field path to validate
     * @throws SyntaxErrorException if the field path is invalid
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
}

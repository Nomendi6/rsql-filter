package rsql.helper;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Root;
import rsql.where.RsqlContext;
import rsql.helper.SimpleQueryExecutor;

/**
 * Represents a field reference in a SELECT expression.
 * Example: "price", "productType.name"
 */
public class FieldExpression extends SelectExpression {

    private final String fieldPath;

    /**
     * Creates a new field expression.
     * @param fieldPath The field path (e.g., "price", "productType.name")
     */
    public FieldExpression(String fieldPath) {
        this.fieldPath = fieldPath;
    }

    /**
     * Creates a new field expression with an alias.
     * @param fieldPath The field path
     * @param alias The alias
     */
    public FieldExpression(String fieldPath, String alias) {
        this.fieldPath = fieldPath;
        this.alias = alias;
    }

    /**
     * Gets the field path.
     * @return The field path
     */
    public String getFieldPath() {
        return fieldPath;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> Expression<T> toJpaExpression(
        CriteriaBuilder builder,
        Root<?> root,
        RsqlContext<?> context
    ) {
        Path<?> path = SimpleQueryExecutor.getPropertyPathRecursive(
            fieldPath,
            root,
            context,
            context.joinsMap,
            context.classMetadataMap
        );
        return (Expression<T>) path;
    }

    @Override
    public String toExpressionString() {
        return fieldPath + (hasAlias() ? ":" + alias : "");
    }

    @Override
    public String toString() {
        return "FieldExpression{" +
               "fieldPath='" + fieldPath + '\'' +
               ", alias='" + alias + '\'' +
               '}';
    }
}

package rsql.helper;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Root;
import rsql.exceptions.SyntaxErrorException;
import rsql.helper.AggregateField.AggregateFunction;
import rsql.where.RsqlContext;
import rsql.helper.SimpleQueryExecutor;

/**
 * Represents an aggregate function in a SELECT expression.
 * Examples: SUM(price), AVG(quantity), COUNT(*), COUNT(DIST productType.id)
 */
public class FunctionExpression extends SelectExpression {

    private final String fieldPath;
    private final AggregateFunction function;
    private final boolean distinct;

    /**
     * Creates a new function expression.
     * @param fieldPath The field path (e.g., "price", "id" for COUNT(*))
     * @param function The aggregate function
     */
    public FunctionExpression(String fieldPath, AggregateFunction function) {
        this(fieldPath, function, false);
    }

    /**
     * Creates a new function expression with optional DISTINCT modifier.
     * @param fieldPath The field path
     * @param function The aggregate function
     * @param distinct Whether to use DISTINCT (only valid for COUNT)
     */
    public FunctionExpression(String fieldPath, AggregateFunction function, boolean distinct) {
        this.fieldPath = fieldPath;
        this.function = function;
        this.distinct = distinct;

        // Validate DISTINCT is only used with COUNT
        if (distinct && function != AggregateFunction.COUNT && function != AggregateFunction.COUNT_DISTINCT) {
            throw new SyntaxErrorException("DISTINCT modifier is only supported for COUNT function");
        }
    }

    /**
     * Creates a new function expression with an alias.
     * @param fieldPath The field path
     * @param function The aggregate function
     * @param alias The alias
     */
    public FunctionExpression(String fieldPath, AggregateFunction function, String alias) {
        this(fieldPath, function, false);
        this.alias = alias;
    }

    /**
     * Gets the field path.
     * @return The field path
     */
    public String getFieldPath() {
        return fieldPath;
    }

    /**
     * Gets the aggregate function.
     * @return The aggregate function
     */
    public AggregateFunction getFunction() {
        return function;
    }

    /**
     * Checks if DISTINCT modifier is used.
     * @return true if DISTINCT is used
     */
    public boolean isDistinct() {
        return distinct;
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

        Expression<?> result = switch (function) {
            case SUM -> builder.sum((Expression<Number>) path);
            case AVG -> builder.avg((Expression<Number>) path);
            case COUNT -> distinct || function == AggregateFunction.COUNT_DISTINCT
                ? builder.countDistinct(path)
                : builder.count(path);
            case COUNT_DISTINCT -> builder.countDistinct(path);
            case MIN -> builder.least((Expression) path);
            case MAX -> builder.greatest((Expression) path);
            case NONE -> path; // GRP() function or plain field
        };

        return (Expression<T>) result;
    }

    @Override
    public String toExpressionString() {
        String funcName = function == AggregateFunction.COUNT_DISTINCT ? "COUNT(DIST " : function.name() + "(";
        String distinctStr = (distinct && function == AggregateFunction.COUNT) ? "DIST " : "";
        return funcName + distinctStr + fieldPath + ")" + (hasAlias() ? ":" + alias : "");
    }

    @Override
    public String toString() {
        return "FunctionExpression{" +
               "fieldPath='" + fieldPath + '\'' +
               ", function=" + function +
               ", distinct=" + distinct +
               ", alias='" + alias + '\'' +
               '}';
    }
}

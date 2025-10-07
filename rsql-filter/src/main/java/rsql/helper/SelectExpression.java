package rsql.helper;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Root;
import rsql.where.RsqlContext;

/**
 * Abstract base class for SELECT expressions.
 * Supports fields, aggregate functions, arithmetic operations, and literals.
 *
 * <p>Usage example:</p>
 * <pre>
 * // SUM(price) - SUM(cost)
 * SelectExpression expr = new BinaryOpExpression(
 *     new FunctionExpression("price", AggregateFunction.SUM),
 *     BinaryOperator.SUB,
 *     new FunctionExpression("cost", AggregateFunction.SUM)
 * );
 * expr.setAlias("profit");
 * </pre>
 */
public abstract class SelectExpression {

    protected String alias;

    /**
     * Gets the alias for this expression.
     * @return The alias, or null if no alias is set
     */
    public String getAlias() {
        return alias;
    }

    /**
     * Sets the alias for this expression.
     * @param alias The alias
     */
    public void setAlias(String alias) {
        this.alias = alias;
    }

    /**
     * Checks if this expression has an alias.
     * @return true if alias is not null and not empty
     */
    public boolean hasAlias() {
        return alias != null && !alias.isEmpty();
    }

    /**
     * Converts this expression to a JPA Criteria API Expression.
     *
     * @param builder CriteriaBuilder for constructing expressions
     * @param root Query root
     * @param context RSQL context with EntityManager and shared JOIN cache
     * @param <T> Expression type
     * @return JPA Expression representing this SELECT expression
     */
    public abstract <T> Expression<T> toJpaExpression(
        CriteriaBuilder builder,
        Root<?> root,
        RsqlContext<?> context
    );

    /**
     * Returns a string representation of this expression for debugging.
     * @return String representation
     */
    public abstract String toExpressionString();
}

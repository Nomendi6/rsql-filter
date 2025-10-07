package rsql.helper;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Root;
import rsql.where.RsqlContext;

import java.math.BigDecimal;

/**
 * Represents a numeric literal in a SELECT expression.
 * Examples: 10, 1.5, 100.0
 */
public class LiteralExpression extends SelectExpression {

    private final BigDecimal value;

    /**
     * Creates a new literal expression.
     * @param value The numeric value
     */
    public LiteralExpression(BigDecimal value) {
        this.value = value;
    }

    /**
     * Creates a new literal expression from a string.
     * @param valueString The numeric value as string
     */
    public LiteralExpression(String valueString) {
        this.value = new BigDecimal(valueString);
    }

    /**
     * Creates a new literal expression from a double.
     * @param value The numeric value
     */
    public LiteralExpression(double value) {
        this.value = BigDecimal.valueOf(value);
    }

    /**
     * Creates a new literal expression from an integer.
     * @param value The numeric value
     */
    public LiteralExpression(int value) {
        this.value = BigDecimal.valueOf(value);
    }

    /**
     * Gets the numeric value.
     * @return The value
     */
    public BigDecimal getValue() {
        return value;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> Expression<T> toJpaExpression(
        CriteriaBuilder builder,
        Root<?> root,
        RsqlContext<?> context
    ) {
        // Create a literal expression in JPA Criteria API
        return (Expression<T>) builder.literal(value);
    }

    @Override
    public String toExpressionString() {
        return value.toString() + (hasAlias() ? ":" + alias : "");
    }

    @Override
    public String toString() {
        return "LiteralExpression{" +
               "value=" + value +
               ", alias='" + alias + '\'' +
               '}';
    }
}

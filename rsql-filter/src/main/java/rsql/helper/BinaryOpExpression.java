package rsql.helper;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Root;
import rsql.where.RsqlContext;

/**
 * Represents a binary arithmetic operation in a SELECT expression.
 * Examples:
 * - SUM(price) + 10
 * - SUM(revenue) - SUM(cost)
 * - COUNT(*) / 2
 * - (SUM(x) + SUM(y)) * 1.2
 */
public class BinaryOpExpression extends SelectExpression {

    private final SelectExpression left;
    private final BinaryOperator operator;
    private final SelectExpression right;

    /**
     * Creates a new binary operation expression.
     * @param left Left operand
     * @param operator The operator (+, -, *, /)
     * @param right Right operand
     */
    public BinaryOpExpression(SelectExpression left, BinaryOperator operator, SelectExpression right) {
        this.left = left;
        this.operator = operator;
        this.right = right;
    }

    /**
     * Creates a new binary operation expression with an alias.
     * @param left Left operand
     * @param operator The operator
     * @param right Right operand
     * @param alias The alias
     */
    public BinaryOpExpression(SelectExpression left, BinaryOperator operator, SelectExpression right, String alias) {
        this(left, operator, right);
        this.alias = alias;
    }

    /**
     * Gets the left operand.
     * @return The left operand
     */
    public SelectExpression getLeft() {
        return left;
    }

    /**
     * Gets the operator.
     * @return The operator
     */
    public BinaryOperator getOperator() {
        return operator;
    }

    /**
     * Gets the right operand.
     * @return The right operand
     */
    public SelectExpression getRight() {
        return right;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> Expression<T> toJpaExpression(
        CriteriaBuilder builder,
        Root<?> root,
        RsqlContext<?> context
    ) {
        // Recursively convert left and right operands to JPA expressions
        Expression<Number> leftExpr = left.toJpaExpression(builder, root, context);
        Expression<Number> rightExpr = right.toJpaExpression(builder, root, context);

        // Apply the operator
        Expression<Number> result = switch (operator) {
            case ADD -> builder.sum(leftExpr, rightExpr);
            case SUB -> builder.diff(leftExpr, rightExpr);
            case MUL -> builder.prod(leftExpr, rightExpr);
            case DIV -> builder.quot(leftExpr, rightExpr);
        };

        return (Expression<T>) result;
    }

    @Override
    public String toExpressionString() {
        String leftStr = left.toExpressionString();
        String rightStr = right.toExpressionString();

        // Add parentheses if needed for readability
        if (left instanceof BinaryOpExpression) {
            leftStr = "(" + leftStr + ")";
        }
        if (right instanceof BinaryOpExpression) {
            rightStr = "(" + rightStr + ")";
        }

        return leftStr + " " + operator.getSymbol() + " " + rightStr +
               (hasAlias() ? ":" + alias : "");
    }

    @Override
    public String toString() {
        return "BinaryOpExpression{" +
               "left=" + left +
               ", operator=" + operator +
               ", right=" + right +
               ", alias='" + alias + '\'' +
               '}';
    }
}

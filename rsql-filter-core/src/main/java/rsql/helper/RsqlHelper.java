package rsql.helper;

/**
 * This class provides helper methods for manipulating RSQL expressions.
 */
public class RsqlHelper {

    /**
     * Adds an RSQL expression to an existing one using the AND operator.
     *
     * @param originalExpression The original RSQL expression.
     * @param expressionToAdd The RSQL expression to be added.
     * @return The combined RSQL expression, or the original expression if the expression to be added is null or empty.
     */
    public static String andRsql(String originalExpression, String expressionToAdd) {
        if (expressionToAdd == null) {
            return originalExpression;
        }
        if (expressionToAdd.equals("")) {
            return originalExpression;
        }
        if (originalExpression == null) {
            return expressionToAdd;
        }
        if (originalExpression.equals("")) {
            return expressionToAdd;
        }
        return originalExpression + ";" + expressionToAdd;
    }

    /**
     * Adds an RSQL expression to an existing one using the OR operator.
     *
     * @param originalExpression The original RSQL expression.
     * @param expressionToAdd The RSQL expression to be added.
     * @return The combined RSQL expression, or the original expression if the expression to be added is null or empty.
     */
    public static String orRsql(String originalExpression, String expressionToAdd) {
        if (expressionToAdd == null) {
            return originalExpression;
        }
        if (expressionToAdd.equals("")) {
            return originalExpression;
        }
        if (originalExpression == null) {
            return expressionToAdd;
        }
        if (originalExpression.equals("")) {
            return expressionToAdd;
        }
        return originalExpression + "," + expressionToAdd;
    }

    /**
     * Adds parentheses to an RSQL expression.
     *
     * @param originalExpression The original RSQL expression.
     * @return The RSQL expression with added parentheses, or the original expression if it is null or empty.
     */
    public static String addParenthases(String originalExpression) {
        if (originalExpression == null) {
            return originalExpression;
        }
        if (originalExpression.equals("")) {
            return originalExpression;
        }
        return "(" + originalExpression + ")";
    }
}

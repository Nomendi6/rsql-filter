package rsql.helper;

public class RsqlHelper {

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

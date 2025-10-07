package rsql.helper;

/**
 * Enum representing binary arithmetic operators in SELECT expressions.
 */
public enum BinaryOperator {
    /**
     * Addition operator (+)
     */
    ADD("+"),

    /**
     * Subtraction operator (-)
     */
    SUB("-"),

    /**
     * Multiplication operator (*)
     */
    MUL("*"),

    /**
     * Division operator (/)
     */
    DIV("/");

    private final String symbol;

    BinaryOperator(String symbol) {
        this.symbol = symbol;
    }

    /**
     * Gets the operator symbol.
     * @return The symbol (e.g., "+", "-", "*", "/")
     */
    public String getSymbol() {
        return symbol;
    }

    /**
     * Parses an operator symbol into a BinaryOperator enum.
     * @param symbol The operator symbol
     * @return The corresponding BinaryOperator
     * @throws IllegalArgumentException if symbol is not recognized
     */
    public static BinaryOperator fromSymbol(String symbol) {
        return switch (symbol) {
            case "+" -> ADD;
            case "-" -> SUB;
            case "*" -> MUL;
            case "/" -> DIV;
            default -> throw new IllegalArgumentException("Unknown operator: " + symbol);
        };
    }

    @Override
    public String toString() {
        return symbol;
    }
}

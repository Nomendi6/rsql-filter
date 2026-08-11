package rsql.describe;

/**
 * The comparison an operand is subjected to.
 * <p>
 * Fourteen constants come straight from the grammar - the ten alternatives of the {@code operator} rule plus
 * {@code IN} / {@code NIN} / {@code BT} / {@code NBT}. {@code IS_NULL} and {@code IS_NOT_NULL} are derived:
 * the grammar writes them as {@code operatorBasic NULL}, but the library executes them as a unary predicate
 * ({@code criteriaBuilder.isNull}) rather than a comparison with a value, so the description follows the
 * execution rather than the syntax.
 * <p>
 * There is deliberately no {@code getDefaultLabel()}. A label is not a property of the operator alone: the
 * LIKE family reads as "starts with" or "contains" depending on the {@link PatternShape}, which comes from the
 * value. Defaults live in {@link FilterLabelResolver#operatorLabel(FilterCondition)}, where the application
 * can override them; an enum carrying its own labels would be a second, unreachable source of truth.
 */
public enum FilterOperator {
    EQ, NEQ,
    LT, GT, LE, GE,
    LIKE, NLIKE, CLIKE, CNLIKE,
    IN, NIN,
    BT, NBT,
    IS_NULL, IS_NOT_NULL
}

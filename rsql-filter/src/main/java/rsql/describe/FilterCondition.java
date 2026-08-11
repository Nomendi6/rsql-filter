package rsql.describe;

import java.util.Objects;

/**
 * One condition: a left-hand operand, an operator, and a right-hand side.
 * <p>
 * The pattern fields describe a LIKE comparison and are {@code NONE} / {@code null} for everything else. They
 * are kept here rather than derived at render time because the derivation needs the raw value, which the
 * renderer no longer has once the resolver has been applied.
 *
 * @param left          What is being compared
 * @param operator      How it is compared
 * @param rightSide     What it is compared against
 * @param patternShape  For the LIKE family, what the pattern means; {@link PatternShape#NONE} otherwise
 * @param patternNeedle The value with the surrounding {@code *} removed - {@code null} unless the shape is
 *                      EXACT, STARTS_WITH, ENDS_WITH or CONTAINS
 */
public record FilterCondition(
    Operand left,
    FilterOperator operator,
    RightSide rightSide,
    PatternShape patternShape,
    String patternNeedle
) implements FilterNode {

    public FilterCondition {
        Objects.requireNonNull(left, "left");
        Objects.requireNonNull(operator, "operator");
        Objects.requireNonNull(rightSide, "rightSide");
        Objects.requireNonNull(patternShape, "patternShape");

        boolean nullOperator = operator == FilterOperator.IS_NULL || operator == FilterOperator.IS_NOT_NULL;
        if (nullOperator != (rightSide instanceof RightSide.NoValue)) {
            throw new IllegalArgumentException("NoValue goes with IS_NULL / IS_NOT_NULL and with nothing else");
        }
        boolean inOperator = operator == FilterOperator.IN || operator == FilterOperator.NIN;
        if (inOperator != (rightSide instanceof RightSide.ValueList)) {
            throw new IllegalArgumentException("ValueList goes with IN / NIN and with nothing else");
        }
        boolean betweenOperator = operator == FilterOperator.BT || operator == FilterOperator.NBT;
        if (betweenOperator != (rightSide instanceof RightSide.Range)) {
            throw new IllegalArgumentException("Range goes with BT / NBT and with nothing else");
        }

        if (patternShape != PatternShape.NONE) {
            if (!Invariants.isLikeFamily(operator)) {
                throw new IllegalArgumentException("a PatternShape only makes sense for the LIKE family");
            }
            if (!(rightSide instanceof RightSide.SingleValue)) {
                throw new IllegalArgumentException(
                    "a PatternShape is derived from a literal, so it needs a SingleValue right-hand side"
                );
            }
        }
        if (patternShape == PatternShape.NONE) {
            if (patternNeedle != null) {
                throw new IllegalArgumentException("a needle without a shape describes nothing");
            }
        } else {
            // the shape and the needle must match the value they claim to describe: a hand-built condition
            // saying STARTS_WITH "WRONG" over the value "A*" would render a description that is simply false
            Object value = ((RightSide.SingleValue) rightSide).value();
            if (!(value instanceof String raw)) {
                throw new IllegalArgumentException(
                    "a LIKE pattern is derived from a string, but the value was " + value.getClass().getName()
                );
            }
            PatternShape derived = PatternShape.of(raw);
            if (derived != patternShape) {
                throw new IllegalArgumentException(
                    "the pattern " + raw + " is " + derived + ", not " + patternShape);
            }
            String expectedNeedle = derived.needleOf(raw);
            if (!java.util.Objects.equals(expectedNeedle, patternNeedle)) {
                throw new IllegalArgumentException(
                    "the needle of " + raw + " is " + expectedNeedle + ", not " + patternNeedle);
            }
        }
    }
}

package rsql.describe;

import java.util.List;

/**
 * Turns the parts of a filter into text a reader understands.
 * <p>
 * The library has no source of readable names - no labels, no i18n, no annotations - and the JPA metamodel
 * only exposes the Java identifier. Without an implementation of this interface a description can print
 * {@code productType.name} and nothing better, which is fine for diagnostics and useless in a report.
 * <p>
 * Every method has a default, so an application overrides only what it cares about. {@link #TECHNICAL} is the
 * all-defaults instance.
 */
public interface FilterLabelResolver {

    /** All defaults: technical field paths and English operator labels. Useful for diagnostics. */
    FilterLabelResolver TECHNICAL = new FilterLabelResolver() {
    };

    /**
     * The left-hand side.
     * <p>
     * Takes an {@link Operand} rather than a {@code String} so that the aggregate and alias forms a HAVING
     * description will need can be added without changing this signature. An unknown form degrades to
     * {@code toString()} rather than throwing - a report should not die on a shape it has not seen.
     *
     * @param operand What is being compared
     * @return Its label
     */
    default String operandLabel(Operand operand) {
        if (operand instanceof Operand.FieldOperand field) {
            return field.fieldPath();
        }
        return String.valueOf(operand);
    }

    /**
     * The operator.
     * <p>
     * Takes the whole condition, not just the {@link FilterOperator}, because the label is not a property of
     * the operator alone: all four LIKE operators read differently depending on the {@link PatternShape}, and
     * that comes from the value.
     *
     * @param condition The condition being described
     * @return The operator label
     */
    default String operatorLabel(FilterCondition condition) {
        FilterOperator operator = condition.operator();
        return switch (operator) {
            case EQ -> "is";
            case NEQ -> "is not";
            case GT -> "is greater than";
            case GE -> "is greater than or equal to";
            case LT -> "is less than";
            case LE -> "is less than or equal to";
            case IN -> "is one of";
            case NIN -> "is not one of";
            case BT -> "is between";
            case NBT -> "is not between";
            case IS_NULL -> "is empty";
            case IS_NOT_NULL -> "is not empty";
            case LIKE, NLIKE, CLIKE, CNLIKE -> patternStem(condition.patternShape(), negated(operator))
                + caseSuffix(operator);
        };
    }

    /**
     * A value.
     *
     * @param left  The left-hand side, so an application can mask by field
     * @param value The value
     * @return Its label
     */
    default String valueLabel(Operand left, Object value) {
        return value instanceof String text ? quote(text) : String.valueOf(value);
    }

    /**
     * Puts a string between quotes and escapes what would otherwise make the description ambiguous.
     * <p>
     * Without this, {@code name=='A and status is B'} reads as two conditions joined by "and", a comma inside
     * a value cannot be told from the separator of an {@code IN} list, an empty value vanishes, and a value
     * containing a newline breaks the promise that {@link FilterDescription#getText()} is one line.
     *
     * @param text The raw value
     * @return The value, quoted and escaped
     */
    static String quote(String text) {
        StringBuilder quoted = new StringBuilder(text.length() + 2).append('"');
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            switch (c) {
                case '"' -> quoted.append("\\\"");
                case '\\' -> quoted.append("\\\\");
                case '\n' -> quoted.append("\\n");
                case '\r' -> quoted.append("\\r");
                case '\t' -> quoted.append("\\t");
                default -> {
                    if (c < 0x20) {
                        quoted.append(String.format("\\u%04x", (int) c));
                    } else {
                        quoted.append(c);
                    }
                }
            }
        }
        return quoted.append('"').toString();
    }

    /**
     * The value of a LIKE pattern.
     * <p>
     * Returns the needle rather than the raw pattern, because the operator label already says "starts with"
     * or "contains" - printing {@code "*abc*"} next to "contains" would repeat the wildcards. For
     * {@link PatternShape#CUSTOM} there is no honest needle, so the raw value is shown as the user wrote it.
     *
     * @param left      The left-hand side
     * @param raw       The pattern as written
     * @param shape     What the pattern means
     * @param needle    The pattern without its surrounding wildcards, or {@code null}
     * @return The label for the value cell
     */
    default String patternValueLabel(Operand left, Object raw, PatternShape shape, String needle) {
        return needle != null ? valueLabel(left, needle) : valueLabel(left, raw);
    }

    /**
     * A named parameter.
     *
     * @param left       The left-hand side
     * @param name       The parameter name, without the colon
     * @param resolution Whether a value was supplied, and which
     * @return Its label
     */
    default String parameterLabel(Operand left, String name, ParameterResolution resolution) {
        return resolution.supplied() ? valueLabel(left, resolution.value()) : ":" + name;
    }

    /**
     * A right-hand side this version does not know.
     * <p>
     * {@link RightSide} is open for extension, so a caller may hand {@code describe} a tree containing a shape
     * added later - or one of their own. Without this hook such a value would go straight to
     * {@code toString()}, skipping localisation and, more seriously, any masking the resolver applies.
     *
     * @param left      The left-hand side
     * @param rightSide The shape this version cannot render
     * @return Its label
     */
    default String rightSideLabel(Operand left, RightSide rightSide) {
        return valueLabel(left, String.valueOf(rightSide));
    }

    /** How conditions are joined. */
    default String junctionLabel(FilterGroup.Junction junction) {
        return junction == FilterGroup.Junction.AND ? "and" : "or";
    }

    /** How the elements of an {@code IN} list are joined once each has been rendered. */
    default String joinList(List<String> renderedValues) {
        return String.join(", ", renderedValues);
    }

    /** How the two bounds of a range are joined once each has been rendered. */
    default String joinRange(String from, String to) {
        return from + " and " + to;
    }

    /**
     * Whether a parameter's value was supplied, and which.
     * <p>
     * A flag rather than a nullable value, because "no map was passed" and "the map holds null for this key"
     * are different things and a report should not print them the same way.
     *
     * @param supplied Whether a value was available
     * @param value    The value, which may itself be {@code null}
     */
    record ParameterResolution(boolean supplied, Object value) {
        public static ParameterResolution unresolved() {
            return new ParameterResolution(false, null);
        }

        public static ParameterResolution of(Object value) {
            return new ParameterResolution(true, value);
        }
    }

    private static boolean negated(FilterOperator operator) {
        return operator == FilterOperator.NLIKE || operator == FilterOperator.CNLIKE;
    }

    private static String patternStem(PatternShape shape, boolean negated) {
        return switch (shape) {
            case EXACT -> negated ? "does not equal" : "equals";
            case STARTS_WITH -> negated ? "does not start with" : "starts with";
            case ENDS_WITH -> negated ? "does not end with" : "ends with";
            case CONTAINS -> negated ? "does not contain" : "contains";
            case CUSTOM, NONE -> negated ? "does not match pattern" : "matches pattern";
        };
    }

    /**
     * Whether case matters has to be said on both sides, not only on the case-sensitive one: {@code =like=} in
     * this library lowercases both the field and the pattern, so a bare "starts with" would read as
     * case-sensitive and be wrong.
     */
    private static String caseSuffix(FilterOperator operator) {
        return switch (operator) {
            case CLIKE, CNLIKE -> " (case-sensitive)";
            default -> " (ignoring case)";
        };
    }
}

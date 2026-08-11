package rsql.describe;

import java.util.Objects;
import java.util.ResourceBundle;

/**
 * Field, operator and junction labels from a {@link ResourceBundle}.
 * <p>
 * A translated field name is not enough on its own: "Cijena is greater than 10" is not a Croatian sentence.
 * This resolver therefore localises the operators and the "and"/"or" as well, which is the difference between it
 * and {@link MapFilterLabelResolver}.
 * <p>
 * Keys, all optionally behind a prefix given to the constructor:
 * <table border="1">
 * <caption>Bundle keys</caption>
 * <tr><th>Key</th><th>Example</th><th>Value it produces</th></tr>
 * <tr><td>{@code field.<path>}</td><td>{@code field.productType.name}</td><td>The left-hand side</td></tr>
 * <tr><td>{@code operator.<NAME>}</td><td>{@code operator.GT}</td><td>The operator, for everything but LIKE</td></tr>
 * <tr><td>{@code operator.<NAME>.<SHAPE>}</td><td>{@code operator.LIKE.STARTS_WITH}</td><td>The operator, for the four LIKE forms</td></tr>
 * <tr><td>{@code junction.AND}, {@code junction.OR}</td><td></td><td>How conditions are joined</td></tr>
 * </table>
 * The LIKE family is keyed by operator <em>and</em> {@link PatternShape} because {@code =like=} reads as "starts
 * with", "ends with" or "contains" depending on where the caller put the wildcards. Each of those keys carries
 * the whole label, so a translation says as much or as little about case sensitivity as its language needs -
 * unlike the English default, which always appends a parenthesised note.
 * <p>
 * Use {@link #operatorKey(FilterCondition)} and {@link #fieldKey(String)} to generate a starter bundle rather
 * than typing the 32 operator keys by hand. A key that is not in the bundle falls back to the delegate, so a
 * partial translation still prints.
 * <pre>{@code
 * # messages_hr.properties
 * field.productType.name = Vrsta proizvoda
 * field.price            = Cijena
 * operator.EQ            = je
 * operator.GT            = je veci od
 * operator.LIKE.STARTS_WITH = pocinje s
 * junction.AND           = i
 * junction.OR            = ili
 * }</pre>
 */
public class ResourceBundleFilterLabelResolver extends DelegatingFilterLabelResolver {

    private final ResourceBundle bundle;
    private final String prefix;

    /**
     * @param bundle The bundle holding the labels
     */
    public ResourceBundleFilterLabelResolver(ResourceBundle bundle) {
        this(bundle, "", TECHNICAL);
    }

    /**
     * @param bundle The bundle holding the labels
     * @param prefix Put in front of every key, for a bundle shared with the rest of the application, e.g.
     *               {@code "filter."}; use {@code ""} for none
     */
    public ResourceBundleFilterLabelResolver(ResourceBundle bundle, String prefix) {
        this(bundle, prefix, TECHNICAL);
    }

    /**
     * @param bundle   The bundle holding the labels
     * @param prefix   Put in front of every key; use {@code ""} for none
     * @param delegate Where a missing key, and every label this resolver does not handle, comes from
     */
    public ResourceBundleFilterLabelResolver(ResourceBundle bundle, String prefix, FilterLabelResolver delegate) {
        super(delegate);
        this.bundle = Objects.requireNonNull(bundle, "bundle");
        this.prefix = Objects.requireNonNull(prefix, "prefix");
    }

    /**
     * The bundle key for a field, without the prefix.
     *
     * @param fieldPath The path as written in the filter
     * @return Its key
     */
    public static String fieldKey(String fieldPath) {
        return "field." + fieldPath;
    }

    /**
     * The bundle key for a condition's operator, without the prefix.
     * <p>
     * Depends on the condition rather than the operator alone because the four LIKE forms are keyed by their
     * {@link PatternShape} too.
     *
     * @param condition The condition
     * @return Its key
     */
    public static String operatorKey(FilterCondition condition) {
        FilterOperator operator = condition.operator();
        return switch (operator) {
            case LIKE, NLIKE, CLIKE, CNLIKE -> "operator." + operator.name() + "." + condition.patternShape().name();
            default -> "operator." + operator.name();
        };
    }

    /**
     * The bundle key for a junction, without the prefix.
     *
     * @param junction AND or OR
     * @return Its key
     */
    public static String junctionKey(FilterGroup.Junction junction) {
        return "junction." + junction.name();
    }

    @Override
    public String operandLabel(Operand operand) {
        if (operand instanceof Operand.FieldOperand field) {
            String label = lookUp(fieldKey(field.fieldPath()));
            if (label != null) {
                return label;
            }
        }
        return super.operandLabel(operand);
    }

    @Override
    public String operatorLabel(FilterCondition condition) {
        String label = lookUp(operatorKey(condition));
        return label != null ? label : super.operatorLabel(condition);
    }

    @Override
    public String junctionLabel(FilterGroup.Junction junction) {
        String label = lookUp(junctionKey(junction));
        return label != null ? label : super.junctionLabel(junction);
    }

    /** {@code null} rather than an exception for a missing key: a half-translated report still has to print. */
    private String lookUp(String key) {
        String full = prefix + key;
        return bundle.containsKey(full) ? bundle.getString(full) : null;
    }
}

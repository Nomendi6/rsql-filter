package rsql.describe;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Field labels from a map, everything else from the delegate.
 * <p>
 * The smallest thing that makes a description readable: a report usually needs to turn
 * {@code productType.name} into "Product type" and can live with English operators.
 * <pre>{@code
 * FilterLabelResolver labels = new MapFilterLabelResolver(Map.of(
 *     "productType.name", "Product type",
 *     "price",            "Price"));
 *
 * new RsqlFilterDescription().describe("productType.name=='A';price=gt=10", labels).getText();
 * // Product type is "A" and Price is greater than 10
 * }</pre>
 * A path that is not in the map keeps its technical form rather than throwing - a report that is missing one
 * label should still print. Pass an empty map to get exactly the delegate's behaviour.
 * <p>
 * The map is copied, so later changes to the caller's map do not affect this resolver.
 */
public class MapFilterLabelResolver extends DelegatingFilterLabelResolver {

    private final Map<String, String> fieldLabels;

    /**
     * @param fieldLabels Field path to label, e.g. {@code "productType.name"} to {@code "Product type"}
     */
    public MapFilterLabelResolver(Map<String, String> fieldLabels) {
        this(fieldLabels, TECHNICAL);
    }

    /**
     * @param fieldLabels Field path to label
     * @param delegate    Where the operator, value and junction labels come from
     */
    public MapFilterLabelResolver(Map<String, String> fieldLabels, FilterLabelResolver delegate) {
        super(delegate);
        this.fieldLabels = Map.copyOf(new HashMap<>(Objects.requireNonNull(fieldLabels, "fieldLabels")));
    }

    @Override
    public String operandLabel(Operand operand) {
        if (operand instanceof Operand.FieldOperand field) {
            String label = fieldLabels.get(field.fieldPath());
            if (label != null) {
                return label;
            }
        }
        return super.operandLabel(operand);
    }
}

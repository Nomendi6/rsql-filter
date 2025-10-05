package rsql.helper;

import jakarta.persistence.Tuple;
import jakarta.persistence.TupleElement;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Utility class for converting JPA Tuple results to JSON-friendly formats.
 * Useful for REST controllers that need to return SELECT query results.
 *
 * <p>Example usage in a REST controller:</p>
 * <pre>
 * &#64;GetMapping("/products/summary")
 * public ResponseEntity&lt;List&lt;Map&lt;String, Object&gt;&gt;&gt; getProductSummary(
 *     &#64;RequestParam(required = false) String filter) {
 *
 *     List&lt;Tuple&gt; tuples = queryService.getTupleWithSelect(
 *         "code:id, name, price, productType.name:category",
 *         filter,
 *         PageRequest.of(0, 100)
 *     );
 *
 *     // Convert to JSON-friendly format
 *     List&lt;Map&lt;String, Object&gt;&gt; results = TupleConverter.toMapList(tuples);
 *
 *     return ResponseEntity.ok(results);
 * }
 * </pre>
 */
public class TupleConverter {

    private TupleConverter() {
        // Utility class, prevent instantiation
    }

    /**
     * Converts a list of JPA Tuples to a list of JSON-friendly maps.
     *
     * <p>Each tuple is converted to a map where keys are column aliases
     * (from the SELECT string) and values are the corresponding data.
     * The order of fields in the map matches the order in the SELECT string.</p>
     *
     * @param tuples the list of tuples to convert
     * @return a list of maps where keys are column aliases and values are the data
     */
    public static List<Map<String, Object>> toMapList(List<Tuple> tuples) {
        return tuples.stream()
            .map(TupleConverter::toMap)
            .collect(Collectors.toList());
    }

    /**
     * Converts a single JPA Tuple to a JSON-friendly map.
     *
     * <p>The tuple is converted to a LinkedHashMap to preserve the field order
     * from the SELECT string. Keys are column aliases and values are the data.</p>
     *
     * <p>Example:</p>
     * <pre>
     * // SELECT string: "code:productCode, name, price"
     * // Result map: {"productCode": "P001", "name": "Laptop", "price": 1000.00}
     * </pre>
     *
     * @param tuple the tuple to convert
     * @return a map where keys are column aliases and values are the data
     */
    public static Map<String, Object> toMap(Tuple tuple) {
        Map<String, Object> row = new LinkedHashMap<>();
        for (TupleElement<?> element : tuple.getElements()) {
            String alias = element.getAlias();
            Object value = tuple.get(alias);
            row.put(alias, value);
        }
        return row;
    }
}

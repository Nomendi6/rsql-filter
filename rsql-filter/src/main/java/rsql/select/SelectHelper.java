package rsql.select;

/**
 * Utility class providing helper methods for SELECT query processing.
 */
public class SelectHelper {

    private SelectHelper() {
        // Utility class, prevent instantiation
    }

    /**
     * Extracts the last field name from a field path.
     * Used to generate default aliases when no explicit alias is provided.
     *
     * @param fieldPath the field path (e.g., "productType.name" or "code")
     * @return the last field name (e.g., "name" or "code")
     *
     * @example
     * <pre>
     * getLastFieldName("productType.name")  → "name"
     * getLastFieldName("code")              → "code"
     * getLastFieldName("")                  → ""
     * getLastFieldName(null)                → null
     * </pre>
     */
    public static String getLastFieldName(String fieldPath) {
        if (fieldPath == null || fieldPath.isEmpty()) {
            return fieldPath;
        }
        int lastDotIndex = fieldPath.lastIndexOf('.');
        return lastDotIndex >= 0 ? fieldPath.substring(lastDotIndex + 1) : fieldPath;
    }
}

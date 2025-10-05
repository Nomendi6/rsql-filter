package rsql.select;

import java.util.Objects;

/**
 * Represents a field selection in a SELECT clause for non-aggregate queries.
 * Contains the field path and optional alias.
 */
public class SelectField {
    private final String fieldPath;
    private final String alias;

    /**
     * Creates a new SelectField with the given field path and alias.
     *
     * @param fieldPath The field path (e.g., "field1", "productType.name")
     * @param alias The optional alias (can be null)
     */
    public SelectField(String fieldPath, String alias) {
        this.fieldPath = fieldPath;
        this.alias = alias;
    }

    /**
     * Gets the field path.
     *
     * @return The field path
     */
    public String getFieldPath() {
        return fieldPath;
    }

    /**
     * Gets the alias.
     *
     * @return The alias, or null if no alias is set
     */
    public String getAlias() {
        return alias;
    }

    /**
     * Checks if this field has an alias.
     *
     * @return true if alias is not null and not empty, false otherwise
     */
    public boolean hasAlias() {
        return alias != null && !alias.isEmpty();
    }

    /**
     * Gets the alias if present, otherwise returns the field path.
     * Useful for compatibility with APIs that expect a simple string identifier.
     *
     * @return The alias if present, otherwise the field path
     */
    public String getFieldPathOrAlias() {
        return hasAlias() ? alias : fieldPath;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SelectField that = (SelectField) o;
        return Objects.equals(fieldPath, that.fieldPath) &&
               Objects.equals(alias, that.alias);
    }

    @Override
    public int hashCode() {
        return Objects.hash(fieldPath, alias);
    }

    @Override
    public String toString() {
        return "SelectField{" +
               "fieldPath='" + fieldPath + '\'' +
               ", alias='" + alias + '\'' +
               '}';
    }
}

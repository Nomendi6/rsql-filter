package rsql.describe;

/**
 * One element of an {@code IN} list or one bound of a {@code BETWEEN} range.
 * <p>
 * Mirrors the single-valued shapes, because the grammar allows the same things here: {@code inListElement} is
 * not only a literal but also a {@code PARAM_LITERAL} and a {@code field}, so {@code code=in=(status,name)}
 * and {@code price=bt=(minPrice,:upper)} are legal filters. A plain {@code List<Object>} could not tell a
 * field reference from a string that happens to look like one.
 * <p>
 * Sealed: the grammar closes this set at eight alternatives, all of which map onto these three.
 */
public sealed interface ListItem {

    /** A literal: {@code 'a'}, {@code 1}, {@code 2.5}, {@code #ACTIVE#}, a date. */
    record ItemValue(Object value) implements ListItem {
        public ItemValue {
            Invariants.requireAllowedType(value);
        }
    }

    /** Another field, as in {@code code=in=(status,name)}. */
    record ItemField(String fieldPath) implements ListItem {
        public ItemField {
            Invariants.requireNonBlank(fieldPath, "fieldPath");
        }
    }

    /** A named parameter, as in {@code code=in=(:p1,:p2)}. */
    record ItemParam(String name) implements ListItem {
        public ItemParam {
            Invariants.requireNonBlank(name, "name");
        }
    }
}

package rsql.describe;

/**
 * What a LIKE pattern means, derived from where the {@code *} wildcards sit.
 * <p>
 * {@code NONE} for every operator outside the LIKE family, and for a LIKE whose right-hand side is not a
 * string literal - a field reference or a parameter carries no pattern that could be read at parse time.
 * <p>
 * The derivation deliberately gives up ({@code CUSTOM}) whenever it cannot describe the pattern truthfully:
 * {@code %} and {@code _} are not escaped by this library and stay SQL wildcards, so {@code '50%'} is not an
 * exact match, and a {@code *} in the middle ({@code 'A*B'}) is neither "starts with" nor "contains".
 */
public enum PatternShape {
    NONE,
    EXACT,
    STARTS_WITH,
    ENDS_WITH,
    CONTAINS,
    CUSTOM;

    /**
     * Reads a raw LIKE pattern.
     * <p>
     * The single place this derivation lives, so that the visitor that builds a tree and the constructor that
     * validates a hand-built one cannot disagree.
     *
     * @param raw The value as written in the filter
     * @return What the pattern means
     */
    public static PatternShape of(String raw) {
        if (raw.indexOf('%') >= 0 || raw.indexOf('_') >= 0) {
            return CUSTOM;                       // these stay SQL wildcards, so nothing can be claimed
        }
        long stars = raw.chars().filter(c -> c == '*').count();
        if (stars == 0) {
            return EXACT;
        }
        boolean leading = raw.startsWith("*");
        boolean trailing = raw.endsWith("*");
        if (stars == 1 && trailing && raw.length() > 1) {
            return STARTS_WITH;
        }
        if (stars == 1 && leading && raw.length() > 1) {
            return ENDS_WITH;
        }
        if (stars == 2 && leading && trailing && raw.length() > 2) {
            return CONTAINS;
        }
        return CUSTOM;                           // a star in the middle, or an empty needle
    }

    /**
     * The pattern without its surrounding wildcards.
     *
     * @param raw The value as written
     * @return The needle, or {@code null} when the shape has none
     */
    public String needleOf(String raw) {
        return switch (this) {
            case EXACT -> raw;
            case STARTS_WITH -> raw.substring(0, raw.length() - 1);
            case ENDS_WITH -> raw.substring(1);
            case CONTAINS -> raw.substring(1, raw.length() - 1);
            case CUSTOM, NONE -> null;
        };
    }
}

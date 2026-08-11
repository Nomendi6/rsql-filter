package rsql.describe;

/**
 * A node of the tree that describes a WHERE filter: either a group of conditions joined by one logical
 * operator, or a single condition.
 * <p>
 * The tree is the faithful record of the filter. Everything else - the flat rows, the one-line text - is
 * derived from it and may lose structure; see {@link FilterDescription}.
 * <p>
 * Sealed because the WHERE grammar has no third shape: {@code condition} is either a {@code singleCondition}
 * or one of the two logical combinations, and there is no unary {@code NOT}.
 */
public sealed interface FilterNode permits FilterGroup, FilterCondition {
}

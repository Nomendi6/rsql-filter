package rsql.describe;

import java.util.List;
import java.util.Objects;

/**
 * Conditions joined by one logical operator.
 * <p>
 * Normalised: a group always has at least two children, and no child is a group with the same junction.
 * {@code a;b;c;d} is therefore one AND group with four children rather than three nested binary nodes, and a
 * group node exists only where the grouping actually changes the meaning. Several later rules depend on this -
 * junctions alternate strictly by depth, so every non-root group needs parentheses when rendered.
 *
 * @param junction How the children are joined
 * @param children At least two nodes; the list is copied, so the caller cannot change the group afterwards
 */
public record FilterGroup(Junction junction, List<FilterNode> children) implements FilterNode {

    public enum Junction { AND, OR }

    public FilterGroup {
        Objects.requireNonNull(junction, "junction");
        Objects.requireNonNull(children, "children");
        children = List.copyOf(children);
        if (children.size() < 2) {
            throw new IllegalArgumentException("a normalised group has at least two children");
        }
        for (FilterNode child : children) {
            if (child instanceof FilterGroup group && group.junction() == junction) {
                throw new IllegalArgumentException(
                    "a child must not be a group with the same junction - flatten it instead"
                );
            }
        }
    }
}

package rsql.describe;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * A filter rendered for a report: the tree it came from, a flat list of rows, and a one-line summary.
 * <p>
 * {@link #getRoot()} is the faithful record. {@link #getRows()} and {@link #getText()} are derived and pass
 * every part through the {@link FilterLabelResolver}, so an application that masks sensitive values must use
 * those two - {@code getRoot()} exposes the raw values and bypasses masking entirely.
 */
public class FilterDescription {

    private final FilterNode root;
    private final List<FilterRow> rows;
    private final String text;
    private final boolean pureAndChain;

    private FilterDescription(FilterNode root, List<FilterRow> rows, String text, boolean pureAndChain) {
        this.root = root;
        this.rows = rows;
        this.text = text;
        this.pureAndChain = pureAndChain;
    }

    static FilterDescription of(FilterNode root, FilterLabelResolver labels, Map<String, Object> parameters) {
        Objects.requireNonNull(labels, "labels");
        if (root == null) {
            return new FilterDescription(null, List.of(), "", true);
        }
        // every condition is rendered exactly once, and both the rows and the text are built from those
        // same pieces - otherwise a resolver that is not perfectly pure could make the two disagree
        Renderer renderer = new Renderer(labels, parameters);
        renderer.renderConditions(root);
        return new FilterDescription(root, renderer.rows(root), renderer.text(root), !containsOr(root));
    }

    /** The tree. The only faithful form - and the only one that bypasses the resolver, so also the raw one. */
    public FilterNode getRoot() {
        return root;
    }

    /** The rows, ready for {@code JRBeanCollectionDataSource}. Empty for an empty filter. */
    public List<FilterRow> getRows() {
        return rows;
    }

    /** The whole filter on one line. Empty for an empty filter. */
    public String getText() {
        return text;
    }

    /**
     * The same, truncated with an ellipsis. A filter of several hundred conditions produces a line that
     * neither a spreadsheet cell nor a page header can show.
     *
     * @param maxLength The most characters to return, ellipsis included
     * @return The text, shortened if it was longer
     */
    public String getText(int maxLength) {
        if (maxLength <= 0) {
            throw new IllegalArgumentException("maxLength must be positive");
        }
        return text.length() <= maxLength ? text : text.substring(0, Math.max(0, maxLength - 1)) + "…";
    }

    /**
     * Whether the filter contains no OR at all.
     * <p>
     * When it does not, every row sits at depth 0 with no parentheses, and a plain table is an exact
     * rendering. That is the common case for a filter built from a UI form.
     */
    public boolean isPureAndChain() {
        return pureAndChain;
    }

    /** Whether the filter was empty, in which case the rows and the text are empty too. */
    public boolean isEmpty() {
        return root == null;
    }

    private static boolean containsOr(FilterNode node) {
        if (node instanceof FilterGroup group) {
            if (group.junction() == FilterGroup.Junction.OR) {
                return true;
            }
            for (FilterNode child : group.children()) {
                if (containsOr(child)) {
                    return true;
                }
            }
        }
        return false;
    }

    /** Walks the tree once for the rows and once for the text, applying the resolver as it goes. */
    private static final class Renderer {

        private final FilterLabelResolver labels;
        private final Map<String, Object> parameters;

        /** One condition's three labels, produced by a single pass over the resolver. */
        private record Rendered(String field, String operator, String value) {
        }

        private final java.util.IdentityHashMap<FilterCondition, Rendered> rendered =
            new java.util.IdentityHashMap<>();

        Renderer(FilterLabelResolver labels, Map<String, Object> parameters) {
            this.labels = labels;
            this.parameters = parameters == null ? Collections.emptyMap() : parameters;
        }

        /** Walks the tree once and asks the resolver once per condition. */
        void renderConditions(FilterNode node) {
            if (node instanceof FilterCondition condition) {
                rendered.put(condition, new Rendered(
                    labels.operandLabel(condition.left()),
                    labels.operatorLabel(condition),
                    condition.rightSide() instanceof RightSide.NoValue ? "" : valueOf(condition)
                ));
                return;
            }
            for (FilterNode child : ((FilterGroup) node).children()) {
                renderConditions(child);
            }
        }

        private Rendered of(FilterCondition condition) {
            return rendered.get(condition);
        }

        List<FilterRow> rows(FilterNode root) {
            List<FilterRow> rows = new ArrayList<>();
            if (root instanceof FilterGroup group) {
                // the root group is not printed with parentheses, so it does not add a level either
                collectChildren(group, 0, null, rows);
            } else {
                rows.add(rowOf((FilterCondition) root, 0, null, 0));
            }
            return List.copyOf(rows);
        }

        /**
         * The connector of a row is the junction of the lowest common ancestor of that row and the previous
         * one. That falls out of handing the group's junction to every child but the first, which instead
         * inherits whatever came from above.
         */
        private void collectChildren(FilterGroup group, int depth, FilterGroup.Junction inherited,
                                     List<FilterRow> rows) {
            List<FilterNode> children = group.children();
            for (int i = 0; i < children.size(); i++) {
                FilterNode child = children.get(i);
                FilterGroup.Junction connector = i == 0 ? inherited : group.junction();
                if (child instanceof FilterCondition condition) {
                    rows.add(rowOf(condition, depth, connector, 0));
                } else {
                    int before = rows.size();
                    collectChildren((FilterGroup) child, depth + 1, connector, rows);
                    openFirst(rows, before);
                    closeLast(rows);
                }
            }
        }

        private FilterRow rowOf(FilterCondition condition, int depth, FilterGroup.Junction connector,
                                int opens) {
            Rendered parts = of(condition);
            return new FilterRow(
                parts.field(), parts.operator(), parts.value(),
                connector == null ? null : labels.junctionLabel(connector),
                depth, opens, 0
            );
        }

        private void openFirst(List<FilterRow> rows, int index) {
            FilterRow row = rows.get(index);
            rows.set(index, copyWith(row, row.getOpenGroups() + 1, row.getCloseGroups()));
        }

        private void closeLast(List<FilterRow> rows) {
            int index = rows.size() - 1;
            FilterRow row = rows.get(index);
            rows.set(index, copyWith(row, row.getOpenGroups(), row.getCloseGroups() + 1));
        }

        private FilterRow copyWith(FilterRow row, int opens, int closes) {
            return new FilterRow(row.getField(), row.getOperator(), row.getValue(), row.getConnector(),
                row.getDepth(), opens, closes);
        }

        String text(FilterNode node) {
            return render(node, null);
        }

        /** Parentheses go round a group whose junction differs from its parent's - which, after normalisation,
         *  means every group except the root. */
        private String render(FilterNode node, FilterGroup.Junction parentJunction) {
            if (node instanceof FilterCondition condition) {
                Rendered parts = of(condition);
                String head = parts.field() + " " + parts.operator();
                // whether there is a right-hand side is a property of the type, not of the rendered text:
                // name=='' has an empty value but is still a comparison, and must not read like a==null
                return condition.rightSide() instanceof RightSide.NoValue
                    ? head
                    : head + " " + parts.value();
            }
            FilterGroup group = (FilterGroup) node;
            List<String> parts = new ArrayList<>(group.children().size());
            for (FilterNode child : group.children()) {
                parts.add(render(child, group.junction()));
            }
            String joined = String.join(" " + labels.junctionLabel(group.junction()) + " ", parts);
            return parentJunction == null ? joined : "(" + joined + ")";
        }

        private String valueOf(FilterCondition condition) {
            Operand left = condition.left();
            RightSide rightSide = condition.rightSide();
            if (rightSide instanceof RightSide.NoValue) {
                return "";
            }
            if (rightSide instanceof RightSide.SingleValue single) {
                return condition.patternShape() == PatternShape.NONE
                    ? labels.valueLabel(left, single.value())
                    : labels.patternValueLabel(left, single.value(), condition.patternShape(),
                        condition.patternNeedle());
            }
            if (rightSide instanceof RightSide.FieldRef ref) {
                return labels.operandLabel(new Operand.FieldOperand(ref.fieldPath()));
            }
            if (rightSide instanceof RightSide.Parameter parameter) {
                return labels.parameterLabel(left, parameter.name(), resolve(parameter.name()));
            }
            if (rightSide instanceof RightSide.ValueList list) {
                List<String> rendered = new ArrayList<>(list.values().size());
                for (ListItem item : list.values()) {
                    rendered.add(itemOf(left, item));
                }
                return labels.joinList(rendered);
            }
            if (rightSide instanceof RightSide.Range range) {
                return labels.joinRange(itemOf(left, range.from()), itemOf(left, range.to()));
            }
            // an unknown shape still goes through the resolver, so masking is not bypassed
            return labels.rightSideLabel(left, rightSide);
        }

        private String itemOf(Operand left, ListItem item) {
            if (item instanceof ListItem.ItemValue value) {
                return labels.valueLabel(left, value.value());
            }
            if (item instanceof ListItem.ItemField field) {
                return labels.operandLabel(new Operand.FieldOperand(field.fieldPath()));
            }
            ListItem.ItemParam parameter = (ListItem.ItemParam) item;
            return labels.parameterLabel(left, parameter.name(), resolve(parameter.name()));
        }

        private FilterLabelResolver.ParameterResolution resolve(String name) {
            return parameters.containsKey(name)
                ? FilterLabelResolver.ParameterResolution.of(parameters.get(name))
                : FilterLabelResolver.ParameterResolution.unresolved();
        }
    }
}

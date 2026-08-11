package rsql.describe;

/**
 * One row of the flat, report-facing form of a description.
 * <p>
 * A plain JavaBean with {@code getX()} methods rather than a record, and deliberately so: Jasper's
 * {@code JRBeanCollectionDataSource} reads properties through {@code java.beans.Introspector}, which does not
 * recognise a record's accessors - {@code Introspector.getBeanInfo(SomeRecord.class, Object.class)} reports no
 * properties at all. This is the one place in the package that departs from the record style, because this
 * class exists for Jasper.
 * <p>
 * The rows are a <em>display</em> form. {@code depth} together with {@code connector} does not determine the
 * tree - two different filters can produce identical rows - so the parenthesis counts are there to make the
 * printed output unambiguous. No guarantee is published that the tree can be reconstructed from the rows; for
 * that, use {@link FilterDescription#getRoot()}.
 */
public class FilterRow {

    private final String field;
    private final String operator;
    private final String value;
    private final String connector;
    private final int depth;
    private final int openGroups;
    private final int closeGroups;

    FilterRow(String field, String operator, String value, String connector,
              int depth, int openGroups, int closeGroups) {
        this.field = field;
        this.operator = operator;
        this.value = value;
        this.connector = connector;
        this.depth = depth;
        this.openGroups = openGroups;
        this.closeGroups = closeGroups;
    }

    /** The label of the left-hand side. */
    public String getField() {
        return field;
    }

    /** The label of the operator. */
    public String getOperator() {
        return operator;
    }

    /** The label of the right-hand side; empty when the operator carries the whole meaning. */
    public String getValue() {
        return value;
    }

    /**
     * How this row joins the previous one, localised; {@code null} for the first row.
     * <p>
     * Precisely: the junction of the lowest common ancestor of this row and the previous one. For
     * {@code a;(b,c)} the row for {@code b} therefore carries "and" - the outer group - and only {@code c}
     * carries "or". A report that reads this as "the operator inside my group" will print the wrong thing.
     */
    public String getConnector() {
        return connector;
    }

    /** Nesting level, 0 at the top. Useful for indentation. */
    public int getDepth() {
        return depth;
    }

    /** How many groups open on this row; print as that many {@code (}. */
    public int getOpenGroups() {
        return openGroups;
    }

    /** How many groups close on this row; print as that many {@code )}. */
    public int getCloseGroups() {
        return closeGroups;
    }

    @Override
    public String toString() {
        return "FilterRow[" + connector + " " + "(".repeat(openGroups) + field + " " + operator + " " + value
            + ")".repeat(closeGroups) + ", depth=" + depth + "]";
    }
}

package rsql.describe;

import org.antlr.v4.runtime.CharStreams;
import rsql.where.RsqlWhereTreeParser;
import rsql.where.WhereDescriptionVisitor;

import java.util.Map;
import java.util.Objects;

/**
 * Describes a WHERE filter for a report.
 * <p>
 * An instance class holding its own tree parser, in the style of {@code RsqlWhereString}. Purely textual - no
 * {@code EntityManager} and no JPA context are needed, because the description is built from the parse tree
 * alone.
 * <p>
 * Deep or malformed input is rejected by the shared tree parser before this class sees it, with the same
 * limits every other entry point uses, so a filter the query path accepts can always be described.
 *
 * <pre>{@code
 * RsqlFilterDescription describer = new RsqlFilterDescription();
 * FilterDescription description = describer.describe("name=*'A*';status==#ACTIVE#", labels);
 * jasperParameters.put("filterRows", new JRBeanCollectionDataSource(description.getRows()));
 * jasperParameters.put("filterText", description.getText(200));
 * }</pre>
 */
public class RsqlFilterDescription {

    private final RsqlWhereTreeParser treeParser = new RsqlWhereTreeParser();

    /**
     * Parses a filter into a tree.
     *
     * @param filter The RSQL filter; blank or {@code null} gives {@code null}
     * @return The tree, or {@code null} for an empty filter
     * @throws rsql.exceptions.SyntaxErrorException if the filter does not parse
     */
    public FilterNode parse(String filter) {
        if (filter == null || filter.isBlank()) {
            return null;
        }
        return new WhereDescriptionVisitor().visit(treeParser.parseStream(CharStreams.fromString(filter)));
    }

    /**
     * Describes an already-built tree.
     * <p>
     * Named apart from the {@code String} overloads on purpose: both accept {@code null} as an empty filter,
     * and with one name {@code describe(null, labels)} would not compile - the compiler cannot choose between
     * {@code String} and {@code FilterNode}.
     *
     * @param root   The tree; {@code null} means an empty filter
     * @param labels Where readable names come from; must not be {@code null}
     * @return The description
     */
    public FilterDescription describeNode(FilterNode root, FilterLabelResolver labels) {
        return describeNode(root, labels, null);
    }

    /**
     * The same, with values for the named parameters.
     * <p>
     * Without the map a {@code :name} parameter is shown as {@code :name}. The caller has the values anyway -
     * they have to be bound on the query, since only generated parameters are filled in automatically.
     *
     * @param root       The tree; {@code null} means an empty filter
     * @param labels     Where readable names come from; must not be {@code null}
     * @param parameters Values by parameter name; a key present with a {@code null} value counts as supplied
     * @return The description
     */
    public FilterDescription describeNode(FilterNode root, FilterLabelResolver labels,
                                          Map<String, Object> parameters) {
        Objects.requireNonNull(labels, "labels");
        return FilterDescription.of(root, labels, parameters);
    }

    /** Parses and describes, using technical field paths. */
    public FilterDescription describe(String filter) {
        return describe(filter, FilterLabelResolver.TECHNICAL);
    }

    /** Parses and describes. */
    public FilterDescription describe(String filter, FilterLabelResolver labels) {
        return describeNode(parse(filter), labels, null);
    }

    /** Parses and describes, with values for the named parameters. */
    public FilterDescription describe(String filter, FilterLabelResolver labels,
                                      Map<String, Object> parameters) {
        return describeNode(parse(filter), labels, parameters);
    }
}

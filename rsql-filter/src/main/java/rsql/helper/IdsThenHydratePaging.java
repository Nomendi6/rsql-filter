package rsql.helper;

import org.springframework.data.domain.Sort;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.regex.Pattern;

/**
 * The string work behind {@link rsql.PagingStrategy#IDS_THEN_HYDRATE}.
 *
 * <p>Everything here is a pure function of its arguments, so that the shape of the two statements can be
 * tested without a database. {@link SimpleQueryExecutor#getJpqlQueryResultAsPageIdsThenHydrate} does the
 * execution.</p>
 */
final class IdsThenHydratePaging {

    /** The parameter the hydration statement binds the page's identifiers to. */
    static final String IDS_PARAMETER = "rsqlPageIds";

    private static final Pattern JOIN_FETCH = Pattern.compile("(?i)\\bjoin\\s+fetch\\b");

    private IdsThenHydratePaging() {}

    /**
     * The {@code from} clause of a JPQL select, as the first statement needs it.
     *
     * <p>The clause is taken whole, joins included, rather than rebuilt as a bare {@code from Entity alias}.
     * That is not a convenience. Hibernate reuses an explicit {@code left join a0.x} for the implicit path
     * {@code a0.x.y} that the filter and the sort are written with; without the explicit join the same path
     * is an implicit - and therefore inner - join, and {@code a0.x.y is null} stops matching the rows that
     * have no {@code x} at all. Reusing the caller's clause keeps the filter meaning exactly what it means in
     * the single-statement form.</p>
     *
     * <p>{@code join fetch} is demoted to {@code join}: a fetch is only legal when the fetched association's
     * owner is in the select list, which an identifier-only select is not, and fetching changes nothing about
     * which rows match.</p>
     *
     * @param jpql The caller's select statement.
     * @return The clause from the {@code from} keyword to the end of the statement, or null when no
     *         top-level {@code from} can be found - in which case the caller falls back to one statement,
     *         which will report whatever is wrong with the JPQL far better than this could.
     */
    static String fromClause(String jpql) {
        int at = topLevelFromIndex(jpql);
        if (at < 0) return null;
        return JOIN_FETCH.matcher(jpql.substring(at)).replaceAll("join");
    }

    /**
     * Index of the {@code from} keyword that belongs to the outer query.
     *
     * <p>A {@code select new X(...)} puts its arguments in parentheses, and an argument may itself be a
     * subquery with a {@code from} of its own, so the first occurrence of the word is not necessarily the
     * right one. The scan skips everything inside parentheses and inside string literals and takes the first
     * whole-word {@code from} at depth zero.</p>
     */
    static int topLevelFromIndex(String jpql) {
        int depth = 0;
        boolean quoted = false;
        int length = jpql.length();
        for (int i = 0; i < length; i++) {
            char c = jpql.charAt(i);
            if (quoted) {
                if (c == '\'') quoted = false;
                continue;
            }
            switch (c) {
                case '\'' -> quoted = true;
                case '(' -> depth++;
                case ')' -> depth--;
                default -> {
                    if (depth == 0 && Character.isWhitespace(c) && isWord(jpql, i + 1, "from")) {
                        return i + 1;
                    }
                }
            }
        }
        return -1;
    }

    private static boolean isWord(String text, int at, String word) {
        int end = at + word.length();
        if (end > text.length()) return false;
        if (!text.regionMatches(true, at, word, 0, word.length())) return false;
        return end == text.length() || Character.isWhitespace(text.charAt(end));
    }

    /**
     * The first statement: the identifiers of one page, in page order.
     *
     * <pre>{@code
     * select distinct a0.id, a0.name from Order a0 left join a0.customer where ... order by a0.name DESC, a0.id ASC
     * }</pre>
     *
     * <p>Three things about its shape are deliberate.</p>
     * <ul>
     *   <li>{@code distinct}, because the caller's {@code from} may join a collection, and then one entity is
     *       several rows. Without it the offset and the limit would count rows rather than entities, and a
     *       page would come back short.</li>
     *   <li>The sort columns are in the select list. With {@code distinct}, every database that follows the
     *       standard - DB2, PostgreSQL, Oracle, H2 - refuses to order by an expression that is not selected.
     *       Selecting the identifier alone and ordering by something else is the form that fails.</li>
     *   <li>The identifier is the last sort key. Two rows with equal sort values otherwise have no defined
     *       order, and the page boundary between them would move from one execution to the next. The single
     *       statement has always had that flaw; this statement does not.</li>
     * </ul>
     *
     * @param fromClause Output of {@link #fromClause}.
     * @param alias      The root alias, which the filter and the sort are written against.
     * @param idName     The entity's identifier attribute.
     * @param sort       The requested sort; may be null or unsorted.
     * @param where      The rendered filter, or null when there is none.
     * @return The statement, ready for {@code setFirstResult} / {@code setMaxResults}.
     */
    static String idPageQuery(String fromClause, String alias, String idName, Sort sort, String where) {
        String idPath = alias + "." + idName;
        List<String> selected = new ArrayList<>();
        selected.add(idPath);

        StringBuilder orderBy = new StringBuilder();
        boolean sortedById = false;
        if (sort != null && sort.isSorted()) {
            for (Sort.Order order : sort) {
                String path = alias + "." + order.getProperty();
                if (path.equals(idPath)) {
                    sortedById = true;
                } else if (!selected.contains(path)) {
                    selected.add(path);
                }
                orderBy.append(path).append(' ').append(order.getDirection().name()).append(", ");
            }
        }
        if (sortedById) {
            orderBy.setLength(orderBy.length() - 2);
        } else {
            orderBy.append(idPath).append(" ASC");
        }

        StringBuilder jpql = new StringBuilder("select distinct ")
                .append(String.join(", ", selected))
                .append(' ')
                .append(fromClause);
        if (where != null) {
            jpql.append(" where ").append(where);
        }
        return jpql.append(" order by ").append(orderBy).toString();
    }

    /**
     * The second statement: the caller's own select, restricted to the page's identifiers.
     *
     * <p>No sort and no limit - the identifiers already are the page, and the rows are put back in order by
     * {@link #inIdOrder} afterwards.</p>
     */
    static String hydrationQuery(String jpql, String alias, String idName) {
        return jpql + " where " + alias + "." + idName + " in (:" + IDS_PARAMETER + ")";
    }

    /**
     * The identifier out of a row of the first statement.
     *
     * <p>Hibernate returns a scalar when one thing is selected and an {@code Object[]} when several are; the
     * identifier is always first.</p>
     */
    static Object idOfRow(Object row) {
        return row instanceof Object[] columns ? columns[0] : row;
    }

    /**
     * Put hydrated rows in the order of the identifiers, one row per identifier.
     *
     * <p>A row whose identifier is not in the list is dropped - it cannot happen unless the data changed
     * between the two statements. An identifier with no row is skipped for the same reason. Where the
     * hydration joins a collection, one identifier may come back as several rows; the first is kept.</p>
     */
    static <R> List<R> inIdOrder(List<Object> ids, Collection<R> rows, Function<R, ?> idOf) {
        Map<Object, R> byId = new HashMap<>();
        for (R row : rows) {
            byId.putIfAbsent(idOf.apply(row), row);
        }
        List<R> ordered = new ArrayList<>(ids.size());
        for (Object id : ids) {
            R row = byId.get(id);
            if (row != null) ordered.add(row);
        }
        return ordered;
    }

    /** Split a list into consecutive pieces of at most {@code size} elements. */
    static <T> List<List<T>> chunks(List<T> list, int size) {
        List<List<T>> pieces = new ArrayList<>();
        for (int from = 0; from < list.size(); from += size) {
            pieces.add(list.subList(from, Math.min(from + size, list.size())));
        }
        return pieces;
    }
}

package rsql.helper;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.function.Function;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Sort;

/** The shape of the two statements, checked without a database. */
class IdsThenHydratePagingTest {

    private static final String SELECT_NEW =
        "select new Order(a0.id, a0.number, a0.customer.name) from Order a0 left join a0.customer left join a0.lines l";

    @Test
    @DisplayName("the from clause is taken whole, joins included")
    void fromClauseKeepsTheJoins() {
        assertThat(IdsThenHydratePaging.fromClause(SELECT_NEW))
            .isEqualTo("from Order a0 left join a0.customer left join a0.lines l");
    }

    @Test
    @DisplayName("a from inside the constructor arguments or a string literal is not the one")
    void fromInsideParenthesesOrQuotesIsSkipped() {
        String withSubquery = "select new X(a0.id, (select count(c) from Comment c where c.post = a0)) from Post a0";
        assertThat(IdsThenHydratePaging.fromClause(withSubquery)).isEqualTo("from Post a0");

        String withLiteral = "select new X(a0.id, ' from nowhere ') from Post a0";
        assertThat(IdsThenHydratePaging.fromClause(withLiteral)).isEqualTo("from Post a0");
    }

    @Test
    @DisplayName("the keyword is matched whole and case-insensitively")
    void fromIsMatchedAsAWord() {
        assertThat(IdsThenHydratePaging.fromClause("SELECT a0 FROM Order a0")).isEqualTo("FROM Order a0");
        assertThat(IdsThenHydratePaging.fromClause("select a0.fromDate from Order a0")).isEqualTo("from Order a0");
        assertThat(IdsThenHydratePaging.fromClause("select a0")).isNull();
    }

    @Test
    @DisplayName("join fetch is demoted to join, whatever its spacing or case")
    void joinFetchIsDemoted() {
        assertThat(IdsThenHydratePaging.fromClause("select a0 from Order a0 left join fetch a0.customer LEFT JOIN  FETCH a0.lines"))
            .isEqualTo("from Order a0 left join a0.customer LEFT join a0.lines");
    }

    @Test
    @DisplayName("the id page selects the identifier and the sort columns, and ends the sort with the identifier")
    void idPageSelectsSortColumnsAndBreaksTiesById() {
        String jpql = IdsThenHydratePaging.idPageQuery(
            "from Order a0 left join a0.customer",
            "a0",
            "id",
            Sort.by(Sort.Order.desc("customer.name"), Sort.Order.asc("number")),
            "a0.status = :p0"
        );
        assertThat(jpql).isEqualTo(
            "select distinct a0.id, a0.customer.name, a0.number from Order a0 left join a0.customer"
            + " where a0.status = :p0 order by a0.customer.name DESC, a0.number ASC, a0.id ASC"
        );
    }

    @Test
    @DisplayName("without a sort the page is ordered by the identifier, and without a filter there is no where")
    void unsortedAndUnfiltered() {
        assertThat(IdsThenHydratePaging.idPageQuery("from Order a0", "a0", "id", Sort.unsorted(), null))
            .isEqualTo("select distinct a0.id from Order a0 order by a0.id ASC");
        assertThat(IdsThenHydratePaging.idPageQuery("from Order a0", "a0", "id", null, null))
            .isEqualTo("select distinct a0.id from Order a0 order by a0.id ASC");
    }

    @Test
    @DisplayName("a sort that already uses the identifier is not given a second one")
    void sortByIdIsNotDuplicated() {
        assertThat(IdsThenHydratePaging.idPageQuery("from Order a0", "a0", "id", Sort.by(Sort.Order.desc("id")), null))
            .isEqualTo("select distinct a0.id from Order a0 order by a0.id DESC");
        assertThat(IdsThenHydratePaging.idPageQuery("from Order a0", "a0", "id", Sort.by("number", "id"), null))
            .isEqualTo("select distinct a0.id, a0.number from Order a0 order by a0.number ASC, a0.id ASC");
    }

    @Test
    @DisplayName("the identifier attribute need not be called id")
    void identifierNameIsWhateverTheEntityCallsIt() {
        assertThat(IdsThenHydratePaging.idPageQuery("from Thing t", "t", "objectId", Sort.by("name"), null))
            .isEqualTo("select distinct t.objectId, t.name from Thing t order by t.name ASC, t.objectId ASC");
        assertThat(IdsThenHydratePaging.hydrationQuery("select t from Thing t", "t", "objectId"))
            .isEqualTo("select t from Thing t where t.objectId in (:rsqlPageIds)");
    }

    @Test
    @DisplayName("a row is the identifier itself or an array whose first element is")
    void idOfRow() {
        assertThat(IdsThenHydratePaging.idOfRow(7L)).isEqualTo(7L);
        assertThat(IdsThenHydratePaging.idOfRow(new Object[] { 7L, "name" })).isEqualTo(7L);
    }

    @Test
    @DisplayName("rows come back in the order of the identifiers, one per identifier, missing ones skipped")
    void inIdOrder() {
        record Row(long id, String label) {}
        List<Row> hydrated = List.of(new Row(1, "one"), new Row(3, "three"), new Row(3, "three again"), new Row(2, "two"));
        Function<Row, Object> idOf = Row::id;

        assertThat(IdsThenHydratePaging.inIdOrder(List.of(3L, 1L, 2L), hydrated, idOf))
            .extracting(Row::label)
            .containsExactly("three", "one", "two");
        assertThat(IdsThenHydratePaging.inIdOrder(List.of(2L, 99L, 1L), hydrated, idOf))
            .extracting(Row::label)
            .containsExactly("two", "one");
    }

    @Test
    @DisplayName("chunks are consecutive and the last one may be short")
    void chunks() {
        assertThat(IdsThenHydratePaging.chunks(List.of(1, 2, 3, 4, 5), 2)).containsExactly(List.of(1, 2), List.of(3, 4), List.of(5));
        assertThat(IdsThenHydratePaging.chunks(List.of(1, 2), 10)).containsExactly(List.of(1, 2));
        assertThat(IdsThenHydratePaging.chunks(List.of(), 10)).isEmpty();
    }
}

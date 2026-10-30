package com.nomendi6.rsql.it;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.nomendi6.rsql.it.config.IntegrationTest;
import com.nomendi6.rsql.it.domain.uuid.UuidRecord;
import com.nomendi6.rsql.it.repository.UuidRecordRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Tuple;
import jakarta.transaction.Transactional;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import rsql.PagingStrategy;
import rsql.RsqlCompiler;
import rsql.RsqlQueryService;
import rsql.exceptions.SyntaxErrorException;
import rsql.helper.SimpleQueryExecutor;
import rsql.mapper.EntityMapper;
import rsql.where.RsqlContext;
import rsql.where.RsqlQuery;

/**
 * A UUID written as a string - {@code uuid=='f71adfde-…'} - compared with a {@code UUID} attribute, behind an
 * {@code AttributeConverter<UUID, String>} on a VARCHAR column and mapped natively.
 *
 * <p>The string is bound as the {@code UUID} it names on every path: Hibernate refuses a {@code String} for a
 * {@code UUID} parameter of a JPQL query - 6.x for a single value, 7.x for the elements of a list too - and the
 * converter, which writes the stored form, sees a UUID only when it is given one. Both WHERE paths must agree,
 * because a filter string must not mean one thing in one service mode and another thing in the other.</p>
 */
@IntegrationTest
@Transactional
class UuidFilterIT {

    private static final String A = "f71adfde-51bd-5c9d-bbf8-f4a17f73f465";
    private static final String B = "0b6f3c1e-2d4a-4f8b-9c7e-1a2b3c4d5e6f";

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private UuidRecordRepository repository;

    /** A and B carry a UUID in both attributes, N carries none, P carries none and points at A. */
    @BeforeEach
    void seed() {
        entityManager.clear();
        entityManager.createQuery("update UuidRecord r set r.parent = null").executeUpdate();
        entityManager.createQuery("delete from UuidRecord r").executeUpdate();
        UuidRecord a = new UuidRecord(1L, "A", UUID.fromString(A));
        entityManager.persist(a);
        entityManager.persist(new UuidRecord(2L, "B", UUID.fromString(B)));
        entityManager.persist(new UuidRecord(3L, "N", null));
        UuidRecord p = new UuidRecord(4L, "P", null);
        p.setParent(a);
        entityManager.persist(p);
        entityManager.flush();
        entityManager.clear();
    }

    // ------------------------------------------------------------------
    // Helpers
    // ------------------------------------------------------------------

    private List<String> throughSpecification(String filter) {
        RsqlContext<UuidRecord> context = new RsqlContext<>(UuidRecord.class).defineEntityManager(entityManager);
        return repository.findAll(new RsqlCompiler<UuidRecord>().compileToSpecification(filter, context)).stream()
            .map(UuidRecord::getName).sorted().toList();
    }

    private List<String> throughJpql(String filter) {
        RsqlContext<UuidRecord> context = new RsqlContext<>(UuidRecord.class).defineEntityManager(entityManager);
        context.root.alias("a0");
        return SimpleQueryExecutor
            .getJpqlQueryResult(UuidRecord.class, UuidRecord.class, "select a0 from UuidRecord a0", "a0", filter, null, context, new RsqlCompiler<>())
            .stream().map(UuidRecord::getName).sorted().toList();
    }

    /** The rows both paths return; fails when the paths disagree. */
    private List<String> rows(String filter) {
        List<String> specification = throughSpecification(filter);
        entityManager.clear();
        List<String> jpql = throughJpql(filter);
        entityManager.clear();
        assertThat(jpql).as("the JPQL path reads %s like the Specification path", filter).isEqualTo(specification);
        return specification;
    }

    /** Both paths refuse the filter with a SyntaxErrorException whose message contains the given text. */
    private void rejected(String filter, String message) {
        assertThatThrownBy(() -> throughSpecification(filter))
            .as("Specification path, %s", filter)
            .isInstanceOf(SyntaxErrorException.class)
            .hasMessageContaining(message);
        assertThatThrownBy(() -> throughJpql(filter))
            .as("JPQL path, %s", filter)
            .isInstanceOf(SyntaxErrorException.class)
            .hasMessageContaining(message);
    }

    // ------------------------------------------------------------------
    // Equality
    // ------------------------------------------------------------------

    @ParameterizedTest
    @ValueSource(strings = { "converted", "nativeUuid", "parent.converted", "parent.nativeUuid" })
    @DisplayName("==, !=, =in= and =nin= select the same rows on both paths")
    void equality(String field) {
        boolean throughParent = field.startsWith("parent.");
        List<String> a = throughParent ? List.of("P") : List.of("A");
        assertThat(rows(field + "=='" + A + "'")).isEqualTo(a);
        assertThat(rows(field + "=in=('" + A + "')")).isEqualTo(a);
        assertThat(rows(field + "=in=('" + A + "','" + B + "')")).isEqualTo(a.equals(List.of("P")) ? List.of("P") : List.of("A", "B"));
        assertThat(rows(field + "!='" + A + "'")).isEqualTo(throughParent ? List.of() : List.of("B"));
        assertThat(rows(field + "=nin=('" + A + "')")).isEqualTo(throughParent ? List.of() : List.of("B"));
        assertThat(rows(field + "=='" + A + "';name!='B'")).isEqualTo(a);
        assertThat(rows(field + "=='00000000-0000-0000-0000-000000000000'")).isEmpty();
    }

    @ParameterizedTest
    @ValueSource(strings = { "converted", "nativeUuid" })
    @DisplayName("the case of the literal does not matter: the converter writes the stored form")
    void caseIgnored(String field) {
        String upper = A.toUpperCase();
        assertThat(rows(field + "=='" + upper + "'")).containsExactly("A");
        assertThat(rows(field + "=in=('" + upper + "')")).containsExactly("A");
        assertThat(rows(field + "!='" + upper + "'")).containsExactly("B");
    }

    @Test
    @DisplayName("the JPQL path binds a UUID, single and in a list")
    void boundAsUuid() {
        RsqlContext<UuidRecord> context = new RsqlContext<>(UuidRecord.class).defineEntityManager(entityManager);
        context.root.alias("a0");
        RsqlQuery single = new RsqlCompiler<UuidRecord>().compileToRsqlQuery("converted=='" + A.toUpperCase() + "'", context);
        assertThat(single.where).isEqualTo("a0.converted=:p1");
        assertThat(single.params.get(0).value).isEqualTo(UUID.fromString(A));
        RsqlQuery list = new RsqlCompiler<UuidRecord>().compileToRsqlQuery("converted=in=('" + A + "','" + B + "')", context);
        assertThat(list.paramLists.get(0).list).containsExactly(UUID.fromString(A), UUID.fromString(B));
    }

    // ------------------------------------------------------------------
    // What a UUID refuses
    // ------------------------------------------------------------------

    @ParameterizedTest
    @ValueSource(strings = { "converted", "nativeUuid" })
    @DisplayName("an invalid UUID is a SyntaxErrorException on both paths, single and in a list")
    void invalidUuid(String field) {
        rejected(field + "=='not-a-uuid'", "Invalid UUID format: not-a-uuid");
        rejected(field + "!='not-a-uuid'", "Invalid UUID format: not-a-uuid");
        rejected(field + "=in=('" + A + "','not-a-uuid')", "Invalid UUID format: not-a-uuid");
        rejected(field + "=nin=('not-a-uuid')", "Invalid UUID format: not-a-uuid");
    }

    @ParameterizedTest
    @ValueSource(strings = { "converted", "nativeUuid" })
    @DisplayName("ordering and pattern operators with a string are a SyntaxErrorException on both paths")
    void operatorsWithoutMeaning(String field) {
        rejected(field + "=like='*f4a17f*'", "Unknown operator for UUID: =like=");
        rejected(field + "=nlike='*f4a17f*'", "Unknown operator for UUID: =nlike=");
        rejected(field + "=clike='*f4a17f*'", "Unknown operator for UUID: =clike=");
        rejected(field + "=cnlike='*f4a17f*'", "Unknown operator for UUID: =cnlike=");
        rejected(field + "=gt='" + B + "'", "Unknown operator for UUID: =gt=");
        rejected(field + "=ge='" + B + "'", "Unknown operator for UUID: =ge=");
        rejected(field + "=lt='" + B + "'", "Unknown operator for UUID: =lt=");
        rejected(field + "=le='" + B + "'", "Unknown operator for UUID: =le=");
        rejected(field + "=bt=('" + B + "','" + A + "')", "Unknown operator for UUID: =bt=");
        rejected(field + "=nbt=('" + B + "','" + A + "')", "Unknown operator for UUID: =nbt=");
    }

    @Test
    @DisplayName("null and not null are unchanged")
    void nulls() {
        assertThat(rows("converted==null")).containsExactly("N", "P");
        assertThat(rows("nativeUuid!=null")).containsExactly("A", "B");
    }

    // ------------------------------------------------------------------
    // The service, in both modes
    // ------------------------------------------------------------------

    @Test
    @DisplayName("a JPQL RsqlQueryService finds, pages, sorts and counts by a UUID, in both paging strategies")
    void service() {
        RsqlQueryService<UuidRecord, UuidRecord, UuidRecordRepository, EntityMapper<UuidRecord, UuidRecord>> specificationMode =
            new RsqlQueryService<>(repository, null, entityManager, UuidRecord.class);
        RsqlQueryService<UuidRecord, UuidRecord, UuidRecordRepository, EntityMapper<UuidRecord, UuidRecord>> jpqlMode =
            new RsqlQueryService<>(repository, null, entityManager, UuidRecord.class, "select a0 from UuidRecord a0", "select count(a0) from UuidRecord a0");
        RsqlQueryService<UuidRecord, UuidRecord, UuidRecordRepository, EntityMapper<UuidRecord, UuidRecord>> twoPhase =
            new RsqlQueryService<>(repository, null, entityManager, UuidRecord.class, "select a0 from UuidRecord a0", "select count(a0) from UuidRecord a0");
        twoPhase.setPagingStrategy(PagingStrategy.IDS_THEN_HYDRATE);
        PageRequest page = PageRequest.of(0, 3, Sort.by("name"));
        for (String filter : List.of("converted=='" + A.toUpperCase() + "'", "converted=in=('" + A + "','" + B + "')", "nativeUuid!='" + A + "'")) {
            List<String> expected = specificationMode.findEntitiesByFilter(filter, page).map(UuidRecord::getName).getContent();
            assertThat(jpqlMode.findEntitiesByFilter(filter, page).map(UuidRecord::getName).getContent()).as(filter).isEqualTo(expected);
            assertThat(twoPhase.findEntitiesByFilter(filter, page).map(UuidRecord::getName).getContent()).as(filter).isEqualTo(expected);
            assertThat(jpqlMode.findEntitiesByFilterAndSort(filter, page).stream().map(UuidRecord::getName).toList()).as(filter).isEqualTo(expected);
            assertThat(jpqlMode.countByFilter(filter)).as(filter).isEqualTo(specificationMode.countByFilter(filter)).isEqualTo(expected.size());
        }
    }

    // ------------------------------------------------------------------
    // HAVING
    // ------------------------------------------------------------------

    @ParameterizedTest
    @ValueSource(strings = { "converted", "nativeUuid" })
    @DisplayName("HAVING over a grouped UUID binds the literal as a UUID")
    void having(String field) {
        RsqlQueryService<UuidRecord, UuidRecord, UuidRecordRepository, EntityMapper<UuidRecord, UuidRecord>> service =
            new RsqlQueryService<>(repository, null, entityManager, UuidRecord.class);
        String select = field + ":u, COUNT(*):n";
        List<Tuple> rows = service.getAggregateResult(select, null, field + "=='" + A.toUpperCase() + "'", null);
        assertThat(rows).extracting(t -> t.get("u")).containsExactly(UUID.fromString(A));
        assertThat(service.getAggregateResult(select, null, field + "=in=('" + A + "','" + B + "')", null)).hasSize(2);
        assertThat(service.getAggregateResult(select, null, field + "!='" + A + "'", null)).extracting(t -> t.get("u")).containsExactly(UUID.fromString(B));
        // Compared as text, an upper-case literal used to exclude nothing here.
        assertThat(service.getAggregateResult(select, null, field + "!='" + A.toUpperCase() + "'", null)).extracting(t -> t.get("u")).containsExactly(UUID.fromString(B));
        assertThat(service.getAggregateResult(select, null, field + "=nin=('" + A.toUpperCase() + "')", null)).extracting(t -> t.get("u")).containsExactly(UUID.fromString(B));
        assertThatThrownBy(() -> service.getAggregateResult(select, null, field + "=='not-a-uuid'", null))
            .isInstanceOf(SyntaxErrorException.class).hasMessageContaining("Invalid UUID format: not-a-uuid");
        assertThatThrownBy(() -> service.getAggregateResult(select, null, field + "=gt='" + B + "'", null))
            .isInstanceOf(SyntaxErrorException.class).hasMessageContaining("Unknown operator for UUID: =gt=");
        assertThatThrownBy(() -> service.getAggregateResult(select, null, field + "=bt=('" + B + "','" + A + "')", null))
            .isInstanceOf(SyntaxErrorException.class).hasMessageContaining("Unknown operator for UUID: =bt=");
        assertThatThrownBy(() -> service.getAggregateResult(select, null, field + "=like='%f4a17f%'", null))
            .isInstanceOf(SyntaxErrorException.class).hasMessageContaining("Unknown operator for UUID: =like=");
    }
}

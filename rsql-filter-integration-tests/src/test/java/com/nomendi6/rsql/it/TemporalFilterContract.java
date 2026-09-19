package com.nomendi6.rsql.it;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.nomendi6.rsql.it.config.SqlStatementCapture;
import com.nomendi6.rsql.it.domain.temporal.TemporalRecord;
import com.nomendi6.rsql.it.repository.TemporalRecordRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Tuple;
import jakarta.transaction.Transactional;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.List;
import java.util.TimeZone;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import rsql.RsqlCompiler;
import rsql.exceptions.SyntaxErrorException;
import rsql.helper.SimpleQueryExecutor;
import rsql.where.RsqlContext;

/**
 * What a date or datetime literal means against each temporal type, asserted in every JVM time zone.
 *
 * <p>The contract has two halves. An attribute that holds a <em>moment</em> - {@code Instant},
 * {@code OffsetDateTime}, {@code ZonedDateTime} - is compared by instant, so literals that name the same
 * instant with different offsets select the same rows. An attribute that holds <em>calendar fields</em> -
 * {@code LocalDateTime}, {@code LocalDate} - is compared with the fields exactly as the filter writes them.
 * Neither may depend on the zone of the JVM, which is why every test runs in four of them, the far ends
 * included: a conversion that leaks the default zone passes in UTC and fails everywhere else.</p>
 *
 * <p>The subclasses run the same contract under different Hibernate time zone settings, because the defect
 * this pins down was reported under {@code hibernate.jdbc.time_zone=UTC}.</p>
 */
abstract class TemporalFilterContract {

    private static final List<String> ZONES = List.of("UTC", "Europe/Zagreb", "America/Los_Angeles", "Pacific/Kiritimati");
    private static final TimeZone ORIGINAL_ZONE = TimeZone.getDefault();

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private TemporalRecordRepository repository;

    @AfterEach
    void restoreZone() {
        TimeZone.setDefault(ORIGINAL_ZONE);
    }

    /** Seed and run the assertions once per zone; the data is rewritten each time, as a real JVM would write it. */
    private void inEveryZone(Runnable assertions) {
        for (String zone : ZONES) {
            TimeZone.setDefault(TimeZone.getTimeZone(zone));
            seed();
            try {
                assertions.run();
            } catch (AssertionError | RuntimeException e) {
                throw new AssertionError("in JVM zone " + zone + ": " + e.getMessage(), e);
            }
        }
    }

    /**
     * A, B and C are an hour apart on 19 September; D is the following midnight, for the day boundary; N has
     * no values at all; P points at B through {@code parent}. The moment types hold the same wall clock at
     * +02:00, so B's moment is 07:30:45Z.
     */
    private void seed() {
        // A bulk delete does not touch the persistence context, and the previous zone's rows are still in it.
        entityManager.clear();
        entityManager.createQuery("delete from TemporalRecord t where t.parent is not null").executeUpdate();
        entityManager.createQuery("delete from TemporalRecord t").executeUpdate();
        TemporalRecord b = null;
        String[] names = { "A", "B", "C", "D" };
        LocalDateTime[] locals = {
            LocalDateTime.of(2026, 9, 19, 8, 30, 45),
            LocalDateTime.of(2026, 9, 19, 9, 30, 45),
            LocalDateTime.of(2026, 9, 19, 10, 30, 45),
            LocalDateTime.of(2026, 9, 20, 0, 0, 0),
        };
        LocalDate[] days = { LocalDate.of(2026, 9, 18), LocalDate.of(2026, 9, 19), LocalDate.of(2026, 9, 20), LocalDate.of(2026, 9, 20) };
        for (int i = 0; i < names.length; i++) {
            TemporalRecord record = new TemporalRecord((long) (i + 1), names[i]);
            OffsetDateTime moment = locals[i].atOffset(ZoneOffset.ofHours(2));
            record.setLocal(locals[i]);
            record.setDay(days[i]);
            record.setMoment(moment.toInstant());
            record.setOffsetMoment(moment);
            record.setZoned(moment.atZoneSameInstant(ZoneId.of("Europe/Zagreb")));
            entityManager.persist(record);
            if (i == 1) b = record;
        }
        entityManager.persist(new TemporalRecord(90L, "N"));
        TemporalRecord child = new TemporalRecord(91L, "P");
        child.setParent(b);
        entityManager.persist(child);
        entityManager.flush();
        entityManager.clear();
    }

    private Specification<TemporalRecord> specification(String filter) {
        RsqlContext<TemporalRecord> context = new RsqlContext<>(TemporalRecord.class).defineEntityManager(entityManager);
        return new RsqlCompiler<TemporalRecord>().compileToSpecification(filter, context);
    }

    private List<String> names(String filter) {
        return repository.findAll(specification(filter)).stream().map(TemporalRecord::getName).sorted().toList();
    }

    /** The same filter through the JPQL-text path, which binds its own parameters. */
    private List<String> namesThroughJpql(String filter) {
        RsqlContext<TemporalRecord> context = new RsqlContext<>(TemporalRecord.class).defineEntityManager(entityManager);
        context.root.alias("a0");
        return SimpleQueryExecutor
            .getJpqlQueryResult(TemporalRecord.class, TemporalRecord.class, "select a0 from TemporalRecord a0", "a0", filter, null, context, new RsqlCompiler<>())
            .stream().map(TemporalRecord::getName).sorted().toList();
    }

    // ------------------------------------------------------------------
    // Calendar fields
    // ------------------------------------------------------------------

    @Test
    @Transactional
    @DisplayName("LocalDateTime: the hour written is the hour compared, with Z, with an offset and with no zone")
    void localDateTimeComparesTheFieldsAsWritten() {
        inEveryZone(() -> {
            for (String literal : List.of("#2026-09-19T09:30:45Z#", "#2026-09-19T09:30:45#", "#2026-09-19T09:30:45+02:00#", "#2026-09-19T09:30:45-11:00#")) {
                assertThat(names("local==" + literal)).as("== %s", literal).containsExactly("B");
                assertThat(names("local!=" + literal)).as("!= %s", literal).containsExactly("A", "C", "D");
                assertThat(names("local=lt=" + literal)).as("=lt= %s", literal).containsExactly("A");
                assertThat(names("local=le=" + literal)).as("=le= %s", literal).containsExactly("A", "B");
                assertThat(names("local=gt=" + literal)).as("=gt= %s", literal).containsExactly("C", "D");
                assertThat(names("local=ge=" + literal)).as("=ge= %s", literal).containsExactly("B", "C", "D");
            }
        });
    }

    @Test
    @Transactional
    @DisplayName("LocalDateTime: BETWEEN, IN and their negations take the same reading")
    void localDateTimeInListsAndRanges() {
        inEveryZone(() -> {
            assertThat(names("local=bt=(#2026-09-19T08:30:45Z#,#2026-09-19T09:30:45#)")).containsExactly("A", "B");
            assertThat(names("local=nbt=(#2026-09-19T08:30:45#,#2026-09-19T09:30:45#)")).containsExactly("C", "D");
            assertThat(names("local=in=(#2026-09-19T08:30:45Z#,#2026-09-19T10:30:45#)")).containsExactly("A", "C");
            assertThat(names("local=nin=(#2026-09-19T08:30:45#,#2026-09-19T10:30:45#)")).containsExactly("B", "D");
        });
    }

    @Test
    @Transactional
    @DisplayName("LocalDateTime: a date literal is the start of that day, in calendar fields")
    void dateLiteralAgainstLocalDateTime() {
        inEveryZone(() -> {
            assertThat(names("local==#2026-09-20#")).containsExactly("D");
            assertThat(names("local=lt=#2026-09-20#")).containsExactly("A", "B", "C");
            assertThat(names("local=ge=#2026-09-20#")).containsExactly("D");
            assertThat(names("local=bt=(#2026-09-19#,#2026-09-20#)")).containsExactly("A", "B", "C", "D");
        });
    }

    @Test
    @Transactional
    @DisplayName("LocalDate: compared by calendar date; a datetime literal contributes the date it writes")
    void localDateKeepsTheCalendarDate() {
        inEveryZone(() -> {
            assertThat(names("day==#2026-09-19#")).containsExactly("B");
            assertThat(names("day=lt=#2026-09-19#")).containsExactly("A");
            assertThat(names("day=ge=#2026-09-19#")).containsExactly("B", "C", "D");
            // 23:59:59Z is already the 20th east of Greenwich and still the 19th west of it. The date written
            // is the 19th, and that is the date compared, wherever the server is.
            assertThat(names("day==#2026-09-19T23:59:59Z#")).containsExactly("B");
            assertThat(names("day==#2026-09-19T00:00:01#")).containsExactly("B");
        });
    }

    // ------------------------------------------------------------------
    // Moments
    // ------------------------------------------------------------------

    @Test
    @Transactional
    @DisplayName("Instant, OffsetDateTime, ZonedDateTime: the same instant selects the same rows under any offset")
    void momentTypesCompareInstants() {
        inEveryZone(() -> {
            for (String field : List.of("moment", "offsetMoment", "zoned")) {
                // B is 09:30:45 at +02:00. All three literals name that instant; the last one on another date.
                for (String literal : List.of("#2026-09-19T07:30:45Z#", "#2026-09-19T09:30:45+02:00#", "#2026-09-18T20:30:45-11:00#")) {
                    String because = field + " " + literal;
                    assertThat(names(field + "==" + literal)).as("== %s", because).containsExactly("B");
                    assertThat(names(field + "!=" + literal)).as("!= %s", because).containsExactly("A", "C", "D");
                    assertThat(names(field + "=lt=" + literal)).as("=lt= %s", because).containsExactly("A");
                    assertThat(names(field + "=le=" + literal)).as("=le= %s", because).containsExactly("A", "B");
                    assertThat(names(field + "=gt=" + literal)).as("=gt= %s", because).containsExactly("C", "D");
                    assertThat(names(field + "=ge=" + literal)).as("=ge= %s", because).containsExactly("B", "C", "D");
                }
                // D is midnight on the 20th at +02:00, which is still the 19th in UTC.
                assertThat(names(field + "==#2026-09-19T22:00:00Z#")).as("day boundary, %s", field).containsExactly("D");
                assertThat(names(field + "=bt=(#2026-09-19T06:30:45Z#,#2026-09-19T09:30:45+02:00#)")).as("between, %s", field).containsExactly("A", "B");
                assertThat(names(field + "=in=(#2026-09-19T06:30:45Z#,#2026-09-19T10:30:45+02:00#)")).as("in, %s", field).containsExactly("A", "C");
            }
        });
    }

    @Test
    @Transactional
    @DisplayName("a literal without a zone names no instant, so a moment attribute rejects it - and says which")
    void zonelessLiteralIsRejectedForMoments() {
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
        seed();
        for (String field : List.of("moment", "offsetMoment", "zoned")) {
            assertThatThrownBy(() -> names(field + "==#2026-09-19T07:30:45#"))
                .satisfies(thrown -> {
                    Throwable root = thrown;
                    while (root.getCause() != null) root = root.getCause();
                    assertThat(root).isInstanceOf(SyntaxErrorException.class).hasMessageContaining(field).hasMessageContaining("no zone");
                });
        }
    }

    // ------------------------------------------------------------------
    // Around the comparison
    // ------------------------------------------------------------------

    @Test
    @Transactional
    @DisplayName("null and not-null work on every temporal type")
    void nullChecks() {
        inEveryZone(() -> {
            for (String field : List.of("local", "day", "moment", "offsetMoment", "zoned")) {
                assertThat(names(field + "==null")).as("%s==null", field).containsExactly("N", "P");
                assertThat(names(field + "!=null")).as("%s!=null", field).containsExactly("A", "B", "C", "D");
            }
        });
    }

    @Test
    @Transactional
    @DisplayName("the same reading applies through a relation")
    void throughARelation() {
        inEveryZone(() -> {
            assertThat(names("parent.local==#2026-09-19T09:30:45Z#")).containsExactly("P");
            assertThat(names("parent.local=gt=#2026-09-19T09:30:45#")).isEmpty();
            assertThat(names("parent.moment==#2026-09-19T09:30:45+02:00#")).containsExactly("P");
            assertThat(names("parent.day==#2026-09-19#")).containsExactly("P");
        });
    }

    @Test
    @Transactional
    @DisplayName("rows, count and aggregate agree on one filter, a grouped OR beside the date condition included")
    void rowsCountAndAggregateAgree() {
        inEveryZone(() -> {
            String filter = "(name=='A',name=='B',name=='D');local=ge=#2026-09-19T09:30:45Z#";
            assertThat(names(filter)).containsExactly("B", "D");
            assertThat(repository.count(specification(filter))).isEqualTo(2);

            RsqlContext<TemporalRecord> context = new RsqlContext<>(TemporalRecord.class).defineEntityManager(entityManager);
            List<Tuple> aggregate = SimpleQueryExecutor.getAggregateQueryResultWithSelectExpression(
                TemporalRecord.class, Tuple.class, "COUNT(*):total", filter, null, null, context, new RsqlCompiler<>());
            assertThat(((Number) aggregate.get(0).get("total")).longValue()).isEqualTo(2);
        });
    }

    @Test
    @Transactional
    @DisplayName("the JPQL-text path binds its parameters the same way")
    void jpqlPathTakesTheSameReading() {
        inEveryZone(() -> {
            assertThat(namesThroughJpql("local==#2026-09-19T09:30:45Z#")).containsExactly("B");
            assertThat(namesThroughJpql("local=gt=#2026-09-19T09:30:45#")).containsExactly("C", "D");
            assertThat(namesThroughJpql("local=bt=(#2026-09-19T08:30:45#,#2026-09-19T09:30:45Z#)")).containsExactly("A", "B");
            assertThat(namesThroughJpql("local=in=(#2026-09-19T08:30:45#,#2026-09-19T10:30:45Z#)")).containsExactly("A", "C");
            assertThat(namesThroughJpql("local==#2026-09-20#")).containsExactly("D");
            assertThat(namesThroughJpql("moment==#2026-09-19T09:30:45+02:00#")).containsExactly("B");
            assertThat(namesThroughJpql("offsetMoment=lt=#2026-09-19T07:30:45Z#")).containsExactly("A");
        });
    }

    @Test
    @Transactional
    @DisplayName("HAVING compares an aggregate over a LocalDateTime with the fields as written")
    void havingTakesTheSameReading() {
        inEveryZone(() -> {
            RsqlContext<TemporalRecord> context = new RsqlContext<>(TemporalRecord.class).defineEntityManager(entityManager);
            List<Tuple> groups = SimpleQueryExecutor.getAggregateQueryResultWithSelectExpression(
                TemporalRecord.class, Tuple.class, "name:who, MAX(local):latest", "local!=null", "latest=ge=#2026-09-19T10:30:45Z#", null, context, new RsqlCompiler<>());
            assertThat(groups).extracting(tuple -> tuple.get("who")).containsExactlyInAnyOrder("C", "D");
        });
    }

    @Test
    @Transactional
    @DisplayName("without any filter, a moment survives a write and a read")
    void momentsRoundTrip() {
        // Not a filter test: the report that prompted these also described an OffsetDateTime coming back two
        // hours early. Under these settings it does not, on this provider - so that loss happens above JPA.
        inEveryZone(() -> {
            OffsetDateTime sent = OffsetDateTime.parse("2026-09-19T11:30:45+02:00");
            TemporalRecord record = new TemporalRecord(500L, "X");
            record.setOffsetMoment(sent);
            record.setZoned(sent.toZonedDateTime());
            record.setMoment(sent.toInstant());
            entityManager.persist(record);
            entityManager.flush();
            entityManager.clear();

            TemporalRecord back = entityManager.find(TemporalRecord.class, 500L);
            Instant expected = Instant.parse("2026-09-19T09:30:45Z");
            assertThat(back.getOffsetMoment().toInstant()).isEqualTo(expected);
            assertThat(back.getZoned().toInstant()).isEqualTo(expected);
            assertThat(back.getMoment()).isEqualTo(expected);
        });
    }
}

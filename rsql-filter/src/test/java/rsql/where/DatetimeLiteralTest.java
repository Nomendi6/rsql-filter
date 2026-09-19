package rsql.where;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Calendar;
import java.util.Date;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import rsql.RsqlCompiler;
import rsql.describe.FilterCondition;
import rsql.describe.RightSide;
import rsql.exceptions.SyntaxErrorException;

/** A datetime literal becomes a value of the type it is compared with - no database needed to say which. */
class DatetimeLiteralTest {

    private static final LocalDateTime FIELDS = LocalDateTime.of(2026, 9, 19, 9, 30, 45);
    private static final Instant AT_PLUS_TWO = Instant.parse("2026-09-19T07:30:45Z");

    @Test
    @DisplayName("Z, an offset, a fraction and no zone at all are all read")
    void parsesEveryForm() {
        assertThat(DatetimeLiteral.parse("2026-09-19T09:30:45Z").hasZone()).isTrue();
        assertThat(DatetimeLiteral.parse("2026-09-19T09:30:45+02:00").toInstant()).isEqualTo(AT_PLUS_TWO);
        assertThat(DatetimeLiteral.parse("2026-09-19T09:30:45-11:00").toInstant()).isEqualTo(Instant.parse("2026-09-19T20:30:45Z"));
        assertThat(DatetimeLiteral.parse("2026-09-19T09:30:45.123Z").toInstant()).isEqualTo(Instant.parse("2026-09-19T09:30:45.123Z"));
        assertThat(DatetimeLiteral.parse("2026-09-19T09:30:45").hasZone()).isFalse();
        assertThat(DatetimeLiteral.parse("2026-09-19T09:30:45.5").toLocalDateTime()).isEqualTo(FIELDS.plusNanos(500_000_000));
    }

    @Test
    @DisplayName("calendar types take the fields as written and ignore the zone")
    void calendarTypesIgnoreTheZone() {
        for (String text : new String[] { "2026-09-19T09:30:45", "2026-09-19T09:30:45Z", "2026-09-19T09:30:45+02:00", "2026-09-19T09:30:45-11:00" }) {
            DatetimeLiteral literal = DatetimeLiteral.parse(text);
            assertThat(literal.as(LocalDateTime.class, "f")).as(text).isEqualTo(FIELDS);
            assertThat(literal.as(LocalDate.class, "f")).as(text).isEqualTo(LocalDate.of(2026, 9, 19));
        }
    }

    @Test
    @DisplayName("moment types get the instant, in their own type")
    void momentTypesGetTheInstant() {
        DatetimeLiteral literal = DatetimeLiteral.parse("2026-09-19T09:30:45+02:00");
        assertThat(literal.as(Instant.class, "f")).isEqualTo(AT_PLUS_TWO);
        assertThat(literal.as(OffsetDateTime.class, "f")).isEqualTo(OffsetDateTime.of(FIELDS, ZoneOffset.ofHours(2)));
        assertThat(((ZonedDateTime) literal.as(ZonedDateTime.class, "f")).toInstant()).isEqualTo(AT_PLUS_TWO);
        assertThat(literal.as(Date.class, "f")).isEqualTo(Date.from(AT_PLUS_TWO));
        assertThat(literal.as(Timestamp.class, "f")).isEqualTo(Timestamp.from(AT_PLUS_TWO));
        assertThat(((Calendar) literal.as(Calendar.class, "f")).toInstant()).isEqualTo(AT_PLUS_TWO);
    }

    @Test
    @DisplayName("a moment type rejects a literal without a zone, naming the field and the type")
    void momentTypesNeedAZone() {
        DatetimeLiteral literal = DatetimeLiteral.parse("2026-09-19T09:30:45");
        for (Class<?> type : new Class<?>[] { Instant.class, OffsetDateTime.class, ZonedDateTime.class, Date.class, Timestamp.class }) {
            assertThatThrownBy(() -> literal.as(type, "validFrom"))
                .isInstanceOf(SyntaxErrorException.class)
                .hasMessageContaining("validFrom").hasMessageContaining(type.getSimpleName()).hasMessageContaining("no zone");
        }
        assertThatThrownBy(literal::toInstant).isInstanceOf(SyntaxErrorException.class);
    }

    @Test
    @DisplayName("a type that is not temporal gets what earlier versions bound")
    void otherTypesKeepTheOldValue() {
        assertThat(DatetimeLiteral.parse("2026-09-19T09:30:45Z").as(Object.class, "f")).isEqualTo(Instant.parse("2026-09-19T09:30:45Z"));
        assertThat(DatetimeLiteral.parse("2026-09-19T09:30:45Z").as(null, "f")).isEqualTo(Instant.parse("2026-09-19T09:30:45Z"));
        assertThat(DatetimeLiteral.parse("2026-09-19T09:30:45").as(Object.class, "f")).isEqualTo(FIELDS);
    }

    @Test
    @DisplayName("text that fits the grammar but names no real time is a syntax error, not a parse exception")
    void impossibleValuesAreSyntaxErrors() {
        assertThatThrownBy(() -> DatetimeLiteral.parse("2026-13-19T09:30:45Z")).isInstanceOf(SyntaxErrorException.class).hasMessageContaining("2026-13-19");
        assertThatThrownBy(() -> DatetimeLiteral.parse("2026-09-19T25:30:45")).isInstanceOf(SyntaxErrorException.class);
    }

    @Test
    @DisplayName("a date literal is the start of the day for a LocalDateTime and itself for everything else")
    void dateLiteral() {
        LocalDate date = LocalDate.of(2026, 9, 19);
        assertThat(DatetimeLiteral.dateAs(date, LocalDateTime.class)).isEqualTo(LocalDateTime.of(2026, 9, 19, 0, 0));
        assertThat(DatetimeLiteral.dateAs(date, LocalDate.class)).isEqualTo(date);
        assertThat(DatetimeLiteral.dateAs(date, Instant.class)).isEqualTo(date);
        assertThat(DatetimeLiteral.dateAs(null, LocalDateTime.class)).isNull();
    }

    @Test
    @DisplayName("the grammar accepts the zone-less form, and the JPQL text and the description carry it")
    void zonelessLiteralThroughTheGrammar() {
        assertThat(new RsqlWhereString().parseString("validFrom=ge=#2026-09-19T09:30:45#")).isEqualTo("validFrom>='2026-09-19T09:30:45'");
        assertThat(new RsqlWhereString().parseString("validFrom=ge=#2026-09-19T09:30:45Z#")).isEqualTo("validFrom>='2026-09-19T09:30:45Z'");

        FilterCondition zoneless = (FilterCondition) new RsqlCompiler<Object>().compileToFilterNode("validFrom==#2026-09-19T09:30:45#");
        assertThat(((RightSide.SingleValue) zoneless.rightSide()).value()).isEqualTo(FIELDS);
        FilterCondition zoned = (FilterCondition) new RsqlCompiler<Object>().compileToFilterNode("validFrom==#2026-09-19T09:30:45+02:00#");
        assertThat(((RightSide.SingleValue) zoned.rightSide()).value()).isEqualTo(AT_PLUS_TWO);
    }
}

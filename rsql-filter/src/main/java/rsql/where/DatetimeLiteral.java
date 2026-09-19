package rsql.where;

import rsql.exceptions.SyntaxErrorException;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 * A datetime literal as it was written - {@code #2026-09-19T09:30:45Z#}, {@code #2026-09-19T09:30:45+02:00#}
 * or, without a zone, {@code #2026-09-19T09:30:45#} - before it is known what it will be compared with.
 *
 * <p>The literal used to become an {@code Instant} the moment it was parsed, and the attribute's path was
 * cast to {@code Path<Instant>} to match. That is right for an attribute that holds a moment and wrong for
 * one that holds calendar fields: bound against a {@code LocalDateTime}, the provider converts the instant
 * to local fields through whatever zone the JVM or the JDBC connection happens to be in, so
 * {@code local==#2026-09-19T09:30:45Z#} compared 11:30:45 in a JVM at +02:00 and found nothing. The value a
 * filter binds has to have the type of the attribute, and only the attribute's type says how to get there,
 * so the conversion is put off until it is known - {@link #as(Class, String)}.</p>
 *
 * <p>The two families of temporal type read the literal differently, and that is the contract:</p>
 * <ul>
 *   <li><b>A moment</b> - {@code Instant}, {@code OffsetDateTime}, {@code ZonedDateTime}, {@code Date},
 *       {@code Timestamp}, {@code Calendar}. The literal names an instant, so it has to carry a zone, and two
 *       literals that name the same instant with different offsets select the same rows.</li>
 *   <li><b>Calendar fields</b> - {@code LocalDateTime}, {@code LocalDate}. The fields are taken exactly as
 *       written. A zone on the literal is accepted and ignored: there is no zone to convert <em>into</em>, and
 *       inventing one - the JVM's, the connection's - is the defect described above. Clients that can only
 *       send a zoned literal therefore send the local fields with {@code Z}; clients that can choose should
 *       leave the zone off.</li>
 * </ul>
 */
public final class DatetimeLiteral {

    private final String text;
    private final LocalDateTime local;
    private final ZoneOffset offset;

    private DatetimeLiteral(String text, LocalDateTime local, ZoneOffset offset) {
        this.text = text;
        this.local = local;
        this.offset = offset;
    }

    /**
     * @param text The literal without its {@code #} delimiters.
     * @return The literal; its zone is absent when none was written.
     * @throws SyntaxErrorException when the text matches the grammar's shape but names no real date or time,
     *                              such as month 13
     */
    public static DatetimeLiteral parse(String text) {
        try {
            if (hasZoneSuffix(text)) {
                OffsetDateTime zoned = OffsetDateTime.parse(text);
                return new DatetimeLiteral(text, zoned.toLocalDateTime(), zoned.getOffset());
            }
            return new DatetimeLiteral(text, LocalDateTime.parse(text), null);
        } catch (DateTimeParseException e) {
            throw new SyntaxErrorException("Invalid datetime literal #" + text + "#: " + e.getMessage());
        }
    }

    /** A zone is {@code Z} or a signed offset after the time; the date part's own hyphens come before the T. */
    private static boolean hasZoneSuffix(String text) {
        int time = text.indexOf('T');
        if (time < 0) return false;
        return text.endsWith("Z") || text.indexOf('+', time) >= 0 || text.indexOf('-', time) >= 0;
    }

    /** @return true when the literal was written with {@code Z} or an offset */
    public boolean hasZone() {
        return offset != null;
    }

    /** @return the date and time exactly as written, whatever zone followed them */
    public LocalDateTime toLocalDateTime() {
        return local;
    }

    /**
     * @return the instant the literal names
     * @throws SyntaxErrorException when it was written without a zone and so names none
     */
    public Instant toInstant() {
        if (offset == null) {
            throw new SyntaxErrorException("Datetime literal #" + text + "# has no zone, so it does not name an instant."
                + " Write it with Z or an offset, e.g. #" + text + "Z#.");
        }
        return local.toInstant(offset);
    }

    /**
     * The value for a description or any other use that has no attribute to take a type from: the instant
     * when the literal names one, otherwise its calendar fields.
     */
    public Object toNeutralValue() {
        return offset != null ? (Object) toInstant() : local;
    }

    /**
     * The value to bind when this literal is compared with an attribute of the given type.
     *
     * @param attributeType Java type of the attribute, or of the expression, on the other side.
     * @param fieldName     The selector, for the error message.
     * @return A value of {@code attributeType} for every temporal type listed on this class; for any other
     *         type, what earlier versions bound - the instant - or the calendar fields when there is no zone.
     * @throws SyntaxErrorException when the attribute holds a moment and the literal has no zone
     */
    public Object as(Class<?> attributeType, String fieldName) {
        if (attributeType == LocalDateTime.class) return local;
        if (attributeType == LocalDate.class) return local.toLocalDate();
        if (attributeType == null || !isMoment(attributeType)) return toNeutralValue();

        if (offset == null) {
            throw new SyntaxErrorException("Datetime literal #" + text + "# has no zone, but " + fieldName + " is a "
                + attributeType.getSimpleName() + " and holds a moment. Write the literal with Z or an offset.");
        }
        if (attributeType == Instant.class) return toInstant();
        if (attributeType == OffsetDateTime.class) return OffsetDateTime.of(local, offset);
        if (attributeType == ZonedDateTime.class) return ZonedDateTime.of(local, offset);
        if (Timestamp.class.isAssignableFrom(attributeType)) return Timestamp.from(toInstant());
        if (Calendar.class.isAssignableFrom(attributeType)) return GregorianCalendar.from(ZonedDateTime.of(local, offset));
        return Date.from(toInstant());
    }

    private static boolean isMoment(Class<?> type) {
        return type == Instant.class
            || type == OffsetDateTime.class
            || type == ZonedDateTime.class
            || Date.class.isAssignableFrom(type)
            || Calendar.class.isAssignableFrom(type);
    }

    /**
     * The value to bind when a <em>date</em> literal - {@code #2026-09-19#} - is compared with an attribute of
     * the given type.
     *
     * <p>Against a {@code LocalDateTime} it is the start of that day, in calendar fields: left as a
     * {@code LocalDate}, the provider would make a timestamp of it through the JVM's zone and the comparison
     * would move with the server. Every other type receives the date unchanged, as before.</p>
     */
    public static Object dateAs(LocalDate date, Class<?> attributeType) {
        if (date != null && attributeType == LocalDateTime.class) return date.atStartOfDay();
        return date;
    }

    @Override
    public String toString() {
        return "#" + text + "#";
    }
}

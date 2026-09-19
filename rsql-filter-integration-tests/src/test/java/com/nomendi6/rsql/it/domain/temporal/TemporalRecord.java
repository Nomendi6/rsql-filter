package com.nomendi6.rsql.it.domain.temporal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.io.Serializable;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZonedDateTime;

/**
 * One attribute of every temporal type a datetime literal can be compared with.
 *
 * <p>The types fall in two families that a filter has to treat differently. {@code Instant},
 * {@code OffsetDateTime} and {@code ZonedDateTime} name a moment: two literals with different offsets that
 * name the same moment must select the same rows. {@code LocalDateTime} and {@code LocalDate} name calendar
 * fields: the hour written in the filter is the hour compared, whatever zone the JVM or the JDBC connection
 * is in. {@code parent} is there so the same can be asserted through a relation.</p>
 */
@Entity
@Table(name = "temporal_record")
public class TemporalRecord implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    private Long id;

    @Column(name = "name")
    private String name;

    @Column(name = "day_value")
    private LocalDate day;

    @Column(name = "moment_value")
    private Instant moment;

    @Column(name = "zoned_value")
    private ZonedDateTime zoned;

    @Column(name = "offset_value")
    private OffsetDateTime offsetMoment;

    @Column(name = "local_value")
    private LocalDateTime local;

    @ManyToOne
    @JoinColumn(name = "parent_id")
    private TemporalRecord parent;

    public TemporalRecord() {}

    public TemporalRecord(Long id, String name) {
        this.id = id;
        this.name = name;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public LocalDate getDay() { return day; }
    public void setDay(LocalDate day) { this.day = day; }
    public Instant getMoment() { return moment; }
    public void setMoment(Instant moment) { this.moment = moment; }
    public ZonedDateTime getZoned() { return zoned; }
    public void setZoned(ZonedDateTime zoned) { this.zoned = zoned; }
    public OffsetDateTime getOffsetMoment() { return offsetMoment; }
    public void setOffsetMoment(OffsetDateTime offsetMoment) { this.offsetMoment = offsetMoment; }
    public LocalDateTime getLocal() { return local; }
    public void setLocal(LocalDateTime local) { this.local = local; }
    public TemporalRecord getParent() { return parent; }
    public void setParent(TemporalRecord parent) { this.parent = parent; }
}

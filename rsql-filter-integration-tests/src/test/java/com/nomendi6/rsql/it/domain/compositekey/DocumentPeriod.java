package com.nomendi6.rsql.it.domain.compositekey;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;

/** An embeddable that is not an identifier, with its own textual form {@code 2024-09}. */
@Embeddable
public class DocumentPeriod implements Serializable {

    private static final long serialVersionUID = 1L;

    @Column(name = "period_year")
    private Integer year;

    @Column(name = "period_month")
    private Integer month;

    public DocumentPeriod() {}

    public DocumentPeriod(Integer year, Integer month) {
        this.year = year;
        this.month = month;
    }

    public static DocumentPeriod valueOf(String text) {
        String[] parts = text.split("-");
        if (parts.length != 2) {
            throw new IllegalArgumentException("Expected yyyy-mm: " + text);
        }
        return new DocumentPeriod(Integer.valueOf(parts[0]), Integer.valueOf(parts[1]));
    }

    public Integer getYear() {
        return year;
    }

    public Integer getMonth() {
        return month;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DocumentPeriod other)) return false;
        return Objects.equals(year, other.year) && Objects.equals(month, other.month);
    }

    @Override
    public int hashCode() {
        return Objects.hash(year, month);
    }
}

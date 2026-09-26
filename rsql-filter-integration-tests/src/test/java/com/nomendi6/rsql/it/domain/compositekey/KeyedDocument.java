package com.nomendi6.rsql.it.domain.compositekey;

import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.io.Serializable;

/** A document addressed by a three-column {@link DocumentKey}. */
@Entity
@Table(name = "keyed_document")
public class KeyedDocument implements Serializable {

    private static final long serialVersionUID = 1L;

    @EmbeddedId
    private DocumentKey id;

    @Column(name = "title")
    private String title;

    @Embedded
    private DocumentPeriod period;

    public KeyedDocument() {}

    public KeyedDocument(DocumentKey id, String title, DocumentPeriod period) {
        this.id = id;
        this.title = title;
        this.period = period;
    }

    public DocumentKey getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public DocumentPeriod getPeriod() {
        return period;
    }
}

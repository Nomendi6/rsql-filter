package com.nomendi6.rsql.it.domain.compositekey;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinColumns;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;
import java.io.Serializable;

/** A line whose key contains its document's key, shared with the association through {@code @MapsId}. */
@Entity
@Table(name = "keyed_line")
public class KeyedLine implements Serializable {

    private static final long serialVersionUID = 1L;

    @EmbeddedId
    private LineKey id;

    @MapsId("documentKey")
    @ManyToOne
    @JoinColumns({
        @JoinColumn(name = "company_code", referencedColumnName = "company_code"),
        @JoinColumn(name = "doc_year", referencedColumnName = "doc_year"),
        @JoinColumn(name = "doc_no", referencedColumnName = "doc_no"),
    })
    private KeyedDocument document;

    @Column(name = "text")
    private String text;

    public KeyedLine() {}

    public KeyedLine(KeyedDocument document, Integer lineNo, String text) {
        this.id = new LineKey(document.getId(), lineNo);
        this.document = document;
        this.text = text;
    }

    public LineKey getId() {
        return id;
    }

    public KeyedDocument getDocument() {
        return document;
    }

    public String getText() {
        return text;
    }
}

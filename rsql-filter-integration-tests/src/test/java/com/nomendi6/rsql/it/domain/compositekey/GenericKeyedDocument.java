package com.nomendi6.rsql.it.domain.compositekey;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;

/** {@link KeyedDocument}'s shape, with the key and the period inherited from a generic base. */
@Entity
@Table(name = "generic_keyed_document")
public class GenericKeyedDocument extends KeyedBase<DocumentKey, DocumentPeriod> {

    private static final long serialVersionUID = 1L;

    public GenericKeyedDocument() {}

    public GenericKeyedDocument(DocumentKey id, String title, DocumentPeriod period) {
        this.id = id;
        this.title = title;
        this.period = period;
    }
}

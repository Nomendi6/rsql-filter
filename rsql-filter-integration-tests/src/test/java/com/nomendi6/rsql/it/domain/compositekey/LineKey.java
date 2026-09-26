package com.nomendi6.rsql.it.domain.compositekey;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Embedded;
import java.io.Serializable;
import java.util.Objects;

/** A key nesting the document's key, with no {@code valueOf(String)} - so it cannot be written as a string. */
@Embeddable
public class LineKey implements Serializable {

    private static final long serialVersionUID = 1L;

    @Embedded
    private DocumentKey documentKey;

    @Column(name = "line_no")
    private Integer lineNo;

    public LineKey() {}

    public LineKey(DocumentKey documentKey, Integer lineNo) {
        this.documentKey = documentKey;
        this.lineNo = lineNo;
    }

    public DocumentKey getDocumentKey() {
        return documentKey;
    }

    public Integer getLineNo() {
        return lineNo;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof LineKey other)) return false;
        return Objects.equals(documentKey, other.documentKey) && Objects.equals(lineNo, other.lineNo);
    }

    @Override
    public int hashCode() {
        return Objects.hash(documentKey, lineNo);
    }
}

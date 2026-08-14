package com.nomendi6.rsql.it.domain.idshortcut;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;

/** A composite key: two columns, so no single foreign key column carries it. */
@Embeddable
public class ShortcutEmbeddedIdKey implements Serializable {

    private static final long serialVersionUID = 1L;

    @Column(name = "part_a")
    private Long partA;

    @Column(name = "part_b")
    private Long partB;

    public ShortcutEmbeddedIdKey() {}

    public ShortcutEmbeddedIdKey(Long partA, Long partB) {
        this.partA = partA;
        this.partB = partB;
    }

    public Long getPartA() {
        return partA;
    }

    public void setPartA(Long partA) {
        this.partA = partA;
    }

    public Long getPartB() {
        return partB;
    }

    public void setPartB(Long partB) {
        this.partB = partB;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (!(other instanceof ShortcutEmbeddedIdKey key)) return false;
        return Objects.equals(partA, key.partA) && Objects.equals(partB, key.partB);
    }

    @Override
    public int hashCode() {
        return Objects.hash(partA, partB);
    }
}

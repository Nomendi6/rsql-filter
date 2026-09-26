package com.nomendi6.rsql.it.domain.compositekey;

import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.MappedSuperclass;
import java.io.Serializable;

/**
 * A generic base whose key and embedded value are type variables. On the path the attribute reports the erased
 * bound, {@code Serializable}; the concrete classes are known only per entity.
 */
@MappedSuperclass
public abstract class KeyedBase<K extends Serializable, P extends Serializable> implements Serializable {

    private static final long serialVersionUID = 1L;

    @EmbeddedId
    protected K id;

    @Embedded
    protected P period;

    @Column(name = "title")
    protected String title;

    public K getId() {
        return id;
    }

    public P getPeriod() {
        return period;
    }

    public String getTitle() {
        return title;
    }
}

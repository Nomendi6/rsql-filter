package com.nomendi6.rsql.it.domain.idshortcut;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.io.Serializable;

/** A target with the ordinary shape: one basic identifier called {@code id}. */
@Entity
@Table(name = "shortcut_simple_target")
public class ShortcutSimpleTarget implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    private Long id;

    @Column(name = "name")
    private String name;

    public ShortcutSimpleTarget() {}

    public ShortcutSimpleTarget(Long id, String name) {
        this.id = id;
        this.name = name;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}

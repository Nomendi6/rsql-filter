package com.nomendi6.rsql.it.domain.idshortcut;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.io.Serializable;

/** A target with a composite identifier, which the shortcut must decline. */
@Entity
@Table(name = "shortcut_embedded_id_target")
public class ShortcutEmbeddedIdTarget implements Serializable {

    private static final long serialVersionUID = 1L;

    @EmbeddedId
    private ShortcutEmbeddedIdKey id;

    @Column(name = "name")
    private String name;

    public ShortcutEmbeddedIdTarget() {}

    public ShortcutEmbeddedIdTarget(ShortcutEmbeddedIdKey id, String name) {
        this.id = id;
        this.name = name;
    }

    public ShortcutEmbeddedIdKey getId() {
        return id;
    }

    public void setId(ShortcutEmbeddedIdKey id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}

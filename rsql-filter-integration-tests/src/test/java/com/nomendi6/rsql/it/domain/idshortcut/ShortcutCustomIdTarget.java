package com.nomendi6.rsql.it.domain.idshortcut;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.io.Serializable;

/**
 * A target whose identifier attribute is called {@code objectId}. A selector written as
 * {@code customIdTarget.id} names no attribute at all here, and one written as
 * {@code customIdTarget.objectId} names the identifier under a name the shortcut has to discover
 * rather than assume.
 */
@Entity
@Table(name = "shortcut_custom_id_target")
public class ShortcutCustomIdTarget implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "object_id")
    private Long objectId;

    @Column(name = "name")
    private String name;

    public ShortcutCustomIdTarget() {}

    public ShortcutCustomIdTarget(Long objectId, String name) {
        this.objectId = objectId;
        this.name = name;
    }

    public Long getObjectId() {
        return objectId;
    }

    public void setObjectId(Long objectId) {
        this.objectId = objectId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}

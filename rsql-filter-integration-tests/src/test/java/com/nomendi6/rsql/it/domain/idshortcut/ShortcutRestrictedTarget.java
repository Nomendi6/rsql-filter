package com.nomendi6.rsql.it.domain.idshortcut;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.io.Serializable;
import org.hibernate.annotations.SQLRestriction;

/**
 * A target under an {@code @SQLRestriction}, the soft-delete shape. The restriction is a condition on
 * the join; when the identifier is read off the foreign key instead there is no join to carry it, so
 * this entity exists to pin down that difference rather than leave it to argument.
 */
@Entity
@Table(name = "shortcut_restricted_target")
@SQLRestriction("archived = false")
public class ShortcutRestrictedTarget implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    private Long id;

    @Column(name = "name")
    private String name;

    @Column(name = "archived", nullable = false)
    private boolean archived;

    public ShortcutRestrictedTarget() {}

    public ShortcutRestrictedTarget(Long id, String name, boolean archived) {
        this.id = id;
        this.name = name;
        this.archived = archived;
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

    public boolean isArchived() {
        return archived;
    }

    public void setArchived(boolean archived) {
        this.archived = archived;
    }
}

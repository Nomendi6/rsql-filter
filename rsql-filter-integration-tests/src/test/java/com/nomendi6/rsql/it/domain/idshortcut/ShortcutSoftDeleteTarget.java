package com.nomendi6.rsql.it.domain.idshortcut;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.io.Serializable;
import org.hibernate.annotations.SoftDelete;

/**
 * A target under {@code @SoftDelete}. Like {@code @SQLRestriction} this narrows the rows the entity has,
 * but Hibernate applies it through its own mapping rather than through an annotation the caller can read.
 */
@Entity
@Table(name = "shortcut_soft_delete_target")
@SoftDelete(columnName = "deleted")
public class ShortcutSoftDeleteTarget implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    private Long id;

    @Column(name = "name")
    private String name;

    public ShortcutSoftDeleteTarget() {}

    public ShortcutSoftDeleteTarget(Long id, String name) {
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

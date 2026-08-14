package com.nomendi6.rsql.it.domain.idshortcut;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.io.Serializable;

/**
 * The owning side of the inverse {@code OneToOne}. Seen from {@link ShortcutRoot} the association is
 * a to-one, but its foreign key sits here, so there is nothing on the root's table to read.
 */
@Entity
@Table(name = "shortcut_inverse_side")
public class ShortcutInverseSide implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    private Long id;

    @Column(name = "name")
    private String name;

    @OneToOne
    @JoinColumn(name = "root_id")
    private ShortcutRoot root;

    public ShortcutInverseSide() {}

    public ShortcutInverseSide(Long id, String name, ShortcutRoot root) {
        this.id = id;
        this.name = name;
        this.root = root;
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

    public ShortcutRoot getRoot() {
        return root;
    }

    public void setRoot(ShortcutRoot root) {
        this.root = root;
    }
}

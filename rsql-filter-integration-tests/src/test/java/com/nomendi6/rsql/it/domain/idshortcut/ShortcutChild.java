package com.nomendi6.rsql.it.domain.idshortcut;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.io.Serializable;

/** The many side of the {@code OneToMany}: its foreign key points back at the root. */
@Entity
@Table(name = "shortcut_child")
public class ShortcutChild implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    private Long id;

    @Column(name = "name")
    private String name;

    @ManyToOne
    @JoinColumn(name = "root_id")
    private ShortcutRoot root;

    public ShortcutChild() {}

    public ShortcutChild(Long id, String name, ShortcutRoot root) {
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

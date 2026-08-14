package com.nomendi6.rsql.it.domain.idshortcut;

import jakarta.persistence.*;
import java.io.Serializable;

/** Base of a single-table hierarchy: every subtype shares one table and is told apart by a discriminator. */
@Entity
@Table(name = "shortcut_animal")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "kind")
public abstract class ShortcutAnimal implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    private Long id;

    @Column(name = "name")
    private String name;

    protected ShortcutAnimal() {}

    protected ShortcutAnimal(Long id, String name) {
        this.id = id;
        this.name = name;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
}

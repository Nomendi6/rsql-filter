package com.nomendi6.rsql.it.domain.idshortcut;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue("CAT")
public class ShortcutCat extends ShortcutAnimal {

    public ShortcutCat() {}

    public ShortcutCat(Long id, String name) {
        super(id, name);
    }
}

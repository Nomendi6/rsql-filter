package com.nomendi6.rsql.it.domain.idshortcut;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue("DOG")
public class ShortcutDog extends ShortcutAnimal {

    public ShortcutDog() {}

    public ShortcutDog(Long id, String name) {
        super(id, name);
    }
}

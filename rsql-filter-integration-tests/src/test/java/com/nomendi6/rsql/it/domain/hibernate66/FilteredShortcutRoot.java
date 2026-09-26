package com.nomendi6.rsql.it.domain.hibernate66;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.io.Serializable;

/** Points at a {@link ShortcutFilteredTarget}; its own entity so the filter stays out of the shared test model. */
@Entity
@Table(name = "filtered_shortcut_root")
public class FilteredShortcutRoot implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    private Long id;

    @Column(name = "name")
    private String name;

    @ManyToOne
    @JoinColumn(name = "filtered_target_id")
    private ShortcutFilteredTarget filteredTarget;

    public FilteredShortcutRoot() {}

    public FilteredShortcutRoot(Long id, String name, ShortcutFilteredTarget filteredTarget) {
        this.id = id;
        this.name = name;
        this.filteredTarget = filteredTarget;
    }

    public Long getId() {
        return id;
    }

    public ShortcutFilteredTarget getFilteredTarget() {
        return filteredTarget;
    }
}

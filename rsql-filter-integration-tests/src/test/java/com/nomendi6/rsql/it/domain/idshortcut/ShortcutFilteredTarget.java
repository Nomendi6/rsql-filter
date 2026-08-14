package com.nomendi6.rsql.it.domain.idshortcut;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.io.Serializable;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.ParamDef;

/**
 * A target scoped by an enabled {@code @Filter}, the idiomatic way to express tenant or row-level security.
 *
 * <p>{@code applyToLoadByKey} is what makes Hibernate put the condition on a to-one join rather than only on
 * a query of the entity itself, so this is the shape where dropping the join would widen an authorization
 * filter - the exact thing the shortcut was requested for.</p>
 */
@Entity
@Table(name = "shortcut_filtered_target")
@FilterDef(name = "tenantScope", parameters = @ParamDef(name = "tenant", type = Long.class), applyToLoadByKey = true)
@Filter(name = "tenantScope", condition = "tenant_id = :tenant")
public class ShortcutFilteredTarget implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    private Long id;

    @Column(name = "name")
    private String name;

    @Column(name = "tenant_id")
    private Long tenantId;

    public ShortcutFilteredTarget() {}

    public ShortcutFilteredTarget(Long id, String name, Long tenantId) {
        this.id = id;
        this.name = name;
        this.tenantId = tenantId;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Long getTenantId() { return tenantId; }
    public void setTenantId(Long tenantId) { this.tenantId = tenantId; }
}

package com.nomendi6.rsql.it.domain.idshortcut;

import jakarta.persistence.*;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

/**
 * The entity queried by the foreign key id shortcut tests.
 *
 * <p>It carries one association of every shape the shortcut has to tell apart: a plain
 * {@code ManyToOne}, an owning and an inverse {@code OneToOne}, a {@code OneToMany}, a target with a
 * composite identifier, a target whose identifier is not called {@code id}, and a target under an
 * {@code @SQLRestriction}. The existing {@code Product} graph covers the ordinary cases; these exist
 * so the guards can be tested rather than argued about.</p>
 */
@Entity
@Table(name = "shortcut_root")
public class ShortcutRoot implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    private Long id;

    @Column(name = "name")
    private String name;

    /** The ordinary case the shortcut is for: foreign key on this table. */
    @ManyToOne
    @JoinColumn(name = "simple_target_id")
    private ShortcutSimpleTarget simpleTarget;

    /** Owning OneToOne: foreign key is also on this table, so the shortcut applies. */
    @OneToOne
    @JoinColumn(name = "owned_one_to_one_id")
    private ShortcutSimpleTarget ownedOneToOne;

    /** Inverse OneToOne: the foreign key lives on the other table. */
    @OneToOne(mappedBy = "root")
    private ShortcutInverseSide inverseOneToOne;

    /** Collection: the foreign key lives on the other table. */
    @OneToMany(mappedBy = "root")
    private Set<ShortcutChild> children = new HashSet<>();

    /** Target with an {@code @EmbeddedId}, i.e. an identifier spanning two columns. */
    @ManyToOne
    @JoinColumns({
        @JoinColumn(name = "embedded_target_part_a", referencedColumnName = "part_a"),
        @JoinColumn(name = "embedded_target_part_b", referencedColumnName = "part_b"),
    })
    private ShortcutEmbeddedIdTarget embeddedIdTarget;

    /** Target whose identifier attribute is called {@code objectId}, not {@code id}. */
    @ManyToOne
    @JoinColumn(name = "custom_id_target_id")
    private ShortcutCustomIdTarget customIdTarget;

    /** Target under an {@code @SQLRestriction}, the documented hazard of the shortcut. */
    @ManyToOne
    @JoinColumn(name = "restricted_target_id")
    private ShortcutRestrictedTarget restrictedTarget;

    /** A foreign key allowed to point at nothing: Hibernate has to check the target really exists. */
    @ManyToOne
    @JoinColumn(name = "not_found_target_id")
    @org.hibernate.annotations.NotFound(action = org.hibernate.annotations.NotFoundAction.IGNORE)
    private ShortcutNotFoundTarget notFoundTarget;

    /** Target under {@code @SoftDelete}, a restriction carried by the mapping rather than an annotation. */
    @ManyToOne
    @JoinColumn(name = "soft_delete_target_id")
    private ShortcutSoftDeleteTarget softDeleteTarget;

    /**
     * Target typed to one subtype of a single-table hierarchy. The foreign key column holds an identifier
     * from the shared table, which need not belong to that subtype - the join is what would check.
     */
    @ManyToOne
    @JoinColumn(name = "cat_target_id")
    private ShortcutCat catTarget;

    public ShortcutRoot() {}

    public ShortcutRoot(Long id, String name) {
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

    public ShortcutSimpleTarget getSimpleTarget() {
        return simpleTarget;
    }

    public void setSimpleTarget(ShortcutSimpleTarget simpleTarget) {
        this.simpleTarget = simpleTarget;
    }

    public ShortcutSimpleTarget getOwnedOneToOne() {
        return ownedOneToOne;
    }

    public void setOwnedOneToOne(ShortcutSimpleTarget ownedOneToOne) {
        this.ownedOneToOne = ownedOneToOne;
    }

    public ShortcutInverseSide getInverseOneToOne() {
        return inverseOneToOne;
    }

    public void setInverseOneToOne(ShortcutInverseSide inverseOneToOne) {
        this.inverseOneToOne = inverseOneToOne;
    }

    public Set<ShortcutChild> getChildren() {
        return children;
    }

    public void setChildren(Set<ShortcutChild> children) {
        this.children = children;
    }

    public ShortcutEmbeddedIdTarget getEmbeddedIdTarget() {
        return embeddedIdTarget;
    }

    public void setEmbeddedIdTarget(ShortcutEmbeddedIdTarget embeddedIdTarget) {
        this.embeddedIdTarget = embeddedIdTarget;
    }

    public ShortcutCustomIdTarget getCustomIdTarget() {
        return customIdTarget;
    }

    public void setCustomIdTarget(ShortcutCustomIdTarget customIdTarget) {
        this.customIdTarget = customIdTarget;
    }

    public ShortcutRestrictedTarget getRestrictedTarget() {
        return restrictedTarget;
    }

    public void setRestrictedTarget(ShortcutRestrictedTarget restrictedTarget) {
        this.restrictedTarget = restrictedTarget;
    }

    public ShortcutNotFoundTarget getNotFoundTarget() {
        return notFoundTarget;
    }

    public void setNotFoundTarget(ShortcutNotFoundTarget notFoundTarget) {
        this.notFoundTarget = notFoundTarget;
    }

    public ShortcutSoftDeleteTarget getSoftDeleteTarget() {
        return softDeleteTarget;
    }

    public void setSoftDeleteTarget(ShortcutSoftDeleteTarget softDeleteTarget) {
        this.softDeleteTarget = softDeleteTarget;
    }

    public ShortcutCat getCatTarget() {
        return catTarget;
    }

    public void setCatTarget(ShortcutCat catTarget) {
        this.catTarget = catTarget;
    }
}

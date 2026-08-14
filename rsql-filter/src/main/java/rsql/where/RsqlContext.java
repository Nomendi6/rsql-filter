package rsql.where;

import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.metamodel.ManagedType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * This class represents the context for RSQL operations on a specific entity type.
 * It provides the necessary JPA components for creating and executing queries.
 *
 * <p>The context maintains shared JOIN and metadata caches that are used across
 * SELECT, WHERE, GROUP BY, HAVING, and ORDER BY clauses to ensure consistency
 * and avoid duplicate JOINs.</p>
 *
 * @param <ENTITY> The type of the entity that the RSQL operations are targeting.
 */
public class RsqlContext<ENTITY> {

    private static final Logger log = LoggerFactory.getLogger(RsqlContext.class);

    /**
     * The Class object representing the entity type.
     */
    public Class<ENTITY> entityClass;

    /**
     * The Root object for the entity type, used in criteria queries.
     */
    public Root<ENTITY> root;

    /**
     * The EntityManager used to create and execute queries.
     */
    public EntityManager entityManager;

    /**
     * The CriteriaQuery object for the entity type.
     */
    public CriteriaQuery<ENTITY> criteriaQuery;

    /**
     * The CriteriaBuilder used to construct the CriteriaQuery object.
     */
    public CriteriaBuilder criteriaBuilder;

    /**
     * Shared cache for JOIN paths. This map ensures that JOINs created in SELECT, WHERE,
     * or other clauses are reused instead of creating duplicate JOINs.
     *
     * <p>Key: dot-separated path (e.g., "productType" or "productType.category")</p>
     * <p>Value: JPA Path object for the joined entity</p>
     */
    public Map<String, Path<?>> joinsMap;

    /**
     * Shared cache for entity metadata. This map stores ManagedType objects for entities
     * accessed through JOINs, avoiding redundant metamodel lookups.
     *
     * <p>Key: dot-separated path (e.g., "productType" or "productType.category")</p>
     * <p>Value: ManagedType for the entity at that path</p>
     */
    public Map<String, ManagedType<?>> classMetadataMap;

    /**
     * Whether a selector that ends in the identifier of a to-one association is resolved against this
     * table's own foreign key column instead of through a join.
     *
     * <p>{@code ownerOrg.id} names a value that is already stored on the owning table, so reading it
     * needs no join. With this flag off, every such selector adds a LEFT JOIN for a column the query
     * already has.</p>
     *
     * <p>The shortcut already stands aside wherever the two forms are not interchangeable. It asks Hibernate
     * whether it will resolve the identifier from the foreign key at all, which rules out a collection, the
     * inverse side of a {@code OneToOne}, {@code @NotFound}, {@code @SoftDelete} and a foreign key that
     * references something other than the target's primary key; and it declines a composite identifier and a
     * target under {@code @SQLRestriction} / {@code @Where} on its own. What remains is a foreign key
     * pointing at a row that does not exist, which a schema with referential integrity cannot produce. Turn
     * the shortcut off if yours can.</p>
     *
     * <p>This is the default for every association. To decide per association instead, see
     * {@link #foreignKeyIdShortcutOverrides}.</p>
     */
    public boolean useForeignKeyIdShortcut = true;

    /**
     * Per-association answers that override {@link #useForeignKeyIdShortcut}.
     *
     * <p>An entity usually has several to-one associations and they need not be treated alike. This map
     * decides for the ones named in it; {@link #useForeignKeyIdShortcut} decides for the rest. Between the
     * two, all four arrangements are expressible without any rule about which wins:</p>
     *
     * <table border="1">
     * <caption>What the two settings express together</caption>
     * <tr><th>Wanted</th><th>{@code useForeignKeyIdShortcut}</th><th>Overrides</th></tr>
     * <tr><td>Every association reads its foreign key (the default)</td><td>{@code true}</td><td>empty</td></tr>
     * <tr><td>Every association joins, as before 0.7.7</td><td>{@code false}</td><td>empty</td></tr>
     * <tr><td>Only {@code ownerOrg} and {@code ownerCompany} read the foreign key</td><td>{@code false}</td><td>both {@code true}</td></tr>
     * <tr><td>Everything but {@code legacyOwner} reads the foreign key</td><td>{@code true}</td><td>{@code legacyOwner} {@code false}</td></tr>
     * </table>
     *
     * <p>A key is the association path exactly as the filter writes it, without the identifier segment:
     * {@code "ownerOrg"} covers {@code ownerOrg.id}, and {@code "parent.parent.productType"} covers
     * {@code parent.parent.productType.id}. One association reached by two paths is two keys - the path is
     * what the filter names, and naming what the filter names is what keeps this predictable.</p>
     *
     * <p>Use {@link #withForeignKeyIdShortcutFor} and {@link #withoutForeignKeyIdShortcutFor} rather than
     * writing to the map directly.</p>
     */
    public Map<String, Boolean> foreignKeyIdShortcutOverrides = new HashMap<>();

    /**
     * Read the identifier of these associations off the foreign key column, whatever
     * {@link #useForeignKeyIdShortcut} says.
     *
     * <pre>{@code
     * context.useForeignKeyIdShortcut = false;
     * context.withForeignKeyIdShortcutFor("ownerOrg", "ownerCompany");
     * // ownerOrg.id and ownerCompany.id read the foreign key; every other to-one id joins
     * }</pre>
     *
     * @param associationPaths Paths as the filter writes them, without the identifier segment.
     * @return this context, for chaining
     */
    public RsqlContext<ENTITY> withForeignKeyIdShortcutFor(String... associationPaths) {
        for (String path : associationPaths) {
            foreignKeyIdShortcutOverrides.put(path, Boolean.TRUE);
        }
        return this;
    }

    /**
     * Resolve the identifier of these associations through a join, whatever
     * {@link #useForeignKeyIdShortcut} says.
     *
     * @param associationPaths Paths as the filter writes them, without the identifier segment.
     * @return this context, for chaining
     */
    public RsqlContext<ENTITY> withoutForeignKeyIdShortcutFor(String... associationPaths) {
        for (String path : associationPaths) {
            foreignKeyIdShortcutOverrides.put(path, Boolean.FALSE);
        }
        return this;
    }

    /**
     * Whether the identifier of the association at this path is read off the foreign key column.
     *
     * @param associationPath Path as the filter writes it, without the identifier segment.
     * @return the override for that path if there is one, otherwise {@link #useForeignKeyIdShortcut}
     */
    public boolean isForeignKeyIdShortcutEnabledFor(String associationPath) {
        return foreignKeyIdShortcutOverrides.getOrDefault(associationPath, useForeignKeyIdShortcut);
    }

    /**
     * Take the foreign key shortcut settings from another context.
     *
     * <p>A paged query builds a second context for its count, with its own root and its own joins map. That
     * context must still resolve selectors the same way as the one that fetched the page: if the two
     * disagree, the count is counting a different filter than the page shows.</p>
     *
     * @param source Context to copy the settings from.
     */
    public void copyForeignKeyIdShortcutSettingsFrom(RsqlContext<?> source) {
        this.useForeignKeyIdShortcut = source.useForeignKeyIdShortcut;
        this.foreignKeyIdShortcutOverrides = new HashMap<>(source.foreignKeyIdShortcutOverrides);
    }

    //    public Specification<ENTITY> specification;

    /**
     * Constructor for the RsqlContext class.
     *
     * @param entityClass The Class object representing the entity type.
     */
    public RsqlContext(Class<ENTITY> entityClass) {
        this.entityClass = entityClass;
        this.joinsMap = new HashMap<>();
        this.classMetadataMap = new HashMap<>();
        log.trace("RsqlContext created for entity: {}", entityClass.getSimpleName());
    }

    /**
     * Defines the EntityManager for the RsqlContext and initializes the context.
     *
     * @param entityManager The EntityManager to be used.
     * @return The RsqlContext object with the defined EntityManager.
     */
    public RsqlContext<ENTITY> defineEntityManager(EntityManager entityManager) {
        this.entityManager = entityManager;
        initContext();
        return this;
    }

    /**
     * Initializes the context by creating the CriteriaBuilder, CriteriaQuery, and Root objects.
     * Also clears the JOIN and metadata caches to start fresh for a new query.
     * Preserves the root alias to ensure WHERE clauses have proper alias prefixes.
     */
    public void initContext() {
        // Preserve existing alias if root already exists
        String existingAlias = null;
        if (this.root != null) {
            existingAlias = this.root.getAlias();
        }

        // If no existing alias, use default
        if (existingAlias == null || existingAlias.isEmpty()) {
            existingAlias = "a0";
        }

        this.criteriaBuilder = entityManager.getCriteriaBuilder();
        this.criteriaQuery = criteriaBuilder.createQuery(entityClass);
        this.root = criteriaQuery.from(entityClass);

        // Always set alias on new root to ensure WHERE clauses work correctly
        this.root.alias(existingAlias);

        this.joinsMap.clear();
        this.classMetadataMap.clear();

        log.trace("RsqlContext initialized for entity: {}", entityClass.getSimpleName());
    }

    /**
     * Creates a new instance of RsqlContext with the same entityClass and entityManager,
     * but with fresh joinsMap and classMetadataMap. This ensures thread-safety by
     * providing each query execution with its own isolated context.
     *
     * <p>This method should be called at the beginning of each query execution to avoid
     * shared mutable state between concurrent requests.</p>
     *
     * @return A new RsqlContext instance with initialized context
     */
    public RsqlContext<ENTITY> createNewInstance() {
        RsqlContext<ENTITY> newContext = new RsqlContext<>(this.entityClass);
        newContext.defineEntityManager(this.entityManager);
        newContext.copyForeignKeyIdShortcutSettingsFrom(this);

        // Preserve the root alias from the original context
        if (this.root != null && this.root.getAlias() != null) {
            newContext.root.alias(this.root.getAlias());
        }

        return newContext;
    }
}

package rsql.where;

import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.metamodel.ManagedType;

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
     */
    public void initContext() {
        this.criteriaBuilder = entityManager.getCriteriaBuilder();
        this.criteriaQuery = criteriaBuilder.createQuery(entityClass);
        this.root = criteriaQuery.from(entityClass);
        this.joinsMap.clear();
        this.classMetadataMap.clear();
        //        this.specification = Specification.where(null);
    }
}

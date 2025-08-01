package rsql.where;

import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;

/**
 * This class represents the context for RSQL operations on a specific entity type.
 * It provides the necessary JPA components for creating and executing queries.
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

    //    public Specification<ENTITY> specification;

    /**
     * Constructor for the RsqlContext class.
     *
     * @param entityClass The Class object representing the entity type.
     */
    public RsqlContext(Class<ENTITY> entityClass) {
        this.entityClass = entityClass;
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
     */
    public void initContext() {
        this.criteriaBuilder = entityManager.getCriteriaBuilder();
        this.criteriaQuery = criteriaBuilder.createQuery(entityClass);
        this.root = criteriaQuery.from(entityClass);
        //        this.specification = Specification.where(null);
    }
}

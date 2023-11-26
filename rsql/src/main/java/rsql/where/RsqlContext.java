package rsql.where;

import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;

public class RsqlContext<ENTITY> {

    public Class<ENTITY> entityClass;
    public Root<ENTITY> root;
    public EntityManager entityManager;
    public CriteriaQuery<ENTITY> criteriaQuery;
    public CriteriaBuilder criteriaBuilder;

    //    public Specification<ENTITY> specification;

    public RsqlContext(Class<ENTITY> entityClass) {
        this.entityClass = entityClass;
    }

    public RsqlContext<ENTITY> defineEntityManager(EntityManager entityManager) {
        this.entityManager = entityManager;
        initContext();
        return this;
    }

    public void initContext() {
        this.criteriaBuilder = entityManager.getCriteriaBuilder();
        this.criteriaQuery = criteriaBuilder.createQuery(entityClass);
        this.root = criteriaQuery.from(entityClass);
        //        this.specification = Specification.where(null);
    }
}

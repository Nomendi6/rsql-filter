package rsql;

import rsql.where.RsqlContext;
import rsql.dto.LovDTO;
import rsql.mapper.EntityMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.Tuple;
import java.util.List;
import java.util.stream.Collectors;

import static rsql.helper.SimpleQueryExecutor.getQueryResult;

/**
 * Service for executing complex queries for  entities in the database.
 * The main input is a filter as a string which gets converted to {@link Specification},
 * in a way that all the filters must apply.
 * It returns a {@link List} of ENTITY_DTO or a {@link Page} of ENTITY_DTO which fulfills the criteria.
 */
public class RsqlQueryService<
    ENTITY,
    ENTITY_DTO,
    REPOS extends JpaRepository<ENTITY, Long> & JpaSpecificationExecutor<ENTITY>,
    MAPPER extends EntityMapper<ENTITY_DTO, ENTITY>
> {

    private final Logger log = LoggerFactory.getLogger(RsqlQueryService.class);

    private final REPOS appObjectRepository;

    private final MAPPER appObjectMapper;

    private final RsqlCompiler<ENTITY> rsqlCompiler = new RsqlCompiler<>();

    private final RsqlContext<ENTITY> rsqlContext;

    private final Class<ENTITY> entityClass;

    public RsqlQueryService(REPOS appObjectRepository, MAPPER appObjectMapper, EntityManager entityManager, Class<ENTITY> entityClass) {
        this.appObjectRepository = appObjectRepository;
        this.appObjectMapper = appObjectMapper;
        this.rsqlContext = new RsqlContext<>(entityClass).defineEntityManager(entityManager);
        this.entityClass = entityClass;
    }

    public RsqlCompiler<ENTITY> getRsqlCompiler() {
        return rsqlCompiler;
    }

    public RsqlContext<ENTITY> getRsqlContext() {
        return rsqlContext;
    }

    public Class<ENTITY> getEntityClass() {
        return entityClass;
    }

    /**
     * Return a {@link List} of {@link ENTITY_DTO} which matches the filter from the database.
     * @param filter Filter containing RSQL where statement, which the entities should match.
     * @return the matching entities.
     */
    @Transactional(readOnly = true)
    public List<ENTITY_DTO> findByFilter(String filter) {
        log.debug("find by filter : {}", filter);

        final Specification<ENTITY> specification = createSpecification(filter);
        return appObjectMapper.toDto(appObjectRepository.findAll(specification));
    }

    /**
     * Return a specification which matches the filter from the database.
     *
     * @param filter Filter containing RSQL where statement, which the entities should match.
     * @return the specification.
     */
    public Specification<ENTITY> getSpecification(String filter) {
        return createSpecification(filter);
    }

    /**
     * Return a {@link List} of {@link ENTITY_DTO} which matches the criteria from the database.
     * @param filter Filter containing RSQL where statement, which the entities should match.
     * @param sortOrder The sort order in which the result should be returned
     * @return the {@link List} of the matching entities
     */
    @Transactional(readOnly = true)
    public List<ENTITY_DTO> findByFilterAndSort(String filter, Pageable sortOrder) {
        log.debug("find by filter : {}, page: {}", filter, sortOrder);
        Sort sort;
        if (sortOrder == null) {
            sort = null;
        } else {
            sort = sortOrder.getSort();
        }

        final Specification<ENTITY> specification = createSpecification(filter);

        return appObjectRepository.findAll(specification, sort).stream().map(appObjectMapper::toDto).collect(Collectors.toList());
    }

    /**
     * Return a {@link Page} of {@link ENTITY_DTO} which matches the criteria from the database.
     * @param filter Filter containing RSQL where statement, which the entities should match.
     * @param page The page, which should be returned.
     * @return the {@link Page} of matching entities.
     */
    @Transactional(readOnly = true)
    public Page<ENTITY_DTO> findByFilter(String filter, Pageable page) {
        log.debug("find by filter : {}, page: {}", filter, page);
        if (page == null) {
            page = PageRequest.of(0, 20);
        }
        final Specification<ENTITY> specification = createSpecification(filter);
        return appObjectRepository.findAll(specification, page).map(appObjectMapper::toDto);
    }

    /**
     * Return the number of matching entities in the database.
     * @param filter Filter containing RSQL where statement, which the entities should match.
     * @return the number of matching entities.
     */
    @Transactional(readOnly = true)
    public long countByFilter(String filter) {
        log.debug("count by filter : {}", filter);
        final Specification<ENTITY> specification = createSpecification(filter);
        if (specification == null) {
            return appObjectRepository.count();
        }
        return appObjectRepository.count(specification);
    }

    /**
     * Function to convert filter to a {@link Specification}
     * @param filter Filter containing RSQL where statement, which the entities should match.
     * @return the matching {@link Specification} of the entity.
     */
    public Specification<ENTITY> createSpecification(String filter) {
        return rsqlCompiler.compileToSpecification(filter, rsqlContext);
    }

    /**
     * Return a list of values with id, code and name for the entity filtered by a provided filter.
     * @param filter    Filter that will generate where criteria for the query
     * @param pageable  Pageable containing the sort order that will be used
     * @return          List of values in LovDTO form
     */
    @Transactional(readOnly = true)
    public List<LovDTO> getLOV(String filter, Pageable pageable) {
        return getQueryResult(
            entityClass,
            LovDTO.class,
            new String[] { "id", "code", "name" },
            filter,
            pageable,
            rsqlContext,
            rsqlCompiler
        );
    }

    @Transactional(readOnly = true)
    public List<Tuple> getTuple(String filter, Pageable pageable, String[] fields) {
        return getQueryResult(
            entityClass,
            Tuple.class,
            fields,
            filter,
            pageable,
            rsqlContext,
            rsqlCompiler
        );
    }
}

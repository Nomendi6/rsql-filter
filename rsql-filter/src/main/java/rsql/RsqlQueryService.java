package rsql;

import jakarta.persistence.TypedQuery;
import rsql.helper.SimpleQueryExecutor;
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

import jakarta.persistence.EntityManager;
import jakarta.persistence.Tuple;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Selection;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import rsql.helper.AggregateField;
import rsql.helper.AggregateQueryBuilder;
import static rsql.helper.SimpleQueryExecutor.getQueryResult;
import static rsql.helper.SimpleQueryExecutor.getQueryResultWithSelect;
import static rsql.helper.SimpleQueryExecutor.getQueryResultAsPageWithSelect;
import static rsql.helper.SimpleQueryExecutor.getAggregateQueryResultWithSelect;
import static rsql.helper.SimpleQueryExecutor.getAggregateQueryResultAsPageWithSelect;
import static rsql.helper.SimpleQueryExecutor.getAggregateQueryResult;
import static rsql.helper.SimpleQueryExecutor.getAggregateQueryResultWithSelectExpression;
import static rsql.helper.SimpleQueryExecutor.getAggregateQueryResultAsPageWithSelectExpression;

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

    private static final String DEFAULT_ALIAS_FOR_STARTROOT = "a0";

    private final Logger log = LoggerFactory.getLogger(RsqlQueryService.class);

    private final REPOS appObjectRepository;

    private final MAPPER appObjectMapper;

    private final RsqlCompiler<ENTITY> rsqlCompiler = new RsqlCompiler<>();

    private final RsqlContext<ENTITY> rsqlContext;

    private final Class<ENTITY> entityClass;

    private final EntityManager entityManager;

    private String jpqlSelectAllFromEntity;

    private String selectAlias = DEFAULT_ALIAS_FOR_STARTROOT;
    private String jpqlSelectCountFromEntity;

    private String countAlias = DEFAULT_ALIAS_FOR_STARTROOT;

    private boolean useJpqlSelect = false;

    public RsqlQueryService(REPOS appObjectRepository, MAPPER appObjectMapper, EntityManager entityManager, Class<ENTITY> entityClass) {
        this.appObjectRepository = appObjectRepository;
        this.appObjectMapper = appObjectMapper;
        this.entityManager = entityManager;
        this.rsqlContext = new RsqlContext<>(entityClass).defineEntityManager(entityManager);
        this.entityClass = entityClass;
        // Note: Don't set alias on shared rsqlContext here - it will be set on each query context
    }

    public RsqlQueryService(REPOS appObjectRepository, MAPPER appObjectMapper, EntityManager entityManager, Class<ENTITY> entityClass, String jpqlSelectAllFromEntity, String jpqlSelectCountFromEntity) {
        this.appObjectRepository = appObjectRepository;
        this.appObjectMapper = appObjectMapper;
        this.entityManager = entityManager;
        this.rsqlContext = new RsqlContext<>(entityClass).defineEntityManager(entityManager);
        this.entityClass = entityClass;

        this.jpqlSelectAllFromEntity = jpqlSelectAllFromEntity;
        this.jpqlSelectCountFromEntity = jpqlSelectCountFromEntity;
        // Note: Don't set alias on shared rsqlContext here - it will be set on each query context
        this.useJpqlSelect = true;
    }

    public RsqlCompiler<ENTITY> getRsqlCompiler() {
        return rsqlCompiler;
    }

    /**
     * Returns the template RsqlContext. This context should NOT be used directly for queries.
     * Instead, use getQueryContext() to get a fresh instance for each query execution.
     *
     * @return The template RsqlContext
     * @deprecated Use getQueryContext() instead to ensure thread-safety
     */
    @Deprecated
    public RsqlContext<ENTITY> getRsqlContext() {
        return rsqlContext;
    }

    /**
     * Creates a new RsqlContext instance for query execution. This ensures thread-safety
     * by providing each query with its own isolated context (joinsMap, classMetadataMap, etc.).
     *
     * <p>This method should be called at the beginning of each query execution instead of
     * using the shared rsqlContext field directly.</p>
     *
     * @return A new RsqlContext instance for the current query
     */
    private RsqlContext<ENTITY> getQueryContext() {
        RsqlContext<ENTITY> newContext = rsqlContext.createNewInstance();

        // Apply the current selectAlias from this service instance
        // This ensures that alias changes via setSelectAlias() are respected
        if (selectAlias != null && !selectAlias.isEmpty()) {
            newContext.root.alias(selectAlias);
        }

        return newContext;
    }

    public Class<ENTITY> getEntityClass() {
        return entityClass;
    }

    /**
     * Sets the JPQL 'SELECT ... FROM ...' query string to be used for retrieving
     * all entities and determines the select alias from the provided query.
     * This method also enables the use of JPQL select functionality.
     *
     * @param jpqlSelectAllFromEntity the JPQL 'SELECT ... FROM ...' query string
     *                                representing the entity selection. This query
     *                                must define the structure for entity retrieval
     *                                and the parameter alias.
     */
    public void setJpqlSelectAllFromEntity(String jpqlSelectAllFromEntity) {
        this.jpqlSelectAllFromEntity = jpqlSelectAllFromEntity;
        this.selectAlias = findAliasFromJpqlSelectString(jpqlSelectAllFromEntity);
        this.useJpqlSelect = true;
    }

    /**
     * Sets the JPQL select count from entity query string and determines the count alias
     * from the provided query. This method updates the internal JPQL select count structure
     * for subsequent operations.
     *
     * @param jpqlSelectCountFromEntity the JPQL 'SELECT ... FROM ...' query string used to count
     *                                  the entities. This query must define the necessary structure
     *                                  to retrieve the entity count and alias.
     */
    public void setJpqlSelectCountFromEntity(String jpqlSelectCountFromEntity) {
        this.jpqlSelectCountFromEntity = jpqlSelectCountFromEntity;
        this.countAlias = findAliasFromJpqlSelectString(jpqlSelectCountFromEntity);
    }

    /**
     * Sets whether JPQL queries should use the specified select statement.
     *
     * @param useJpqlSelect a boolean value indicating whether JPQL queries
     *                      should utilize the select operation. True to enable,
     *                      false to disable.
     */
    public void setUseJpqlSelect(boolean useJpqlSelect) {
        this.useJpqlSelect = useJpqlSelect;
    }

    /**
     * Determines whether JPQL queries are configured to use a specific select operation.
     *
     * @return true if JPQL queries should use the specified select operation; false otherwise.
     */
    public boolean getUseJpqlSelect() {
        return this.useJpqlSelect;
    }

    /**
     * Sets the alias to be used for selecting entities in JPQL queries.
     * This alias will be applied to each new query context created via getQueryContext().
     *
     * @param selectAlias the alias to be used for the select query as a String
     */
    public void setSelectAlias(String selectAlias) {
        this.selectAlias = selectAlias;
        // Note: We don't modify rsqlContext.root here to maintain thread-safety
        // The alias will be applied when getQueryContext() creates a new instance
    }

    public String getSelectAlias() {
        return this.selectAlias;
    }

    /**
     * Sets the alias used for counting entities in JPQL queries.
     *
     * @param countAlias the alias to be used for the count query as a String
     */
    public void setCountAlias(String countAlias) {
        this.countAlias = countAlias;
    }

    /**
     * Retrieves the alias used for counting entities in JPQL queries.
     *
     * @return the alias used for the count query as a String.
     */
    public String getCountAlias() {
        return this.countAlias;
    }

    /**
     * Return a {@link List} of {@link ENTITY_DTO} which matches the filter from the database.
     * @param filter Filter containing RSQL where statement, which the entities should match.
     * @return the matching entities.
     */
    @Transactional(readOnly = true)
    public List<ENTITY_DTO> findByFilter(String filter) {
        log.debug("find by filter : {}", filter);

        if (useJpqlSelect) {
            return SimpleQueryExecutor.getJpqlQueryResult(
                entityClass,
                entityClass,
                this.jpqlSelectAllFromEntity,
                    selectAlias, filter,
                null,
                getQueryContext(),
                rsqlCompiler).stream().map(appObjectMapper::toDto).collect(Collectors.toList());
        } else {
            final Specification<ENTITY> specification = createSpecification(filter);
            return appObjectMapper.toDto(appObjectRepository.findAll(specification));
        }
    }

    /**
     * Return a {@link List} of {@link ENTITY} which matches the filter from the database.
     * This method returns the entities directly without mapping them to DTOs.
     * @param filter Filter containing RSQL where statement, which the entities should match.
     * @return the matching entities.
     */
    @Transactional(readOnly = true)
    public List<ENTITY> findEntitiesByFilter(String filter) {
        log.debug("find entities by filter : {}", filter);

        if (useJpqlSelect) {
            return SimpleQueryExecutor.getJpqlQueryResult(
                entityClass,
                entityClass,
                this.jpqlSelectAllFromEntity,
                selectAlias, filter,
                null,
                getQueryContext(),
                rsqlCompiler);
        } else {
            final Specification<ENTITY> specification = createSpecification(filter);
            return appObjectRepository.findAll(specification);
        }
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
        log.debug("find by filter and sort : {}, page: {}", filter, sortOrder);
        Sort sort;
        if (sortOrder == null) {
            sort = null;
        } else {
            sort = sortOrder.getSort();
        }

        if (useJpqlSelect) {
            return SimpleQueryExecutor.getJpqlQueryResult(
                    entityClass,
                    entityClass,
                    this.jpqlSelectAllFromEntity,
                    selectAlias, filter,
                    sortOrder,
                    getQueryContext(),
                    rsqlCompiler).stream().map(appObjectMapper::toDto).collect(Collectors.toList());
        } else {
            final Specification<ENTITY> specification = createSpecification(filter);
            return appObjectRepository.findAll(specification, sort).stream().map(appObjectMapper::toDto).collect(Collectors.toList());
        }
    }

    /**
     * Finds and retrieves a list of entities based on the specified filter and sorting order.
     * The query may use JPQL or a JPA Specification for filtering and sorting the data.
     *
     * @param filter the filtering criteria to apply when retrieving the entities.
     * @param sortOrder the sorting parameters, including page and sort details.
     *                  If null, no sorting will be applied.
     * @return a list of entities that match the given filter and sort order.
     */
    @Transactional(readOnly = true)
    public List<ENTITY> findEntitiesByFilterAndSort(String filter, Pageable sortOrder) {
        log.debug("find entities by filter and sort: {}, page: {}", filter, sortOrder);
        Sort sort;
        if (sortOrder == null) {
            sort = null;
        } else {
            sort = sortOrder.getSort();
        }

        if (useJpqlSelect) {
            return SimpleQueryExecutor.getJpqlQueryResult(
                    entityClass,
                    entityClass,
                    this.jpqlSelectAllFromEntity,
                    selectAlias, filter,
                    sortOrder,
                    getQueryContext(),
                    rsqlCompiler);
        } else {
            final Specification<ENTITY> specification = createSpecification(filter);
            return appObjectRepository.findAll(specification, sort);
        }
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
        if (useJpqlSelect) {
             Page<ENTITY>  entityPage = SimpleQueryExecutor.getJpqlQueryResultAsPage(
                entityClass,
                entityClass,
                this.jpqlSelectAllFromEntity,
                     selectAlias, this.jpqlSelectCountFromEntity,
                     countAlias, filter,
                page,
                getQueryContext(),
                rsqlCompiler);

                return entityPage.map(appObjectMapper::toDto);

        } else {
            final Specification<ENTITY> specification = createSpecification(filter);
            return appObjectRepository.findAll(specification, page).map(appObjectMapper::toDto);
        }
    }

    /**
     * Finds entities using a specified filter and pageable request.
     *
     * This method supports dynamic filtering and performs either a JPQL-based
     * query or uses a Specification-based query depending on the configuration.
     *
     * @param filter the string filter used to apply dynamic conditions to the query
     * @param page the pageable request containing pagination and sorting information;
     *             if null, a default PageRequest with page 0 and size 20 is used
     * @return a Page containing the list of entities that match the specified filter
     */
    @Transactional(readOnly = true)
    public Page<ENTITY> findEntitiesByFilter(String filter, Pageable page) {
        log.debug("find by filter : {}, page: {}", filter, page);
        if (page == null) {
            page = PageRequest.of(0, 20);
        }
        if (useJpqlSelect) {
             Page<ENTITY>  entityPage = SimpleQueryExecutor.getJpqlQueryResultAsPage(
                entityClass,
                entityClass,
                this.jpqlSelectAllFromEntity,
                     selectAlias, this.jpqlSelectCountFromEntity,
                     countAlias, filter,
                page,
                getQueryContext(),
                rsqlCompiler);

                return entityPage;

        } else {
            final Specification<ENTITY> specification = createSpecification(filter);
            return appObjectRepository.findAll(specification, page);
        }
    }

    /**
     * Return the number of matching entities in the database.
     * @param filter Filter containing RSQL where statement, which the entities should match.
     * @return the number of matching entities.
     */
    @Transactional(readOnly = true)
    public long countByFilter(String filter) {
        log.debug("count by filter : {}", filter);

        if (useJpqlSelect) {
            return SimpleQueryExecutor.getJpqlQueryCount(
                entityClass,
                this.jpqlSelectCountFromEntity,
                countAlias,
                filter,
                getQueryContext(),
                rsqlCompiler);
        } else {
            final Specification<ENTITY> specification = createSpecification(filter);
            if (specification == null) {
                return appObjectRepository.count();
            }
            return appObjectRepository.count(specification);
        }
    }

    /**
     * Function to convert filter to a {@link Specification}
     * @param filter Filter containing RSQL where statement, which the entities should match.
     * @return the matching {@link Specification} of the entity.
     */
    public Specification<ENTITY> createSpecification(String filter) {
        return rsqlCompiler.compileToSpecification(filter, getQueryContext());
    }

    /**
     * Creates JPA Criteria Selections from a SELECT string (non-aggregate queries).
     * This method is analogous to createSpecification() but for SELECT clauses.
     *
     * <p>The SELECT string can include:</p>
     * <ul>
     *   <li>Simple fields: {@code "code, name, price"}</li>
     *   <li>Nested properties: {@code "productType.name, productType.code"}</li>
     *   <li>Aliases: {@code "name:productName, productType.name:typeName"}</li>
     * </ul>
     *
     * <p>Example usage:</p>
     * <pre>
     * CriteriaBuilder builder = em.getCriteriaBuilder();
     * CriteriaQuery&lt;Tuple&gt; query = builder.createQuery(Tuple.class);
     * Root&lt;Product&gt; root = query.from(Product.class);
     *
     * List&lt;Selection&lt;?&gt;&gt; selections = queryService.createSelections(
     *     "code, name, productType.name:typeName", builder, root
     * );
     * query.multiselect(selections);
     * query.where(queryService.createSpecification("status==ACTIVE")
     *     .toPredicate(root, query, builder));
     *
     * List&lt;Tuple&gt; results = em.createQuery(query).getResultList();
     * </pre>
     *
     * @param selectString SELECT clause string (e.g., "code, name, productType.name:typeName")
     * @param builder CriteriaBuilder for creating selections
     * @param root Query root
     * @return List of Selection&lt;?&gt; for use with CriteriaQuery.multiselect()
     * @throws rsql.exceptions.SyntaxErrorException if SELECT string is invalid or contains aggregate functions
     */
    public List<Selection<?>> createSelections(
        String selectString,
        CriteriaBuilder builder,
        Root<ENTITY> root
    ) {
        return SimpleQueryExecutor.createSelectionsFromString(
            selectString, builder, root, getQueryContext()
        );
    }

    /**
     * Creates an aggregate query builder from a SELECT string containing aggregate functions.
     * This method is analogous to createSpecification() but for aggregate queries.
     *
     * <p>The SELECT string can include:</p>
     * <ul>
     *   <li>Simple fields for GROUP BY: {@code "productType.name, status"}</li>
     *   <li>Aggregate functions: {@code "COUNT(*):count, SUM(price):total, AVG(price):avg"}</li>
     *   <li>Aliases for all fields: {@code "productType.name:category, COUNT(*):count"}</li>
     *   <li>COUNT(DIST field) for COUNT DISTINCT: {@code "COUNT(DIST productType.id):typeCount"}</li>
     * </ul>
     *
     * <p>The returned {@link AggregateQueryBuilder} provides:</p>
     * <ul>
     *   <li>{@code getSelections()} - SELECT clause selections</li>
     *   <li>{@code getGroupByExpressions()} - GROUP BY clause expressions</li>
     *   <li>{@code createHavingPredicate(havingFilter, compiler)} - HAVING clause predicate</li>
     * </ul>
     *
     * <p>Example usage:</p>
     * <pre>
     * CriteriaBuilder builder = em.getCriteriaBuilder();
     * CriteriaQuery&lt;Tuple&gt; query = builder.createQuery(Tuple.class);
     * Root&lt;Product&gt; root = query.from(Product.class);
     *
     * AggregateQueryBuilder&lt;Product&gt; aggQuery = queryService.createAggregateQuery(
     *     "productType.name:category, COUNT(*):count, SUM(price):total",
     *     builder, root
     * );
     *
     * query.multiselect(aggQuery.getSelections());
     * query.groupBy(aggQuery.getGroupByExpressions());
     * query.where(queryService.createSpecification("status==ACTIVE")
     *     .toPredicate(root, query, builder));
     * query.having(aggQuery.createHavingPredicate("total=gt=50000;count=ge=10", rsqlCompiler));
     *
     * List&lt;Tuple&gt; results = em.createQuery(query).getResultList();
     * </pre>
     *
     * @param selectString Aggregate SELECT clause string (e.g., "category, COUNT(*):count, SUM(price):total")
     * @param builder CriteriaBuilder for creating selections and expressions
     * @param root Query root
     * @return AggregateQueryBuilder with selections, GROUP BY expressions, and HAVING state
     * @throws rsql.exceptions.SyntaxErrorException if SELECT string is invalid
     */
    public AggregateQueryBuilder<ENTITY> createAggregateQuery(
        String selectString,
        CriteriaBuilder builder,
        Root<ENTITY> root
    ) {
        return SimpleQueryExecutor.createAggregateQuery(
            selectString, builder, root, getQueryContext()
        );
    }

    /**
     * Retrieves a list of {@link LovDTO} (List of Values Data Transfer Objects) for the entity,
     * filtered by the provided RSQL filter and paginated according to the given {@link Pageable}.
     * The fields included in the result can be customized by specifying the id, code, and name fields.
     *
     * <p>
     * The method dynamically selects which fields to include in the result based on which of
     * {@code codeField} and {@code nameField} are provided (non-null):
     * <ul>
     *   <li>If both {@code codeField} and {@code nameField} are provided, the result will include
     *       {@code idField}, {@code codeField}, and {@code nameField}.</li>
     *   <li>If only {@code nameField} is provided, the result will include {@code idField} and {@code nameField}.</li>
     *   <li>If only {@code codeField} is provided, the result will include {@code idField} and {@code codeField}.</li>
     *   <li>If neither is provided, only {@code idField} will be included.</li>
     * </ul>
     *
     * @param filter    RSQL filter string to apply to the query.
     * @param pageable  Pagination and sorting information.
     * @param idField   Name of the ID field to include in the result (required).
     * @param codeField Name of the code field to include in the result (optional).
     * @param nameField Name of the name field to include in the result (optional).
     * @return          List of {@link LovDTO} objects matching the filter and field selection.
     */
    @Transactional(readOnly = true)
    public List<LovDTO> getLOV(String filter, Pageable pageable,
                               String idField, String codeField, String nameField) {
        List<String> fieldsList = new java.util.ArrayList<>();
        fieldsList.add(idField);

        // Add optional fields when not null
        if (codeField != null) {
            fieldsList.add(codeField);
        }
        if (nameField != null) {
            fieldsList.add(nameField);
        }

        // Convert to array and execute query
        String[] fields = fieldsList.toArray(new String[0]);
        return getQueryResult(
                entityClass,
                LovDTO.class,
                fields,
                filter,
                pageable,
                getQueryContext(),
                rsqlCompiler
        );
    }

    /**
     * Return a list of values with id, code and name for the entity filtered by a provided filter.
     * @param filter    Filter that will generate where criteria for the query
     * @param pageable  Pageable containing the sort order that will be used
     * @return          List of values in LovDTO form
     */
    @Transactional(readOnly = true)
    public List<LovDTO> getLOV(String filter, Pageable pageable) {
        return getLOV(filter, pageable, "id", "code", "name");
    }

    /**
     * Return a list of values with id, name for the entity filtered by a provided filter.
     * @param filter    Filter that will generate where criteria for the query
     * @param pageable  Pageable containing the sort order that will be used
     * @return          List of values in LovDTO form
     */
    @Transactional(readOnly = true)
    public List<LovDTO> getLOVwithIdAndName(String filter, Pageable pageable) {
        return getQueryResult(
            entityClass,
            LovDTO.class,
            new String[] { "id", "name" },
            filter,
            pageable,
            getQueryContext(),
            rsqlCompiler
        );
    }

    /**
     * Retrieves a list of values (LOV) using SELECT string syntax with support for aliases and navigation properties.
     * This method provides maximum flexibility for LOV queries by allowing any SELECT string format.
     *
     * The SELECT string must specify fields that map to LovDTO constructor:
     * - First field: id (Long)
     * - Second field (optional): code (String)
     * - Third field (optional): name (String)
     *
     * Example usage:
     * <pre>
     * // Basic: id, code, name
     * getLOVWithSelect("id, code, name", "status==ACTIVE", pageable)
     *
     * // With navigation properties: id, parent.code as code, parent.name as name
     * getLOVWithSelect("id, parent.code, parent.name", "status==ACTIVE", pageable)
     *
     * // With aliases for clarity
     * getLOVWithSelect("id, productType.code:code, productType.name:name", "status==ACTIVE", pageable)
     * </pre>
     *
     * @param selectString SELECT clause string specifying id, code, and name fields in that order
     * @param filter RSQL filter string for WHERE clause
     * @param pageable pagination and sorting information
     * @return list of LovDTO objects matching the filter
     */
    @Transactional(readOnly = true)
    public List<LovDTO> getLOVWithSelect(String selectString, String filter, Pageable pageable) {
        return getQueryResultWithSelect(
            entityClass,
            LovDTO.class,
            selectString,
            filter,
            pageable,
            getQueryContext(),
            rsqlCompiler
        );
    }

    /**
     * Generic method to retrieve a list of results using SELECT string syntax with any result class.
     * This is the most flexible query method that supports aliases, navigation properties, and custom result types.
     *
     * The SELECT string fields must match the constructor parameters of the result class in order and type.
     *
     * Example usage:
     * <pre>
     * // Using Tuple.class for flexible column access
     * getSelectResult(Tuple.class, "code:productCode, name, price", "status==ACTIVE", pageable)
     *
     * // Using custom DTO class
     * getSelectResult(ProductSummaryDTO.class, "code, name, price", "status==ACTIVE", pageable)
     *
     * // With navigation properties
     * getSelectResult(Tuple.class, "code, productType.name:typeName", "status==ACTIVE", pageable)
     * </pre>
     *
     * @param <RESULT> the result type class
     * @param resultClass the class of the result objects
     * @param selectString SELECT clause string (e.g., "field1, field2:alias, related.field3")
     * @param filter RSQL filter string for WHERE clause
     * @param pageable pagination and sorting information
     * @return list of result objects matching the filter
     */
    @Transactional(readOnly = true)
    public <RESULT> List<RESULT> getSelectResult(
        Class<RESULT> resultClass,
        String selectString,
        String filter,
        Pageable pageable
    ) {
        return getQueryResultWithSelect(
            entityClass,
            resultClass,
            selectString,
            filter,
            pageable,
            getQueryContext(),
            rsqlCompiler
        );
    }

    /**
     * Generic method to retrieve a paginated list of results using SELECT string syntax with any result class.
     * This is the paginated version of getSelectResult() with the same flexibility.
     *
     * Example usage:
     * <pre>
     * // Using Tuple.class with pagination
     * getSelectResultAsPage(Tuple.class, "code:productCode, name, price", "status==ACTIVE", pageable)
     *
     * // Using custom DTO class with pagination
     * getSelectResultAsPage(ProductSummaryDTO.class, "code, name, price", "status==ACTIVE", pageable)
     * </pre>
     *
     * @param <RESULT> the result type class
     * @param resultClass the class of the result objects
     * @param selectString SELECT clause string (e.g., "field1, field2:alias, related.field3")
     * @param filter RSQL filter string for WHERE clause
     * @param pageable pagination and sorting information
     * @return page of result objects matching the filter
     */
    @Transactional(readOnly = true)
    public <RESULT> Page<RESULT> getSelectResultAsPage(
        Class<RESULT> resultClass,
        String selectString,
        String filter,
        Pageable pageable
    ) {
        if (pageable == null) {
            pageable = PageRequest.of(0, 20);
        }
        return getQueryResultAsPageWithSelect(
            entityClass,
            resultClass,
            selectString,
            filter,
            pageable,
            getQueryContext(),
            rsqlCompiler,
            appObjectRepository
        );
    }

    /**
     * Retrieves a list of tuples based on the provided filter, pageable, and fields.
     *
     * @param filter a string representing the filter condition for the query
     * @param pageable an object specifying pagination and sorting information
     * @param fields an array of field names to include in the result tuple
     * @return a list of tuples matching the specified filter, pageable, and fields
     */
    @Transactional(readOnly = true)
    public List<Tuple> getTuple(String filter, Pageable pageable, String[] fields) {
        return getQueryResult(
            entityClass,
            Tuple.class,
            fields,
            filter,
            pageable,
            getQueryContext(),
            rsqlCompiler
        );
    }

    /**
     * Retrieves a list of tuples using SELECT string syntax with support for aliases and navigation properties.
     * This method provides more flexibility than getTuple() by allowing SELECT string parsing.
     *
     * Example usage:
     * <pre>
     * getTupleWithSelect("code:productCode, name, productType.name:typeName", "status==ACTIVE", pageable)
     * </pre>
     *
     * @param selectString SELECT clause string (e.g., "code, name:productName, productType.name")
     * @param filter RSQL filter string for WHERE clause
     * @param pageable pagination and sorting information
     * @return list of tuples with aliased fields matching the filter
     */
    @Transactional(readOnly = true)
    public List<Tuple> getTupleWithSelect(String selectString, String filter, Pageable pageable) {
        return getQueryResultWithSelect(
            entityClass,
            Tuple.class,
            selectString,
            filter,
            pageable,
            getQueryContext(),
            rsqlCompiler
        );
    }

    /**
     * Retrieves a paginated list of tuples using SELECT string syntax with support for aliases and navigation properties.
     * This method provides pagination support with flexible SELECT string parsing.
     *
     * Example usage:
     * <pre>
     * getTupleAsPageWithSelect("code:productCode, name, productType.name:typeName", "status==ACTIVE", pageable)
     * </pre>
     *
     * @param selectString SELECT clause string (e.g., "code, name:productName, productType.name")
     * @param filter RSQL filter string for WHERE clause
     * @param pageable pagination and sorting information
     * @return page of tuples with aliased fields matching the filter
     */
    @Transactional(readOnly = true)
    public Page<Tuple> getTupleAsPageWithSelect(String selectString, String filter, Pageable pageable) {
        if (pageable == null) {
            pageable = PageRequest.of(0, 20);
        }
        return getQueryResultAsPageWithSelect(
            entityClass,
            Tuple.class,
            selectString,
            filter,
            pageable,
            getQueryContext(),
            rsqlCompiler,
            appObjectRepository
        );
    }

    /**
     * Executes an aggregate query with SELECT string syntax supporting aggregate functions, GROUP BY, and HAVING.
     * Automatically extracts GROUP BY fields from the SELECT string (fields without aggregate functions).
     *
     * Supported aggregate functions: COUNT(*), COUNT(field), SUM(field), AVG(field), MIN(field), MAX(field), COUNT(DIST field)
     *
     * Example usage:
     * <pre>
     * // Group by productType.name with aggregates and HAVING filter
     * getAggregateResult("productType.name:type, COUNT(*):count, SUM(price):total",
     *                    "status==ACTIVE", "total=gt=50000;count=ge=10", pageable)
     *
     * // Multiple GROUP BY fields with HAVING
     * getAggregateResult("productType.code, status, COUNT(*):count", "", "count=gt=5", pageable)
     * </pre>
     *
     * @param selectString SELECT clause with aggregate functions (e.g., "field1, COUNT(*):count, SUM(field2):total")
     * @param filter RSQL filter string for WHERE clause (applied before aggregation)
     * @param havingFilter RSQL filter string for HAVING clause (applied after aggregation, filters groups)
     * @param pageable pagination and sorting information
     * @return list of tuples with aggregated results
     */
    @Transactional(readOnly = true)
    public List<Tuple> getAggregateResult(String selectString, String filter, String havingFilter, Pageable pageable) {
        return getAggregateQueryResultWithSelect(
            entityClass,
            Tuple.class,
            selectString,
            filter,
            havingFilter,
            pageable,
            getQueryContext(),
            rsqlCompiler
        );
    }

    /**
     * Executes a paginated aggregate query with SELECT string syntax supporting aggregate functions,
     * GROUP BY, WHERE, HAVING, and arithmetic expressions. Returns Page with pagination metadata.
     *
     * This method supports both simple aggregate queries and complex arithmetic expressions:
     * - Simple: "productType.name:type, COUNT(*):count, SUM(price):total"
     * - Arithmetic: "productType.name:type, SUM(price) * 1.2:totalWithTax"
     *
     * Supported aggregate functions: COUNT(*), COUNT(field), SUM(field), AVG(field), MIN(field), MAX(field), COUNT(DIST field)
     * Supported arithmetic operators: +, -, *, / (only in aggregate queries)
     *
     * Example usage:
     * <pre>
     * // Simple pagination with aggregates
     * Page&lt;Tuple&gt; page = getAggregateResultAsPage(
     *     "productType.name:type, COUNT(*):count, SUM(price):total",
     *     "status==ACTIVE",
     *     "total=gt=50000;count=ge=10", // HAVING filter
     *     PageRequest.of(0, 10, Sort.by("total").descending())
     * );
     *
     * // With arithmetic expressions
     * Page&lt;Tuple&gt; page2 = getAggregateResultAsPage(
     *     "category, SUM(price) * 1.2:totalWithTax, COUNT(*):count",
     *     "",
     *     null,
     *     PageRequest.of(0, 20)
     * );
     *
     * // Access results
     * System.out.println("Total elements: " + page.getTotalElements());
     * System.out.println("Total pages: " + page.getTotalPages());
     * for (Tuple tuple : page.getContent()) {
     *     System.out.println(tuple.get("type") + ": " + tuple.get("count"));
     * }
     * </pre>
     *
     * @param selectString SELECT clause with aggregate functions and optional arithmetic (e.g., "field1, COUNT(*):count, SUM(field2) * 1.2:total")
     * @param filter RSQL filter string for WHERE clause (applied before aggregation)
     * @param havingFilter RSQL filter string for HAVING clause (applied after aggregation, filters groups)
     * @param pageable pagination and sorting information (offset, limit, sort)
     * @return Page of tuples with aggregated results and pagination metadata
     */
    @Transactional(readOnly = true)
    public Page<Tuple> getAggregateResultAsPage(String selectString, String filter, String havingFilter, Pageable pageable) {
        return getAggregateQueryResultAsPageWithSelect(
            entityClass,
            Tuple.class,
            selectString,
            filter,
            havingFilter,
            pageable,
            getQueryContext(),
            rsqlCompiler
        );
    }

    /**
     * Executes an aggregate query with arithmetic expressions support (returns List without pagination).
     * This method uses SelectExpressionVisitor which supports complex arithmetic operations and HAVING clause.
     *
     * <p><b>Key features:</b></p>
     * <ul>
     *   <li>Supports arithmetic expressions: {@code SUM(debit) - SUM(credit):balance}</li>
     *   <li>Supports HAVING clause for filtering aggregated results</li>
     *   <li>Uses expression-based parsing instead of field-based parsing</li>
     * </ul>
     *
     * <p><b>Example usage:</b></p>
     * <pre>
     * // Calculate balance with arithmetic and filter results
     * List&lt;Tuple&gt; results = queryService.getAggregateResultWithExpressions(
     *     "account.number:accountNumber, SUM(debit) - SUM(credit):balance",
     *     "year==2024",
     *     "balance=gt=0",  // Only positive balances
     *     PageRequest.of(0, 100, Sort.by("accountNumber"))
     * );
     *
     * for (Tuple tuple : results) {
     *     String account = (String) tuple.get("accountNumber");
     *     BigDecimal balance = (BigDecimal) tuple.get("balance");
     *     System.out.println(account + ": " + balance);
     * }
     * </pre>
     *
     * @param selectString SELECT clause with arithmetic expressions (e.g., "field1, SUM(field2) - SUM(field3):diff")
     * @param filter RSQL filter string for WHERE clause (applied before aggregation)
     * @param havingFilter RSQL HAVING filter for filtering aggregated results
     * @param pageable Pagination and sorting (only Sort is used, offset/limit ignored - use getAggregateResultAsPageWithExpressions for pagination)
     * @return List of tuples with aggregated results
     */
    @Transactional(readOnly = true)
    public List<Tuple> getAggregateResultWithExpressions(String selectString, String filter, String havingFilter, Pageable pageable) {
        return getAggregateQueryResultWithSelectExpression(
            entityClass,
            Tuple.class,
            selectString,
            filter,
            havingFilter,
            pageable,
            getQueryContext(),
            rsqlCompiler
        );
    }

    /**
     * Executes a paginated aggregate query with arithmetic expressions support (returns Page with metadata).
     * This method uses SelectExpressionVisitor which supports complex arithmetic operations, HAVING clause, and proper pagination.
     *
     * <p><b>Key features:</b></p>
     * <ul>
     *   <li>✅ Supports arithmetic expressions: {@code SUM(price) * 1.2:totalWithTax}</li>
     *   <li>✅ Full pagination support with metadata (totalElements, totalPages, etc.)</li>
     *   <li>✅ Sorting by SELECT aliases (including arithmetic expression aliases)</li>
     *   <li>✅ Supports HAVING clause for filtering aggregated results</li>
     * </ul>
     *
     * <p><b>Example usage:</b></p>
     * <pre>
     * // Paginated query with arithmetic, HAVING filter, and sorting by alias
     * Page&lt;Tuple&gt; page = queryService.getAggregateResultAsPageWithExpressions(
     *     "productType.name:category, SUM(price) * 1.2:totalWithTax, COUNT(*):count",
     *     "status==ACTIVE",
     *     "totalWithTax=gt=1000",  // Only groups with total > 1000
     *     PageRequest.of(0, 10, Sort.by("totalWithTax").descending())
     * );
     *
     * // Access pagination metadata
     * long totalElements = page.getTotalElements();  // Total number of groups after HAVING
     * int totalPages = page.getTotalPages();
     * List&lt;Tuple&gt; results = page.getContent();
     *
     * for (Tuple tuple : results) {
     *     String category = (String) tuple.get("category");
     *     BigDecimal total = (BigDecimal) tuple.get("totalWithTax");
     *     Long count = (Long) tuple.get("count");
     *     System.out.println(category + ": " + total + " (count: " + count + ")");
     * }
     * </pre>
     *
     * <p><b>When to use this vs getAggregateResultAsPage():</b></p>
     * <ul>
     *   <li>Use this method when you need arithmetic expressions (both methods support HAVING)</li>
     *   <li>Both methods support all aggregate query features</li>
     * </ul>
     *
     * @param selectString SELECT clause with arithmetic expressions (e.g., "field1, SUM(field2) * 1.2:total")
     * @param filter RSQL filter string for WHERE clause (applied before aggregation)
     * @param havingFilter RSQL HAVING filter for filtering aggregated results
     * @param pageable Pagination and sorting (offset, limit, sort by aliases)
     * @return Page of tuples with aggregated results and pagination metadata
     */
    @Transactional(readOnly = true)
    public Page<Tuple> getAggregateResultAsPageWithExpressions(String selectString, String filter, String havingFilter, Pageable pageable) {
        return getAggregateQueryResultAsPageWithSelectExpression(
            entityClass,
            Tuple.class,
            selectString,
            filter,
            havingFilter,
            pageable,
            getQueryContext(),
            rsqlCompiler
        );
    }

    /**
     * Executes a JPQL query with filtering and pagination, and returns the result as a list of DTOs.
     *
     * @param jpqlSelectQuery the JPQL select query string to be executed
     * @param filter the filter string to refine the query results
     * @param page the pagination information, including page number, size, and sort order
     * @return a list of DTOs that match the query and filters
     */
    @Transactional(readOnly = true)
    public List<ENTITY_DTO> getJpqlQueryResult(String jpqlSelectQuery, String filter, Pageable page) {
        String alias = findAliasFromJpqlSelectString(jpqlSelectQuery);
        return SimpleQueryExecutor.getJpqlQueryResult(
            entityClass,
            entityClass,
            jpqlSelectQuery,
            alias, filter,
            page,
            getQueryContext(),
            rsqlCompiler).stream().map(appObjectMapper::toDto).collect(Collectors.toList());
    }

    /**
     * Executes a JPQL select query and retrieves the result as a list of {@code Tuple} objects.
     *
     * @param jpqlSelectQuery the JPQL select query string to be executed
     * @param filter an optional filter to apply to the query
     * @param page the pagination and sorting information for the query
     * @return a list of {@code Tuple} objects containing the results of the query
     */
    @Transactional(readOnly = true)
    public List<Tuple> getJpqlQueryResultAsTuple(String jpqlSelectQuery, String filter, Pageable page) {
        String alias = findAliasFromJpqlSelectString(jpqlSelectQuery);
        return SimpleQueryExecutor.getJpqlQueryResult(
            entityClass,
            Tuple.class,
            jpqlSelectQuery,
            alias, filter,
            page,
            getQueryContext(),
            rsqlCompiler);
    }

    /**
     * Executes the provided JPQL select and count queries with the given filter and pagination information,
     * returning a paginated result transformed to DTOs.
     *
     * @param jpqlSelectQuery the JPQL select query to fetch the entities
     * @param jpqlCountQuery the JPQL count query to calculate the total number of entities
     * @param filter a filter string to apply additional conditions to the query results
     * @param page the pagination information encapsulating page number, size, and sorting
     * @return a paginated result of DTOs wrapping the entities fetched using the JPQL query
     */
    @Transactional(readOnly = true)
    public Page<ENTITY_DTO> getJpqlQueryResultAsPage(String jpqlSelectQuery, String jpqlCountQuery, String filter, Pageable page) {
        Page<ENTITY>  entityPage = SimpleQueryExecutor.getJpqlQueryResultAsPage(
            entityClass,
            entityClass,
            jpqlSelectQuery,
                selectAlias, jpqlCountQuery,
                countAlias, filter,
            page,
            getQueryContext(),
            rsqlCompiler);

        return entityPage.map(appObjectMapper::toDto);
    }

    /**
     * Executes a JPQL query with the given filter and pagination, returning entity objects.
     * 
     * @param jpqlSelectQuery The JPQL select query to execute
     * @param filter The RSQL filter to apply
     * @param page Pagination information
     * @return List of entity objects matching the query and filter
     */
    @Transactional(readOnly = true)
    public List<ENTITY> getJpqlQueryEntities(String jpqlSelectQuery, String filter, Pageable page) {
        String alias = findAliasFromJpqlSelectString(jpqlSelectQuery);
        return SimpleQueryExecutor.getJpqlQueryResult(
            entityClass,
            entityClass,
            jpqlSelectQuery,
            alias, filter,
            page,
            getQueryContext(),
            rsqlCompiler);
    }

    /**
     * Return a list of values as maps for the entity filtered by a provided filter.
     * Each map contains field names as keys and their values.
     * @param filter    Filter that will generate where criteria for the query
     * @param pageable  Pageable containing the sort order that will be used
     * @param fields    Entity fields to return in the map
     * @return          List of maps with field-value pairs
     */
    @Transactional(readOnly = true)
    public List<Map<String, Object>> getResultAsMap(String filter, Pageable pageable, String... fields) {
        List<Object[]> results = SimpleQueryExecutor.getQueryResult(
                entityClass,
                Object[].class,
                fields,
                filter,
                pageable,
                getQueryContext(),
                rsqlCompiler
        );
        List<Map<String, Object>> mappedResults = new java.util.ArrayList<>();
        for (Object[] row : results) {
            java.util.Map<String, Object> map = new java.util.HashMap<>();
            for (int i = 0; i < fields.length; i++) {
                map.put(fields[i], row[i]);
            }
            mappedResults.add(map);
        }
        return mappedResults;
    }

    /**
     * Extracts the alias used in a JPQL select string. If no alias is found,
     * the method returns a default alias value.
     *
     * @param jpqlSelect the JPQL select string to parse for the alias; must not be null or empty.
     * @return the alias name found within the JPQL string, or "a0" if no alias is detected
     *         or an exception occurs during parsing.
     */
    public String findAliasFromJpqlSelectString(String jpqlSelect) {
        String alias = "a0"; // pretpostavljeni defaultni alias

        if (jpqlSelect != null && !jpqlSelect.trim().isEmpty()) {
            try {
                // Kreiranje TypedQuery objekta
                TypedQuery<ENTITY> query = entityManager.createQuery(jpqlSelect, entityClass);
                String jpql = query.unwrap(org.hibernate.query.Query.class).getQueryString();

                // Regularni izraz za pronalaženje aliasa nakon 'FROM' klauzule
                Pattern pattern = Pattern.compile(" from \\s+\\w+\\s+(\\w+)", Pattern.CASE_INSENSITIVE);
                Matcher matcher = pattern.matcher(jpql);

                if (matcher.find()) {
                    // Ako se pronađe odgovarajući pattern, uzima se prva grupa kao alias
                    alias = matcher.group(1);
                }
            } catch (Exception e) {
                // U slučaju greške, vraća se defaultni alias
                // Ovdje možete logirati ili obraditi grešku
            }
        }

        return "a0";
    }

}

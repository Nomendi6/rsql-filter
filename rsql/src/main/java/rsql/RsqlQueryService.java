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
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
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
        this.rsqlContext.root.alias(this.selectAlias);
    }

    public RsqlQueryService(REPOS appObjectRepository, MAPPER appObjectMapper, EntityManager entityManager, Class<ENTITY> entityClass, String jpqlSelectAllFromEntity, String jpqlSelectCountFromEntity) {
        this.appObjectRepository = appObjectRepository;
        this.appObjectMapper = appObjectMapper;
        this.entityManager = entityManager;
        this.rsqlContext = new RsqlContext<>(entityClass).defineEntityManager(entityManager);
        this.entityClass = entityClass;

        this.jpqlSelectAllFromEntity = jpqlSelectAllFromEntity;
        this.jpqlSelectCountFromEntity = jpqlSelectCountFromEntity;
        this.rsqlContext.root.alias(this.selectAlias);
        this.useJpqlSelect = true;
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
     *
     * @param selectAlias the alias to be used for the select query as a String
     */
    public void setSelectAlias(String selectAlias) {
        this.selectAlias = selectAlias;
        this.rsqlContext.root.alias(this.selectAlias);
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
                rsqlContext,
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
                rsqlContext,
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
                    rsqlContext,
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
                    rsqlContext,
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
                rsqlContext,
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
                rsqlContext,
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
                rsqlContext,
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
        return rsqlCompiler.compileToSpecification(filter, rsqlContext);
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
                rsqlContext,
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
            rsqlContext,
            rsqlCompiler
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
            rsqlContext,
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
            rsqlContext,
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
            rsqlContext,
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
            rsqlContext,
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
            rsqlContext,
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
                rsqlContext,
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

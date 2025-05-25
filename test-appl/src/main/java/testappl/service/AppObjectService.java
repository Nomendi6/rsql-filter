// Template: EntityServiceImpl|v3.1
// Security type: none
package testappl.service;

import jakarta.persistence.EntityManager;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rsql.RsqlQueryService;
import testappl.domain.AppObject;
import testappl.repository.AppObjectRepository;
import testappl.service.dto.AppObjectDTO;
import testappl.service.mapper.AppObjectMapper;

/**
 * Service Implementation for managing {@link AppObject}.
 */
@Service
@Transactional
public class AppObjectService {

    private final Logger log = LoggerFactory.getLogger(AppObjectService.class);

    private final AppObjectRepository appObjectRepository;

    private final AppObjectMapper appObjectMapper;

    private final EntityManager entityManager;

    private RsqlQueryService<AppObject, AppObjectDTO, AppObjectRepository, AppObjectMapper> queryService;

    public AppObjectService(AppObjectRepository appObjectRepository, AppObjectMapper appObjectMapper, EntityManager entityManager) {
        this.appObjectRepository = appObjectRepository;
        this.appObjectMapper = appObjectMapper;
        this.entityManager = entityManager;
        this.queryService = new RsqlQueryService<>(appObjectRepository, appObjectMapper, entityManager, AppObject.class);
    }

    /**
     * Save a appObject.
     *
     * @param appObjectDTO the entity to save.
     * @return the persisted entity.
     */
    public AppObjectDTO save(AppObjectDTO appObjectDTO) {
        log.debug("Request to save AppObject : {}", appObjectDTO);
        AppObject appObject = appObjectMapper.toEntity(appObjectDTO);
        appObject = appObjectRepository.save(appObject);
        return appObjectMapper.toDto(appObject);
    }

    /**
     * Partially update a appObject.
     *
     * @param appObjectDTO the entity to update partially.
     * @return the persisted entity.
     */
    public Optional<AppObjectDTO> partialUpdate(AppObjectDTO appObjectDTO) {
        log.debug("Request to partially update AppObject : {}", appObjectDTO);

        return appObjectRepository
            .findById(appObjectDTO.getId())
            .map(existingAppObject -> {
                appObjectMapper.partialUpdate(existingAppObject, appObjectDTO);

                return existingAppObject;
            })
            .map(appObjectRepository::save)
            .map(appObjectMapper::toDto);
    }

    /**
     * Get all the appObjects.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    @Transactional(readOnly = true)
    public Page<AppObjectDTO> findAll(Pageable pageable) {
        log.debug("Request to get all AppObjects");
        return appObjectRepository.findAll(pageable).map(appObjectMapper::toDto);
    }

    /**
     * Get all the appObjects with eager load of many-to-many relationships.
     *
     * @return the list of entities.
     */
    public Page<AppObjectDTO> findAllWithEagerRelationships(Pageable pageable) {
        return appObjectRepository.findAllWithEagerRelationships(pageable).map(appObjectMapper::toDto);
    }

    /**
     * Get one appObject by id.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    @Transactional(readOnly = true)
    public Optional<AppObjectDTO> findOne(Long id) {
        log.debug("Request to get AppObject : {}", id);
        return appObjectRepository.findOneWithEagerRelationships(id).map(appObjectMapper::toDto);
    }

    /**
     * Delete the appObject by id.
     *
     * @param id the id of the entity.
     */
    public void delete(Long id) {
        log.debug("Request to delete AppObject : {}", id);
        appObjectRepository.deleteById(id);
    }

    /**
     * Revert the attributes that are not allowed to be changed
     *
     * @param updated   Updated AppObjectDTO which attributes has to be reverted
     * @param existing  Existing AppObjectDTO
     */
    public void revertUnUpdatableAttributes(AppObjectDTO updated, AppObjectDTO existing) {
        updated.setCreatedBy(existing.getCreatedBy());
        updated.setCreatedDate(existing.getCreatedDate());
    }

    /**
     * Find all entities by filter with pagination.
     *
     * @param filter the filter which the requested entities should match.
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    public Page<AppObjectDTO> findAll(String filter, Pageable pageable) {
        log.debug("Request to get all AppObjects by filter: {}", filter);
        return getQueryService().findByFilter(filter, pageable);
    }

    /**
     * Find all entities by filter and sort.
     *
     * @param filter the filter which the requested entities should match.
     * @param pageable the sort information.
     * @return the list of entities.
     */
    public List<AppObjectDTO> findByFilterAndSort(String filter, Pageable pageable) {
        log.debug("Request to get a list of all AppObjects by filter: {}", filter);
        return getQueryService().findByFilterAndSort(filter, pageable);
    }

    /**
     * Get entities as a list of values (id, code, name).
     *
     * @param filter the filter which the requested entities should match.
     * @param pageable the sort information.
     * @param idField the id field name.
     * @param codeField the code field name.
     * @param nameField the name field name.
     * @return the list of LOV DTOs.
     */
    public List<Map<String, Object>> getLOV(String filter, Pageable pageable, String idField, String codeField, String nameField) {
        log.debug("Request to get LOV AppObjects by filter: {}", filter);
        return getQueryService().getResultAsMap(filter, pageable, idField, codeField, nameField);
    }

    /**
     * Count all entities by filter.
     *
     * @param filter the filter which the requested entities should match.
     * @return the count of entities.
     */
    public Long countByFilter(String filter) {
        log.debug("Request to count AppObjects by filter: {}", filter);
        return getQueryService().countByFilter(filter);
    }

    /**
     * Return a rsqlQueryService used for executing queries with rsql filters.
     *
     * @return RsqlQueryService
     */
    public RsqlQueryService<AppObject, AppObjectDTO, AppObjectRepository, AppObjectMapper> getQueryService() {
        if (this.queryService == null) {
            this.queryService = new RsqlQueryService<>(appObjectRepository, appObjectMapper, entityManager, AppObject.class);
        }
        return this.queryService;
    }
}

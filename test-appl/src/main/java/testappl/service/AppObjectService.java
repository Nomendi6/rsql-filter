package testappl.service;

import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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

    public AppObjectService(AppObjectRepository appObjectRepository, AppObjectMapper appObjectMapper) {
        this.appObjectRepository = appObjectRepository;
        this.appObjectMapper = appObjectMapper;
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
     * Update a appObject.
     *
     * @param appObjectDTO the entity to save.
     * @return the persisted entity.
     */
    public AppObjectDTO update(AppObjectDTO appObjectDTO) {
        log.debug("Request to update AppObject : {}", appObjectDTO);
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
}

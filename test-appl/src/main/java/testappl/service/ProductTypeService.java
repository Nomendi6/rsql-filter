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
import testappl.domain.ProductType;
import testappl.repository.ProductTypeRepository;
import testappl.service.dto.ProductTypeDTO;
import testappl.service.mapper.ProductTypeMapper;

/**
 * Service Implementation for managing {@link ProductType}.
 */
@Service
@Transactional
public class ProductTypeService {

    private final Logger log = LoggerFactory.getLogger(ProductTypeService.class);

    private final ProductTypeRepository productTypeRepository;

    private final ProductTypeMapper productTypeMapper;

    private final EntityManager entityManager;

    private RsqlQueryService<ProductType, ProductTypeDTO, ProductTypeRepository, ProductTypeMapper> queryService;

    public ProductTypeService(
        ProductTypeRepository productTypeRepository,
        ProductTypeMapper productTypeMapper,
        EntityManager entityManager
    ) {
        this.productTypeRepository = productTypeRepository;
        this.productTypeMapper = productTypeMapper;
        this.entityManager = entityManager;
        this.queryService = new RsqlQueryService<>(productTypeRepository, productTypeMapper, entityManager, ProductType.class);
    }

    /**
     * Save a productType.
     *
     * @param productTypeDTO the entity to save.
     * @return the persisted entity.
     */
    public ProductTypeDTO save(ProductTypeDTO productTypeDTO) {
        log.debug("Request to save ProductType : {}", productTypeDTO);
        ProductType productType = productTypeMapper.toEntity(productTypeDTO);
        productType = productTypeRepository.save(productType);
        return productTypeMapper.toDto(productType);
    }

    /**
     * Partially update a productType.
     *
     * @param productTypeDTO the entity to update partially.
     * @return the persisted entity.
     */
    public Optional<ProductTypeDTO> partialUpdate(ProductTypeDTO productTypeDTO) {
        log.debug("Request to partially update ProductType : {}", productTypeDTO);

        return productTypeRepository
            .findById(productTypeDTO.getId())
            .map(existingProductType -> {
                productTypeMapper.partialUpdate(existingProductType, productTypeDTO);

                return existingProductType;
            })
            .map(productTypeRepository::save)
            .map(productTypeMapper::toDto);
    }

    /**
     * Get all the productTypes.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    @Transactional(readOnly = true)
    public Page<ProductTypeDTO> findAll(Pageable pageable) {
        log.debug("Request to get all ProductTypes");
        return productTypeRepository.findAll(pageable).map(productTypeMapper::toDto);
    }

    /**
     * Get one productType by id.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    @Transactional(readOnly = true)
    public Optional<ProductTypeDTO> findOne(Long id) {
        log.debug("Request to get ProductType : {}", id);
        return productTypeRepository.findById(id).map(productTypeMapper::toDto);
    }

    /**
     * Delete the productType by id.
     *
     * @param id the id of the entity.
     */
    public void delete(Long id) {
        log.debug("Request to delete ProductType : {}", id);
        productTypeRepository.deleteById(id);
    }

    /**
     * Revert the attributes that are not allowed to be changed
     *
     * @param updated   Updated ProductTypeDTO which attributes has to be reverted
     * @param existing  Existing ProductTypeDTO
     */
    public void revertUnUpdatableAttributes(ProductTypeDTO updated, ProductTypeDTO existing) {
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
    public Page<ProductTypeDTO> findAll(String filter, Pageable pageable) {
        log.debug("Request to get all ProductTypes by filter: {}", filter);
        return getQueryService().findByFilter(filter, pageable);
    }

    /**
     * Find all entities by filter and sort.
     *
     * @param filter the filter which the requested entities should match.
     * @param pageable the sort information.
     * @return the list of entities.
     */
    public List<ProductTypeDTO> findByFilterAndSort(String filter, Pageable pageable) {
        log.debug("Request to get a list of all ProductTypes by filter: {}", filter);
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
        log.debug("Request to get LOV ProductTypes by filter: {}", filter);
        return getQueryService().getResultAsMap(filter, pageable, idField, codeField, nameField);
    }

    /**
     * Count all entities by filter.
     *
     * @param filter the filter which the requested entities should match.
     * @return the count of entities.
     */
    public Long countByFilter(String filter) {
        log.debug("Request to count ProductTypes by filter: {}", filter);
        return getQueryService().countByFilter(filter);
    }

    /**
     * Return a rsqlQueryService used for executing queries with rsql filters.
     *
     * @return RsqlQueryService
     */
    public RsqlQueryService<ProductType, ProductTypeDTO, ProductTypeRepository, ProductTypeMapper> getQueryService() {
        if (this.queryService == null) {
            this.queryService = new RsqlQueryService<>(productTypeRepository, productTypeMapper, entityManager, ProductType.class);
        }
        return this.queryService;
    }
}

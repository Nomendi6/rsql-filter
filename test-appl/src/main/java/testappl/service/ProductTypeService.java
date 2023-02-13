package testappl.service;

import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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

    public ProductTypeService(ProductTypeRepository productTypeRepository, ProductTypeMapper productTypeMapper) {
        this.productTypeRepository = productTypeRepository;
        this.productTypeMapper = productTypeMapper;
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
     * Update a productType.
     *
     * @param productTypeDTO the entity to save.
     * @return the persisted entity.
     */
    public ProductTypeDTO update(ProductTypeDTO productTypeDTO) {
        log.debug("Request to update ProductType : {}", productTypeDTO);
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
}

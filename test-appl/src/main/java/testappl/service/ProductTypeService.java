package testappl.service;

import java.util.List;
import java.util.Optional;

import jakarta.persistence.EntityManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rsql.RsqlQueryService;
import testappl.domain.ProductType;
import testappl.repository.ProductTypeRepository;
import testappl.service.dto.ProductTypeDTO;
import testappl.service.mapper.ProductTypeMapper;

/**
 * Service Implementation for managing {@link testappl.domain.ProductType}.
 */
@Service
@Transactional
public class ProductTypeService {

    private final Logger log = LoggerFactory.getLogger(ProductTypeService.class);

    private final ProductTypeRepository productTypeRepository;

    private final ProductTypeMapper productTypeMapper;

    private final EntityManager entityManager;

    private RsqlQueryService<ProductType, ProductTypeDTO, ProductTypeRepository, ProductTypeMapper> queryService;

    private String jpqlSelectAll = "SELECT a0 FROM ProductType a0";

    private String jpqlSelectAllCount = "SELECT count(a0) FROM ProductType a0";

    public ProductTypeService(ProductTypeRepository productTypeRepository, ProductTypeMapper productTypeMapper, EntityManager entityManager) {
        this.productTypeRepository = productTypeRepository;
        this.productTypeMapper = productTypeMapper;
        this.entityManager = entityManager;
        this.queryService = new RsqlQueryService<>(productTypeRepository, productTypeMapper, this.entityManager, ProductType.class, jpqlSelectAll, jpqlSelectAllCount);
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

    public RsqlQueryService<ProductType, ProductTypeDTO, ProductTypeRepository, ProductTypeMapper> getQueryService() {
        if (this.queryService == null) {
            this.queryService = new RsqlQueryService<>(productTypeRepository, productTypeMapper, this.entityManager, ProductType.class, jpqlSelectAll, jpqlSelectAllCount);
        }
        return this.queryService;
    }

    public void testRsql() {

        // test find by filter and return list

        Pageable pageable = PageRequest.of(0,1000, Sort.by("name").ascending());

/*
        List<ProductTypeDTO> list1 = this.getQueryService().findByFilter("name=*'a*'");
        Long counted = this.queryService.countByFilter("name=*'a*'");
        Page<ProductTypeDTO> page1 = this.getQueryService().findByFilter("name=*'a*'", pageable);
        Page<ProductTypeDTO> page2 = this.getQueryService().findByFilter("name=*'a*'", Pageable.unpaged());
        List<ProductTypeDTO> byFilterAndSort = this.getQueryService().findByFilterAndSort("name=*'a*'", pageable);
*/

        this.getQueryService().findAliasFromJpqlSelectString("SELECT productType FROM ProductType productType");
        // test sa drugim jpqlom
        String selectAll = "SELECT new ProductType(pt.id, pt.code, pt.name, pt.description) FROM ProductType pt";
        List<ProductTypeDTO> list2 = this.getQueryService().getJpqlQueryResult(selectAll, "name=*'a*'", pageable);
        System.out.println("list2 = " + list2);

    }
}

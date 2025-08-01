// Template: EntityServiceImpl|v3.1
// Security type: none
package com.nomendi6.rsql.demo.service;

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
import com.nomendi6.rsql.demo.domain.Product;
import com.nomendi6.rsql.demo.repository.ProductRepository;
import com.nomendi6.rsql.demo.service.dto.ProductDTO;
import com.nomendi6.rsql.demo.service.mapper.ProductMapper;

/**
 * Service Implementation for managing {@link Product}.
 */
@Service
@Transactional
public class ProductService {

    private final Logger log = LoggerFactory.getLogger(ProductService.class);

    private final ProductRepository productRepository;

    private final ProductMapper productMapper;

    private final EntityManager entityManager;

    private RsqlQueryService<Product, ProductDTO, ProductRepository, ProductMapper> queryService;

    public ProductService(ProductRepository productRepository, ProductMapper productMapper, EntityManager entityManager) {
        this.productRepository = productRepository;
        this.productMapper = productMapper;
        this.entityManager = entityManager;
        this.queryService = new RsqlQueryService<>(productRepository, productMapper, entityManager, Product.class);
    }

    /**
     * Save a product.
     *
     * @param productDTO the entity to save.
     * @return the persisted entity.
     */
    public ProductDTO save(ProductDTO productDTO) {
        log.debug("Request to save Product : {}", productDTO);
        Product product = productMapper.toEntity(productDTO);
        product = productRepository.save(product);
        return productMapper.toDto(product);
    }

    /**
     * Partially update a product.
     *
     * @param productDTO the entity to update partially.
     * @return the persisted entity.
     */
    public Optional<ProductDTO> partialUpdate(ProductDTO productDTO) {
        log.debug("Request to partially update Product : {}", productDTO);

        return productRepository
            .findById(productDTO.getId())
            .map(existingProduct -> {
                productMapper.partialUpdate(existingProduct, productDTO);

                return existingProduct;
            })
            .map(productRepository::save)
            .map(productMapper::toDto);
    }

    /**
     * Get all the products.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    @Transactional(readOnly = true)
    public Page<ProductDTO> findAll(Pageable pageable) {
        log.debug("Request to get all Products");
        return productRepository.findAll(pageable).map(productMapper::toDto);
    }

    /**
     * Get all the products with eager load of many-to-many relationships.
     *
     * @return the list of entities.
     */
    public Page<ProductDTO> findAllWithEagerRelationships(Pageable pageable) {
        return productRepository.findAllWithEagerRelationships(pageable).map(productMapper::toDto);
    }

    /**
     * Get one product by id.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    @Transactional(readOnly = true)
    public Optional<ProductDTO> findOne(Long id) {
        log.debug("Request to get Product : {}", id);
        return productRepository.findOneWithEagerRelationships(id).map(productMapper::toDto);
    }

    /**
     * Delete the product by id.
     *
     * @param id the id of the entity.
     */
    public void delete(Long id) {
        log.debug("Request to delete Product : {}", id);
        productRepository.deleteById(id);
    }

    /**
     * Revert the attributes that are not allowed to be changed
     *
     * @param updated   Updated ProductDTO which attributes has to be reverted
     * @param existing  Existing ProductDTO
     */
    public void revertUnUpdatableAttributes(ProductDTO updated, ProductDTO existing) {
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
    public Page<ProductDTO> findAll(String filter, Pageable pageable) {
        log.debug("Request to get all Products by filter: {}", filter);
        return getQueryService().findByFilter(filter, pageable);
    }

    /**
     * Find all entities by filter and sort.
     *
     * @param filter the filter which the requested entities should match.
     * @param pageable the sort information.
     * @return the list of entities.
     */
    public List<ProductDTO> findByFilterAndSort(String filter, Pageable pageable) {
        log.debug("Request to get a list of all Products by filter: {}", filter);
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
        log.debug("Request to get LOV Products by filter: {}", filter);
        return getQueryService().getResultAsMap(filter, pageable, idField, codeField, nameField);
    }

    /**
     * Count all entities by filter.
     *
     * @param filter the filter which the requested entities should match.
     * @return the count of entities.
     */
    public Long countByFilter(String filter) {
        log.debug("Request to count Products by filter: {}", filter);
        return getQueryService().countByFilter(filter);
    }

    /**
     * Return a rsqlQueryService used for executing queries with rsql filters.
     *
     * @return RsqlQueryService
     */
    public RsqlQueryService<Product, ProductDTO, ProductRepository, ProductMapper> getQueryService() {
        if (this.queryService == null) {
            this.queryService = new RsqlQueryService<>(productRepository, productMapper, entityManager, Product.class);
        }
        return this.queryService;
    }
}

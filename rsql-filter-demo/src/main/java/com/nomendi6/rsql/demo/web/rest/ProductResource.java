// Template: EntityResource | v4.2
// Security is enabled on this entity
// Security type = none
package com.nomendi6.rsql.demo.web.rest;

import static java.net.URLDecoder.*;

import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import tech.jhipster.web.util.PaginationUtil;
import tech.jhipster.web.util.ResponseUtil;
import com.nomendi6.rsql.demo.repository.ProductRepository;
import com.nomendi6.rsql.demo.service.ProductService;
import com.nomendi6.rsql.demo.service.dto.ProductDTO;
import com.nomendi6.rsql.demo.web.rest.errors.BadRequestAlertException;
import com.nomendi6.rsql.demo.web.rest.util.HeaderUtil;
import com.nomendi6.rsql.demo.web.rest.util.ListHeaderUtil;

/**
 * REST controller for managing {@link com.nomendi6.rsql.demo.domain.Product}.
 */
@RestController
@RequestMapping("/api")
public class ProductResource {

    private final Logger log = LoggerFactory.getLogger(ProductResource.class);

    // private static final String ENTITY_NAME = "product";
    private static final String ENTITY_NAME = "product";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final ProductService productService;

    private final ProductRepository productRepository;

    public ProductResource(ProductService productService, ProductRepository productRepository) {
        this.productService = productService;
        this.productRepository = productRepository;
    }

    /**
     * {@code POST  /product} : Create a new product.
     *
     * @param productDTO the productDTO to create.
     * @param entityName the entity name from the request header.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new productDTO, or with status {@code 400 (Bad Request)} if the product has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("/product")
    public ResponseEntity<ProductDTO> createProduct(
        @Valid @RequestBody ProductDTO productDTO,
        @RequestHeader(value = "X-ENTITY-NAME", required = false, defaultValue = ENTITY_NAME) String entityName
    ) throws URISyntaxException {
        log.debug("REST request to save Product : {}", productDTO);
        if (productDTO.getId() != null) {
            throw new BadRequestAlertException("A new product cannot already have an ID", entityName, "idexists");
        }
        ProductDTO result = productService.save(productDTO);
        return ResponseEntity.created(new URI("/api/product/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(entityName, result.getName()))
            .body(result);
    }

    /**
     * {@code PUT /product/:id } : Updates an existing product.
     *
     * @param id the id of the productDTO to save.
     * @param productDTO the productDTO to update.
     * @param entityName the entity name from the request header.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated productDTO,
     * or with status {@code 400 (Bad Request)} if the productDTO is not valid,
     * or with status {@code 500 (Internal Server Error)} if the productDTO couldn't be updated.
     */
    @PutMapping("/product/{id}")
    public ResponseEntity<ProductDTO> updateProduct(
        @PathVariable(value = "id", required = false) final Long id,
        @Valid @RequestBody ProductDTO productDTO,
        @RequestHeader(value = "X-ENTITY-NAME", required = false, defaultValue = ENTITY_NAME) String entityName
    ) {
        log.debug("REST request to update Product : {}, {}", id, productDTO);
        if (productDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", entityName, "idnull");
        }
        if (!Objects.equals(id, productDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", entityName, "idinvalid");
        }
        if (!productRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", entityName, "idnotfound");
        }

        ProductDTO existing = productService
            .findOne(productDTO.getId())
            .orElseThrow(() -> new BadRequestAlertException("Invalid id", entityName, "idnotfound"));
        productService.revertUnUpdatableAttributes(productDTO, existing);
        ProductDTO result = productService.save(productDTO);
        return ResponseEntity.ok().headers(HeaderUtil.createEntityUpdateAlert(entityName, result.getName())).body(result);
    }

    /**
     * {@code PATCH  /product/:id} : Partial updates given fields of an existing product, field will ignore if it is null
     *
     * @param id the id of the productDTO to save.
     * @param productDTO the productDTO to update.
     * @param entityName the entity name from the request header.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated productDTO,
     * or with status {@code 400 (Bad Request)} if the productDTO is not valid,
     * or with status {@code 404 (Not Found)} if the productDTO is not found,
     * or with status {@code 500 (Internal Server Error)} if the productDTO couldn't be updated.
     */
    @PatchMapping(value = "/product/{id}", consumes = { "application/json", "application/merge-patch+json" })
    public ResponseEntity<ProductDTO> partialUpdateProduct(
        @PathVariable(value = "id", required = false) final Long id,
        @NotNull @RequestBody ProductDTO productDTO,
        @RequestHeader(value = "X-ENTITY-NAME", required = false, defaultValue = ENTITY_NAME) String entityName
    ) {
        log.debug("REST request to partial update Product partially : {}, {}", id, productDTO);
        if (productDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", entityName, "idnull");
        }
        if (!Objects.equals(id, productDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", entityName, "idinvalid");
        }

        if (!productRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", entityName, "idnotfound");
        }

        Optional<ProductDTO> result = productService.partialUpdate(productDTO);

        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(entityName, result.map(dto -> dto.getName()).orElse(null))
        );
    }

    /**
     * {@code GET  /product} : get all the products.
     *
     * @param pageable the pagination information.
     * @param filter the filter which the requested entities should match.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of products in body.
     */
    @GetMapping("/product")
    public ResponseEntity<List<ProductDTO>> getAllProducts(
        @Parameter(
            name = "filter",
            description = "Rsql criteria filter. Example: name=='Some text' or status==#active# (more examples on https://github.com/SpiralUp/rsql-grammar)"
        ) @RequestParam(value = "filter", required = false) String filter,
        @org.springdoc.core.annotations.ParameterObject Pageable pageable
    ) {
        if (filter != null) {
            filter = decode(filter, StandardCharsets.UTF_8);
        }

        log.debug("REST request to get a pageable list of all Products by filter: {}", filter);
        Page<ProductDTO> page = productService.findAll(filter, pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    /**
     * {@code GET  /product/all} : get all the products.
     *
     * @param filter the filter which the requested entities should match.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of products in body.
     */
    @GetMapping("/product/all")
    public ResponseEntity<List<ProductDTO>> getAllProductsAsList(
        @Parameter(
            name = "filter",
            description = "Rsql criteria filter. Example: name=='Some text' or status==#active# (more examples on https://github.com/SpiralUp/rsql-grammar)"
        ) @RequestParam(value = "filter", required = false) String filter,
        @org.springdoc.core.annotations.ParameterObject Pageable pageable
    ) {
        if (filter != null) {
            filter = decode(filter, StandardCharsets.UTF_8);
        }

        log.debug("REST request to get a list of all Products by filter: {}", filter);
        List<ProductDTO> list = productService.findByFilterAndSort(filter, pageable);
        HttpHeaders headers = ListHeaderUtil.generateListHttpHeaders(
            ServletUriComponentsBuilder.fromCurrentRequest().toUriString(),
            filter,
            list.size()
        );
        return ResponseEntity.ok().headers(headers).body(list);
    }

    /**
     * {@code GET  /product/lov} : get products as a list of values (id, code, name).
     *
     * @param filter the filter which the requested entities should match.
     * @param pageable  the sort criteria
     * @return          the {@link ResponseEntity} with status {@code 200 (OK)} and list of maps with id, code, name values in the body.
     */
    @GetMapping("/product/lov")
    public ResponseEntity<List<Map<String, Object>>> getLovProducts(
        @Parameter(
            name = "filter",
            description = "Rsql criteria filter. Example: name=='Some text' or status==#active# (more examples on https://github.com/SpiralUp/rsql-grammar)"
        ) @RequestParam(value = "filter", required = false) String filter,
        @org.springdoc.core.annotations.ParameterObject Pageable pageable
    ) {
        if (filter != null) {
            filter = decode(filter, StandardCharsets.UTF_8);
        }

        log.debug("REST request to get LOV Products by filter: {}", filter);
        List<Map<String, Object>> list = productService.getLOV(filter, pageable, "id", "code", "name");
        return ResponseEntity.ok().body(list);
    }

    /**
     * {@code GET  /product/count} : count all the products.
     *
     * @param filter the filter which the requested entities should match.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the count in body.
     */
    @GetMapping("/product/count")
    public ResponseEntity<Long> countProducts(
        @Parameter(
            name = "filter",
            description = "Rsql criteria filter. Example: name=='Some text' or status==#active# (more examples on https://github.com/SpiralUp/rsql-grammar)",
            example = "name=='Some text' or status==#active#"
        ) @RequestParam(value = "filter", required = false) String filter
    ) {
        if (filter != null) {
            filter = decode(filter, StandardCharsets.UTF_8);
        }

        log.debug("REST request to count Products by filter: {}", filter);
        return ResponseEntity.ok().body(productService.countByFilter(filter));
    }

    /**
     * {@code GET  /product/:id} : get the "id" product.
     *
     * @param id the id of the productDTO to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the productDTO, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/product/{id}")
    public ResponseEntity<ProductDTO> getProduct(@PathVariable(value = "id") Long id) {
        log.debug("REST request to get Product : {}", id);
        Optional<ProductDTO> productDTO = productService.findOne(id);
        return ResponseUtil.wrapOrNotFound(productDTO);
    }

    /**
     * {@code DELETE  /product/:id} : delete the "id" product.
     *
     * @param id the id of the productDTO to delete.
     * @param entityName the entity name from the request header.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/product/{id}")
    public ResponseEntity<Void> deleteProduct(
        @PathVariable(value = "id") Long id,
        @RequestHeader(value = "X-ENTITY-NAME", required = false, defaultValue = ENTITY_NAME) String entityName
    ) {
        log.debug("REST request to delete Product : {}", id);
        ProductDTO existing = productService
            .findOne(id)
            .orElseThrow(() -> new BadRequestAlertException("Invalid id", entityName, "idnotfound"));
        productService.delete(id);
        return ResponseEntity.noContent().headers(HeaderUtil.createEntityDeletionAlert(entityName, existing.getName())).build();
    }
}

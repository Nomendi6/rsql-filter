// Template: EntityResource | v4.2
// Security is enabled on this entity
// Security type = none
package testappl.web.rest;

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
import testappl.repository.ProductTypeRepository;
import testappl.service.ProductTypeService;
import testappl.service.dto.ProductTypeDTO;
import testappl.web.rest.errors.BadRequestAlertException;
import testappl.web.rest.util.HeaderUtil;
import testappl.web.rest.util.ListHeaderUtil;

/**
 * REST controller for managing {@link testappl.domain.ProductType}.
 */
@RestController
@RequestMapping("/api")
public class ProductTypeResource {

    private final Logger log = LoggerFactory.getLogger(ProductTypeResource.class);

    // private static final String ENTITY_NAME = "productType";
    private static final String ENTITY_NAME = "productType";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final ProductTypeService productTypeService;

    private final ProductTypeRepository productTypeRepository;

    public ProductTypeResource(ProductTypeService productTypeService, ProductTypeRepository productTypeRepository) {
        this.productTypeService = productTypeService;
        this.productTypeRepository = productTypeRepository;
    }

    /**
     * {@code POST  /product-type} : Create a new productType.
     *
     * @param productTypeDTO the productTypeDTO to create.
     * @param entityName the entity name from the request header.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new productTypeDTO, or with status {@code 400 (Bad Request)} if the productType has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("/product-type")
    public ResponseEntity<ProductTypeDTO> createProductType(
        @Valid @RequestBody ProductTypeDTO productTypeDTO,
        @RequestHeader(value = "X-ENTITY-NAME", required = false, defaultValue = ENTITY_NAME) String entityName
    ) throws URISyntaxException {
        log.debug("REST request to save ProductType : {}", productTypeDTO);
        if (productTypeDTO.getId() != null) {
            throw new BadRequestAlertException("A new productType cannot already have an ID", entityName, "idexists");
        }
        ProductTypeDTO result = productTypeService.save(productTypeDTO);
        return ResponseEntity.created(new URI("/api/product-type/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(entityName, result.getName()))
            .body(result);
    }

    /**
     * {@code PUT /product-type/:id } : Updates an existing productType.
     *
     * @param id the id of the productTypeDTO to save.
     * @param productTypeDTO the productTypeDTO to update.
     * @param entityName the entity name from the request header.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated productTypeDTO,
     * or with status {@code 400 (Bad Request)} if the productTypeDTO is not valid,
     * or with status {@code 500 (Internal Server Error)} if the productTypeDTO couldn't be updated.
     */
    @PutMapping("/product-type/{id}")
    public ResponseEntity<ProductTypeDTO> updateProductType(
        @PathVariable(value = "id", required = false) final Long id,
        @Valid @RequestBody ProductTypeDTO productTypeDTO,
        @RequestHeader(value = "X-ENTITY-NAME", required = false, defaultValue = ENTITY_NAME) String entityName
    ) {
        log.debug("REST request to update ProductType : {}, {}", id, productTypeDTO);
        if (productTypeDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", entityName, "idnull");
        }
        if (!Objects.equals(id, productTypeDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", entityName, "idinvalid");
        }
        if (!productTypeRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", entityName, "idnotfound");
        }

        ProductTypeDTO existing = productTypeService
            .findOne(productTypeDTO.getId())
            .orElseThrow(() -> new BadRequestAlertException("Invalid id", entityName, "idnotfound"));
        productTypeService.revertUnUpdatableAttributes(productTypeDTO, existing);
        ProductTypeDTO result = productTypeService.save(productTypeDTO);
        return ResponseEntity.ok().headers(HeaderUtil.createEntityUpdateAlert(entityName, result.getName())).body(result);
    }

    /**
     * {@code PATCH  /product-type/:id} : Partial updates given fields of an existing productType, field will ignore if it is null
     *
     * @param id the id of the productTypeDTO to save.
     * @param productTypeDTO the productTypeDTO to update.
     * @param entityName the entity name from the request header.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated productTypeDTO,
     * or with status {@code 400 (Bad Request)} if the productTypeDTO is not valid,
     * or with status {@code 404 (Not Found)} if the productTypeDTO is not found,
     * or with status {@code 500 (Internal Server Error)} if the productTypeDTO couldn't be updated.
     */
    @PatchMapping(value = "/product-type/{id}", consumes = { "application/json", "application/merge-patch+json" })
    public ResponseEntity<ProductTypeDTO> partialUpdateProductType(
        @PathVariable(value = "id", required = false) final Long id,
        @NotNull @RequestBody ProductTypeDTO productTypeDTO,
        @RequestHeader(value = "X-ENTITY-NAME", required = false, defaultValue = ENTITY_NAME) String entityName
    ) {
        log.debug("REST request to partial update ProductType partially : {}, {}", id, productTypeDTO);
        if (productTypeDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", entityName, "idnull");
        }
        if (!Objects.equals(id, productTypeDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", entityName, "idinvalid");
        }

        if (!productTypeRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", entityName, "idnotfound");
        }

        Optional<ProductTypeDTO> result = productTypeService.partialUpdate(productTypeDTO);

        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(entityName, result.map(dto -> dto.getName()).orElse(null))
        );
    }

    /**
     * {@code GET  /product-type} : get all the productTypes.
     *
     * @param pageable the pagination information.
     * @param filter the filter which the requested entities should match.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of productTypes in body.
     */
    @GetMapping("/product-type")
    public ResponseEntity<List<ProductTypeDTO>> getAllProductTypes(
        @Parameter(
            name = "filter",
            description = "Rsql criteria filter. Example: name=='Some text' or status==#active# (more examples on https://github.com/SpiralUp/rsql-grammar)"
        ) @RequestParam(value = "filter", required = false) String filter,
        @org.springdoc.core.annotations.ParameterObject Pageable pageable
    ) {
        if (filter != null) {
            filter = decode(filter, StandardCharsets.UTF_8);
        }

        log.debug("REST request to get a pageable list of all ProductTypes by filter: {}", filter);
        Page<ProductTypeDTO> page = productTypeService.findAll(filter, pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    /**
     * {@code GET  /product-type/all} : get all the productTypes.
     *
     * @param filter the filter which the requested entities should match.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of productTypes in body.
     */
    @GetMapping("/product-type/all")
    public ResponseEntity<List<ProductTypeDTO>> getAllProductTypesAsList(
        @Parameter(
            name = "filter",
            description = "Rsql criteria filter. Example: name=='Some text' or status==#active# (more examples on https://github.com/SpiralUp/rsql-grammar)"
        ) @RequestParam(value = "filter", required = false) String filter,
        @org.springdoc.core.annotations.ParameterObject Pageable pageable
    ) {
        if (filter != null) {
            filter = decode(filter, StandardCharsets.UTF_8);
        }

        log.debug("REST request to get a list of all ProductTypes by filter: {}", filter);
        List<ProductTypeDTO> list = productTypeService.findByFilterAndSort(filter, pageable);
        HttpHeaders headers = ListHeaderUtil.generateListHttpHeaders(
            ServletUriComponentsBuilder.fromCurrentRequest().toUriString(),
            filter,
            list.size()
        );
        return ResponseEntity.ok().headers(headers).body(list);
    }

    /**
     * {@code GET  /product-type/lov} : get productTypes as a list of values (id, code, name).
     *
     * @param filter the filter which the requested entities should match.
     * @param pageable  the sort criteria
     * @return          the {@link ResponseEntity} with status {@code 200 (OK)} and list of maps with id, code, name values in the body.
     */
    @GetMapping("/product-type/lov")
    public ResponseEntity<List<Map<String, Object>>> getLovProductTypes(
        @Parameter(
            name = "filter",
            description = "Rsql criteria filter. Example: name=='Some text' or status==#active# (more examples on https://github.com/SpiralUp/rsql-grammar)"
        ) @RequestParam(value = "filter", required = false) String filter,
        @org.springdoc.core.annotations.ParameterObject Pageable pageable
    ) {
        if (filter != null) {
            filter = decode(filter, StandardCharsets.UTF_8);
        }

        log.debug("REST request to get LOV ProductTypes by filter: {}", filter);
        List<Map<String, Object>> list = productTypeService.getLOV(filter, pageable, "id", "code", "name");
        return ResponseEntity.ok().body(list);
    }

    /**
     * {@code GET  /product-type/count} : count all the productTypes.
     *
     * @param filter the filter which the requested entities should match.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the count in body.
     */
    @GetMapping("/product-type/count")
    public ResponseEntity<Long> countProductTypes(
        @Parameter(
            name = "filter",
            description = "Rsql criteria filter. Example: name=='Some text' or status==#active# (more examples on https://github.com/SpiralUp/rsql-grammar)",
            example = "name=='Some text' or status==#active#"
        ) @RequestParam(value = "filter", required = false) String filter
    ) {
        if (filter != null) {
            filter = decode(filter, StandardCharsets.UTF_8);
        }

        log.debug("REST request to count ProductTypes by filter: {}", filter);
        return ResponseEntity.ok().body(productTypeService.countByFilter(filter));
    }

    /**
     * {@code GET  /product-type/:id} : get the "id" productType.
     *
     * @param id the id of the productTypeDTO to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the productTypeDTO, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/product-type/{id}")
    public ResponseEntity<ProductTypeDTO> getProductType(@PathVariable(value = "id") Long id) {
        log.debug("REST request to get ProductType : {}", id);
        Optional<ProductTypeDTO> productTypeDTO = productTypeService.findOne(id);
        return ResponseUtil.wrapOrNotFound(productTypeDTO);
    }

    /**
     * {@code DELETE  /product-type/:id} : delete the "id" productType.
     *
     * @param id the id of the productTypeDTO to delete.
     * @param entityName the entity name from the request header.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/product-type/{id}")
    public ResponseEntity<Void> deleteProductType(
        @PathVariable(value = "id") Long id,
        @RequestHeader(value = "X-ENTITY-NAME", required = false, defaultValue = ENTITY_NAME) String entityName
    ) {
        log.debug("REST request to delete ProductType : {}", id);
        ProductTypeDTO existing = productTypeService
            .findOne(id)
            .orElseThrow(() -> new BadRequestAlertException("Invalid id", entityName, "idnotfound"));
        productTypeService.delete(id);
        return ResponseEntity.noContent().headers(HeaderUtil.createEntityDeletionAlert(entityName, existing.getName())).build();
    }
}

package testappl.web.rest;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
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
import tech.jhipster.web.util.HeaderUtil;
import tech.jhipster.web.util.PaginationUtil;
import tech.jhipster.web.util.ResponseUtil;
import testappl.repository.ProductTypeRepository;
import testappl.service.ProductTypeQueryService;
import testappl.service.ProductTypeService;
import testappl.service.criteria.ProductTypeCriteria;
import testappl.service.dto.ProductTypeDTO;
import testappl.web.rest.errors.BadRequestAlertException;

/**
 * REST controller for managing {@link testappl.domain.ProductType}.
 */
@RestController
@RequestMapping("/api/product-types")
public class ProductTypeResource {

    private final Logger log = LoggerFactory.getLogger(ProductTypeResource.class);

    private static final String ENTITY_NAME = "productType";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final ProductTypeService productTypeService;

    private final ProductTypeRepository productTypeRepository;

    private final ProductTypeQueryService productTypeQueryService;

    public ProductTypeResource(
        ProductTypeService productTypeService,
        ProductTypeRepository productTypeRepository,
        ProductTypeQueryService productTypeQueryService
    ) {
        this.productTypeService = productTypeService;
        this.productTypeRepository = productTypeRepository;
        this.productTypeQueryService = productTypeQueryService;
    }

    /**
     * {@code POST  /product-types} : Create a new productType.
     *
     * @param productTypeDTO the productTypeDTO to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new productTypeDTO, or with status {@code 400 (Bad Request)} if the productType has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("")
    public ResponseEntity<ProductTypeDTO> createProductType(@Valid @RequestBody ProductTypeDTO productTypeDTO) throws URISyntaxException {
        log.debug("REST request to save ProductType : {}", productTypeDTO);
        if (productTypeDTO.getId() != null) {
            throw new BadRequestAlertException("A new productType cannot already have an ID", ENTITY_NAME, "idexists");
        }
        ProductTypeDTO result = productTypeService.save(productTypeDTO);
        return ResponseEntity
            .created(new URI("/api/product-types/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, false, ENTITY_NAME, result.getId().toString()))
            .body(result);
    }

    /**
     * {@code PUT  /product-types/:id} : Updates an existing productType.
     *
     * @param id the id of the productTypeDTO to save.
     * @param productTypeDTO the productTypeDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated productTypeDTO,
     * or with status {@code 400 (Bad Request)} if the productTypeDTO is not valid,
     * or with status {@code 500 (Internal Server Error)} if the productTypeDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/{id}")
    public ResponseEntity<ProductTypeDTO> updateProductType(
        @PathVariable(value = "id", required = false) final Long id,
        @Valid @RequestBody ProductTypeDTO productTypeDTO
    ) throws URISyntaxException {
        log.debug("REST request to update ProductType : {}, {}", id, productTypeDTO);
        if (productTypeDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, productTypeDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!productTypeRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        ProductTypeDTO result = productTypeService.update(productTypeDTO);
        return ResponseEntity
            .ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, false, ENTITY_NAME, productTypeDTO.getId().toString()))
            .body(result);
    }

    /**
     * {@code PATCH  /product-types/:id} : Partial updates given fields of an existing productType, field will ignore if it is null
     *
     * @param id the id of the productTypeDTO to save.
     * @param productTypeDTO the productTypeDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated productTypeDTO,
     * or with status {@code 400 (Bad Request)} if the productTypeDTO is not valid,
     * or with status {@code 404 (Not Found)} if the productTypeDTO is not found,
     * or with status {@code 500 (Internal Server Error)} if the productTypeDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/{id}", consumes = { "application/json", "application/merge-patch+json" })
    public ResponseEntity<ProductTypeDTO> partialUpdateProductType(
        @PathVariable(value = "id", required = false) final Long id,
        @NotNull @RequestBody ProductTypeDTO productTypeDTO
    ) throws URISyntaxException {
        log.debug("REST request to partial update ProductType partially : {}, {}", id, productTypeDTO);
        if (productTypeDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, productTypeDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!productTypeRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Optional<ProductTypeDTO> result = productTypeService.partialUpdate(productTypeDTO);

        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, false, ENTITY_NAME, productTypeDTO.getId().toString())
        );
    }

    /**
     * {@code GET  /product-types} : get all the productTypes.
     *
     * @param pageable the pagination information.
     * @param criteria the criteria which the requested entities should match.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of productTypes in body.
     */
    @GetMapping("")
    public ResponseEntity<List<ProductTypeDTO>> getAllProductTypes(
        ProductTypeCriteria criteria,
        @org.springdoc.core.annotations.ParameterObject Pageable pageable
    ) {
        log.debug("REST request to get ProductTypes by criteria: {}", criteria);

        Page<ProductTypeDTO> page = productTypeQueryService.findByCriteria(criteria, pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    /**
     * {@code GET  /product-types/count} : count all the productTypes.
     *
     * @param criteria the criteria which the requested entities should match.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the count in body.
     */
    @GetMapping("/count")
    public ResponseEntity<Long> countProductTypes(ProductTypeCriteria criteria) {
        log.debug("REST request to count ProductTypes by criteria: {}", criteria);
        return ResponseEntity.ok().body(productTypeQueryService.countByCriteria(criteria));
    }

    /**
     * {@code GET  /product-types/:id} : get the "id" productType.
     *
     * @param id the id of the productTypeDTO to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the productTypeDTO, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/{id}")
    public ResponseEntity<ProductTypeDTO> getProductType(@PathVariable Long id) {
        log.debug("REST request to get ProductType : {}", id);
        Optional<ProductTypeDTO> productTypeDTO = productTypeService.findOne(id);
        return ResponseUtil.wrapOrNotFound(productTypeDTO);
    }

    /**
     * {@code DELETE  /product-types/:id} : delete the "id" productType.
     *
     * @param id the id of the productTypeDTO to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProductType(@PathVariable Long id) {
        log.debug("REST request to delete ProductType : {}", id);
        productTypeService.delete(id);
        return ResponseEntity
            .noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, false, ENTITY_NAME, id.toString()))
            .build();
    }
}

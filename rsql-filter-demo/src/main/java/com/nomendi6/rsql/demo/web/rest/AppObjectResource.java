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
import com.nomendi6.rsql.demo.repository.AppObjectRepository;
import com.nomendi6.rsql.demo.service.AppObjectService;
import com.nomendi6.rsql.demo.service.dto.AppObjectDTO;
import com.nomendi6.rsql.demo.web.rest.errors.BadRequestAlertException;
import com.nomendi6.rsql.demo.web.rest.util.HeaderUtil;
import com.nomendi6.rsql.demo.web.rest.util.ListHeaderUtil;

/**
 * REST controller for managing {@link com.nomendi6.rsql.demo.domain.AppObject}.
 */
@RestController
@RequestMapping("/api")
public class AppObjectResource {

    private final Logger log = LoggerFactory.getLogger(AppObjectResource.class);

    // private static final String ENTITY_NAME = "appObject";
    private static final String ENTITY_NAME = "appObject";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final AppObjectService appObjectService;

    private final AppObjectRepository appObjectRepository;

    public AppObjectResource(AppObjectService appObjectService, AppObjectRepository appObjectRepository) {
        this.appObjectService = appObjectService;
        this.appObjectRepository = appObjectRepository;
    }

    /**
     * {@code POST  /app-object} : Create a new appObject.
     *
     * @param appObjectDTO the appObjectDTO to create.
     * @param entityName the entity name from the request header.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new appObjectDTO, or with status {@code 400 (Bad Request)} if the appObject has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("/app-object")
    public ResponseEntity<AppObjectDTO> createAppObject(
        @Valid @RequestBody AppObjectDTO appObjectDTO,
        @RequestHeader(value = "X-ENTITY-NAME", required = false, defaultValue = ENTITY_NAME) String entityName
    ) throws URISyntaxException {
        log.debug("REST request to save AppObject : {}", appObjectDTO);
        if (appObjectDTO.getId() != null) {
            throw new BadRequestAlertException("A new appObject cannot already have an ID", entityName, "idexists");
        }
        AppObjectDTO result = appObjectService.save(appObjectDTO);
        return ResponseEntity.created(new URI("/api/app-object/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(entityName, result.getName()))
            .body(result);
    }

    /**
     * {@code PUT /app-object/:id } : Updates an existing appObject.
     *
     * @param id the id of the appObjectDTO to save.
     * @param appObjectDTO the appObjectDTO to update.
     * @param entityName the entity name from the request header.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated appObjectDTO,
     * or with status {@code 400 (Bad Request)} if the appObjectDTO is not valid,
     * or with status {@code 500 (Internal Server Error)} if the appObjectDTO couldn't be updated.
     */
    @PutMapping("/app-object/{id}")
    public ResponseEntity<AppObjectDTO> updateAppObject(
        @PathVariable(value = "id", required = false) final Long id,
        @Valid @RequestBody AppObjectDTO appObjectDTO,
        @RequestHeader(value = "X-ENTITY-NAME", required = false, defaultValue = ENTITY_NAME) String entityName
    ) {
        log.debug("REST request to update AppObject : {}, {}", id, appObjectDTO);
        if (appObjectDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", entityName, "idnull");
        }
        if (!Objects.equals(id, appObjectDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", entityName, "idinvalid");
        }
        if (!appObjectRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", entityName, "idnotfound");
        }

        AppObjectDTO existing = appObjectService
            .findOne(appObjectDTO.getId())
            .orElseThrow(() -> new BadRequestAlertException("Invalid id", entityName, "idnotfound"));
        appObjectService.revertUnUpdatableAttributes(appObjectDTO, existing);
        AppObjectDTO result = appObjectService.save(appObjectDTO);
        return ResponseEntity.ok().headers(HeaderUtil.createEntityUpdateAlert(entityName, result.getName())).body(result);
    }

    /**
     * {@code PATCH  /app-object/:id} : Partial updates given fields of an existing appObject, field will ignore if it is null
     *
     * @param id the id of the appObjectDTO to save.
     * @param appObjectDTO the appObjectDTO to update.
     * @param entityName the entity name from the request header.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated appObjectDTO,
     * or with status {@code 400 (Bad Request)} if the appObjectDTO is not valid,
     * or with status {@code 404 (Not Found)} if the appObjectDTO is not found,
     * or with status {@code 500 (Internal Server Error)} if the appObjectDTO couldn't be updated.
     */
    @PatchMapping(value = "/app-object/{id}", consumes = { "application/json", "application/merge-patch+json" })
    public ResponseEntity<AppObjectDTO> partialUpdateAppObject(
        @PathVariable(value = "id", required = false) final Long id,
        @NotNull @RequestBody AppObjectDTO appObjectDTO,
        @RequestHeader(value = "X-ENTITY-NAME", required = false, defaultValue = ENTITY_NAME) String entityName
    ) {
        log.debug("REST request to partial update AppObject partially : {}, {}", id, appObjectDTO);
        if (appObjectDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", entityName, "idnull");
        }
        if (!Objects.equals(id, appObjectDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", entityName, "idinvalid");
        }

        if (!appObjectRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", entityName, "idnotfound");
        }

        Optional<AppObjectDTO> result = appObjectService.partialUpdate(appObjectDTO);

        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(entityName, result.map(dto -> dto.getName()).orElse(null))
        );
    }

    /**
     * {@code GET  /app-object} : get all the appObjects.
     *
     * @param pageable the pagination information.
     * @param filter the filter which the requested entities should match.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of appObjects in body.
     */
    @GetMapping("/app-object")
    public ResponseEntity<List<AppObjectDTO>> getAllAppObjects(
        @Parameter(
            name = "filter",
            description = "Rsql criteria filter. Example: name=='Some text' or status==#active# (more examples on https://github.com/SpiralUp/rsql-grammar)"
        ) @RequestParam(value = "filter", required = false) String filter,
        @org.springdoc.core.annotations.ParameterObject Pageable pageable
    ) {
        if (filter != null) {
            filter = decode(filter, StandardCharsets.UTF_8);
        }

        log.debug("REST request to get a pageable list of all AppObjects by filter: {}", filter);
        Page<AppObjectDTO> page = appObjectService.findAll(filter, pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    /**
     * {@code GET  /app-object/all} : get all the appObjects.
     *
     * @param filter the filter which the requested entities should match.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of appObjects in body.
     */
    @GetMapping("/app-object/all")
    public ResponseEntity<List<AppObjectDTO>> getAllAppObjectsAsList(
        @Parameter(
            name = "filter",
            description = "Rsql criteria filter. Example: name=='Some text' or status==#active# (more examples on https://github.com/SpiralUp/rsql-grammar)"
        ) @RequestParam(value = "filter", required = false) String filter,
        @org.springdoc.core.annotations.ParameterObject Pageable pageable
    ) {
        if (filter != null) {
            filter = decode(filter, StandardCharsets.UTF_8);
        }

        log.debug("REST request to get a list of all AppObjects by filter: {}", filter);
        List<AppObjectDTO> list = appObjectService.findByFilterAndSort(filter, pageable);
        HttpHeaders headers = ListHeaderUtil.generateListHttpHeaders(
            ServletUriComponentsBuilder.fromCurrentRequest().toUriString(),
            filter,
            list.size()
        );
        return ResponseEntity.ok().headers(headers).body(list);
    }

    /**
     * {@code GET  /app-object/lov} : get appObjects as a list of values (id, code, name).
     *
     * @param filter the filter which the requested entities should match.
     * @param pageable  the sort criteria
     * @return          the {@link ResponseEntity} with status {@code 200 (OK)} and list of maps with id, code, name values in the body.
     */
    @GetMapping("/app-object/lov")
    public ResponseEntity<List<Map<String, Object>>> getLovAppObjects(
        @Parameter(
            name = "filter",
            description = "Rsql criteria filter. Example: name=='Some text' or status==#active# (more examples on https://github.com/SpiralUp/rsql-grammar)"
        ) @RequestParam(value = "filter", required = false) String filter,
        @org.springdoc.core.annotations.ParameterObject Pageable pageable
    ) {
        if (filter != null) {
            filter = decode(filter, StandardCharsets.UTF_8);
        }

        log.debug("REST request to get LOV AppObjects by filter: {}", filter);
        List<Map<String, Object>> list = appObjectService.getLOV(filter, pageable, "id", "code", "name");
        return ResponseEntity.ok().body(list);
    }

    /**
     * {@code GET  /app-object/count} : count all the appObjects.
     *
     * @param filter the filter which the requested entities should match.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the count in body.
     */
    @GetMapping("/app-object/count")
    public ResponseEntity<Long> countAppObjects(
        @Parameter(
            name = "filter",
            description = "Rsql criteria filter. Example: name=='Some text' or status==#active# (more examples on https://github.com/SpiralUp/rsql-grammar)",
            example = "name=='Some text' or status==#active#"
        ) @RequestParam(value = "filter", required = false) String filter
    ) {
        if (filter != null) {
            filter = decode(filter, StandardCharsets.UTF_8);
        }

        log.debug("REST request to count AppObjects by filter: {}", filter);
        return ResponseEntity.ok().body(appObjectService.countByFilter(filter));
    }

    /**
     * {@code GET  /app-object/:id} : get the "id" appObject.
     *
     * @param id the id of the appObjectDTO to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the appObjectDTO, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/app-object/{id}")
    public ResponseEntity<AppObjectDTO> getAppObject(@PathVariable(value = "id") Long id) {
        log.debug("REST request to get AppObject : {}", id);
        Optional<AppObjectDTO> appObjectDTO = appObjectService.findOne(id);
        return ResponseUtil.wrapOrNotFound(appObjectDTO);
    }

    /**
     * {@code DELETE  /app-object/:id} : delete the "id" appObject.
     *
     * @param id the id of the appObjectDTO to delete.
     * @param entityName the entity name from the request header.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/app-object/{id}")
    public ResponseEntity<Void> deleteAppObject(
        @PathVariable(value = "id") Long id,
        @RequestHeader(value = "X-ENTITY-NAME", required = false, defaultValue = ENTITY_NAME) String entityName
    ) {
        log.debug("REST request to delete AppObject : {}", id);
        AppObjectDTO existing = appObjectService
            .findOne(id)
            .orElseThrow(() -> new BadRequestAlertException("Invalid id", entityName, "idnotfound"));
        appObjectService.delete(id);
        return ResponseEntity.noContent().headers(HeaderUtil.createEntityDeletionAlert(entityName, existing.getName())).build();
    }
}

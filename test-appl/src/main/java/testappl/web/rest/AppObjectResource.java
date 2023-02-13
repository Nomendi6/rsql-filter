package testappl.web.rest;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
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
import testappl.repository.AppObjectRepository;
import testappl.service.AppObjectQueryService;
import testappl.service.AppObjectService;
import testappl.service.criteria.AppObjectCriteria;
import testappl.service.dto.AppObjectDTO;
import testappl.web.rest.errors.BadRequestAlertException;

/**
 * REST controller for managing {@link testappl.domain.AppObject}.
 */
@RestController
@RequestMapping("/api")
public class AppObjectResource {

    private final Logger log = LoggerFactory.getLogger(AppObjectResource.class);

    private static final String ENTITY_NAME = "appObject";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final AppObjectService appObjectService;

    private final AppObjectRepository appObjectRepository;

    private final AppObjectQueryService appObjectQueryService;

    public AppObjectResource(
        AppObjectService appObjectService,
        AppObjectRepository appObjectRepository,
        AppObjectQueryService appObjectQueryService
    ) {
        this.appObjectService = appObjectService;
        this.appObjectRepository = appObjectRepository;
        this.appObjectQueryService = appObjectQueryService;
    }

    /**
     * {@code POST  /app-objects} : Create a new appObject.
     *
     * @param appObjectDTO the appObjectDTO to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new appObjectDTO, or with status {@code 400 (Bad Request)} if the appObject has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("/app-objects")
    public ResponseEntity<AppObjectDTO> createAppObject(@Valid @RequestBody AppObjectDTO appObjectDTO) throws URISyntaxException {
        log.debug("REST request to save AppObject : {}", appObjectDTO);
        if (appObjectDTO.getId() != null) {
            throw new BadRequestAlertException("A new appObject cannot already have an ID", ENTITY_NAME, "idexists");
        }
        AppObjectDTO result = appObjectService.save(appObjectDTO);
        return ResponseEntity
            .created(new URI("/api/app-objects/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, false, ENTITY_NAME, result.getId().toString()))
            .body(result);
    }

    /**
     * {@code PUT  /app-objects/:id} : Updates an existing appObject.
     *
     * @param id the id of the appObjectDTO to save.
     * @param appObjectDTO the appObjectDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated appObjectDTO,
     * or with status {@code 400 (Bad Request)} if the appObjectDTO is not valid,
     * or with status {@code 500 (Internal Server Error)} if the appObjectDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/app-objects/{id}")
    public ResponseEntity<AppObjectDTO> updateAppObject(
        @PathVariable(value = "id", required = false) final Long id,
        @Valid @RequestBody AppObjectDTO appObjectDTO
    ) throws URISyntaxException {
        log.debug("REST request to update AppObject : {}, {}", id, appObjectDTO);
        if (appObjectDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, appObjectDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!appObjectRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        AppObjectDTO result = appObjectService.update(appObjectDTO);
        return ResponseEntity
            .ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, false, ENTITY_NAME, appObjectDTO.getId().toString()))
            .body(result);
    }

    /**
     * {@code PATCH  /app-objects/:id} : Partial updates given fields of an existing appObject, field will ignore if it is null
     *
     * @param id the id of the appObjectDTO to save.
     * @param appObjectDTO the appObjectDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated appObjectDTO,
     * or with status {@code 400 (Bad Request)} if the appObjectDTO is not valid,
     * or with status {@code 404 (Not Found)} if the appObjectDTO is not found,
     * or with status {@code 500 (Internal Server Error)} if the appObjectDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/app-objects/{id}", consumes = { "application/json", "application/merge-patch+json" })
    public ResponseEntity<AppObjectDTO> partialUpdateAppObject(
        @PathVariable(value = "id", required = false) final Long id,
        @NotNull @RequestBody AppObjectDTO appObjectDTO
    ) throws URISyntaxException {
        log.debug("REST request to partial update AppObject partially : {}, {}", id, appObjectDTO);
        if (appObjectDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, appObjectDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!appObjectRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Optional<AppObjectDTO> result = appObjectService.partialUpdate(appObjectDTO);

        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, false, ENTITY_NAME, appObjectDTO.getId().toString())
        );
    }

    /**
     * {@code GET  /app-objects} : get all the appObjects.
     *
     * @param pageable the pagination information.
     * @param criteria the criteria which the requested entities should match.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of appObjects in body.
     */
    @GetMapping("/app-objects")
    public ResponseEntity<List<AppObjectDTO>> getAllAppObjects(
        AppObjectCriteria criteria,
        @org.springdoc.api.annotations.ParameterObject Pageable pageable
    ) {
        log.debug("REST request to get AppObjects by criteria: {}", criteria);
        Page<AppObjectDTO> page = appObjectQueryService.findByCriteria(criteria, pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    /**
     * {@code GET  /app-objects/count} : count all the appObjects.
     *
     * @param criteria the criteria which the requested entities should match.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the count in body.
     */
    @GetMapping("/app-objects/count")
    public ResponseEntity<Long> countAppObjects(AppObjectCriteria criteria) {
        log.debug("REST request to count AppObjects by criteria: {}", criteria);
        return ResponseEntity.ok().body(appObjectQueryService.countByCriteria(criteria));
    }

    /**
     * {@code GET  /app-objects/:id} : get the "id" appObject.
     *
     * @param id the id of the appObjectDTO to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the appObjectDTO, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/app-objects/{id}")
    public ResponseEntity<AppObjectDTO> getAppObject(@PathVariable Long id) {
        log.debug("REST request to get AppObject : {}", id);
        Optional<AppObjectDTO> appObjectDTO = appObjectService.findOne(id);
        return ResponseUtil.wrapOrNotFound(appObjectDTO);
    }

    /**
     * {@code DELETE  /app-objects/:id} : delete the "id" appObject.
     *
     * @param id the id of the appObjectDTO to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/app-objects/{id}")
    public ResponseEntity<Void> deleteAppObject(@PathVariable Long id) {
        log.debug("REST request to delete AppObject : {}", id);
        appObjectService.delete(id);
        return ResponseEntity
            .noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, false, ENTITY_NAME, id.toString()))
            .build();
    }
}

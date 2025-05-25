// Template: EntityResourceIT | v4.1
package testappl.web.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import testappl.IntegrationTest;
import testappl.domain.AppObject;
import testappl.domain.enumeration.AppObjectType;
import testappl.domain.enumeration.StandardRecordStatus;
import testappl.repository.AppObjectRepository;
import testappl.service.AppObjectService;
import testappl.service.dto.AppObjectDTO;
import testappl.service.mapper.AppObjectMapper;

/**
 * Integration tests for the {@link AppObjectResource} REST controller.
 */
@IntegrationTest
@ExtendWith(MockitoExtension.class)
@AutoConfigureMockMvc
@WithMockUser
class AppObjectResourceIT {

    private static final String DEFAULT_CODE = "AAAAAAAAAA";
    private static final String UPDATED_CODE = "BBBBBBBBBB";

    private static final String DEFAULT_NAME = "AAAAAAAAAA";
    private static final String UPDATED_NAME = "BBBBBBBBBB";

    private static final String DEFAULT_DESCRIPTION = "AAAAAAAAAA";
    private static final String UPDATED_DESCRIPTION = "BBBBBBBBBB";

    private static final AppObjectType DEFAULT_OBJECT_TYPE = AppObjectType.FUNCTIONAL_MODULE;
    private static final AppObjectType UPDATED_OBJECT_TYPE = AppObjectType.FORM;

    private static final Instant DEFAULT_LAST_CHANGE = Instant.ofEpochMilli(0L);
    private static final Instant UPDATED_LAST_CHANGE = Instant.now().truncatedTo(ChronoUnit.MILLIS);

    private static final Long DEFAULT_SEQ = 1L;
    private static final Long UPDATED_SEQ = 2L;

    private static final StandardRecordStatus DEFAULT_STATUS = StandardRecordStatus.ACTIVE;
    private static final StandardRecordStatus UPDATED_STATUS = StandardRecordStatus.NOT_ACTIVE;

    private static final Double DEFAULT_QUANTITY = 1D;
    private static final Double UPDATED_QUANTITY = 2D;

    private static final Instant DEFAULT_VALID_FROM = Instant.ofEpochMilli(0L);
    private static final Instant UPDATED_VALID_FROM = Instant.now().truncatedTo(ChronoUnit.MILLIS);

    private static final Instant DEFAULT_VALID_UNTIL = Instant.ofEpochMilli(0L);
    private static final Instant UPDATED_VALID_UNTIL = Instant.now().truncatedTo(ChronoUnit.MILLIS);

    private static final String ENTITY_API_URL = "/api/app-object";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";
    private static final String ENTITY_API_URL_ALL = ENTITY_API_URL + "/all";
    private static final String ENTITY_API_URL_LOV = ENTITY_API_URL + "/lov";
    private static final String ENTITY_API_URL_COUNT = ENTITY_API_URL + "/count";

    private static Random random = new Random();
    private static AtomicLong count = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    @Autowired
    private AppObjectRepository appObjectRepository;

    @Mock
    private AppObjectRepository appObjectRepositoryMock;

    @Autowired
    private AppObjectMapper appObjectMapper;

    @Mock
    private AppObjectService appObjectServiceMock;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restAppObjectMockMvc;

    @Autowired
    private ObjectMapper om;

    private AppObject appObject;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static AppObject createEntity(EntityManager em) {
        AppObject appObject = new AppObject()
            .withCode(DEFAULT_CODE)
            .withName(DEFAULT_NAME)
            .withDescription(DEFAULT_DESCRIPTION)
            .withObjectType(DEFAULT_OBJECT_TYPE)
            .withLastChange(DEFAULT_LAST_CHANGE)
            .withSeq(DEFAULT_SEQ)
            .withStatus(DEFAULT_STATUS)
            .withQuantity(DEFAULT_QUANTITY)
            .withValidFrom(DEFAULT_VALID_FROM)
            .withValidUntil(DEFAULT_VALID_UNTIL);
        return appObject;
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static AppObject createUpdatedEntity(EntityManager em) {
        AppObject appObject = new AppObject()
            .withCode(UPDATED_CODE)
            .withName(UPDATED_NAME)
            .withDescription(UPDATED_DESCRIPTION)
            .withObjectType(UPDATED_OBJECT_TYPE)
            .withLastChange(UPDATED_LAST_CHANGE)
            .withSeq(UPDATED_SEQ)
            .withStatus(UPDATED_STATUS)
            .withQuantity(UPDATED_QUANTITY)
            .withValidFrom(UPDATED_VALID_FROM)
            .withValidUntil(UPDATED_VALID_UNTIL);
        return appObject;
    }

    @BeforeEach
    void initTest() {
        appObject = createEntity(em);
    }

    @Test
    @Transactional
    void createAppObject() throws Exception {
        int databaseSizeBeforeCreate = appObjectRepository.findAll().size();
        // Create the AppObject
        AppObjectDTO appObjectDTO = appObjectMapper.toDto(appObject);
        restAppObjectMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(appObjectDTO)))
            .andExpect(status().isCreated());

        // Validate the AppObject in the database
        List<AppObject> appObjectList = appObjectRepository.findAll();
        assertThat(appObjectList).hasSize(databaseSizeBeforeCreate + 1);
        AppObject testAppObject = appObjectList.get(appObjectList.size() - 1);
        assertThat(testAppObject.getCode()).isEqualTo(DEFAULT_CODE);
        assertThat(testAppObject.getName()).isEqualTo(DEFAULT_NAME);
        assertThat(testAppObject.getDescription()).isEqualTo(DEFAULT_DESCRIPTION);
        assertThat(testAppObject.getObjectType()).isEqualTo(DEFAULT_OBJECT_TYPE);
        assertThat(testAppObject.getLastChange()).isEqualTo(DEFAULT_LAST_CHANGE);
        assertThat(testAppObject.getSeq()).isEqualTo(DEFAULT_SEQ);
        assertThat(testAppObject.getStatus()).isEqualTo(DEFAULT_STATUS);
        assertThat(testAppObject.getQuantity()).isEqualTo(DEFAULT_QUANTITY);
        assertThat(testAppObject.getValidFrom()).isEqualTo(DEFAULT_VALID_FROM);
        assertThat(testAppObject.getValidUntil()).isEqualTo(DEFAULT_VALID_UNTIL);
    }

    @Test
    @Transactional
    void createAppObjectWithExistingId() throws Exception {
        // Create the AppObject with an existing ID
        appObject.setId(1L);
        AppObjectDTO appObjectDTO = appObjectMapper.toDto(appObject);

        int databaseSizeBeforeCreate = appObjectRepository.findAll().size();

        // An entity with an existing ID cannot be created, so this API call must fail
        restAppObjectMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(appObjectDTO)))
            .andExpect(status().isBadRequest());

        // Validate the AppObject in the database
        List<AppObject> appObjectList = appObjectRepository.findAll();
        assertThat(appObjectList).hasSize(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    void checkCodeIsRequired() throws Exception {
        int databaseSizeBeforeTest = appObjectRepository.findAll().size();
        // set the field null
        appObject.setCode(null);

        // Create the AppObject, which fails.
        AppObjectDTO appObjectDTO = appObjectMapper.toDto(appObject);

        restAppObjectMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(appObjectDTO)))
            .andExpect(status().isBadRequest());

        List<AppObject> appObjectList = appObjectRepository.findAll();
        assertThat(appObjectList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkNameIsRequired() throws Exception {
        int databaseSizeBeforeTest = appObjectRepository.findAll().size();
        // set the field null
        appObject.setName(null);

        // Create the AppObject, which fails.
        AppObjectDTO appObjectDTO = appObjectMapper.toDto(appObject);

        restAppObjectMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(appObjectDTO)))
            .andExpect(status().isBadRequest());

        List<AppObject> appObjectList = appObjectRepository.findAll();
        assertThat(appObjectList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void getAllAppObjects() throws Exception {
        // Initialize the database
        appObject = appObjectRepository.saveAndFlush(appObject);

        // Get all the appObjectList
        restAppObjectMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(appObject.getId().intValue())))
            .andExpect(jsonPath("$.[*].code").value(hasItem(DEFAULT_CODE)))
            .andExpect(jsonPath("$.[*].name").value(hasItem(DEFAULT_NAME)))
            .andExpect(jsonPath("$.[*].description").value(hasItem(DEFAULT_DESCRIPTION.toString())))
            .andExpect(jsonPath("$.[*].objectType").value(hasItem(DEFAULT_OBJECT_TYPE.toString())))
            .andExpect(jsonPath("$.[*].lastChange").value(hasItem(DEFAULT_LAST_CHANGE.toString())))
            .andExpect(jsonPath("$.[*].seq").value(hasItem(DEFAULT_SEQ.intValue())))
            .andExpect(jsonPath("$.[*].status").value(hasItem(DEFAULT_STATUS.toString())))
            .andExpect(jsonPath("$.[*].quantity").value(hasItem(DEFAULT_QUANTITY.doubleValue())))
            .andExpect(jsonPath("$.[*].validFrom").value(hasItem(DEFAULT_VALID_FROM.toString())))
            .andExpect(jsonPath("$.[*].validUntil").value(hasItem(DEFAULT_VALID_UNTIL.toString())));
    }

    @SuppressWarnings({ "unchecked" })
    void getAllAppObjectsWithEagerRelationshipsIsEnabled() throws Exception {
        when(appObjectServiceMock.findAllWithEagerRelationships(any())).thenReturn(new PageImpl(new ArrayList<>()));

        restAppObjectMockMvc.perform(get(ENTITY_API_URL + "?eagerload=true")).andExpect(status().isOk());

        verify(appObjectServiceMock, times(1)).findAllWithEagerRelationships(any());
    }

    @SuppressWarnings({ "unchecked" })
    void getAllAppObjectsWithEagerRelationshipsIsNotEnabled() throws Exception {
        when(appObjectServiceMock.findAllWithEagerRelationships(any())).thenReturn(new PageImpl(new ArrayList<>()));

        restAppObjectMockMvc.perform(get(ENTITY_API_URL + "?eagerload=false")).andExpect(status().isOk());
        verify(appObjectRepositoryMock, times(1)).findAll(any(Pageable.class));
    }

    @Test
    @Transactional
    void getAppObject() throws Exception {
        // Initialize the database
        appObject = appObjectRepository.saveAndFlush(appObject);

        // Get the appObject
        restAppObjectMockMvc
            .perform(get(ENTITY_API_URL_ID, appObject.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(appObject.getId().intValue()))
            .andExpect(jsonPath("$.code").value(DEFAULT_CODE))
            .andExpect(jsonPath("$.name").value(DEFAULT_NAME))
            .andExpect(jsonPath("$.description").value(DEFAULT_DESCRIPTION.toString()))
            .andExpect(jsonPath("$.objectType").value(DEFAULT_OBJECT_TYPE.toString()))
            .andExpect(jsonPath("$.lastChange").value(DEFAULT_LAST_CHANGE.toString()))
            .andExpect(jsonPath("$.seq").value(DEFAULT_SEQ.intValue()))
            .andExpect(jsonPath("$.status").value(DEFAULT_STATUS.toString()))
            .andExpect(jsonPath("$.quantity").value(DEFAULT_QUANTITY.doubleValue()))
            .andExpect(jsonPath("$.validFrom").value(DEFAULT_VALID_FROM.toString()))
            .andExpect(jsonPath("$.validUntil").value(DEFAULT_VALID_UNTIL.toString()));
    }

    @Test
    @Transactional
    void getNonExistingAppObject() throws Exception {
        // Get the appObject
        restAppObjectMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putNewAppObject() throws Exception {
        // Initialize the database
        appObject = appObjectRepository.saveAndFlush(appObject);

        int databaseSizeBeforeUpdate = appObjectRepository.findAll().size();

        // Update the appObject
        AppObject updatedAppObject = appObjectRepository
            .findById(appObject.getId())
            .orElseThrow(() -> new IllegalArgumentException("Invalid id"));
        // Disconnect from session so that the updates on updatedAppObject are not directly saved in db
        em.detach(updatedAppObject);
        updatedAppObject
            .withCode(UPDATED_CODE)
            .withName(UPDATED_NAME)
            .withDescription(UPDATED_DESCRIPTION)
            .withObjectType(UPDATED_OBJECT_TYPE)
            .withLastChange(UPDATED_LAST_CHANGE)
            .withSeq(UPDATED_SEQ)
            .withStatus(UPDATED_STATUS)
            .withQuantity(UPDATED_QUANTITY)
            .withValidFrom(UPDATED_VALID_FROM)
            .withValidUntil(UPDATED_VALID_UNTIL);
        AppObjectDTO appObjectDTO = appObjectMapper.toDto(updatedAppObject);

        restAppObjectMockMvc
            .perform(
                put(ENTITY_API_URL_ID, appObjectDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(appObjectDTO))
            )
            .andExpect(status().isOk());

        // Validate the AppObject in the database
        List<AppObject> appObjectList = appObjectRepository.findAll();
        assertThat(appObjectList).hasSize(databaseSizeBeforeUpdate);
        AppObject testAppObject = appObjectList.get(appObjectList.size() - 1);
        assertThat(testAppObject.getCode()).isEqualTo(UPDATED_CODE);
        assertThat(testAppObject.getName()).isEqualTo(UPDATED_NAME);
        assertThat(testAppObject.getDescription()).isEqualTo(UPDATED_DESCRIPTION);
        assertThat(testAppObject.getObjectType()).isEqualTo(UPDATED_OBJECT_TYPE);
        assertThat(testAppObject.getLastChange()).isEqualTo(UPDATED_LAST_CHANGE);
        assertThat(testAppObject.getSeq()).isEqualTo(UPDATED_SEQ);
        assertThat(testAppObject.getStatus()).isEqualTo(UPDATED_STATUS);
        assertThat(testAppObject.getQuantity()).isEqualTo(UPDATED_QUANTITY);
        assertThat(testAppObject.getValidFrom()).isEqualTo(UPDATED_VALID_FROM);
        assertThat(testAppObject.getValidUntil()).isEqualTo(UPDATED_VALID_UNTIL);
    }

    @Test
    @Transactional
    void putNonExistingAppObject() throws Exception {
        int databaseSizeBeforeUpdate = appObjectRepository.findAll().size();
        appObject.setId(count.incrementAndGet());

        // Create the AppObject
        AppObjectDTO appObjectDTO = appObjectMapper.toDto(appObject);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restAppObjectMockMvc
            .perform(
                put(ENTITY_API_URL_ID, appObjectDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(appObjectDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the AppObject in the database
        List<AppObject> appObjectList = appObjectRepository.findAll();
        assertThat(appObjectList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithIdMismatchAppObject() throws Exception {
        int databaseSizeBeforeUpdate = appObjectRepository.findAll().size();
        appObject.setId(count.incrementAndGet());

        // Create the AppObject
        AppObjectDTO appObjectDTO = appObjectMapper.toDto(appObject);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restAppObjectMockMvc
            .perform(
                put(ENTITY_API_URL_ID, count.incrementAndGet())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(appObjectDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the AppObject in the database
        List<AppObject> appObjectList = appObjectRepository.findAll();
        assertThat(appObjectList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamAppObject() throws Exception {
        int databaseSizeBeforeUpdate = appObjectRepository.findAll().size();
        appObject.setId(count.incrementAndGet());

        // Create the AppObject
        AppObjectDTO appObjectDTO = appObjectMapper.toDto(appObject);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restAppObjectMockMvc
            .perform(put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(appObjectDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the AppObject in the database
        List<AppObject> appObjectList = appObjectRepository.findAll();
        assertThat(appObjectList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void partialUpdateAppObjectWithPatch() throws Exception {
        // Initialize the database
        appObject = appObjectRepository.saveAndFlush(appObject);

        int databaseSizeBeforeUpdate = appObjectRepository.findAll().size();

        // Update the appObject using partial update
        AppObject partialUpdatedAppObject = new AppObject();
        partialUpdatedAppObject.setId(appObject.getId());

        partialUpdatedAppObject
            .withCode(UPDATED_CODE)
            .withName(UPDATED_NAME)
            .withStatus(UPDATED_STATUS)
            .withQuantity(UPDATED_QUANTITY)
            .withValidFrom(UPDATED_VALID_FROM)
            .withValidUntil(UPDATED_VALID_UNTIL);

        restAppObjectMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedAppObject.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedAppObject))
            )
            .andExpect(status().isOk());

        // Validate the AppObject in the database
        List<AppObject> appObjectList = appObjectRepository.findAll();
        assertThat(appObjectList).hasSize(databaseSizeBeforeUpdate);
        AppObject testAppObject = appObjectList.get(appObjectList.size() - 1);
        assertThat(testAppObject.getCode()).isEqualTo(UPDATED_CODE);
        assertThat(testAppObject.getName()).isEqualTo(UPDATED_NAME);
        assertThat(testAppObject.getDescription()).isEqualTo(DEFAULT_DESCRIPTION);
        assertThat(testAppObject.getObjectType()).isEqualTo(DEFAULT_OBJECT_TYPE);
        assertThat(testAppObject.getLastChange()).isEqualTo(DEFAULT_LAST_CHANGE);
        assertThat(testAppObject.getSeq()).isEqualTo(DEFAULT_SEQ);
        assertThat(testAppObject.getStatus()).isEqualTo(UPDATED_STATUS);
        assertThat(testAppObject.getQuantity()).isEqualTo(UPDATED_QUANTITY);
        assertThat(testAppObject.getValidFrom()).isEqualTo(UPDATED_VALID_FROM);
        assertThat(testAppObject.getValidUntil()).isEqualTo(UPDATED_VALID_UNTIL);
    }

    @Test
    @Transactional
    void fullUpdateAppObjectWithPatch() throws Exception {
        // Initialize the database
        appObject = appObjectRepository.saveAndFlush(appObject);

        int databaseSizeBeforeUpdate = appObjectRepository.findAll().size();

        // Update the appObject using partial update
        AppObject partialUpdatedAppObject = new AppObject();
        partialUpdatedAppObject.setId(appObject.getId());

        partialUpdatedAppObject
            .withCode(UPDATED_CODE)
            .withName(UPDATED_NAME)
            .withDescription(UPDATED_DESCRIPTION)
            .withObjectType(UPDATED_OBJECT_TYPE)
            .withLastChange(UPDATED_LAST_CHANGE)
            .withSeq(UPDATED_SEQ)
            .withStatus(UPDATED_STATUS)
            .withQuantity(UPDATED_QUANTITY)
            .withValidFrom(UPDATED_VALID_FROM)
            .withValidUntil(UPDATED_VALID_UNTIL);

        restAppObjectMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedAppObject.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedAppObject))
            )
            .andExpect(status().isOk());

        // Validate the AppObject in the database
        List<AppObject> appObjectList = appObjectRepository.findAll();
        assertThat(appObjectList).hasSize(databaseSizeBeforeUpdate);
        AppObject testAppObject = appObjectList.get(appObjectList.size() - 1);
        assertThat(testAppObject.getCode()).isEqualTo(UPDATED_CODE);
        assertThat(testAppObject.getName()).isEqualTo(UPDATED_NAME);
        assertThat(testAppObject.getDescription()).isEqualTo(UPDATED_DESCRIPTION);
        assertThat(testAppObject.getObjectType()).isEqualTo(UPDATED_OBJECT_TYPE);
        assertThat(testAppObject.getLastChange()).isEqualTo(UPDATED_LAST_CHANGE);
        assertThat(testAppObject.getSeq()).isEqualTo(UPDATED_SEQ);
        assertThat(testAppObject.getStatus()).isEqualTo(UPDATED_STATUS);
        assertThat(testAppObject.getQuantity()).isEqualTo(UPDATED_QUANTITY);
        assertThat(testAppObject.getValidFrom()).isEqualTo(UPDATED_VALID_FROM);
        assertThat(testAppObject.getValidUntil()).isEqualTo(UPDATED_VALID_UNTIL);
    }

    @Test
    @Transactional
    void patchNonExistingAppObject() throws Exception {
        int databaseSizeBeforeUpdate = appObjectRepository.findAll().size();
        appObject.setId(count.incrementAndGet());

        // Create the AppObject
        AppObjectDTO appObjectDTO = appObjectMapper.toDto(appObject);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restAppObjectMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, appObjectDTO.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(appObjectDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the AppObject in the database
        List<AppObject> appObjectList = appObjectRepository.findAll();
        assertThat(appObjectList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithIdMismatchAppObject() throws Exception {
        int databaseSizeBeforeUpdate = appObjectRepository.findAll().size();
        appObject.setId(count.incrementAndGet());

        // Create the AppObject
        AppObjectDTO appObjectDTO = appObjectMapper.toDto(appObject);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restAppObjectMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, count.incrementAndGet())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(appObjectDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the AppObject in the database
        List<AppObject> appObjectList = appObjectRepository.findAll();
        assertThat(appObjectList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamAppObject() throws Exception {
        int databaseSizeBeforeUpdate = appObjectRepository.findAll().size();
        appObject.setId(count.incrementAndGet());

        // Create the AppObject
        AppObjectDTO appObjectDTO = appObjectMapper.toDto(appObject);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restAppObjectMockMvc
            .perform(patch(ENTITY_API_URL).contentType("application/merge-patch+json").content(om.writeValueAsBytes(appObjectDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the AppObject in the database
        List<AppObject> appObjectList = appObjectRepository.findAll();
        assertThat(appObjectList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void deleteAppObject() throws Exception {
        // Initialize the database
        appObject = appObjectRepository.saveAndFlush(appObject);

        int databaseSizeBeforeDelete = appObjectRepository.findAll().size();

        // Delete the appObject
        restAppObjectMockMvc
            .perform(delete(ENTITY_API_URL_ID, appObject.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        List<AppObject> appObjectList = appObjectRepository.findAll();
        assertThat(appObjectList).hasSize(databaseSizeBeforeDelete - 1);
    }

    @Test
    @Transactional
    void getWithFiltersAppObjects() throws Exception {
        // Initialize the database
        appObject = appObjectRepository.saveAndFlush(appObject);

        // Get with filters the appObjectList
        // -- equal --
        restAppObjectMockMvc
            .perform(get(ENTITY_API_URL + "?filter=id==" + appObject.getId() + "&sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(appObject.getId().intValue())))
            .andExpect(jsonPath("$.[*].code").value(hasItem(DEFAULT_CODE)))
            .andExpect(jsonPath("$.[*].name").value(hasItem(DEFAULT_NAME)))
            .andExpect(jsonPath("$.[*].description").value(hasItem(DEFAULT_DESCRIPTION.toString())))
            .andExpect(jsonPath("$.[*].objectType").value(hasItem(DEFAULT_OBJECT_TYPE.toString())))
            .andExpect(jsonPath("$.[*].lastChange").value(hasItem(DEFAULT_LAST_CHANGE.toString())))
            .andExpect(jsonPath("$.[*].seq").value(hasItem(DEFAULT_SEQ.intValue())))
            .andExpect(jsonPath("$.[*].status").value(hasItem(DEFAULT_STATUS.toString())))
            .andExpect(jsonPath("$.[*].quantity").value(hasItem(DEFAULT_QUANTITY.doubleValue())))
            .andExpect(jsonPath("$.[*].validFrom").value(hasItem(DEFAULT_VALID_FROM.toString())))
            .andExpect(jsonPath("$.[*].validUntil").value(hasItem(DEFAULT_VALID_UNTIL.toString())));

        // -- greater then or equal --
        restAppObjectMockMvc
            .perform(get(ENTITY_API_URL + "?filter=id=GE=" + appObject.getId() + "&sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(appObject.getId().intValue())))
            .andExpect(jsonPath("$.[*].code").value(hasItem(DEFAULT_CODE)))
            .andExpect(jsonPath("$.[*].name").value(hasItem(DEFAULT_NAME)))
            .andExpect(jsonPath("$.[*].description").value(hasItem(DEFAULT_DESCRIPTION.toString())))
            .andExpect(jsonPath("$.[*].objectType").value(hasItem(DEFAULT_OBJECT_TYPE.toString())))
            .andExpect(jsonPath("$.[*].lastChange").value(hasItem(DEFAULT_LAST_CHANGE.toString())))
            .andExpect(jsonPath("$.[*].seq").value(hasItem(DEFAULT_SEQ.intValue())))
            .andExpect(jsonPath("$.[*].status").value(hasItem(DEFAULT_STATUS.toString())))
            .andExpect(jsonPath("$.[*].quantity").value(hasItem(DEFAULT_QUANTITY.doubleValue())))
            .andExpect(jsonPath("$.[*].validFrom").value(hasItem(DEFAULT_VALID_FROM.toString())))
            .andExpect(jsonPath("$.[*].validUntil").value(hasItem(DEFAULT_VALID_UNTIL.toString())));

        // -- less then or equal --
        restAppObjectMockMvc
            .perform(get(ENTITY_API_URL + "?filter=id=LE=" + appObject.getId() + "&sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(appObject.getId().intValue())))
            .andExpect(jsonPath("$.[*].code").value(hasItem(DEFAULT_CODE)))
            .andExpect(jsonPath("$.[*].name").value(hasItem(DEFAULT_NAME)))
            .andExpect(jsonPath("$.[*].description").value(hasItem(DEFAULT_DESCRIPTION.toString())))
            .andExpect(jsonPath("$.[*].objectType").value(hasItem(DEFAULT_OBJECT_TYPE.toString())))
            .andExpect(jsonPath("$.[*].lastChange").value(hasItem(DEFAULT_LAST_CHANGE.toString())))
            .andExpect(jsonPath("$.[*].seq").value(hasItem(DEFAULT_SEQ.intValue())))
            .andExpect(jsonPath("$.[*].status").value(hasItem(DEFAULT_STATUS.toString())))
            .andExpect(jsonPath("$.[*].quantity").value(hasItem(DEFAULT_QUANTITY.doubleValue())))
            .andExpect(jsonPath("$.[*].validFrom").value(hasItem(DEFAULT_VALID_FROM.toString())))
            .andExpect(jsonPath("$.[*].validUntil").value(hasItem(DEFAULT_VALID_UNTIL.toString())));

        // -- between --
        restAppObjectMockMvc
            .perform(get(ENTITY_API_URL + "?filter=id=BT=(" + appObject.getId() + "," + appObject.getId() + ")&sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(appObject.getId().intValue())))
            .andExpect(jsonPath("$.[*].code").value(hasItem(DEFAULT_CODE)))
            .andExpect(jsonPath("$.[*].name").value(hasItem(DEFAULT_NAME)))
            .andExpect(jsonPath("$.[*].description").value(hasItem(DEFAULT_DESCRIPTION.toString())))
            .andExpect(jsonPath("$.[*].objectType").value(hasItem(DEFAULT_OBJECT_TYPE.toString())))
            .andExpect(jsonPath("$.[*].lastChange").value(hasItem(DEFAULT_LAST_CHANGE.toString())))
            .andExpect(jsonPath("$.[*].seq").value(hasItem(DEFAULT_SEQ.intValue())))
            .andExpect(jsonPath("$.[*].status").value(hasItem(DEFAULT_STATUS.toString())))
            .andExpect(jsonPath("$.[*].quantity").value(hasItem(DEFAULT_QUANTITY.doubleValue())))
            .andExpect(jsonPath("$.[*].validFrom").value(hasItem(DEFAULT_VALID_FROM.toString())))
            .andExpect(jsonPath("$.[*].validUntil").value(hasItem(DEFAULT_VALID_UNTIL.toString())));

        // -- in --
        restAppObjectMockMvc
            .perform(get(ENTITY_API_URL + "?filter=id=IN=(" + appObject.getId() + "," + appObject.getId() + ")&sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(appObject.getId().intValue())))
            .andExpect(jsonPath("$.[*].code").value(hasItem(DEFAULT_CODE)))
            .andExpect(jsonPath("$.[*].name").value(hasItem(DEFAULT_NAME)))
            .andExpect(jsonPath("$.[*].description").value(hasItem(DEFAULT_DESCRIPTION.toString())))
            .andExpect(jsonPath("$.[*].objectType").value(hasItem(DEFAULT_OBJECT_TYPE.toString())))
            .andExpect(jsonPath("$.[*].lastChange").value(hasItem(DEFAULT_LAST_CHANGE.toString())))
            .andExpect(jsonPath("$.[*].seq").value(hasItem(DEFAULT_SEQ.intValue())))
            .andExpect(jsonPath("$.[*].status").value(hasItem(DEFAULT_STATUS.toString())))
            .andExpect(jsonPath("$.[*].quantity").value(hasItem(DEFAULT_QUANTITY.doubleValue())))
            .andExpect(jsonPath("$.[*].validFrom").value(hasItem(DEFAULT_VALID_FROM.toString())))
            .andExpect(jsonPath("$.[*].validUntil").value(hasItem(DEFAULT_VALID_UNTIL.toString())));

        // -- like --
        restAppObjectMockMvc
            .perform(get(ENTITY_API_URL + "?filter=name=*'" + appObject.getName() + "*'&sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(appObject.getId().intValue())))
            .andExpect(jsonPath("$.[*].code").value(hasItem(DEFAULT_CODE)))
            .andExpect(jsonPath("$.[*].name").value(hasItem(DEFAULT_NAME)))
            .andExpect(jsonPath("$.[*].description").value(hasItem(DEFAULT_DESCRIPTION.toString())))
            .andExpect(jsonPath("$.[*].objectType").value(hasItem(DEFAULT_OBJECT_TYPE.toString())))
            .andExpect(jsonPath("$.[*].lastChange").value(hasItem(DEFAULT_LAST_CHANGE.toString())))
            .andExpect(jsonPath("$.[*].seq").value(hasItem(DEFAULT_SEQ.intValue())))
            .andExpect(jsonPath("$.[*].status").value(hasItem(DEFAULT_STATUS.toString())))
            .andExpect(jsonPath("$.[*].quantity").value(hasItem(DEFAULT_QUANTITY.doubleValue())))
            .andExpect(jsonPath("$.[*].validFrom").value(hasItem(DEFAULT_VALID_FROM.toString())))
            .andExpect(jsonPath("$.[*].validUntil").value(hasItem(DEFAULT_VALID_UNTIL.toString())));
    }

    @Test
    @Transactional
    void getAsListWithFiltersAppObjects() throws Exception {
        // Initialize the database
        appObject = appObjectRepository.saveAndFlush(appObject);

        // Get a whole list with filters without paging
        // -- without filters --
        restAppObjectMockMvc
            .perform(get(ENTITY_API_URL_ALL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(appObject.getId().intValue())))
            .andExpect(jsonPath("$.[*].code").value(hasItem(DEFAULT_CODE)))
            .andExpect(jsonPath("$.[*].name").value(hasItem(DEFAULT_NAME)))
            .andExpect(jsonPath("$.[*].description").value(hasItem(DEFAULT_DESCRIPTION.toString())))
            .andExpect(jsonPath("$.[*].objectType").value(hasItem(DEFAULT_OBJECT_TYPE.toString())))
            .andExpect(jsonPath("$.[*].lastChange").value(hasItem(DEFAULT_LAST_CHANGE.toString())))
            .andExpect(jsonPath("$.[*].seq").value(hasItem(DEFAULT_SEQ.intValue())))
            .andExpect(jsonPath("$.[*].status").value(hasItem(DEFAULT_STATUS.toString())))
            .andExpect(jsonPath("$.[*].quantity").value(hasItem(DEFAULT_QUANTITY.doubleValue())))
            .andExpect(jsonPath("$.[*].validFrom").value(hasItem(DEFAULT_VALID_FROM.toString())))
            .andExpect(jsonPath("$.[*].validUntil").value(hasItem(DEFAULT_VALID_UNTIL.toString())));

        // -- equal --
        restAppObjectMockMvc
            .perform(get(ENTITY_API_URL_ALL + "?filter=id==" + appObject.getId() + "&sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(appObject.getId().intValue())))
            .andExpect(jsonPath("$.[*].code").value(hasItem(DEFAULT_CODE)))
            .andExpect(jsonPath("$.[*].name").value(hasItem(DEFAULT_NAME)))
            .andExpect(jsonPath("$.[*].description").value(hasItem(DEFAULT_DESCRIPTION.toString())))
            .andExpect(jsonPath("$.[*].objectType").value(hasItem(DEFAULT_OBJECT_TYPE.toString())))
            .andExpect(jsonPath("$.[*].lastChange").value(hasItem(DEFAULT_LAST_CHANGE.toString())))
            .andExpect(jsonPath("$.[*].seq").value(hasItem(DEFAULT_SEQ.intValue())))
            .andExpect(jsonPath("$.[*].status").value(hasItem(DEFAULT_STATUS.toString())))
            .andExpect(jsonPath("$.[*].quantity").value(hasItem(DEFAULT_QUANTITY.doubleValue())))
            .andExpect(jsonPath("$.[*].validFrom").value(hasItem(DEFAULT_VALID_FROM.toString())))
            .andExpect(jsonPath("$.[*].validUntil").value(hasItem(DEFAULT_VALID_UNTIL.toString())));

        // -- greater then or equal --
        restAppObjectMockMvc
            .perform(get(ENTITY_API_URL_ALL + "?filter=id=GE=" + appObject.getId() + "&sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(appObject.getId().intValue())))
            .andExpect(jsonPath("$.[*].code").value(hasItem(DEFAULT_CODE)))
            .andExpect(jsonPath("$.[*].name").value(hasItem(DEFAULT_NAME)))
            .andExpect(jsonPath("$.[*].description").value(hasItem(DEFAULT_DESCRIPTION.toString())))
            .andExpect(jsonPath("$.[*].objectType").value(hasItem(DEFAULT_OBJECT_TYPE.toString())))
            .andExpect(jsonPath("$.[*].lastChange").value(hasItem(DEFAULT_LAST_CHANGE.toString())))
            .andExpect(jsonPath("$.[*].seq").value(hasItem(DEFAULT_SEQ.intValue())))
            .andExpect(jsonPath("$.[*].status").value(hasItem(DEFAULT_STATUS.toString())))
            .andExpect(jsonPath("$.[*].quantity").value(hasItem(DEFAULT_QUANTITY.doubleValue())))
            .andExpect(jsonPath("$.[*].validFrom").value(hasItem(DEFAULT_VALID_FROM.toString())))
            .andExpect(jsonPath("$.[*].validUntil").value(hasItem(DEFAULT_VALID_UNTIL.toString())));

        // -- less then or equal --
        restAppObjectMockMvc
            .perform(get(ENTITY_API_URL_ALL + "?filter=id=LE=" + appObject.getId() + "&sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(appObject.getId().intValue())))
            .andExpect(jsonPath("$.[*].code").value(hasItem(DEFAULT_CODE)))
            .andExpect(jsonPath("$.[*].name").value(hasItem(DEFAULT_NAME)))
            .andExpect(jsonPath("$.[*].description").value(hasItem(DEFAULT_DESCRIPTION.toString())))
            .andExpect(jsonPath("$.[*].objectType").value(hasItem(DEFAULT_OBJECT_TYPE.toString())))
            .andExpect(jsonPath("$.[*].lastChange").value(hasItem(DEFAULT_LAST_CHANGE.toString())))
            .andExpect(jsonPath("$.[*].seq").value(hasItem(DEFAULT_SEQ.intValue())))
            .andExpect(jsonPath("$.[*].status").value(hasItem(DEFAULT_STATUS.toString())))
            .andExpect(jsonPath("$.[*].quantity").value(hasItem(DEFAULT_QUANTITY.doubleValue())))
            .andExpect(jsonPath("$.[*].validFrom").value(hasItem(DEFAULT_VALID_FROM.toString())))
            .andExpect(jsonPath("$.[*].validUntil").value(hasItem(DEFAULT_VALID_UNTIL.toString())));

        // -- between --
        restAppObjectMockMvc
            .perform(get(ENTITY_API_URL_ALL + "?filter=id=BT=(" + appObject.getId() + "," + appObject.getId() + ")&sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(appObject.getId().intValue())))
            .andExpect(jsonPath("$.[*].code").value(hasItem(DEFAULT_CODE)))
            .andExpect(jsonPath("$.[*].name").value(hasItem(DEFAULT_NAME)))
            .andExpect(jsonPath("$.[*].description").value(hasItem(DEFAULT_DESCRIPTION.toString())))
            .andExpect(jsonPath("$.[*].objectType").value(hasItem(DEFAULT_OBJECT_TYPE.toString())))
            .andExpect(jsonPath("$.[*].lastChange").value(hasItem(DEFAULT_LAST_CHANGE.toString())))
            .andExpect(jsonPath("$.[*].seq").value(hasItem(DEFAULT_SEQ.intValue())))
            .andExpect(jsonPath("$.[*].status").value(hasItem(DEFAULT_STATUS.toString())))
            .andExpect(jsonPath("$.[*].quantity").value(hasItem(DEFAULT_QUANTITY.doubleValue())))
            .andExpect(jsonPath("$.[*].validFrom").value(hasItem(DEFAULT_VALID_FROM.toString())))
            .andExpect(jsonPath("$.[*].validUntil").value(hasItem(DEFAULT_VALID_UNTIL.toString())));

        // -- in --
        restAppObjectMockMvc
            .perform(get(ENTITY_API_URL_ALL + "?filter=id=IN=(" + appObject.getId() + "," + appObject.getId() + ")&sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(appObject.getId().intValue())))
            .andExpect(jsonPath("$.[*].code").value(hasItem(DEFAULT_CODE)))
            .andExpect(jsonPath("$.[*].name").value(hasItem(DEFAULT_NAME)))
            .andExpect(jsonPath("$.[*].description").value(hasItem(DEFAULT_DESCRIPTION.toString())))
            .andExpect(jsonPath("$.[*].objectType").value(hasItem(DEFAULT_OBJECT_TYPE.toString())))
            .andExpect(jsonPath("$.[*].lastChange").value(hasItem(DEFAULT_LAST_CHANGE.toString())))
            .andExpect(jsonPath("$.[*].seq").value(hasItem(DEFAULT_SEQ.intValue())))
            .andExpect(jsonPath("$.[*].status").value(hasItem(DEFAULT_STATUS.toString())))
            .andExpect(jsonPath("$.[*].quantity").value(hasItem(DEFAULT_QUANTITY.doubleValue())))
            .andExpect(jsonPath("$.[*].validFrom").value(hasItem(DEFAULT_VALID_FROM.toString())))
            .andExpect(jsonPath("$.[*].validUntil").value(hasItem(DEFAULT_VALID_UNTIL.toString())));

        // -- like --
        restAppObjectMockMvc
            .perform(get(ENTITY_API_URL_ALL + "?filter=name=*'" + appObject.getName() + "*'&sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(appObject.getId().intValue())))
            .andExpect(jsonPath("$.[*].code").value(hasItem(DEFAULT_CODE)))
            .andExpect(jsonPath("$.[*].name").value(hasItem(DEFAULT_NAME)))
            .andExpect(jsonPath("$.[*].description").value(hasItem(DEFAULT_DESCRIPTION.toString())))
            .andExpect(jsonPath("$.[*].objectType").value(hasItem(DEFAULT_OBJECT_TYPE.toString())))
            .andExpect(jsonPath("$.[*].lastChange").value(hasItem(DEFAULT_LAST_CHANGE.toString())))
            .andExpect(jsonPath("$.[*].seq").value(hasItem(DEFAULT_SEQ.intValue())))
            .andExpect(jsonPath("$.[*].status").value(hasItem(DEFAULT_STATUS.toString())))
            .andExpect(jsonPath("$.[*].quantity").value(hasItem(DEFAULT_QUANTITY.doubleValue())))
            .andExpect(jsonPath("$.[*].validFrom").value(hasItem(DEFAULT_VALID_FROM.toString())))
            .andExpect(jsonPath("$.[*].validUntil").value(hasItem(DEFAULT_VALID_UNTIL.toString())));
    }

    @Test
    @Transactional
    void getLovWithFiltersAppObjects() throws Exception {
        // Initialize the database
        appObject = appObjectRepository.saveAndFlush(appObject);

        // Get LOV with filters
        // -- without filters --
        restAppObjectMockMvc
            .perform(get(ENTITY_API_URL_LOV + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(appObject.getId().intValue())))
            .andExpect(jsonPath("$.[*].code").value(hasItem(DEFAULT_CODE)))
            .andExpect(jsonPath("$.[*].name").value(hasItem(DEFAULT_NAME)));

        // -- equal --
        restAppObjectMockMvc
            .perform(get(ENTITY_API_URL_LOV + "?filter=id==" + appObject.getId() + "&sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(appObject.getId().intValue())))
            .andExpect(jsonPath("$.[*].code").value(hasItem(DEFAULT_CODE)))
            .andExpect(jsonPath("$.[*].name").value(hasItem(DEFAULT_NAME)));

        // -- greater then or equal --
        restAppObjectMockMvc
            .perform(get(ENTITY_API_URL_LOV + "?filter=id=GE=" + appObject.getId() + "&sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(appObject.getId().intValue())))
            .andExpect(jsonPath("$.[*].code").value(hasItem(DEFAULT_CODE)))
            .andExpect(jsonPath("$.[*].name").value(hasItem(DEFAULT_NAME)));

        // -- less then or equal --
        restAppObjectMockMvc
            .perform(get(ENTITY_API_URL_LOV + "?filter=id=LE=" + appObject.getId() + "&sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(appObject.getId().intValue())))
            .andExpect(jsonPath("$.[*].code").value(hasItem(DEFAULT_CODE)))
            .andExpect(jsonPath("$.[*].name").value(hasItem(DEFAULT_NAME)));

        // -- between --
        restAppObjectMockMvc
            .perform(get(ENTITY_API_URL_LOV + "?filter=id=BT=(" + appObject.getId() + "," + appObject.getId() + ")&sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(appObject.getId().intValue())))
            .andExpect(jsonPath("$.[*].code").value(hasItem(DEFAULT_CODE)))
            .andExpect(jsonPath("$.[*].name").value(hasItem(DEFAULT_NAME)));

        // -- in --
        restAppObjectMockMvc
            .perform(get(ENTITY_API_URL_LOV + "?filter=id=IN=(" + appObject.getId() + "," + appObject.getId() + ")&sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(appObject.getId().intValue())))
            .andExpect(jsonPath("$.[*].code").value(hasItem(DEFAULT_CODE)))
            .andExpect(jsonPath("$.[*].name").value(hasItem(DEFAULT_NAME)));

        // -- like --
        restAppObjectMockMvc
            .perform(get(ENTITY_API_URL_LOV + "?filter=name=*'" + appObject.getName() + "*'&sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(appObject.getId().intValue())))
            .andExpect(jsonPath("$.[*].code").value(hasItem(DEFAULT_CODE)))
            .andExpect(jsonPath("$.[*].name").value(hasItem(DEFAULT_NAME)));
    }

    @Test
    @Transactional
    void getCountWithFiltersAppObjects() throws Exception {
        // Initialize the database
        appObject = appObjectRepository.saveAndFlush(appObject);

        // Get count with filters
        // -- without filters --
        restAppObjectMockMvc
            .perform(get(ENTITY_API_URL_COUNT + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string(not("0")));

        // -- equal --
        restAppObjectMockMvc
            .perform(get(ENTITY_API_URL_COUNT + "?filter=id==" + appObject.getId() + "&sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string(not("0")));

        // -- greater then or equal --
        restAppObjectMockMvc
            .perform(get(ENTITY_API_URL_COUNT + "?filter=id=GE=" + appObject.getId() + "&sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string(not("0")));

        // -- less then or equal --
        restAppObjectMockMvc
            .perform(get(ENTITY_API_URL_COUNT + "?filter=id=LE=" + appObject.getId() + "&sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string(not("0")));

        // -- between --
        restAppObjectMockMvc
            .perform(get(ENTITY_API_URL_COUNT + "?filter=id=BT=(" + appObject.getId() + "," + appObject.getId() + ")&sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string(not("0")));

        // -- in --
        restAppObjectMockMvc
            .perform(get(ENTITY_API_URL_COUNT + "?filter=id=IN=(" + appObject.getId() + "," + appObject.getId() + ")&sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string(not("0")));

        // -- like --
        restAppObjectMockMvc
            .perform(get(ENTITY_API_URL_COUNT + "?filter=name=*'" + appObject.getName() + "*'&sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string(not("0")));
    }

    // Update on createdBy and createdDate should not be accepted
    @Test
    @Transactional
    void updateOfAuditFields() throws Exception {
        // Initialize the database
        appObject = appObjectRepository.saveAndFlush(appObject);

        int databaseSizeBeforeUpdate = appObjectRepository.findAll().size();

        // Update the appObject
        AppObject updatedAppObject = appObjectRepository
            .findById(appObject.getId())
            .orElseThrow(() -> new IllegalArgumentException("Invalid id"));
        final String createdBy = appObject.getCreatedBy();
        final Instant createdDate = appObject.getCreatedDate();

        // Disconnect from session so that the updates on updatedAppObject are not directly saved in db
        em.detach(updatedAppObject);
        updatedAppObject.setCreatedBy("test");
        updatedAppObject.setCreatedDate(Instant.now().minus(7, ChronoUnit.DAYS));
        AppObjectDTO updatedAppObjectDTO = appObjectMapper.toDto(updatedAppObject);

        // Update the entity
        restAppObjectMockMvc
            .perform(
                put(ENTITY_API_URL_ID, updatedAppObjectDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(updatedAppObjectDTO))
            )
            .andExpect(status().isOk());

        // Validate the AppObject in the database
        List<AppObject> appObjectList = appObjectRepository.findAll();
        assertThat(appObjectList).hasSize(databaseSizeBeforeUpdate);
        AppObject testAppObject = appObjectList.get(appObjectList.size() - 1);
        assertThat(testAppObject.getCreatedBy()).isEqualTo(createdBy);
        assertThat(testAppObject.getCreatedDate().truncatedTo(ChronoUnit.MILLIS)).isEqualTo(createdDate.truncatedTo(ChronoUnit.MILLIS));
    }
}

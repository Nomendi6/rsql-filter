package testappl.web.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import jakarta.persistence.EntityManager;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
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
import testappl.domain.AppObject;
import testappl.domain.Product;
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
    private static final Long SMALLER_SEQ = 1L - 1L;

    private static final StandardRecordStatus DEFAULT_STATUS = StandardRecordStatus.NOT_ACTIVE;
    private static final StandardRecordStatus UPDATED_STATUS = StandardRecordStatus.ACTIVE;

    private static final Double DEFAULT_QUANTITY = 1D;
    private static final Double UPDATED_QUANTITY = 2D;
    private static final Double SMALLER_QUANTITY = 1D - 1D;

    private static final Instant DEFAULT_VALID_FROM = Instant.ofEpochMilli(0L);
    private static final Instant UPDATED_VALID_FROM = Instant.now().truncatedTo(ChronoUnit.MILLIS);

    private static final Instant DEFAULT_VALID_UNTIL = Instant.ofEpochMilli(0L);
    private static final Instant UPDATED_VALID_UNTIL = Instant.now().truncatedTo(ChronoUnit.MILLIS);

    private static final Boolean DEFAULT_IS_VALID = false;
    private static final Boolean UPDATED_IS_VALID = true;

    private static final LocalDate DEFAULT_CREATION_DATE = LocalDate.ofEpochDay(0L);
    private static final LocalDate UPDATED_CREATION_DATE = LocalDate.now(ZoneId.systemDefault());
    private static final LocalDate SMALLER_CREATION_DATE = LocalDate.ofEpochDay(-1L);

    private static final String ENTITY_API_URL = "/api/app-objects";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    private static Random random = new Random();
    private static AtomicLong longCount = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

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

    private AppObject appObject;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static AppObject createEntity(EntityManager em) {
        AppObject appObject = new AppObject()
            .code(DEFAULT_CODE)
            .name(DEFAULT_NAME)
            .description(DEFAULT_DESCRIPTION)
            .objectType(DEFAULT_OBJECT_TYPE)
            .lastChange(DEFAULT_LAST_CHANGE)
            .seq(DEFAULT_SEQ)
            .status(DEFAULT_STATUS)
            .quantity(DEFAULT_QUANTITY)
            .validFrom(DEFAULT_VALID_FROM)
            .validUntil(DEFAULT_VALID_UNTIL)
            .isValid(DEFAULT_IS_VALID)
            .creationDate(DEFAULT_CREATION_DATE);
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
            .code(UPDATED_CODE)
            .name(UPDATED_NAME)
            .description(UPDATED_DESCRIPTION)
            .objectType(UPDATED_OBJECT_TYPE)
            .lastChange(UPDATED_LAST_CHANGE)
            .seq(UPDATED_SEQ)
            .status(UPDATED_STATUS)
            .quantity(UPDATED_QUANTITY)
            .validFrom(UPDATED_VALID_FROM)
            .validUntil(UPDATED_VALID_UNTIL)
            .isValid(UPDATED_IS_VALID)
            .creationDate(UPDATED_CREATION_DATE);
        return appObject;
    }

    @BeforeEach
    public void initTest() {
        appObject = createEntity(em);
    }

    @Test
    @Transactional
    void createAppObject() throws Exception {
        int databaseSizeBeforeCreate = appObjectRepository.findAll().size();
        // Create the AppObject
        AppObjectDTO appObjectDTO = appObjectMapper.toDto(appObject);
        restAppObjectMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(appObjectDTO)))
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
        assertThat(testAppObject.getIsValid()).isEqualTo(DEFAULT_IS_VALID);
        assertThat(testAppObject.getCreationDate()).isEqualTo(DEFAULT_CREATION_DATE);
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
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(appObjectDTO)))
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
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(appObjectDTO)))
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
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(appObjectDTO)))
            .andExpect(status().isBadRequest());

        List<AppObject> appObjectList = appObjectRepository.findAll();
        assertThat(appObjectList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void getAllAppObjects() throws Exception {
        // Initialize the database
        appObjectRepository.saveAndFlush(appObject);

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
            .andExpect(jsonPath("$.[*].validUntil").value(hasItem(DEFAULT_VALID_UNTIL.toString())))
            .andExpect(jsonPath("$.[*].isValid").value(hasItem(DEFAULT_IS_VALID.booleanValue())))
            .andExpect(jsonPath("$.[*].creationDate").value(hasItem(DEFAULT_CREATION_DATE.toString())));
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
        appObjectRepository.saveAndFlush(appObject);

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
            .andExpect(jsonPath("$.validUntil").value(DEFAULT_VALID_UNTIL.toString()))
            .andExpect(jsonPath("$.isValid").value(DEFAULT_IS_VALID.booleanValue()))
            .andExpect(jsonPath("$.creationDate").value(DEFAULT_CREATION_DATE.toString()));
    }

    @Test
    @Transactional
    void getAppObjectsByIdFiltering() throws Exception {
        // Initialize the database
        appObjectRepository.saveAndFlush(appObject);

        Long id = appObject.getId();

        defaultAppObjectShouldBeFound("id.equals=" + id);
        defaultAppObjectShouldNotBeFound("id.notEquals=" + id);

        defaultAppObjectShouldBeFound("id.greaterThanOrEqual=" + id);
        defaultAppObjectShouldNotBeFound("id.greaterThan=" + id);

        defaultAppObjectShouldBeFound("id.lessThanOrEqual=" + id);
        defaultAppObjectShouldNotBeFound("id.lessThan=" + id);
    }

    @Test
    @Transactional
    void getAllAppObjectsByCodeIsEqualToSomething() throws Exception {
        // Initialize the database
        appObjectRepository.saveAndFlush(appObject);

        // Get all the appObjectList where code equals to DEFAULT_CODE
        defaultAppObjectShouldBeFound("code.equals=" + DEFAULT_CODE);

        // Get all the appObjectList where code equals to UPDATED_CODE
        defaultAppObjectShouldNotBeFound("code.equals=" + UPDATED_CODE);
    }

    @Test
    @Transactional
    void getAllAppObjectsByCodeIsInShouldWork() throws Exception {
        // Initialize the database
        appObjectRepository.saveAndFlush(appObject);

        // Get all the appObjectList where code in DEFAULT_CODE or UPDATED_CODE
        defaultAppObjectShouldBeFound("code.in=" + DEFAULT_CODE + "," + UPDATED_CODE);

        // Get all the appObjectList where code equals to UPDATED_CODE
        defaultAppObjectShouldNotBeFound("code.in=" + UPDATED_CODE);
    }

    @Test
    @Transactional
    void getAllAppObjectsByCodeIsNullOrNotNull() throws Exception {
        // Initialize the database
        appObjectRepository.saveAndFlush(appObject);

        // Get all the appObjectList where code is not null
        defaultAppObjectShouldBeFound("code.specified=true");

        // Get all the appObjectList where code is null
        defaultAppObjectShouldNotBeFound("code.specified=false");
    }

    @Test
    @Transactional
    void getAllAppObjectsByCodeContainsSomething() throws Exception {
        // Initialize the database
        appObjectRepository.saveAndFlush(appObject);

        // Get all the appObjectList where code contains DEFAULT_CODE
        defaultAppObjectShouldBeFound("code.contains=" + DEFAULT_CODE);

        // Get all the appObjectList where code contains UPDATED_CODE
        defaultAppObjectShouldNotBeFound("code.contains=" + UPDATED_CODE);
    }

    @Test
    @Transactional
    void getAllAppObjectsByCodeNotContainsSomething() throws Exception {
        // Initialize the database
        appObjectRepository.saveAndFlush(appObject);

        // Get all the appObjectList where code does not contain DEFAULT_CODE
        defaultAppObjectShouldNotBeFound("code.doesNotContain=" + DEFAULT_CODE);

        // Get all the appObjectList where code does not contain UPDATED_CODE
        defaultAppObjectShouldBeFound("code.doesNotContain=" + UPDATED_CODE);
    }

    @Test
    @Transactional
    void getAllAppObjectsByNameIsEqualToSomething() throws Exception {
        // Initialize the database
        appObjectRepository.saveAndFlush(appObject);

        // Get all the appObjectList where name equals to DEFAULT_NAME
        defaultAppObjectShouldBeFound("name.equals=" + DEFAULT_NAME);

        // Get all the appObjectList where name equals to UPDATED_NAME
        defaultAppObjectShouldNotBeFound("name.equals=" + UPDATED_NAME);
    }

    @Test
    @Transactional
    void getAllAppObjectsByNameIsInShouldWork() throws Exception {
        // Initialize the database
        appObjectRepository.saveAndFlush(appObject);

        // Get all the appObjectList where name in DEFAULT_NAME or UPDATED_NAME
        defaultAppObjectShouldBeFound("name.in=" + DEFAULT_NAME + "," + UPDATED_NAME);

        // Get all the appObjectList where name equals to UPDATED_NAME
        defaultAppObjectShouldNotBeFound("name.in=" + UPDATED_NAME);
    }

    @Test
    @Transactional
    void getAllAppObjectsByNameIsNullOrNotNull() throws Exception {
        // Initialize the database
        appObjectRepository.saveAndFlush(appObject);

        // Get all the appObjectList where name is not null
        defaultAppObjectShouldBeFound("name.specified=true");

        // Get all the appObjectList where name is null
        defaultAppObjectShouldNotBeFound("name.specified=false");
    }

    @Test
    @Transactional
    void getAllAppObjectsByNameContainsSomething() throws Exception {
        // Initialize the database
        appObjectRepository.saveAndFlush(appObject);

        // Get all the appObjectList where name contains DEFAULT_NAME
        defaultAppObjectShouldBeFound("name.contains=" + DEFAULT_NAME);

        // Get all the appObjectList where name contains UPDATED_NAME
        defaultAppObjectShouldNotBeFound("name.contains=" + UPDATED_NAME);
    }

    @Test
    @Transactional
    void getAllAppObjectsByNameNotContainsSomething() throws Exception {
        // Initialize the database
        appObjectRepository.saveAndFlush(appObject);

        // Get all the appObjectList where name does not contain DEFAULT_NAME
        defaultAppObjectShouldNotBeFound("name.doesNotContain=" + DEFAULT_NAME);

        // Get all the appObjectList where name does not contain UPDATED_NAME
        defaultAppObjectShouldBeFound("name.doesNotContain=" + UPDATED_NAME);
    }

    @Test
    @Transactional
    void getAllAppObjectsByObjectTypeIsEqualToSomething() throws Exception {
        // Initialize the database
        appObjectRepository.saveAndFlush(appObject);

        // Get all the appObjectList where objectType equals to DEFAULT_OBJECT_TYPE
        defaultAppObjectShouldBeFound("objectType.equals=" + DEFAULT_OBJECT_TYPE);

        // Get all the appObjectList where objectType equals to UPDATED_OBJECT_TYPE
        defaultAppObjectShouldNotBeFound("objectType.equals=" + UPDATED_OBJECT_TYPE);
    }

    @Test
    @Transactional
    void getAllAppObjectsByObjectTypeIsInShouldWork() throws Exception {
        // Initialize the database
        appObjectRepository.saveAndFlush(appObject);

        // Get all the appObjectList where objectType in DEFAULT_OBJECT_TYPE or UPDATED_OBJECT_TYPE
        defaultAppObjectShouldBeFound("objectType.in=" + DEFAULT_OBJECT_TYPE + "," + UPDATED_OBJECT_TYPE);

        // Get all the appObjectList where objectType equals to UPDATED_OBJECT_TYPE
        defaultAppObjectShouldNotBeFound("objectType.in=" + UPDATED_OBJECT_TYPE);
    }

    @Test
    @Transactional
    void getAllAppObjectsByObjectTypeIsNullOrNotNull() throws Exception {
        // Initialize the database
        appObjectRepository.saveAndFlush(appObject);

        // Get all the appObjectList where objectType is not null
        defaultAppObjectShouldBeFound("objectType.specified=true");

        // Get all the appObjectList where objectType is null
        defaultAppObjectShouldNotBeFound("objectType.specified=false");
    }

    @Test
    @Transactional
    void getAllAppObjectsByLastChangeIsEqualToSomething() throws Exception {
        // Initialize the database
        appObjectRepository.saveAndFlush(appObject);

        // Get all the appObjectList where lastChange equals to DEFAULT_LAST_CHANGE
        defaultAppObjectShouldBeFound("lastChange.equals=" + DEFAULT_LAST_CHANGE);

        // Get all the appObjectList where lastChange equals to UPDATED_LAST_CHANGE
        defaultAppObjectShouldNotBeFound("lastChange.equals=" + UPDATED_LAST_CHANGE);
    }

    @Test
    @Transactional
    void getAllAppObjectsByLastChangeIsInShouldWork() throws Exception {
        // Initialize the database
        appObjectRepository.saveAndFlush(appObject);

        // Get all the appObjectList where lastChange in DEFAULT_LAST_CHANGE or UPDATED_LAST_CHANGE
        defaultAppObjectShouldBeFound("lastChange.in=" + DEFAULT_LAST_CHANGE + "," + UPDATED_LAST_CHANGE);

        // Get all the appObjectList where lastChange equals to UPDATED_LAST_CHANGE
        defaultAppObjectShouldNotBeFound("lastChange.in=" + UPDATED_LAST_CHANGE);
    }

    @Test
    @Transactional
    void getAllAppObjectsByLastChangeIsNullOrNotNull() throws Exception {
        // Initialize the database
        appObjectRepository.saveAndFlush(appObject);

        // Get all the appObjectList where lastChange is not null
        defaultAppObjectShouldBeFound("lastChange.specified=true");

        // Get all the appObjectList where lastChange is null
        defaultAppObjectShouldNotBeFound("lastChange.specified=false");
    }

    @Test
    @Transactional
    void getAllAppObjectsBySeqIsEqualToSomething() throws Exception {
        // Initialize the database
        appObjectRepository.saveAndFlush(appObject);

        // Get all the appObjectList where seq equals to DEFAULT_SEQ
        defaultAppObjectShouldBeFound("seq.equals=" + DEFAULT_SEQ);

        // Get all the appObjectList where seq equals to UPDATED_SEQ
        defaultAppObjectShouldNotBeFound("seq.equals=" + UPDATED_SEQ);
    }

    @Test
    @Transactional
    void getAllAppObjectsBySeqIsInShouldWork() throws Exception {
        // Initialize the database
        appObjectRepository.saveAndFlush(appObject);

        // Get all the appObjectList where seq in DEFAULT_SEQ or UPDATED_SEQ
        defaultAppObjectShouldBeFound("seq.in=" + DEFAULT_SEQ + "," + UPDATED_SEQ);

        // Get all the appObjectList where seq equals to UPDATED_SEQ
        defaultAppObjectShouldNotBeFound("seq.in=" + UPDATED_SEQ);
    }

    @Test
    @Transactional
    void getAllAppObjectsBySeqIsNullOrNotNull() throws Exception {
        // Initialize the database
        appObjectRepository.saveAndFlush(appObject);

        // Get all the appObjectList where seq is not null
        defaultAppObjectShouldBeFound("seq.specified=true");

        // Get all the appObjectList where seq is null
        defaultAppObjectShouldNotBeFound("seq.specified=false");
    }

    @Test
    @Transactional
    void getAllAppObjectsBySeqIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        appObjectRepository.saveAndFlush(appObject);

        // Get all the appObjectList where seq is greater than or equal to DEFAULT_SEQ
        defaultAppObjectShouldBeFound("seq.greaterThanOrEqual=" + DEFAULT_SEQ);

        // Get all the appObjectList where seq is greater than or equal to UPDATED_SEQ
        defaultAppObjectShouldNotBeFound("seq.greaterThanOrEqual=" + UPDATED_SEQ);
    }

    @Test
    @Transactional
    void getAllAppObjectsBySeqIsLessThanOrEqualToSomething() throws Exception {
        // Initialize the database
        appObjectRepository.saveAndFlush(appObject);

        // Get all the appObjectList where seq is less than or equal to DEFAULT_SEQ
        defaultAppObjectShouldBeFound("seq.lessThanOrEqual=" + DEFAULT_SEQ);

        // Get all the appObjectList where seq is less than or equal to SMALLER_SEQ
        defaultAppObjectShouldNotBeFound("seq.lessThanOrEqual=" + SMALLER_SEQ);
    }

    @Test
    @Transactional
    void getAllAppObjectsBySeqIsLessThanSomething() throws Exception {
        // Initialize the database
        appObjectRepository.saveAndFlush(appObject);

        // Get all the appObjectList where seq is less than DEFAULT_SEQ
        defaultAppObjectShouldNotBeFound("seq.lessThan=" + DEFAULT_SEQ);

        // Get all the appObjectList where seq is less than UPDATED_SEQ
        defaultAppObjectShouldBeFound("seq.lessThan=" + UPDATED_SEQ);
    }

    @Test
    @Transactional
    void getAllAppObjectsBySeqIsGreaterThanSomething() throws Exception {
        // Initialize the database
        appObjectRepository.saveAndFlush(appObject);

        // Get all the appObjectList where seq is greater than DEFAULT_SEQ
        defaultAppObjectShouldNotBeFound("seq.greaterThan=" + DEFAULT_SEQ);

        // Get all the appObjectList where seq is greater than SMALLER_SEQ
        defaultAppObjectShouldBeFound("seq.greaterThan=" + SMALLER_SEQ);
    }

    @Test
    @Transactional
    void getAllAppObjectsByStatusIsEqualToSomething() throws Exception {
        // Initialize the database
        appObjectRepository.saveAndFlush(appObject);

        // Get all the appObjectList where status equals to DEFAULT_STATUS
        defaultAppObjectShouldBeFound("status.equals=" + DEFAULT_STATUS);

        // Get all the appObjectList where status equals to UPDATED_STATUS
        defaultAppObjectShouldNotBeFound("status.equals=" + UPDATED_STATUS);
    }

    @Test
    @Transactional
    void getAllAppObjectsByStatusIsInShouldWork() throws Exception {
        // Initialize the database
        appObjectRepository.saveAndFlush(appObject);

        // Get all the appObjectList where status in DEFAULT_STATUS or UPDATED_STATUS
        defaultAppObjectShouldBeFound("status.in=" + DEFAULT_STATUS + "," + UPDATED_STATUS);

        // Get all the appObjectList where status equals to UPDATED_STATUS
        defaultAppObjectShouldNotBeFound("status.in=" + UPDATED_STATUS);
    }

    @Test
    @Transactional
    void getAllAppObjectsByStatusIsNullOrNotNull() throws Exception {
        // Initialize the database
        appObjectRepository.saveAndFlush(appObject);

        // Get all the appObjectList where status is not null
        defaultAppObjectShouldBeFound("status.specified=true");

        // Get all the appObjectList where status is null
        defaultAppObjectShouldNotBeFound("status.specified=false");
    }

    @Test
    @Transactional
    void getAllAppObjectsByQuantityIsEqualToSomething() throws Exception {
        // Initialize the database
        appObjectRepository.saveAndFlush(appObject);

        // Get all the appObjectList where quantity equals to DEFAULT_QUANTITY
        defaultAppObjectShouldBeFound("quantity.equals=" + DEFAULT_QUANTITY);

        // Get all the appObjectList where quantity equals to UPDATED_QUANTITY
        defaultAppObjectShouldNotBeFound("quantity.equals=" + UPDATED_QUANTITY);
    }

    @Test
    @Transactional
    void getAllAppObjectsByQuantityIsInShouldWork() throws Exception {
        // Initialize the database
        appObjectRepository.saveAndFlush(appObject);

        // Get all the appObjectList where quantity in DEFAULT_QUANTITY or UPDATED_QUANTITY
        defaultAppObjectShouldBeFound("quantity.in=" + DEFAULT_QUANTITY + "," + UPDATED_QUANTITY);

        // Get all the appObjectList where quantity equals to UPDATED_QUANTITY
        defaultAppObjectShouldNotBeFound("quantity.in=" + UPDATED_QUANTITY);
    }

    @Test
    @Transactional
    void getAllAppObjectsByQuantityIsNullOrNotNull() throws Exception {
        // Initialize the database
        appObjectRepository.saveAndFlush(appObject);

        // Get all the appObjectList where quantity is not null
        defaultAppObjectShouldBeFound("quantity.specified=true");

        // Get all the appObjectList where quantity is null
        defaultAppObjectShouldNotBeFound("quantity.specified=false");
    }

    @Test
    @Transactional
    void getAllAppObjectsByQuantityIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        appObjectRepository.saveAndFlush(appObject);

        // Get all the appObjectList where quantity is greater than or equal to DEFAULT_QUANTITY
        defaultAppObjectShouldBeFound("quantity.greaterThanOrEqual=" + DEFAULT_QUANTITY);

        // Get all the appObjectList where quantity is greater than or equal to UPDATED_QUANTITY
        defaultAppObjectShouldNotBeFound("quantity.greaterThanOrEqual=" + UPDATED_QUANTITY);
    }

    @Test
    @Transactional
    void getAllAppObjectsByQuantityIsLessThanOrEqualToSomething() throws Exception {
        // Initialize the database
        appObjectRepository.saveAndFlush(appObject);

        // Get all the appObjectList where quantity is less than or equal to DEFAULT_QUANTITY
        defaultAppObjectShouldBeFound("quantity.lessThanOrEqual=" + DEFAULT_QUANTITY);

        // Get all the appObjectList where quantity is less than or equal to SMALLER_QUANTITY
        defaultAppObjectShouldNotBeFound("quantity.lessThanOrEqual=" + SMALLER_QUANTITY);
    }

    @Test
    @Transactional
    void getAllAppObjectsByQuantityIsLessThanSomething() throws Exception {
        // Initialize the database
        appObjectRepository.saveAndFlush(appObject);

        // Get all the appObjectList where quantity is less than DEFAULT_QUANTITY
        defaultAppObjectShouldNotBeFound("quantity.lessThan=" + DEFAULT_QUANTITY);

        // Get all the appObjectList where quantity is less than UPDATED_QUANTITY
        defaultAppObjectShouldBeFound("quantity.lessThan=" + UPDATED_QUANTITY);
    }

    @Test
    @Transactional
    void getAllAppObjectsByQuantityIsGreaterThanSomething() throws Exception {
        // Initialize the database
        appObjectRepository.saveAndFlush(appObject);

        // Get all the appObjectList where quantity is greater than DEFAULT_QUANTITY
        defaultAppObjectShouldNotBeFound("quantity.greaterThan=" + DEFAULT_QUANTITY);

        // Get all the appObjectList where quantity is greater than SMALLER_QUANTITY
        defaultAppObjectShouldBeFound("quantity.greaterThan=" + SMALLER_QUANTITY);
    }

    @Test
    @Transactional
    void getAllAppObjectsByValidFromIsEqualToSomething() throws Exception {
        // Initialize the database
        appObjectRepository.saveAndFlush(appObject);

        // Get all the appObjectList where validFrom equals to DEFAULT_VALID_FROM
        defaultAppObjectShouldBeFound("validFrom.equals=" + DEFAULT_VALID_FROM);

        // Get all the appObjectList where validFrom equals to UPDATED_VALID_FROM
        defaultAppObjectShouldNotBeFound("validFrom.equals=" + UPDATED_VALID_FROM);
    }

    @Test
    @Transactional
    void getAllAppObjectsByValidFromIsInShouldWork() throws Exception {
        // Initialize the database
        appObjectRepository.saveAndFlush(appObject);

        // Get all the appObjectList where validFrom in DEFAULT_VALID_FROM or UPDATED_VALID_FROM
        defaultAppObjectShouldBeFound("validFrom.in=" + DEFAULT_VALID_FROM + "," + UPDATED_VALID_FROM);

        // Get all the appObjectList where validFrom equals to UPDATED_VALID_FROM
        defaultAppObjectShouldNotBeFound("validFrom.in=" + UPDATED_VALID_FROM);
    }

    @Test
    @Transactional
    void getAllAppObjectsByValidFromIsNullOrNotNull() throws Exception {
        // Initialize the database
        appObjectRepository.saveAndFlush(appObject);

        // Get all the appObjectList where validFrom is not null
        defaultAppObjectShouldBeFound("validFrom.specified=true");

        // Get all the appObjectList where validFrom is null
        defaultAppObjectShouldNotBeFound("validFrom.specified=false");
    }

    @Test
    @Transactional
    void getAllAppObjectsByValidUntilIsEqualToSomething() throws Exception {
        // Initialize the database
        appObjectRepository.saveAndFlush(appObject);

        // Get all the appObjectList where validUntil equals to DEFAULT_VALID_UNTIL
        defaultAppObjectShouldBeFound("validUntil.equals=" + DEFAULT_VALID_UNTIL);

        // Get all the appObjectList where validUntil equals to UPDATED_VALID_UNTIL
        defaultAppObjectShouldNotBeFound("validUntil.equals=" + UPDATED_VALID_UNTIL);
    }

    @Test
    @Transactional
    void getAllAppObjectsByValidUntilIsInShouldWork() throws Exception {
        // Initialize the database
        appObjectRepository.saveAndFlush(appObject);

        // Get all the appObjectList where validUntil in DEFAULT_VALID_UNTIL or UPDATED_VALID_UNTIL
        defaultAppObjectShouldBeFound("validUntil.in=" + DEFAULT_VALID_UNTIL + "," + UPDATED_VALID_UNTIL);

        // Get all the appObjectList where validUntil equals to UPDATED_VALID_UNTIL
        defaultAppObjectShouldNotBeFound("validUntil.in=" + UPDATED_VALID_UNTIL);
    }

    @Test
    @Transactional
    void getAllAppObjectsByValidUntilIsNullOrNotNull() throws Exception {
        // Initialize the database
        appObjectRepository.saveAndFlush(appObject);

        // Get all the appObjectList where validUntil is not null
        defaultAppObjectShouldBeFound("validUntil.specified=true");

        // Get all the appObjectList where validUntil is null
        defaultAppObjectShouldNotBeFound("validUntil.specified=false");
    }

    @Test
    @Transactional
    void getAllAppObjectsByIsValidIsEqualToSomething() throws Exception {
        // Initialize the database
        appObjectRepository.saveAndFlush(appObject);

        // Get all the appObjectList where isValid equals to DEFAULT_IS_VALID
        defaultAppObjectShouldBeFound("isValid.equals=" + DEFAULT_IS_VALID);

        // Get all the appObjectList where isValid equals to UPDATED_IS_VALID
        defaultAppObjectShouldNotBeFound("isValid.equals=" + UPDATED_IS_VALID);
    }

    @Test
    @Transactional
    void getAllAppObjectsByIsValidIsInShouldWork() throws Exception {
        // Initialize the database
        appObjectRepository.saveAndFlush(appObject);

        // Get all the appObjectList where isValid in DEFAULT_IS_VALID or UPDATED_IS_VALID
        defaultAppObjectShouldBeFound("isValid.in=" + DEFAULT_IS_VALID + "," + UPDATED_IS_VALID);

        // Get all the appObjectList where isValid equals to UPDATED_IS_VALID
        defaultAppObjectShouldNotBeFound("isValid.in=" + UPDATED_IS_VALID);
    }

    @Test
    @Transactional
    void getAllAppObjectsByIsValidIsNullOrNotNull() throws Exception {
        // Initialize the database
        appObjectRepository.saveAndFlush(appObject);

        // Get all the appObjectList where isValid is not null
        defaultAppObjectShouldBeFound("isValid.specified=true");

        // Get all the appObjectList where isValid is null
        defaultAppObjectShouldNotBeFound("isValid.specified=false");
    }

    @Test
    @Transactional
    void getAllAppObjectsByCreationDateIsEqualToSomething() throws Exception {
        // Initialize the database
        appObjectRepository.saveAndFlush(appObject);

        // Get all the appObjectList where creationDate equals to DEFAULT_CREATION_DATE
        defaultAppObjectShouldBeFound("creationDate.equals=" + DEFAULT_CREATION_DATE);

        // Get all the appObjectList where creationDate equals to UPDATED_CREATION_DATE
        defaultAppObjectShouldNotBeFound("creationDate.equals=" + UPDATED_CREATION_DATE);
    }

    @Test
    @Transactional
    void getAllAppObjectsByCreationDateIsInShouldWork() throws Exception {
        // Initialize the database
        appObjectRepository.saveAndFlush(appObject);

        // Get all the appObjectList where creationDate in DEFAULT_CREATION_DATE or UPDATED_CREATION_DATE
        defaultAppObjectShouldBeFound("creationDate.in=" + DEFAULT_CREATION_DATE + "," + UPDATED_CREATION_DATE);

        // Get all the appObjectList where creationDate equals to UPDATED_CREATION_DATE
        defaultAppObjectShouldNotBeFound("creationDate.in=" + UPDATED_CREATION_DATE);
    }

    @Test
    @Transactional
    void getAllAppObjectsByCreationDateIsNullOrNotNull() throws Exception {
        // Initialize the database
        appObjectRepository.saveAndFlush(appObject);

        // Get all the appObjectList where creationDate is not null
        defaultAppObjectShouldBeFound("creationDate.specified=true");

        // Get all the appObjectList where creationDate is null
        defaultAppObjectShouldNotBeFound("creationDate.specified=false");
    }

    @Test
    @Transactional
    void getAllAppObjectsByCreationDateIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        appObjectRepository.saveAndFlush(appObject);

        // Get all the appObjectList where creationDate is greater than or equal to DEFAULT_CREATION_DATE
        defaultAppObjectShouldBeFound("creationDate.greaterThanOrEqual=" + DEFAULT_CREATION_DATE);

        // Get all the appObjectList where creationDate is greater than or equal to UPDATED_CREATION_DATE
        defaultAppObjectShouldNotBeFound("creationDate.greaterThanOrEqual=" + UPDATED_CREATION_DATE);
    }

    @Test
    @Transactional
    void getAllAppObjectsByCreationDateIsLessThanOrEqualToSomething() throws Exception {
        // Initialize the database
        appObjectRepository.saveAndFlush(appObject);

        // Get all the appObjectList where creationDate is less than or equal to DEFAULT_CREATION_DATE
        defaultAppObjectShouldBeFound("creationDate.lessThanOrEqual=" + DEFAULT_CREATION_DATE);

        // Get all the appObjectList where creationDate is less than or equal to SMALLER_CREATION_DATE
        defaultAppObjectShouldNotBeFound("creationDate.lessThanOrEqual=" + SMALLER_CREATION_DATE);
    }

    @Test
    @Transactional
    void getAllAppObjectsByCreationDateIsLessThanSomething() throws Exception {
        // Initialize the database
        appObjectRepository.saveAndFlush(appObject);

        // Get all the appObjectList where creationDate is less than DEFAULT_CREATION_DATE
        defaultAppObjectShouldNotBeFound("creationDate.lessThan=" + DEFAULT_CREATION_DATE);

        // Get all the appObjectList where creationDate is less than UPDATED_CREATION_DATE
        defaultAppObjectShouldBeFound("creationDate.lessThan=" + UPDATED_CREATION_DATE);
    }

    @Test
    @Transactional
    void getAllAppObjectsByCreationDateIsGreaterThanSomething() throws Exception {
        // Initialize the database
        appObjectRepository.saveAndFlush(appObject);

        // Get all the appObjectList where creationDate is greater than DEFAULT_CREATION_DATE
        defaultAppObjectShouldNotBeFound("creationDate.greaterThan=" + DEFAULT_CREATION_DATE);

        // Get all the appObjectList where creationDate is greater than SMALLER_CREATION_DATE
        defaultAppObjectShouldBeFound("creationDate.greaterThan=" + SMALLER_CREATION_DATE);
    }

    @Test
    @Transactional
    void getAllAppObjectsByParentIsEqualToSomething() throws Exception {
        AppObject parent;
        if (TestUtil.findAll(em, AppObject.class).isEmpty()) {
            appObjectRepository.saveAndFlush(appObject);
            parent = AppObjectResourceIT.createEntity(em);
        } else {
            parent = TestUtil.findAll(em, AppObject.class).get(0);
        }
        em.persist(parent);
        em.flush();
        appObject.setParent(parent);
        appObjectRepository.saveAndFlush(appObject);
        Long parentId = parent.getId();
        // Get all the appObjectList where parent equals to parentId
        defaultAppObjectShouldBeFound("parentId.equals=" + parentId);

        // Get all the appObjectList where parent equals to (parentId + 1)
        defaultAppObjectShouldNotBeFound("parentId.equals=" + (parentId + 1));
    }

    @Test
    @Transactional
    void getAllAppObjectsByProductIsEqualToSomething() throws Exception {
        Product product;
        if (TestUtil.findAll(em, Product.class).isEmpty()) {
            appObjectRepository.saveAndFlush(appObject);
            product = ProductResourceIT.createEntity(em);
        } else {
            product = TestUtil.findAll(em, Product.class).get(0);
        }
        em.persist(product);
        em.flush();
        appObject.setProduct(product);
        appObjectRepository.saveAndFlush(appObject);
        Long productId = product.getId();
        // Get all the appObjectList where product equals to productId
        defaultAppObjectShouldBeFound("productId.equals=" + productId);

        // Get all the appObjectList where product equals to (productId + 1)
        defaultAppObjectShouldNotBeFound("productId.equals=" + (productId + 1));
    }

    @Test
    @Transactional
    void getAllAppObjectsByProduct2IsEqualToSomething() throws Exception {
        Product product2;
        if (TestUtil.findAll(em, Product.class).isEmpty()) {
            appObjectRepository.saveAndFlush(appObject);
            product2 = ProductResourceIT.createEntity(em);
        } else {
            product2 = TestUtil.findAll(em, Product.class).get(0);
        }
        em.persist(product2);
        em.flush();
        appObject.setProduct2(product2);
        appObjectRepository.saveAndFlush(appObject);
        Long product2Id = product2.getId();
        // Get all the appObjectList where product2 equals to product2Id
        defaultAppObjectShouldBeFound("product2Id.equals=" + product2Id);

        // Get all the appObjectList where product2 equals to (product2Id + 1)
        defaultAppObjectShouldNotBeFound("product2Id.equals=" + (product2Id + 1));
    }

    @Test
    @Transactional
    void getAllAppObjectsByProduct3IsEqualToSomething() throws Exception {
        Product product3;
        if (TestUtil.findAll(em, Product.class).isEmpty()) {
            appObjectRepository.saveAndFlush(appObject);
            product3 = ProductResourceIT.createEntity(em);
        } else {
            product3 = TestUtil.findAll(em, Product.class).get(0);
        }
        em.persist(product3);
        em.flush();
        appObject.setProduct3(product3);
        appObjectRepository.saveAndFlush(appObject);
        Long product3Id = product3.getId();
        // Get all the appObjectList where product3 equals to product3Id
        defaultAppObjectShouldBeFound("product3Id.equals=" + product3Id);

        // Get all the appObjectList where product3 equals to (product3Id + 1)
        defaultAppObjectShouldNotBeFound("product3Id.equals=" + (product3Id + 1));
    }

    /**
     * Executes the search, and checks that the default entity is returned.
     */
    private void defaultAppObjectShouldBeFound(String filter) throws Exception {
        restAppObjectMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc&" + filter))
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
            .andExpect(jsonPath("$.[*].validUntil").value(hasItem(DEFAULT_VALID_UNTIL.toString())))
            .andExpect(jsonPath("$.[*].isValid").value(hasItem(DEFAULT_IS_VALID.booleanValue())))
            .andExpect(jsonPath("$.[*].creationDate").value(hasItem(DEFAULT_CREATION_DATE.toString())));

        // Check, that the count call also returns 1
        restAppObjectMockMvc
            .perform(get(ENTITY_API_URL + "/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string("1"));
    }

    /**
     * Executes the search, and checks that the default entity is not returned.
     */
    private void defaultAppObjectShouldNotBeFound(String filter) throws Exception {
        restAppObjectMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$").isEmpty());

        // Check, that the count call also returns 0
        restAppObjectMockMvc
            .perform(get(ENTITY_API_URL + "/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string("0"));
    }

    @Test
    @Transactional
    void getNonExistingAppObject() throws Exception {
        // Get the appObject
        restAppObjectMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putExistingAppObject() throws Exception {
        // Initialize the database
        appObjectRepository.saveAndFlush(appObject);

        int databaseSizeBeforeUpdate = appObjectRepository.findAll().size();

        // Update the appObject
        AppObject updatedAppObject = appObjectRepository.findById(appObject.getId()).orElseThrow();
        // Disconnect from session so that the updates on updatedAppObject are not directly saved in db
        em.detach(updatedAppObject);
        updatedAppObject
            .code(UPDATED_CODE)
            .name(UPDATED_NAME)
            .description(UPDATED_DESCRIPTION)
            .objectType(UPDATED_OBJECT_TYPE)
            .lastChange(UPDATED_LAST_CHANGE)
            .seq(UPDATED_SEQ)
            .status(UPDATED_STATUS)
            .quantity(UPDATED_QUANTITY)
            .validFrom(UPDATED_VALID_FROM)
            .validUntil(UPDATED_VALID_UNTIL)
            .isValid(UPDATED_IS_VALID)
            .creationDate(UPDATED_CREATION_DATE);
        AppObjectDTO appObjectDTO = appObjectMapper.toDto(updatedAppObject);

        restAppObjectMockMvc
            .perform(
                put(ENTITY_API_URL_ID, appObjectDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(appObjectDTO))
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
        assertThat(testAppObject.getIsValid()).isEqualTo(UPDATED_IS_VALID);
        assertThat(testAppObject.getCreationDate()).isEqualTo(UPDATED_CREATION_DATE);
    }

    @Test
    @Transactional
    void putNonExistingAppObject() throws Exception {
        int databaseSizeBeforeUpdate = appObjectRepository.findAll().size();
        appObject.setId(longCount.incrementAndGet());

        // Create the AppObject
        AppObjectDTO appObjectDTO = appObjectMapper.toDto(appObject);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restAppObjectMockMvc
            .perform(
                put(ENTITY_API_URL_ID, appObjectDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(appObjectDTO))
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
        appObject.setId(longCount.incrementAndGet());

        // Create the AppObject
        AppObjectDTO appObjectDTO = appObjectMapper.toDto(appObject);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restAppObjectMockMvc
            .perform(
                put(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(appObjectDTO))
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
        appObject.setId(longCount.incrementAndGet());

        // Create the AppObject
        AppObjectDTO appObjectDTO = appObjectMapper.toDto(appObject);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restAppObjectMockMvc
            .perform(put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(appObjectDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the AppObject in the database
        List<AppObject> appObjectList = appObjectRepository.findAll();
        assertThat(appObjectList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void partialUpdateAppObjectWithPatch() throws Exception {
        // Initialize the database
        appObjectRepository.saveAndFlush(appObject);

        int databaseSizeBeforeUpdate = appObjectRepository.findAll().size();

        // Update the appObject using partial update
        AppObject partialUpdatedAppObject = new AppObject();
        partialUpdatedAppObject.setId(appObject.getId());

        partialUpdatedAppObject
            .code(UPDATED_CODE)
            .description(UPDATED_DESCRIPTION)
            .objectType(UPDATED_OBJECT_TYPE)
            .lastChange(UPDATED_LAST_CHANGE)
            .seq(UPDATED_SEQ)
            .validUntil(UPDATED_VALID_UNTIL);

        restAppObjectMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedAppObject.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(partialUpdatedAppObject))
            )
            .andExpect(status().isOk());

        // Validate the AppObject in the database
        List<AppObject> appObjectList = appObjectRepository.findAll();
        assertThat(appObjectList).hasSize(databaseSizeBeforeUpdate);
        AppObject testAppObject = appObjectList.get(appObjectList.size() - 1);
        assertThat(testAppObject.getCode()).isEqualTo(UPDATED_CODE);
        assertThat(testAppObject.getName()).isEqualTo(DEFAULT_NAME);
        assertThat(testAppObject.getDescription()).isEqualTo(UPDATED_DESCRIPTION);
        assertThat(testAppObject.getObjectType()).isEqualTo(UPDATED_OBJECT_TYPE);
        assertThat(testAppObject.getLastChange()).isEqualTo(UPDATED_LAST_CHANGE);
        assertThat(testAppObject.getSeq()).isEqualTo(UPDATED_SEQ);
        assertThat(testAppObject.getStatus()).isEqualTo(DEFAULT_STATUS);
        assertThat(testAppObject.getQuantity()).isEqualTo(DEFAULT_QUANTITY);
        assertThat(testAppObject.getValidFrom()).isEqualTo(DEFAULT_VALID_FROM);
        assertThat(testAppObject.getValidUntil()).isEqualTo(UPDATED_VALID_UNTIL);
        assertThat(testAppObject.getIsValid()).isEqualTo(DEFAULT_IS_VALID);
        assertThat(testAppObject.getCreationDate()).isEqualTo(DEFAULT_CREATION_DATE);
    }

    @Test
    @Transactional
    void fullUpdateAppObjectWithPatch() throws Exception {
        // Initialize the database
        appObjectRepository.saveAndFlush(appObject);

        int databaseSizeBeforeUpdate = appObjectRepository.findAll().size();

        // Update the appObject using partial update
        AppObject partialUpdatedAppObject = new AppObject();
        partialUpdatedAppObject.setId(appObject.getId());

        partialUpdatedAppObject
            .code(UPDATED_CODE)
            .name(UPDATED_NAME)
            .description(UPDATED_DESCRIPTION)
            .objectType(UPDATED_OBJECT_TYPE)
            .lastChange(UPDATED_LAST_CHANGE)
            .seq(UPDATED_SEQ)
            .status(UPDATED_STATUS)
            .quantity(UPDATED_QUANTITY)
            .validFrom(UPDATED_VALID_FROM)
            .validUntil(UPDATED_VALID_UNTIL)
            .isValid(UPDATED_IS_VALID)
            .creationDate(UPDATED_CREATION_DATE);

        restAppObjectMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedAppObject.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(partialUpdatedAppObject))
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
        assertThat(testAppObject.getIsValid()).isEqualTo(UPDATED_IS_VALID);
        assertThat(testAppObject.getCreationDate()).isEqualTo(UPDATED_CREATION_DATE);
    }

    @Test
    @Transactional
    void patchNonExistingAppObject() throws Exception {
        int databaseSizeBeforeUpdate = appObjectRepository.findAll().size();
        appObject.setId(longCount.incrementAndGet());

        // Create the AppObject
        AppObjectDTO appObjectDTO = appObjectMapper.toDto(appObject);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restAppObjectMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, appObjectDTO.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(appObjectDTO))
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
        appObject.setId(longCount.incrementAndGet());

        // Create the AppObject
        AppObjectDTO appObjectDTO = appObjectMapper.toDto(appObject);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restAppObjectMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(appObjectDTO))
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
        appObject.setId(longCount.incrementAndGet());

        // Create the AppObject
        AppObjectDTO appObjectDTO = appObjectMapper.toDto(appObject);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restAppObjectMockMvc
            .perform(
                patch(ENTITY_API_URL).contentType("application/merge-patch+json").content(TestUtil.convertObjectToJsonBytes(appObjectDTO))
            )
            .andExpect(status().isMethodNotAllowed());

        // Validate the AppObject in the database
        List<AppObject> appObjectList = appObjectRepository.findAll();
        assertThat(appObjectList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void deleteAppObject() throws Exception {
        // Initialize the database
        appObjectRepository.saveAndFlush(appObject);

        int databaseSizeBeforeDelete = appObjectRepository.findAll().size();

        // Delete the appObject
        restAppObjectMockMvc
            .perform(delete(ENTITY_API_URL_ID, appObject.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        List<AppObject> appObjectList = appObjectRepository.findAll();
        assertThat(appObjectList).hasSize(databaseSizeBeforeDelete - 1);
    }
}

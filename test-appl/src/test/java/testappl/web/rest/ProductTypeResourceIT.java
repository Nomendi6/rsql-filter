package testappl.web.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import jakarta.persistence.EntityManager;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import testappl.IntegrationTest;
import testappl.domain.ProductType;
import testappl.domain.enumeration.StandardRecordStatus;
import testappl.repository.ProductTypeRepository;
import testappl.service.dto.ProductTypeDTO;
import testappl.service.mapper.ProductTypeMapper;

/**
 * Integration tests for the {@link ProductTypeResource} REST controller.
 */
@IntegrationTest
@AutoConfigureMockMvc
@WithMockUser
class ProductTypeResourceIT {

    private static final String DEFAULT_CODE = "AAAAAAAAAA";
    private static final String UPDATED_CODE = "BBBBBBBBBB";

    private static final String DEFAULT_NAME = "AAAAAAAAAA";
    private static final String UPDATED_NAME = "BBBBBBBBBB";

    private static final String DEFAULT_DESCRIPTION = "AAAAAAAAAA";
    private static final String UPDATED_DESCRIPTION = "BBBBBBBBBB";

    private static final Long DEFAULT_SEQ = 1L;
    private static final Long UPDATED_SEQ = 2L;
    private static final Long SMALLER_SEQ = 1L - 1L;

    private static final StandardRecordStatus DEFAULT_STATUS = StandardRecordStatus.NOT_ACTIVE;
    private static final StandardRecordStatus UPDATED_STATUS = StandardRecordStatus.ACTIVE;

    private static final Instant DEFAULT_VALID_FROM = Instant.ofEpochMilli(0L);
    private static final Instant UPDATED_VALID_FROM = Instant.now().truncatedTo(ChronoUnit.MILLIS);

    private static final Instant DEFAULT_VALID_UNTIL = Instant.ofEpochMilli(0L);
    private static final Instant UPDATED_VALID_UNTIL = Instant.now().truncatedTo(ChronoUnit.MILLIS);

    private static final String ENTITY_API_URL = "/api/product-types";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    private static Random random = new Random();
    private static AtomicLong longCount = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    @Autowired
    private ProductTypeRepository productTypeRepository;

    @Autowired
    private ProductTypeMapper productTypeMapper;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restProductTypeMockMvc;

    private ProductType productType;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static ProductType createEntity(EntityManager em) {
        ProductType productType = new ProductType()
            .code(DEFAULT_CODE)
            .name(DEFAULT_NAME)
            .description(DEFAULT_DESCRIPTION)
            .seq(DEFAULT_SEQ)
            .status(DEFAULT_STATUS)
            .validFrom(DEFAULT_VALID_FROM)
            .validUntil(DEFAULT_VALID_UNTIL);
        return productType;
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static ProductType createUpdatedEntity(EntityManager em) {
        ProductType productType = new ProductType()
            .code(UPDATED_CODE)
            .name(UPDATED_NAME)
            .description(UPDATED_DESCRIPTION)
            .seq(UPDATED_SEQ)
            .status(UPDATED_STATUS)
            .validFrom(UPDATED_VALID_FROM)
            .validUntil(UPDATED_VALID_UNTIL);
        return productType;
    }

    @BeforeEach
    public void initTest() {
        productType = createEntity(em);
    }

    @Test
    @Transactional
    void createProductType() throws Exception {
        int databaseSizeBeforeCreate = productTypeRepository.findAll().size();
        // Create the ProductType
        ProductTypeDTO productTypeDTO = productTypeMapper.toDto(productType);
        restProductTypeMockMvc
            .perform(
                post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(productTypeDTO))
            )
            .andExpect(status().isCreated());

        // Validate the ProductType in the database
        List<ProductType> productTypeList = productTypeRepository.findAll();
        assertThat(productTypeList).hasSize(databaseSizeBeforeCreate + 1);
        ProductType testProductType = productTypeList.get(productTypeList.size() - 1);
        assertThat(testProductType.getCode()).isEqualTo(DEFAULT_CODE);
        assertThat(testProductType.getName()).isEqualTo(DEFAULT_NAME);
        assertThat(testProductType.getDescription()).isEqualTo(DEFAULT_DESCRIPTION);
        assertThat(testProductType.getSeq()).isEqualTo(DEFAULT_SEQ);
        assertThat(testProductType.getStatus()).isEqualTo(DEFAULT_STATUS);
        assertThat(testProductType.getValidFrom()).isEqualTo(DEFAULT_VALID_FROM);
        assertThat(testProductType.getValidUntil()).isEqualTo(DEFAULT_VALID_UNTIL);
    }

    @Test
    @Transactional
    void createProductTypeWithExistingId() throws Exception {
        // Create the ProductType with an existing ID
        productType.setId(1L);
        ProductTypeDTO productTypeDTO = productTypeMapper.toDto(productType);

        int databaseSizeBeforeCreate = productTypeRepository.findAll().size();

        // An entity with an existing ID cannot be created, so this API call must fail
        restProductTypeMockMvc
            .perform(
                post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(productTypeDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the ProductType in the database
        List<ProductType> productTypeList = productTypeRepository.findAll();
        assertThat(productTypeList).hasSize(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    void checkCodeIsRequired() throws Exception {
        int databaseSizeBeforeTest = productTypeRepository.findAll().size();
        // set the field null
        productType.setCode(null);

        // Create the ProductType, which fails.
        ProductTypeDTO productTypeDTO = productTypeMapper.toDto(productType);

        restProductTypeMockMvc
            .perform(
                post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(productTypeDTO))
            )
            .andExpect(status().isBadRequest());

        List<ProductType> productTypeList = productTypeRepository.findAll();
        assertThat(productTypeList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkNameIsRequired() throws Exception {
        int databaseSizeBeforeTest = productTypeRepository.findAll().size();
        // set the field null
        productType.setName(null);

        // Create the ProductType, which fails.
        ProductTypeDTO productTypeDTO = productTypeMapper.toDto(productType);

        restProductTypeMockMvc
            .perform(
                post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(productTypeDTO))
            )
            .andExpect(status().isBadRequest());

        List<ProductType> productTypeList = productTypeRepository.findAll();
        assertThat(productTypeList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void getAllProductTypes() throws Exception {
        // Initialize the database
        productTypeRepository.saveAndFlush(productType);

        // Get all the productTypeList
        restProductTypeMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(productType.getId().intValue())))
            .andExpect(jsonPath("$.[*].code").value(hasItem(DEFAULT_CODE)))
            .andExpect(jsonPath("$.[*].name").value(hasItem(DEFAULT_NAME)))
            .andExpect(jsonPath("$.[*].description").value(hasItem(DEFAULT_DESCRIPTION.toString())))
            .andExpect(jsonPath("$.[*].seq").value(hasItem(DEFAULT_SEQ.intValue())))
            .andExpect(jsonPath("$.[*].status").value(hasItem(DEFAULT_STATUS.toString())))
            .andExpect(jsonPath("$.[*].validFrom").value(hasItem(DEFAULT_VALID_FROM.toString())))
            .andExpect(jsonPath("$.[*].validUntil").value(hasItem(DEFAULT_VALID_UNTIL.toString())));
    }

    @Test
    @Transactional
    void getProductType() throws Exception {
        // Initialize the database
        productTypeRepository.saveAndFlush(productType);

        // Get the productType
        restProductTypeMockMvc
            .perform(get(ENTITY_API_URL_ID, productType.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(productType.getId().intValue()))
            .andExpect(jsonPath("$.code").value(DEFAULT_CODE))
            .andExpect(jsonPath("$.name").value(DEFAULT_NAME))
            .andExpect(jsonPath("$.description").value(DEFAULT_DESCRIPTION.toString()))
            .andExpect(jsonPath("$.seq").value(DEFAULT_SEQ.intValue()))
            .andExpect(jsonPath("$.status").value(DEFAULT_STATUS.toString()))
            .andExpect(jsonPath("$.validFrom").value(DEFAULT_VALID_FROM.toString()))
            .andExpect(jsonPath("$.validUntil").value(DEFAULT_VALID_UNTIL.toString()));
    }

    @Test
    @Transactional
    void getProductTypesByIdFiltering() throws Exception {
        // Initialize the database
        productTypeRepository.saveAndFlush(productType);

        Long id = productType.getId();

        defaultProductTypeShouldBeFound("id.equals=" + id);
        defaultProductTypeShouldNotBeFound("id.notEquals=" + id);

        defaultProductTypeShouldBeFound("id.greaterThanOrEqual=" + id);
        defaultProductTypeShouldNotBeFound("id.greaterThan=" + id);

        defaultProductTypeShouldBeFound("id.lessThanOrEqual=" + id);
        defaultProductTypeShouldNotBeFound("id.lessThan=" + id);
    }

    @Test
    @Transactional
    void getAllProductTypesByCodeIsEqualToSomething() throws Exception {
        // Initialize the database
        productTypeRepository.saveAndFlush(productType);

        // Get all the productTypeList where code equals to DEFAULT_CODE
        defaultProductTypeShouldBeFound("code.equals=" + DEFAULT_CODE);

        // Get all the productTypeList where code equals to UPDATED_CODE
        defaultProductTypeShouldNotBeFound("code.equals=" + UPDATED_CODE);
    }

    @Test
    @Transactional
    void getAllProductTypesByCodeIsInShouldWork() throws Exception {
        // Initialize the database
        productTypeRepository.saveAndFlush(productType);

        // Get all the productTypeList where code in DEFAULT_CODE or UPDATED_CODE
        defaultProductTypeShouldBeFound("code.in=" + DEFAULT_CODE + "," + UPDATED_CODE);

        // Get all the productTypeList where code equals to UPDATED_CODE
        defaultProductTypeShouldNotBeFound("code.in=" + UPDATED_CODE);
    }

    @Test
    @Transactional
    void getAllProductTypesByCodeIsNullOrNotNull() throws Exception {
        // Initialize the database
        productTypeRepository.saveAndFlush(productType);

        // Get all the productTypeList where code is not null
        defaultProductTypeShouldBeFound("code.specified=true");

        // Get all the productTypeList where code is null
        defaultProductTypeShouldNotBeFound("code.specified=false");
    }

    @Test
    @Transactional
    void getAllProductTypesByCodeContainsSomething() throws Exception {
        // Initialize the database
        productTypeRepository.saveAndFlush(productType);

        // Get all the productTypeList where code contains DEFAULT_CODE
        defaultProductTypeShouldBeFound("code.contains=" + DEFAULT_CODE);

        // Get all the productTypeList where code contains UPDATED_CODE
        defaultProductTypeShouldNotBeFound("code.contains=" + UPDATED_CODE);
    }

    @Test
    @Transactional
    void getAllProductTypesByCodeNotContainsSomething() throws Exception {
        // Initialize the database
        productTypeRepository.saveAndFlush(productType);

        // Get all the productTypeList where code does not contain DEFAULT_CODE
        defaultProductTypeShouldNotBeFound("code.doesNotContain=" + DEFAULT_CODE);

        // Get all the productTypeList where code does not contain UPDATED_CODE
        defaultProductTypeShouldBeFound("code.doesNotContain=" + UPDATED_CODE);
    }

    @Test
    @Transactional
    void getAllProductTypesByNameIsEqualToSomething() throws Exception {
        // Initialize the database
        productTypeRepository.saveAndFlush(productType);

        // Get all the productTypeList where name equals to DEFAULT_NAME
        defaultProductTypeShouldBeFound("name.equals=" + DEFAULT_NAME);

        // Get all the productTypeList where name equals to UPDATED_NAME
        defaultProductTypeShouldNotBeFound("name.equals=" + UPDATED_NAME);
    }

    @Test
    @Transactional
    void getAllProductTypesByNameIsInShouldWork() throws Exception {
        // Initialize the database
        productTypeRepository.saveAndFlush(productType);

        // Get all the productTypeList where name in DEFAULT_NAME or UPDATED_NAME
        defaultProductTypeShouldBeFound("name.in=" + DEFAULT_NAME + "," + UPDATED_NAME);

        // Get all the productTypeList where name equals to UPDATED_NAME
        defaultProductTypeShouldNotBeFound("name.in=" + UPDATED_NAME);
    }

    @Test
    @Transactional
    void getAllProductTypesByNameIsNullOrNotNull() throws Exception {
        // Initialize the database
        productTypeRepository.saveAndFlush(productType);

        // Get all the productTypeList where name is not null
        defaultProductTypeShouldBeFound("name.specified=true");

        // Get all the productTypeList where name is null
        defaultProductTypeShouldNotBeFound("name.specified=false");
    }

    @Test
    @Transactional
    void getAllProductTypesByNameContainsSomething() throws Exception {
        // Initialize the database
        productTypeRepository.saveAndFlush(productType);

        // Get all the productTypeList where name contains DEFAULT_NAME
        defaultProductTypeShouldBeFound("name.contains=" + DEFAULT_NAME);

        // Get all the productTypeList where name contains UPDATED_NAME
        defaultProductTypeShouldNotBeFound("name.contains=" + UPDATED_NAME);
    }

    @Test
    @Transactional
    void getAllProductTypesByNameNotContainsSomething() throws Exception {
        // Initialize the database
        productTypeRepository.saveAndFlush(productType);

        // Get all the productTypeList where name does not contain DEFAULT_NAME
        defaultProductTypeShouldNotBeFound("name.doesNotContain=" + DEFAULT_NAME);

        // Get all the productTypeList where name does not contain UPDATED_NAME
        defaultProductTypeShouldBeFound("name.doesNotContain=" + UPDATED_NAME);
    }

    @Test
    @Transactional
    void getAllProductTypesBySeqIsEqualToSomething() throws Exception {
        // Initialize the database
        productTypeRepository.saveAndFlush(productType);

        // Get all the productTypeList where seq equals to DEFAULT_SEQ
        defaultProductTypeShouldBeFound("seq.equals=" + DEFAULT_SEQ);

        // Get all the productTypeList where seq equals to UPDATED_SEQ
        defaultProductTypeShouldNotBeFound("seq.equals=" + UPDATED_SEQ);
    }

    @Test
    @Transactional
    void getAllProductTypesBySeqIsInShouldWork() throws Exception {
        // Initialize the database
        productTypeRepository.saveAndFlush(productType);

        // Get all the productTypeList where seq in DEFAULT_SEQ or UPDATED_SEQ
        defaultProductTypeShouldBeFound("seq.in=" + DEFAULT_SEQ + "," + UPDATED_SEQ);

        // Get all the productTypeList where seq equals to UPDATED_SEQ
        defaultProductTypeShouldNotBeFound("seq.in=" + UPDATED_SEQ);
    }

    @Test
    @Transactional
    void getAllProductTypesBySeqIsNullOrNotNull() throws Exception {
        // Initialize the database
        productTypeRepository.saveAndFlush(productType);

        // Get all the productTypeList where seq is not null
        defaultProductTypeShouldBeFound("seq.specified=true");

        // Get all the productTypeList where seq is null
        defaultProductTypeShouldNotBeFound("seq.specified=false");
    }

    @Test
    @Transactional
    void getAllProductTypesBySeqIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        productTypeRepository.saveAndFlush(productType);

        // Get all the productTypeList where seq is greater than or equal to DEFAULT_SEQ
        defaultProductTypeShouldBeFound("seq.greaterThanOrEqual=" + DEFAULT_SEQ);

        // Get all the productTypeList where seq is greater than or equal to UPDATED_SEQ
        defaultProductTypeShouldNotBeFound("seq.greaterThanOrEqual=" + UPDATED_SEQ);
    }

    @Test
    @Transactional
    void getAllProductTypesBySeqIsLessThanOrEqualToSomething() throws Exception {
        // Initialize the database
        productTypeRepository.saveAndFlush(productType);

        // Get all the productTypeList where seq is less than or equal to DEFAULT_SEQ
        defaultProductTypeShouldBeFound("seq=le=" + DEFAULT_SEQ);

        // Get all the productTypeList where seq is less than or equal to SMALLER_SEQ
        defaultProductTypeShouldNotBeFound("seq=le=" + SMALLER_SEQ);
    }

    @Test
    @Transactional
    void getAllProductTypesBySeqIsLessThanSomething() throws Exception {
        // Initialize the database
        productTypeRepository.saveAndFlush(productType);

        // Get all the productTypeList where seq is less than DEFAULT_SEQ
        defaultProductTypeShouldNotBeFound("seq.lessThan=" + DEFAULT_SEQ);

        // Get all the productTypeList where seq is less than UPDATED_SEQ
        defaultProductTypeShouldBeFound("seq.lessThan=" + UPDATED_SEQ);
    }

    @Test
    @Transactional
    void getAllProductTypesBySeqIsGreaterThanSomething() throws Exception {
        // Initialize the database
        productTypeRepository.saveAndFlush(productType);

        // Get all the productTypeList where seq is greater than DEFAULT_SEQ
        defaultProductTypeShouldNotBeFound("seq.greaterThan=" + DEFAULT_SEQ);

        // Get all the productTypeList where seq is greater than SMALLER_SEQ
        defaultProductTypeShouldBeFound("seq.greaterThan=" + SMALLER_SEQ);
    }

    @Test
    @Transactional
    void getAllProductTypesByStatusIsEqualToSomething() throws Exception {
        // Initialize the database
        productTypeRepository.saveAndFlush(productType);

        // Get all the productTypeList where status equals to DEFAULT_STATUS
        defaultProductTypeShouldBeFound("status.equals=" + DEFAULT_STATUS);

        // Get all the productTypeList where status equals to UPDATED_STATUS
        defaultProductTypeShouldNotBeFound("status.equals=" + UPDATED_STATUS);
    }

    @Test
    @Transactional
    void getAllProductTypesByStatusIsInShouldWork() throws Exception {
        // Initialize the database
        productTypeRepository.saveAndFlush(productType);

        // Get all the productTypeList where status in DEFAULT_STATUS or UPDATED_STATUS
        defaultProductTypeShouldBeFound("status.in=" + DEFAULT_STATUS + "," + UPDATED_STATUS);

        // Get all the productTypeList where status equals to UPDATED_STATUS
        defaultProductTypeShouldNotBeFound("status.in=" + UPDATED_STATUS);
    }

    @Test
    @Transactional
    void getAllProductTypesByStatusIsNullOrNotNull() throws Exception {
        // Initialize the database
        productTypeRepository.saveAndFlush(productType);

        // Get all the productTypeList where status is not null
        defaultProductTypeShouldBeFound("status.specified=true");

        // Get all the productTypeList where status is null
        defaultProductTypeShouldNotBeFound("status.specified=false");
    }

    @Test
    @Transactional
    void getAllProductTypesByValidFromIsEqualToSomething() throws Exception {
        // Initialize the database
        productTypeRepository.saveAndFlush(productType);

        // Get all the productTypeList where validFrom equals to DEFAULT_VALID_FROM
        defaultProductTypeShouldBeFound("validFrom.equals=" + DEFAULT_VALID_FROM);

        // Get all the productTypeList where validFrom equals to UPDATED_VALID_FROM
        defaultProductTypeShouldNotBeFound("validFrom.equals=" + UPDATED_VALID_FROM);
    }

    @Test
    @Transactional
    void getAllProductTypesByValidFromIsInShouldWork() throws Exception {
        // Initialize the database
        productTypeRepository.saveAndFlush(productType);

        // Get all the productTypeList where validFrom in DEFAULT_VALID_FROM or UPDATED_VALID_FROM
        defaultProductTypeShouldBeFound("validFrom.in=" + DEFAULT_VALID_FROM + "," + UPDATED_VALID_FROM);

        // Get all the productTypeList where validFrom equals to UPDATED_VALID_FROM
        defaultProductTypeShouldNotBeFound("validFrom.in=" + UPDATED_VALID_FROM);
    }

    @Test
    @Transactional
    void getAllProductTypesByValidFromIsNullOrNotNull() throws Exception {
        // Initialize the database
        productTypeRepository.saveAndFlush(productType);

        // Get all the productTypeList where validFrom is not null
        defaultProductTypeShouldBeFound("validFrom.specified=true");

        // Get all the productTypeList where validFrom is null
        defaultProductTypeShouldNotBeFound("validFrom.specified=false");
    }

    @Test
    @Transactional
    void getAllProductTypesByValidUntilIsEqualToSomething() throws Exception {
        // Initialize the database
        productTypeRepository.saveAndFlush(productType);

        // Get all the productTypeList where validUntil equals to DEFAULT_VALID_UNTIL
        defaultProductTypeShouldBeFound("validUntil.equals=" + DEFAULT_VALID_UNTIL);

        // Get all the productTypeList where validUntil equals to UPDATED_VALID_UNTIL
        defaultProductTypeShouldNotBeFound("validUntil.equals=" + UPDATED_VALID_UNTIL);
    }

    @Test
    @Transactional
    void getAllProductTypesByValidUntilIsInShouldWork() throws Exception {
        // Initialize the database
        productTypeRepository.saveAndFlush(productType);

        // Get all the productTypeList where validUntil in DEFAULT_VALID_UNTIL or UPDATED_VALID_UNTIL
        defaultProductTypeShouldBeFound("validUntil.in=" + DEFAULT_VALID_UNTIL + "," + UPDATED_VALID_UNTIL);

        // Get all the productTypeList where validUntil equals to UPDATED_VALID_UNTIL
        defaultProductTypeShouldNotBeFound("validUntil.in=" + UPDATED_VALID_UNTIL);
    }

    @Test
    @Transactional
    void getAllProductTypesByValidUntilIsNullOrNotNull() throws Exception {
        // Initialize the database
        productTypeRepository.saveAndFlush(productType);

        // Get all the productTypeList where validUntil is not null
        defaultProductTypeShouldBeFound("validUntil.specified=true");

        // Get all the productTypeList where validUntil is null
        defaultProductTypeShouldNotBeFound("validUntil.specified=false");
    }

    /**
     * Executes the search, and checks that the default entity is returned.
     */
    private void defaultProductTypeShouldBeFound(String filter) throws Exception {
        restProductTypeMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(productType.getId().intValue())))
            .andExpect(jsonPath("$.[*].code").value(hasItem(DEFAULT_CODE)))
            .andExpect(jsonPath("$.[*].name").value(hasItem(DEFAULT_NAME)))
            .andExpect(jsonPath("$.[*].description").value(hasItem(DEFAULT_DESCRIPTION.toString())))
            .andExpect(jsonPath("$.[*].seq").value(hasItem(DEFAULT_SEQ.intValue())))
            .andExpect(jsonPath("$.[*].status").value(hasItem(DEFAULT_STATUS.toString())))
            .andExpect(jsonPath("$.[*].validFrom").value(hasItem(DEFAULT_VALID_FROM.toString())))
            .andExpect(jsonPath("$.[*].validUntil").value(hasItem(DEFAULT_VALID_UNTIL.toString())));

        // Check, that the count call also returns 1
        restProductTypeMockMvc
            .perform(get(ENTITY_API_URL + "/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string("1"));
    }

    /**
     * Executes the search, and checks that the default entity is not returned.
     */
    private void defaultProductTypeShouldNotBeFound(String filter) throws Exception {
        restProductTypeMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$").isEmpty());

        // Check, that the count call also returns 0
        restProductTypeMockMvc
            .perform(get(ENTITY_API_URL + "/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string("0"));
    }

    @Test
    @Transactional
    void getNonExistingProductType() throws Exception {
        // Get the productType
        restProductTypeMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putExistingProductType() throws Exception {
        // Initialize the database
        productTypeRepository.saveAndFlush(productType);

        int databaseSizeBeforeUpdate = productTypeRepository.findAll().size();

        // Update the productType
        ProductType updatedProductType = productTypeRepository.findById(productType.getId()).orElseThrow();
        // Disconnect from session so that the updates on updatedProductType are not directly saved in db
        em.detach(updatedProductType);
        updatedProductType
            .code(UPDATED_CODE)
            .name(UPDATED_NAME)
            .description(UPDATED_DESCRIPTION)
            .seq(UPDATED_SEQ)
            .status(UPDATED_STATUS)
            .validFrom(UPDATED_VALID_FROM)
            .validUntil(UPDATED_VALID_UNTIL);
        ProductTypeDTO productTypeDTO = productTypeMapper.toDto(updatedProductType);

        restProductTypeMockMvc
            .perform(
                put(ENTITY_API_URL_ID, productTypeDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(productTypeDTO))
            )
            .andExpect(status().isOk());

        // Validate the ProductType in the database
        List<ProductType> productTypeList = productTypeRepository.findAll();
        assertThat(productTypeList).hasSize(databaseSizeBeforeUpdate);
        ProductType testProductType = productTypeList.get(productTypeList.size() - 1);
        assertThat(testProductType.getCode()).isEqualTo(UPDATED_CODE);
        assertThat(testProductType.getName()).isEqualTo(UPDATED_NAME);
        assertThat(testProductType.getDescription()).isEqualTo(UPDATED_DESCRIPTION);
        assertThat(testProductType.getSeq()).isEqualTo(UPDATED_SEQ);
        assertThat(testProductType.getStatus()).isEqualTo(UPDATED_STATUS);
        assertThat(testProductType.getValidFrom()).isEqualTo(UPDATED_VALID_FROM);
        assertThat(testProductType.getValidUntil()).isEqualTo(UPDATED_VALID_UNTIL);
    }

    @Test
    @Transactional
    void putNonExistingProductType() throws Exception {
        int databaseSizeBeforeUpdate = productTypeRepository.findAll().size();
        productType.setId(longCount.incrementAndGet());

        // Create the ProductType
        ProductTypeDTO productTypeDTO = productTypeMapper.toDto(productType);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restProductTypeMockMvc
            .perform(
                put(ENTITY_API_URL_ID, productTypeDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(productTypeDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the ProductType in the database
        List<ProductType> productTypeList = productTypeRepository.findAll();
        assertThat(productTypeList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithIdMismatchProductType() throws Exception {
        int databaseSizeBeforeUpdate = productTypeRepository.findAll().size();
        productType.setId(longCount.incrementAndGet());

        // Create the ProductType
        ProductTypeDTO productTypeDTO = productTypeMapper.toDto(productType);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restProductTypeMockMvc
            .perform(
                put(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(productTypeDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the ProductType in the database
        List<ProductType> productTypeList = productTypeRepository.findAll();
        assertThat(productTypeList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamProductType() throws Exception {
        int databaseSizeBeforeUpdate = productTypeRepository.findAll().size();
        productType.setId(longCount.incrementAndGet());

        // Create the ProductType
        ProductTypeDTO productTypeDTO = productTypeMapper.toDto(productType);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restProductTypeMockMvc
            .perform(put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(productTypeDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the ProductType in the database
        List<ProductType> productTypeList = productTypeRepository.findAll();
        assertThat(productTypeList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void partialUpdateProductTypeWithPatch() throws Exception {
        // Initialize the database
        productTypeRepository.saveAndFlush(productType);

        int databaseSizeBeforeUpdate = productTypeRepository.findAll().size();

        // Update the productType using partial update
        ProductType partialUpdatedProductType = new ProductType();
        partialUpdatedProductType.setId(productType.getId());

        partialUpdatedProductType.code(UPDATED_CODE).name(UPDATED_NAME);

        restProductTypeMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedProductType.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(partialUpdatedProductType))
            )
            .andExpect(status().isOk());

        // Validate the ProductType in the database
        List<ProductType> productTypeList = productTypeRepository.findAll();
        assertThat(productTypeList).hasSize(databaseSizeBeforeUpdate);
        ProductType testProductType = productTypeList.get(productTypeList.size() - 1);
        assertThat(testProductType.getCode()).isEqualTo(UPDATED_CODE);
        assertThat(testProductType.getName()).isEqualTo(UPDATED_NAME);
        assertThat(testProductType.getDescription()).isEqualTo(DEFAULT_DESCRIPTION);
        assertThat(testProductType.getSeq()).isEqualTo(DEFAULT_SEQ);
        assertThat(testProductType.getStatus()).isEqualTo(DEFAULT_STATUS);
        assertThat(testProductType.getValidFrom()).isEqualTo(DEFAULT_VALID_FROM);
        assertThat(testProductType.getValidUntil()).isEqualTo(DEFAULT_VALID_UNTIL);
    }

    @Test
    @Transactional
    void fullUpdateProductTypeWithPatch() throws Exception {
        // Initialize the database
        productTypeRepository.saveAndFlush(productType);

        int databaseSizeBeforeUpdate = productTypeRepository.findAll().size();

        // Update the productType using partial update
        ProductType partialUpdatedProductType = new ProductType();
        partialUpdatedProductType.setId(productType.getId());

        partialUpdatedProductType
            .code(UPDATED_CODE)
            .name(UPDATED_NAME)
            .description(UPDATED_DESCRIPTION)
            .seq(UPDATED_SEQ)
            .status(UPDATED_STATUS)
            .validFrom(UPDATED_VALID_FROM)
            .validUntil(UPDATED_VALID_UNTIL);

        restProductTypeMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedProductType.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(partialUpdatedProductType))
            )
            .andExpect(status().isOk());

        // Validate the ProductType in the database
        List<ProductType> productTypeList = productTypeRepository.findAll();
        assertThat(productTypeList).hasSize(databaseSizeBeforeUpdate);
        ProductType testProductType = productTypeList.get(productTypeList.size() - 1);
        assertThat(testProductType.getCode()).isEqualTo(UPDATED_CODE);
        assertThat(testProductType.getName()).isEqualTo(UPDATED_NAME);
        assertThat(testProductType.getDescription()).isEqualTo(UPDATED_DESCRIPTION);
        assertThat(testProductType.getSeq()).isEqualTo(UPDATED_SEQ);
        assertThat(testProductType.getStatus()).isEqualTo(UPDATED_STATUS);
        assertThat(testProductType.getValidFrom()).isEqualTo(UPDATED_VALID_FROM);
        assertThat(testProductType.getValidUntil()).isEqualTo(UPDATED_VALID_UNTIL);
    }

    @Test
    @Transactional
    void patchNonExistingProductType() throws Exception {
        int databaseSizeBeforeUpdate = productTypeRepository.findAll().size();
        productType.setId(longCount.incrementAndGet());

        // Create the ProductType
        ProductTypeDTO productTypeDTO = productTypeMapper.toDto(productType);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restProductTypeMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, productTypeDTO.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(productTypeDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the ProductType in the database
        List<ProductType> productTypeList = productTypeRepository.findAll();
        assertThat(productTypeList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithIdMismatchProductType() throws Exception {
        int databaseSizeBeforeUpdate = productTypeRepository.findAll().size();
        productType.setId(longCount.incrementAndGet());

        // Create the ProductType
        ProductTypeDTO productTypeDTO = productTypeMapper.toDto(productType);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restProductTypeMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(productTypeDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the ProductType in the database
        List<ProductType> productTypeList = productTypeRepository.findAll();
        assertThat(productTypeList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamProductType() throws Exception {
        int databaseSizeBeforeUpdate = productTypeRepository.findAll().size();
        productType.setId(longCount.incrementAndGet());

        // Create the ProductType
        ProductTypeDTO productTypeDTO = productTypeMapper.toDto(productType);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restProductTypeMockMvc
            .perform(
                patch(ENTITY_API_URL).contentType("application/merge-patch+json").content(TestUtil.convertObjectToJsonBytes(productTypeDTO))
            )
            .andExpect(status().isMethodNotAllowed());

        // Validate the ProductType in the database
        List<ProductType> productTypeList = productTypeRepository.findAll();
        assertThat(productTypeList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void deleteProductType() throws Exception {
        // Initialize the database
        productTypeRepository.saveAndFlush(productType);

        int databaseSizeBeforeDelete = productTypeRepository.findAll().size();

        // Delete the productType
        restProductTypeMockMvc
            .perform(delete(ENTITY_API_URL_ID, productType.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        List<ProductType> productTypeList = productTypeRepository.findAll();
        assertThat(productTypeList).hasSize(databaseSizeBeforeDelete - 1);
    }
}

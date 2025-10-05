package com.nomendi6.rsql.it;

import com.nomendi6.rsql.it.config.IntegrationTest;
import com.nomendi6.rsql.it.domain.Product;
import com.nomendi6.rsql.it.domain.ProductType;
import com.nomendi6.rsql.it.domain.StandardRecordStatus;
import com.nomendi6.rsql.it.repository.ProductRepository;
import com.nomendi6.rsql.it.repository.ProductTypeRepository;
import com.nomendi6.rsql.it.service.dto.ProductDTO;
import com.nomendi6.rsql.it.service.mapper.ProductMapper;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import rsql.RsqlQueryService;
import rsql.dto.LovDTO;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for getLOV and getLOVWithSelect methods.
 * These tests ensure that SELECT and WHERE clauses can share JOINs without causing
 * "Already registered a copy: SqmSingularJoin" errors.
 */
@IntegrationTest
public class GetLOVWithSelectIT {

    @Autowired
    private EntityManager em;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ProductTypeRepository productTypeRepository;

    @Autowired
    private ProductMapper productMapper;

    private RsqlQueryService<Product, ProductDTO, ProductRepository, ProductMapper> queryService;

    @BeforeEach
    @Transactional
    void setup() {
        queryService = new RsqlQueryService<>(productRepository, productMapper, em, Product.class);

        // Create test data
        ProductType type1 = new ProductType()
            .withCode("TYPE1")
            .withName("Type One")
            .withDescription("First product type");
        type1 = productTypeRepository.save(type1);

        ProductType type2 = new ProductType()
            .withCode("TYPE2")
            .withName("Type Two")
            .withDescription("Second product type");
        type2 = productTypeRepository.save(type2);

        Product product1 = new Product()
            .withCode("PROD1")
            .withName("Product One")
            .withDescription("First product")
            .withPrice(new BigDecimal("100.00"))
            .withStatus(StandardRecordStatus.ACTIVE)
            .withProductType(type1);
        productRepository.save(product1);

        Product product2 = new Product()
            .withCode("PROD2")
            .withName("Product Two")
            .withDescription("Second product")
            .withPrice(new BigDecimal("200.00"))
            .withStatus(StandardRecordStatus.ACTIVE)
            .withProductType(type1);
        productRepository.save(product2);

        Product product3 = new Product()
            .withCode("PROD3")
            .withName("Product Three")
            .withDescription("Third product")
            .withPrice(new BigDecimal("300.00"))
            .withStatus(StandardRecordStatus.ACTIVE)
            .withProductType(type2);
        productRepository.save(product3);
    }

    @AfterEach
    @Transactional
    void cleanup() {
        productRepository.deleteAll();
        productTypeRepository.deleteAll();
    }

    /**
     * Test case that reproduces the "Already registered a copy: SqmSingularJoin" bug.
     *
     * This test uses the SAME JOIN (productType) in both SELECT and WHERE clauses:
     * - SELECT: id, productType.code, productType.name
     * - WHERE: productType.code=='TYPE1'
     *
     * Before the fix, this would cause Hibernate to register the same JOIN twice,
     * resulting in an IllegalArgumentException.
     *
     * After the fix, both clauses share the same JOIN from rsqlContext.joinsMap.
     */
    @Test
    void testGetLOVWithSelect_sharedJoinBetweenSelectAndWhere() {
        // CRITICAL TEST: SELECT and WHERE both reference productType
        // This triggers JOIN sharing and would fail before the fix
        String selectString = "id, productType.code, productType.name";
        String filter = "productType.code=='TYPE1'";
        Pageable pageable = PageRequest.of(0, 10, Sort.by("id"));

        // This should NOT throw "Already registered a copy: SqmSingularJoin"
        List<LovDTO> result = queryService.getLOVWithSelect(selectString, filter, pageable);

        // Verify results
        assertThat(result).isNotNull();
        assertThat(result).hasSize(2); // PROD1 and PROD2 have TYPE1
        assertThat(result.get(0).getCode()).isEqualTo("TYPE1");
        assertThat(result.get(1).getCode()).isEqualTo("TYPE1");
    }

    /**
     * Test with navigation property in WHERE but not in SELECT.
     * This ensures that JOINs created for WHERE are properly cached.
     */
    @Test
    void testGetLOVWithSelect_joinOnlyInWhere() {
        String selectString = "id, code, name";
        String filter = "productType.code=='TYPE2'";
        Pageable pageable = PageRequest.of(0, 10, Sort.by("id"));

        List<LovDTO> result = queryService.getLOVWithSelect(selectString, filter, pageable);

        assertThat(result).isNotNull();
        assertThat(result).hasSize(1); // Only PROD3 has TYPE2
        assertThat(result.get(0).getCode()).isEqualTo("PROD3");
    }

    /**
     * Test with navigation property in SELECT but not in WHERE.
     * This ensures that JOINs created for SELECT work independently.
     */
    @Test
    void testGetLOVWithSelect_joinOnlyInSelect() {
        String selectString = "id, productType.code:code, productType.name:name";
        String filter = "price=gt=150";
        Pageable pageable = PageRequest.of(0, 10, Sort.by("id"));

        List<LovDTO> result = queryService.getLOVWithSelect(selectString, filter, pageable);

        assertThat(result).isNotNull();
        assertThat(result).hasSize(2); // PROD2 (200) and PROD3 (300)
        assertThat(result.get(0).getCode()).isEqualTo("TYPE1"); // PROD2
        assertThat(result.get(1).getCode()).isEqualTo("TYPE2"); // PROD3
    }

    /**
     * Test with MULTIPLE occurrences of the same JOIN in SELECT.
     * This ensures that even multiple references to the same relation
     * in SELECT share a single JOIN.
     */
    @Test
    void testGetLOVWithSelect_multipleReferencesToSameJoin() {
        String selectString = "id, productType.code:code, productType.name:name";
        String filter = "productType.code=='TYPE1' and productType.name=='Type One'";
        Pageable pageable = PageRequest.of(0, 10, Sort.by("id"));

        List<LovDTO> result = queryService.getLOVWithSelect(selectString, filter, pageable);

        assertThat(result).isNotNull();
        assertThat(result).hasSize(2); // PROD1 and PROD2
    }

    /**
     * Test calling getLOVWithSelect TWICE in a row to ensure rsqlContext
     * is properly cleared between queries.
     *
     * This reproduces the scenario where rsqlContext.joinsMap contains
     * stale JOINs from a previous query.
     */
    @Test
    void testGetLOVWithSelect_consecutiveCalls() {
        Pageable pageable = PageRequest.of(0, 10, Sort.by("id"));

        // FIRST call
        List<LovDTO> result1 = queryService.getLOVWithSelect(
            "id, productType.code, productType.name",
            "productType.code=='TYPE1'",
            pageable
        );
        assertThat(result1).hasSize(2);

        // SECOND call - should work correctly with fresh rsqlContext
        List<LovDTO> result2 = queryService.getLOVWithSelect(
            "id, productType.code, productType.name",
            "productType.code=='TYPE2'",
            pageable
        );
        assertThat(result2).hasSize(1);
    }

    /**
     * Test with complex filter combining multiple conditions on the same JOIN.
     * This ensures that the shared JOIN works with compound WHERE conditions.
     */
    @Test
    void testGetLOVWithSelect_complexFilterOnSharedJoin() {
        String selectString = "id, productType.code, productType.name";
        String filter = "productType.code=='TYPE1' and price=gt=150";
        Pageable pageable = PageRequest.of(0, 10, Sort.by("id"));

        List<LovDTO> result = queryService.getLOVWithSelect(selectString, filter, pageable);

        assertThat(result).isNotNull();
        assertThat(result).hasSize(1); // Only PROD2 (TYPE1 and price=200)
        assertThat(result.get(0).getCode()).isEqualTo("TYPE1");
    }

    /**
     * Edge case: Empty filter with navigation property in SELECT.
     * Ensures that JOIN caching works even without WHERE clause.
     */
    @Test
    void testGetLOVWithSelect_noFilter() {
        String selectString = "id, productType.code, productType.name";
        String filter = "";
        Pageable pageable = PageRequest.of(0, 10, Sort.by("id"));

        List<LovDTO> result = queryService.getLOVWithSelect(selectString, filter, pageable);

        assertThat(result).isNotNull();
        assertThat(result).hasSize(3); // All products
    }
}

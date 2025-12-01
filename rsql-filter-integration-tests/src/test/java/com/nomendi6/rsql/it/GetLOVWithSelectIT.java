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

    // ==================== Tests for getTupleAsPageWithSelect() ====================

    /**
     * Test getTupleAsPageWithSelect() with shared JOIN between SELECT and WHERE.
     * This tests the non-aggregate paginated query with JOIN sharing.
     *
     * CRITICAL: Tests the fix for count query JOIN conflict when using Page<Tuple>.
     */
    @Test
    void testGetTupleAsPageWithSelect_sharedJoinBetweenSelectAndWhere() {
        // Given: SELECT and WHERE both reference productType
        String selectString = "code:productCode, name:productName, productType.code:typeCode, productType.name:typeName";
        String filter = "productType.code=='TYPE1'";
        Pageable pageable = PageRequest.of(0, 10, Sort.by("code"));

        // When
        jakarta.persistence.Tuple firstResult = queryService.getTupleAsPageWithSelect(selectString, filter, pageable)
            .getContent().get(0);

        // Then: Should return paginated results without JOIN conflict
        org.springframework.data.domain.Page<jakarta.persistence.Tuple> page =
            queryService.getTupleAsPageWithSelect(selectString, filter, pageable);

        assertThat(page).isNotNull();
        assertThat(page.getTotalElements()).isEqualTo(2); // PROD1 and PROD2 have TYPE1
        assertThat(page.getTotalPages()).isEqualTo(1);
        assertThat(page.getContent()).hasSize(2);

        // Verify data integrity
        jakarta.persistence.Tuple first = page.getContent().get(0);
        assertThat(first.get("typeCode")).isEqualTo("TYPE1");
        assertThat(first.get("typeName")).isEqualTo("Type One");
    }

    /**
     * Test getTupleAsPageWithSelect() with pagination and shared JOIN.
     * Tests page boundaries and count accuracy.
     */
    @Test
    void testGetTupleAsPageWithSelect_pagination() {
        // Given: Page size of 1, SELECT and WHERE both use productType
        String selectString = "code, price, productType.name:typeName";
        String filter = "productType.name=='Type One'"; // Only TYPE1 products
        Pageable firstPage = PageRequest.of(0, 1, Sort.by("code"));

        // When: Get first page
        org.springframework.data.domain.Page<jakarta.persistence.Tuple> page1 =
            queryService.getTupleAsPageWithSelect(selectString, filter, firstPage);

        // Then: First page should have correct metadata
        assertThat(page1.getTotalElements()).isEqualTo(2);
        assertThat(page1.getTotalPages()).isEqualTo(2);
        assertThat(page1.getNumber()).isEqualTo(0);
        assertThat(page1.getContent()).hasSize(1);
        assertThat(page1.getContent().get(0).get("code")).isEqualTo("PROD1");

        // When: Get second page
        Pageable secondPage = PageRequest.of(1, 1, Sort.by("code"));
        org.springframework.data.domain.Page<jakarta.persistence.Tuple> page2 =
            queryService.getTupleAsPageWithSelect(selectString, filter, secondPage);

        // Then: Second page should also work correctly
        assertThat(page2.getTotalElements()).isEqualTo(2);
        assertThat(page2.getTotalPages()).isEqualTo(2);
        assertThat(page2.getNumber()).isEqualTo(1);
        assertThat(page2.getContent()).hasSize(1);
        assertThat(page2.getContent().get(0).get("code")).isEqualTo("PROD2");
    }

    /**
     * Test getTupleAsPageWithSelect() with complex filter on shared JOIN.
     * This ensures that complex WHERE conditions work with JOIN sharing.
     */
    @Test
    void testGetTupleAsPageWithSelect_complexFilterOnSharedJoin() {
        // Given: Complex filter using multiple fields from same JOIN
        String selectString = "code, productType.code:typeCode, productType.name:typeName";
        String filter = "productType.code=='TYPE1';price=gt=150"; // TYPE1 AND price > 150
        Pageable pageable = PageRequest.of(0, 10);

        // When
        org.springframework.data.domain.Page<jakarta.persistence.Tuple> page =
            queryService.getTupleAsPageWithSelect(selectString, filter, pageable);

        // Then: Only PROD2 matches (TYPE1 with price 200)
        assertThat(page.getTotalElements()).isEqualTo(1);
        assertThat(page.getContent()).hasSize(1);

        jakarta.persistence.Tuple result = page.getContent().get(0);
        assertThat(result.get("code")).isEqualTo("PROD2");
        assertThat(result.get("typeCode")).isEqualTo("TYPE1");
    }

    /**
     * Test getTupleAsPageWithSelect() with multiple JOINs in SELECT.
     * This tests that multiple different navigation properties work correctly.
     */
    @Test
    void testGetTupleAsPageWithSelect_multipleJoinsInSelect() {
        // Given: SELECT uses productType multiple times
        String selectString = "id, code, productType.id:typeId, productType.code:typeCode, productType.name:typeName";
        String filter = "price=gt=100"; // Filter on simple field, not JOIN
        Pageable pageable = PageRequest.of(0, 10, Sort.by("code"));

        // When
        org.springframework.data.domain.Page<jakarta.persistence.Tuple> page =
            queryService.getTupleAsPageWithSelect(selectString, filter, pageable);

        // Then: PROD2 (200) and PROD3 (300)
        assertThat(page.getTotalElements()).isEqualTo(2);
        assertThat(page.getContent()).hasSize(2);

        // Verify all fields are populated
        jakarta.persistence.Tuple first = page.getContent().get(0);
        assertThat(first.get("typeId")).isNotNull();
        assertThat(first.get("typeCode")).isNotNull();
        assertThat(first.get("typeName")).isNotNull();
    }

    /**
     * Test getTupleAsPageWithSelect() with empty result.
     * Ensures pagination metadata is correct even with no results.
     */
    @Test
    void testGetTupleAsPageWithSelect_emptyResult() {
        // Given: Filter that matches no records
        String selectString = "code, productType.code:typeCode";
        String filter = "productType.code=='NONEXISTENT'";
        Pageable pageable = PageRequest.of(0, 10);

        // When
        org.springframework.data.domain.Page<jakarta.persistence.Tuple> page =
            queryService.getTupleAsPageWithSelect(selectString, filter, pageable);

        // Then
        assertThat(page.getTotalElements()).isEqualTo(0);
        assertThat(page.getTotalPages()).isEqualTo(0);
        assertThat(page.getContent()).isEmpty();
    }
}

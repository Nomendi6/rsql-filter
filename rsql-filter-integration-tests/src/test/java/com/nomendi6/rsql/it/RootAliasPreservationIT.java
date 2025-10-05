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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import rsql.RsqlQueryService;
import rsql.dto.LovDTO;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for root alias preservation in WHERE clauses.
 * These tests ensure that the root alias (a0 or custom) is properly preserved
 * when using navigation properties in filters.
 */
@IntegrationTest
public class RootAliasPreservationIT {

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
     * Test that default alias "a0" is used in WHERE clause with navigation properties.
     * This reproduces the user's bug where WHERE was generating:
     * "customerOrg.id=:p1" instead of "a0.customerOrg.id=:p1"
     */
    @Test
    void testDefaultAliasA0WithNavigationProperty() {
        String selectString = "id, code, name";
        String filter = "productType.code=='TYPE1'";
        Pageable pageable = PageRequest.of(0, 10, Sort.by("id"));

        // This should generate WHERE with: a0.productType.code=:p1
        List<LovDTO> result = queryService.getLOVWithSelect(selectString, filter, pageable);

        assertThat(result).isNotNull();
        assertThat(result).hasSize(2); // PROD1 and PROD2 have TYPE1
    }

    /**
     * Test that SELECT with navigation properties works with preserved alias.
     * This test verifies that both SELECT and WHERE use navigation properties correctly.
     */
    @Test
    void testSelectWithNavigationPropertyAndFilter() {
        String selectString = "id, productType.code, productType.name";
        String filter = "productType.code=='TYPE2'";
        Pageable pageable = PageRequest.of(0, 10, Sort.by("id"));

        List<LovDTO> result = queryService.getLOVWithSelect(selectString, filter, pageable);

        assertThat(result).isNotNull();
        assertThat(result).hasSize(1); // PROD3 has TYPE2
        assertThat(result.get(0).getCode()).isEqualTo("TYPE2"); // productType.code is returned in code field
    }

    /**
     * Test using getLOVWithSelect with multiple navigation property references.
     * This ensures that the same navigation path reuses JOINs correctly.
     */
    @Test
    void testMultipleSelectFieldsFromSameJoin() {
        String selectString = "id, productType.code:typeCode, productType.name:typeName";
        String filter = "productType.code=='TYPE1'";
        Pageable pageable = PageRequest.of(0, 10, Sort.by("id"));

        List<LovDTO> result = queryService.getLOVWithSelect(selectString, filter, pageable);

        assertThat(result).isNotNull();
        assertThat(result).hasSize(2); // PROD1 and PROD2 have TYPE1
    }

    /**
     * Test consecutive getLOVWithSelect calls to ensure alias preservation.
     */
    @Test
    void testConsecutiveLOVWithSelectCalls() {
        Pageable pageable = PageRequest.of(0, 10, Sort.by("id"));

        // First call
        List<LovDTO> result1 = queryService.getLOVWithSelect(
            "id, code, productType.code:typeCode",
            "productType.code=='TYPE1'",
            pageable
        );
        assertThat(result1).hasSize(2);

        // Second call
        List<LovDTO> result2 = queryService.getLOVWithSelect(
            "id, code, productType.code:typeCode",
            "productType.code=='TYPE2'",
            pageable
        );
        assertThat(result2).hasSize(1);
    }

    /**
     * Test simple property filter (not navigation property) to ensure alias is preserved.
     * This reproduces the bug where "id=gt=0" would generate "where (id=:p1)"
     * instead of "where (a0.id=:p1)".
     */
    @Test
    void testSimplePropertyFilterPreservesAlias() {
        String selectString = "id, code, name";
        String filter = "id=gt=0";  // Simple property filter, not navigation
        Pageable pageable = PageRequest.of(0, 10, Sort.by("id"));

        List<LovDTO> result = queryService.getLOVWithSelect(selectString, filter, pageable);

        assertThat(result).isNotNull();
        assertThat(result).hasSize(3); // All 3 products have id > 0
    }

    /**
     * Test combining simple and navigation properties in filter.
     * Both should use the same root alias prefix.
     */
    @Test
    void testMixedSimpleAndNavigationPropertiesInFilter() {
        String selectString = "id, code, productType.code:typeCode";
        String filter = "id=gt=0 and productType.code=='TYPE1'";
        Pageable pageable = PageRequest.of(0, 10, Sort.by("id"));

        List<LovDTO> result = queryService.getLOVWithSelect(selectString, filter, pageable);

        assertThat(result).isNotNull();
        assertThat(result).hasSize(2); // PROD1 and PROD2 match both conditions
    }
}

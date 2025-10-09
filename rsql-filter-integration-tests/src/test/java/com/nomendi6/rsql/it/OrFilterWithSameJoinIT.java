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

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for OR filters using the same JOIN.
 *
 * These tests reproduce the "Already registered a copy: SqmSingularJoin" bug
 * that occurs when using OR operator (,) with conditions on the same related entity.
 *
 * For example: konto.oznaka=='99999',konto.oznaka=='99998'
 *              productType.code=='TYPE1',productType.code=='TYPE2'
 */
@IntegrationTest
public class OrFilterWithSameJoinIT {

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

        // Create test data with three product types
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

        ProductType type3 = new ProductType()
            .withCode("TYPE3")
            .withName("Type Three")
            .withDescription("Third product type");
        type3 = productTypeRepository.save(type3);

        // Products with TYPE1
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

        // Products with TYPE2
        Product product3 = new Product()
            .withCode("PROD3")
            .withName("Product Three")
            .withDescription("Third product")
            .withPrice(new BigDecimal("300.00"))
            .withStatus(StandardRecordStatus.ACTIVE)
            .withProductType(type2);
        productRepository.save(product3);

        Product product4 = new Product()
            .withCode("PROD4")
            .withName("Product Four")
            .withDescription("Fourth product")
            .withPrice(new BigDecimal("400.00"))
            .withStatus(StandardRecordStatus.ACTIVE)
            .withProductType(type2);
        productRepository.save(product4);

        // Product with TYPE3
        Product product5 = new Product()
            .withCode("PROD5")
            .withName("Product Five")
            .withDescription("Fifth product")
            .withPrice(new BigDecimal("500.00"))
            .withStatus(StandardRecordStatus.ACTIVE)
            .withProductType(type3);
        productRepository.save(product5);
    }

    @AfterEach
    @Transactional
    void cleanup() {
        productRepository.deleteAll();
        productTypeRepository.deleteAll();
    }

    /**
     * CRITICAL TEST: OR filter with same JOIN used twice on the same field.
     *
     * This is the simplest case that reproduces the bug:
     * - Filter: productType.code=='TYPE1',productType.code=='TYPE2'
     * - Both conditions use the same JOIN (productType)
     * - Both conditions check the same field (code)
     *
     * Before the fix, this throws "Already registered a copy: SqmSingularJoin"
     */
    @Test
    void testOrFilter_sameJoin_sameField() {
        // CRITICAL: OR filter with duplicate JOIN on same field
        String filter = "productType.code=='TYPE1',productType.code=='TYPE2'";
        Pageable pageable = PageRequest.of(0, 10, Sort.by("id"));

        // This should NOT throw "Already registered a copy: SqmSingularJoin"
        List<ProductDTO> result = queryService.findByFilterAndSort(filter, pageable);

        // Verify results: should return products from TYPE1 and TYPE2 (4 products)
        assertThat(result).isNotNull();
        assertThat(result).hasSize(4);
        assertThat(result)
            .extracting(ProductDTO::getCode)
            .containsExactlyInAnyOrder("PROD1", "PROD2", "PROD3", "PROD4");
    }

    /**
     * Test OR filter with same JOIN but different fields.
     *
     * Filter: productType.code=='TYPE1',productType.name=='Type Two'
     *
     * This also uses the same JOIN (productType) but checks different fields.
     */
    @Test
    void testOrFilter_sameJoin_differentFields() {
        String filter = "productType.code=='TYPE1',productType.name=='Type Two'";
        Pageable pageable = PageRequest.of(0, 10, Sort.by("id"));

        List<ProductDTO> result = queryService.findByFilterAndSort(filter, pageable);

        // Should return TYPE1 products (2) and TYPE2 products (2) = 4 total
        assertThat(result).isNotNull();
        assertThat(result).hasSize(4);
        assertThat(result)
            .extracting(ProductDTO::getCode)
            .containsExactlyInAnyOrder("PROD1", "PROD2", "PROD3", "PROD4");
    }

    /**
     * Test OR filter with three conditions on the same JOIN.
     *
     * Filter: productType.code=='TYPE1',productType.code=='TYPE2',productType.code=='TYPE3'
     */
    @Test
    void testOrFilter_sameJoin_threeConditions() {
        String filter = "productType.code=='TYPE1',productType.code=='TYPE2',productType.code=='TYPE3'";
        Pageable pageable = PageRequest.of(0, 10, Sort.by("id"));

        List<ProductDTO> result = queryService.findByFilterAndSort(filter, pageable);

        // Should return all 5 products
        assertThat(result).isNotNull();
        assertThat(result).hasSize(5);
    }

    /**
     * Test complex filter combining AND and OR with same JOIN.
     *
     * Filter: (productType.code=='TYPE1',productType.code=='TYPE2');price=gt=250
     *
     * Should return products from TYPE1 or TYPE2 where price > 250
     */
    @Test
    void testComplexFilter_andOr_sameJoin() {
        String filter = "(productType.code=='TYPE1',productType.code=='TYPE2');price=gt=250";
        Pageable pageable = PageRequest.of(0, 10, Sort.by("id"));

        List<ProductDTO> result = queryService.findByFilterAndSort(filter, pageable);

        // Should return PROD3 (TYPE2, 300) and PROD4 (TYPE2, 400)
        assertThat(result).isNotNull();
        assertThat(result).hasSize(2);
        assertThat(result)
            .extracting(ProductDTO::getCode)
            .containsExactlyInAnyOrder("PROD3", "PROD4");
    }

    /**
     * Test OR filter with IN operator on same JOIN.
     *
     * Filter: productType.code=in=(TYPE1,TYPE2)
     *
     * This is semantically equivalent to TYPE1,TYPE2 but uses IN operator
     */
    @Test
    void testOrFilter_inOperator_sameJoin() {
        String filter = "productType.code=in=('TYPE1','TYPE2')";
        Pageable pageable = PageRequest.of(0, 10, Sort.by("id"));

        List<ProductDTO> result = queryService.findByFilterAndSort(filter, pageable);

        // Should return 4 products from TYPE1 and TYPE2
        assertThat(result).isNotNull();
        assertThat(result).hasSize(4);
    }

    /**
     * Test alternative OR syntax (using 'or' keyword instead of comma).
     *
     * Filter: productType.code=='TYPE1' or productType.code=='TYPE2'
     */
    @Test
    void testOrFilter_alternativeSyntax_sameJoin() {
        String filter = "productType.code=='TYPE1' or productType.code=='TYPE2'";
        Pageable pageable = PageRequest.of(0, 10, Sort.by("id"));

        List<ProductDTO> result = queryService.findByFilterAndSort(filter, pageable);

        // Should return 4 products
        assertThat(result).isNotNull();
        assertThat(result).hasSize(4);
        assertThat(result)
            .extracting(ProductDTO::getCode)
            .containsExactlyInAnyOrder("PROD1", "PROD2", "PROD3", "PROD4");
    }

    /**
     * Test nested OR conditions with same JOIN.
     *
     * Filter: (productType.code=='TYPE1',productType.code=='TYPE2'),(productType.name=='Type Three')
     *
     * This is: (TYPE1 OR TYPE2) OR (TYPE3 by name)
     */
    @Test
    void testNestedOrFilter_sameJoin() {
        String filter = "(productType.code=='TYPE1',productType.code=='TYPE2'),(productType.name=='Type Three')";
        Pageable pageable = PageRequest.of(0, 10, Sort.by("id"));

        List<ProductDTO> result = queryService.findByFilterAndSort(filter, pageable);

        // Should return all 5 products
        assertThat(result).isNotNull();
        assertThat(result).hasSize(5);
    }

    /**
     * Test OR filter using LIKE operator with same JOIN.
     *
     * Filter: productType.name=like='*One*',productType.name=like='*Two*'
     */
    @Test
    void testOrFilter_likeOperator_sameJoin() {
        String filter = "productType.name=like='*One*',productType.name=like='*Two*'";
        Pageable pageable = PageRequest.of(0, 10, Sort.by("id"));

        List<ProductDTO> result = queryService.findByFilterAndSort(filter, pageable);

        // Should return products with Type One and Type Two (4 products)
        assertThat(result).isNotNull();
        assertThat(result).hasSize(4);
    }
}

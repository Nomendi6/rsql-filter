package com.nomendi6.rsql.it;

import com.nomendi6.rsql.it.config.IntegrationTest;
import com.nomendi6.rsql.it.domain.AppObject;
import com.nomendi6.rsql.it.domain.AppObjectType;
import com.nomendi6.rsql.it.domain.Product;
import com.nomendi6.rsql.it.domain.ProductType;
import com.nomendi6.rsql.it.domain.StandardRecordStatus;
import com.nomendi6.rsql.it.repository.AppObjectRepository;
import com.nomendi6.rsql.it.repository.ProductRepository;
import com.nomendi6.rsql.it.repository.ProductTypeRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Tuple;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import rsql.RsqlCompiler;
import rsql.helper.SimpleQueryExecutor;
import rsql.where.RsqlContext;

import java.math.BigDecimal;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration test for multiple chained relations in SELECT clause.
 * Tests scenario similar to user's InvItemRel with many fields across 2-3 level deep relations.
 */
@IntegrationTest
public class MultipleChainedRelationsIT {

    @Autowired
    private EntityManager em;

    @Autowired
    private ProductTypeRepository productTypeRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private AppObjectRepository appObjectRepository;

    private RsqlContext<AppObject> rsqlContext;
    private RsqlCompiler<AppObject> compiler;

    @BeforeEach
    void setup() {
        // Clean existing data
        appObjectRepository.deleteAll();
        productRepository.deleteAll();
        productTypeRepository.deleteAll();

        // Create test data structure:
        // ProductType1 → Product1 (parent=null) → AppObject1 (parent=null)
        //             → Product2 (parent=Product1) → AppObject2 (parent=AppObject1)

        // 1. Create ProductType
        ProductType productType1 = new ProductType()
            .withCode("TYPE1")
            .withName("Product Type 1")
            .withDescription("Test product type");
        productTypeRepository.save(productType1);

        ProductType productType2 = new ProductType()
            .withCode("TYPE2")
            .withName("Product Type 2")
            .withDescription("Test product type 2");
        productTypeRepository.save(productType2);

        // 2. Create Products with parent relationship
        Product product1 = new Product()
            .withCode("PROD1")
            .withName("Product 1")
            .withPrice(new BigDecimal("100.00"))
            .withStatus(StandardRecordStatus.ACTIVE)
            .withProductType(productType1)
            .withParent(null); // No parent
        productRepository.save(product1);

        Product product2 = new Product()
            .withCode("PROD2")
            .withName("Product 2")
            .withPrice(new BigDecimal("200.00"))
            .withStatus(StandardRecordStatus.ACTIVE)
            .withProductType(productType2)
            .withParent(product1); // product1 is parent
        productRepository.save(product2);

        // 3. Create AppObjects with parent relationship and product references
        AppObject appObject1 = new AppObject()
            .withCode("APP1")
            .withName("AppObject 1")
            .withAppObjectType(AppObjectType.TENANT)
            .withStatus(StandardRecordStatus.ACTIVE)
            .withCreatedDate(Instant.now())
            .withProduct(product1)
            .withParent(null); // No parent
        appObjectRepository.save(appObject1);

        AppObject appObject2 = new AppObject()
            .withCode("APP2")
            .withName("AppObject 2")
            .withAppObjectType(AppObjectType.CATEGORY)
            .withStatus(StandardRecordStatus.ACTIVE)
            .withCreatedDate(Instant.now())
            .withProduct(product2)
            .withParent(appObject1); // appObject1 is parent
        appObjectRepository.save(appObject2);

        // Initialize RSQL context and compiler
        rsqlContext = new RsqlContext<>(AppObject.class);
        rsqlContext.defineEntityManager(em);
        compiler = new RsqlCompiler<>();
    }

    /**
     * Test scenario: Query AppObject with many chained relation fields (2-3 levels deep).
     * Similar to user's scenario with InvItemRel having item1.type.id, item2.type.id, etc.
     */
    @Test
    void testMultipleChainedRelationsInSelect() {
        // Given: Filter for appObject with code 'APP2'
        String filter = "code=='APP2'";
        Pageable pageable = PageRequest.of(0, 10);

        // Define 14 properties with chained relations up to 3 levels deep
        String[] properties = {
            "id",
            "code",
            "name",
            // Product relation (2 levels)
            "product.id:productId",
            "product.code:productCode",
            "product.name:productName",
            // Product.ProductType relation (3 levels!)
            "product.productType.id:productTypeId",
            "product.productType.code:productTypeCode",
            "product.productType.name:productTypeName",
            // Product.Parent relation (3 levels!)
            "product.parent.id:productParentId",
            "product.parent.code:productParentCode",
            // Parent relation (2 levels)
            "parent.id:parentId",
            "parent.code:parentCode",
            // Parent.Product relation (3 levels!)
            "parent.product.id:parentProductId"
        };

        // When: Execute query with many chained properties
        try {
            Page<Tuple> result = SimpleQueryExecutor.getQueryResultAsPage(
                AppObject.class,
                Tuple.class,
                properties,
                filter,
                pageable,
                rsqlContext,
                compiler,
                appObjectRepository
            );

            // Then: Verify results
            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(1);

            Tuple row = result.getContent().get(0);

            // Verify direct fields
            assertThat(row.get("code")).isEqualTo("APP2");
            assertThat(row.get("name")).isEqualTo("AppObject 2");

            // Verify 2-level relation: product.*
            assertThat(row.get("productCode")).isEqualTo("PROD2");
            assertThat(row.get("productName")).isEqualTo("Product 2");

            // Verify 3-level relation: product.productType.*
            assertThat(row.get("productTypeCode")).isEqualTo("TYPE2");
            assertThat(row.get("productTypeName")).isEqualTo("Product Type 2");

            // Verify 3-level relation: product.parent.*
            assertThat(row.get("productParentCode")).isEqualTo("PROD1");

            // Verify 2-level relation: parent.*
            assertThat(row.get("parentCode")).isEqualTo("APP1");

            // Verify 3-level relation: parent.product.*
            assertThat(row.get("parentProductId")).isNotNull();

            System.out.println("✅ SUCCESS: Query with 14 chained properties (up to 3 levels) works correctly!");
            System.out.println("Result: " + row);

        } catch (Exception e) {
            System.err.println("❌ ERROR: Query failed with exception:");
            System.err.println("Exception type: " + e.getClass().getName());
            System.err.println("Message: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * Test without filter to get all records with chained relations.
     */
    @Test
    void testMultipleChainedRelationsWithoutFilter() {
        // Given: No filter (get all)
        String filter = "";
        Pageable pageable = PageRequest.of(0, 10);

        String[] properties = {
            "id",
            "code",
            "name",
            "product.id:productId",
            "product.code:productCode",
            "product.productType.id:productTypeId",
            "product.productType.code:productTypeCode",
            "product.parent.id:productParentId",
            "parent.id:parentId",
            "parent.code:parentCode"
        };

        // When: Execute query
        try {
            Page<Tuple> result = SimpleQueryExecutor.getQueryResultAsPage(
                AppObject.class,
                Tuple.class,
                properties,
                filter,
                pageable,
                rsqlContext,
                compiler,
                appObjectRepository
            );

            // Then: Verify we get both records
            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(2);

            System.out.println("✅ SUCCESS: Query without filter returned " + result.getContent().size() + " records");

            for (Tuple row : result.getContent()) {
                System.out.println("  - " + row.get("code") + " → product: " + row.get("productCode") +
                                 ", type: " + row.get("productTypeCode"));
            }

        } catch (Exception e) {
            System.err.println("❌ ERROR: Query without filter failed:");
            System.err.println("Exception type: " + e.getClass().getName());
            System.err.println("Message: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * Test with complex filter combining multiple chained fields.
     */
    @Test
    void testMultipleChainedRelationsWithComplexFilter() {
        // Given: Complex filter using chained fields
        String filter = "product.productType.code=='TYPE2' and parent.code=='APP1'";
        Pageable pageable = PageRequest.of(0, 10);

        String[] properties = {
            "id",
            "code",
            "name",
            "product.productType.code:typeCode",
            "parent.code:parentCode"
        };

        // When: Execute query with complex filter
        try {
            Page<Tuple> result = SimpleQueryExecutor.getQueryResultAsPage(
                AppObject.class,
                Tuple.class,
                properties,
                filter,
                pageable,
                rsqlContext,
                compiler,
                appObjectRepository
            );

            // Then: Should return APP2 (has TYPE2 and parent APP1)
            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).get("code")).isEqualTo("APP2");

            System.out.println("✅ SUCCESS: Complex filter with chained relations works!");

        } catch (Exception e) {
            System.err.println("❌ ERROR: Complex filter query failed:");
            System.err.println("Exception type: " + e.getClass().getName());
            System.err.println("Message: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }
}

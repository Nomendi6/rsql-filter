package com.nomendi6.rsql.it;

import com.nomendi6.rsql.it.config.IntegrationTest;
import com.nomendi6.rsql.it.domain.Product;
import com.nomendi6.rsql.it.domain.ProductType;
import com.nomendi6.rsql.it.domain.StandardRecordStatus;
import com.nomendi6.rsql.it.repository.ProductRepository;
import com.nomendi6.rsql.it.repository.ProductTypeRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Tuple;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import rsql.RsqlCompiler;
import rsql.helper.AggregateField;
import rsql.helper.AggregateField.AggregateFunction;
import rsql.helper.SimpleQueryExecutor;
import rsql.where.RsqlContext;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@IntegrationTest
public class SimpleQueryExecutorAggregateIT {

    @Autowired
    private EntityManager em;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ProductTypeRepository productTypeRepository;

    private RsqlContext<Product> rsqlContext;
    private RsqlCompiler<Product> compiler;

    @BeforeEach
    void init() {
        rsqlContext = new RsqlContext<>(Product.class);
        rsqlContext.defineEntityManager(em);
        compiler = new RsqlCompiler<>();
        setupTestData();
    }

    void setupTestData() {
        // Clean existing data
        productRepository.deleteAll();
        productTypeRepository.deleteAll();

        // Create product types
        ProductType type1 = new ProductType()
            .withCode("TYPE1")
            .withName("Electronics");

        ProductType type2 = new ProductType()
            .withCode("TYPE2")
            .withName("Books");

        productTypeRepository.save(type1);
        productTypeRepository.save(type2);

        // Create products
        productRepository.save(new Product()
            .withCode("P001")
            .withName("Laptop")
            .withPrice(new BigDecimal("1000.00"))
            .withProductType(type1)
            .withStatus(StandardRecordStatus.ACTIVE));

        productRepository.save(new Product()
            .withCode("P002")
            .withName("Mouse")
            .withPrice(new BigDecimal("25.00"))
            .withProductType(type1)
            .withStatus(StandardRecordStatus.ACTIVE));

        productRepository.save(new Product()
            .withCode("P003")
            .withName("Keyboard")
            .withPrice(new BigDecimal("75.00"))
            .withProductType(type1)
            .withStatus(StandardRecordStatus.ACTIVE));

        productRepository.save(new Product()
            .withCode("P004")
            .withName("Java Book")
            .withPrice(new BigDecimal("50.00"))
            .withProductType(type2)
            .withStatus(StandardRecordStatus.ACTIVE));

        productRepository.save(new Product()
            .withCode("P005")
            .withName("Python Book")
            .withPrice(new BigDecimal("45.00"))
            .withProductType(type2)
            .withStatus(StandardRecordStatus.ACTIVE));
    }

    @Test
    void testAggregateQueryGroupByProductType() {
        List<AggregateField> selectFields = Arrays.asList(
            AggregateField.groupBy("productType.name", "typeName"),
            AggregateField.of("price", AggregateFunction.SUM, "totalPrice"),
            AggregateField.of("id", AggregateFunction.COUNT, "productCount")
        );
        List<String> groupByFields = Arrays.asList("productType.name");

        List<Tuple> result = SimpleQueryExecutor.getAggregateQueryResult(
            Product.class,
            Tuple.class,
            selectFields,
            groupByFields,
            "",
            (String) null,  // No HAVING filter
            null,  // No Pageable
            rsqlContext,
            compiler
        );

        assertThat(result).isNotNull();
        assertThat(result).hasSize(2);

        // Verify that we have aggregates for both product types
        boolean hasElectronics = false;
        boolean hasBooks = false;

        for (Tuple tuple : result) {
            String typeName = tuple.get(0, String.class);
            Long count = tuple.get(2, Long.class);

            if ("Electronics".equals(typeName)) {
                hasElectronics = true;
                assertThat(count).isEqualTo(3);
            } else if ("Books".equals(typeName)) {
                hasBooks = true;
                assertThat(count).isEqualTo(2);
            }
        }

        assertThat(hasElectronics).isTrue();
        assertThat(hasBooks).isTrue();
    }

    @Test
    void testAggregateQueryWithFilter() {
        List<AggregateField> selectFields = Arrays.asList(
            AggregateField.groupBy("productType.name", "typeName"),
            AggregateField.of("price", AggregateFunction.AVG, "avgPrice"),
            AggregateField.of("id", AggregateFunction.COUNT, "productCount")
        );
        List<String> groupByFields = Arrays.asList("productType.name");

        // Filter only ACTIVE products with productType.code == 'TYPE1'
        String filter = "status=='ACTIVE' and productType.code=='TYPE1'";

        List<Tuple> result = SimpleQueryExecutor.getAggregateQueryResult(
            Product.class,
            Tuple.class,
            selectFields,
            groupByFields,
            filter,
            (String) null,  // No HAVING filter
            null,  // No Pageable
            rsqlContext,
            compiler
        );

        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);

        Tuple tuple = result.get(0);
        assertThat(tuple.get(0, String.class)).isEqualTo("Electronics");
        assertThat(tuple.get(2, Long.class)).isEqualTo(3);
    }

    @Test
    void testAggregateQueryWithMinMax() {
        List<AggregateField> selectFields = Arrays.asList(
            AggregateField.groupBy("productType.name", "typeName"),
            AggregateField.of("price", AggregateFunction.MIN, "minPrice"),
            AggregateField.of("price", AggregateFunction.MAX, "maxPrice"),
            AggregateField.of("price", AggregateFunction.AVG, "avgPrice")
        );
        List<String> groupByFields = Arrays.asList("productType.name");

        List<Tuple> result = SimpleQueryExecutor.getAggregateQueryResult(
            Product.class,
            Tuple.class,
            selectFields,
            groupByFields,
            "",
            (String) null,  // No HAVING filter
            null,  // No Pageable
            rsqlContext,
            compiler
        );

        assertThat(result).isNotNull();
        assertThat(result).hasSize(2);

        for (Tuple tuple : result) {
            String typeName = tuple.get(0, String.class);
            BigDecimal minPrice = tuple.get(1, BigDecimal.class);
            BigDecimal maxPrice = tuple.get(2, BigDecimal.class);

            if ("Electronics".equals(typeName)) {
                assertThat(minPrice).isEqualByComparingTo(new BigDecimal("25.00"));
                assertThat(maxPrice).isEqualByComparingTo(new BigDecimal("1000.00"));
            } else if ("Books".equals(typeName)) {
                assertThat(minPrice).isEqualByComparingTo(new BigDecimal("45.00"));
                assertThat(maxPrice).isEqualByComparingTo(new BigDecimal("50.00"));
            }
        }
    }

    @Test
    void testAggregateQueryCountDistinct() {
        List<AggregateField> selectFields = Arrays.asList(
            AggregateField.groupBy("status", "status"),
            AggregateField.of("productType.id", AggregateFunction.COUNT_DISTINCT, "distinctTypes")
        );
        List<String> groupByFields = Arrays.asList("status");

        List<Tuple> result = SimpleQueryExecutor.getAggregateQueryResult(
            Product.class,
            Tuple.class,
            selectFields,
            groupByFields,
            "",
            (String) null,  // No HAVING filter
            null,  // No Pageable
            rsqlContext,
            compiler
        );

        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);

        Tuple tuple = result.get(0);
        assertThat(tuple.get(0, StandardRecordStatus.class)).isEqualTo(StandardRecordStatus.ACTIVE);
        assertThat(tuple.get(1, Long.class)).isEqualTo(2); // 2 distinct product types
    }

    @Test
    void testAggregateQueryWithSorting() {
        List<AggregateField> selectFields = Arrays.asList(
            AggregateField.groupBy("productType.name", "typeName"),
            AggregateField.of("price", AggregateFunction.SUM, "totalPrice")
        );
        List<String> groupByFields = Arrays.asList("productType.name");

        Sort sort = Sort.by(Sort.Direction.DESC, "productType.name");
        Pageable pageable = PageRequest.of(0, 10, sort);

        List<Tuple> result = SimpleQueryExecutor.getAggregateQueryResult(
            Product.class,
            Tuple.class,
            selectFields,
            groupByFields,
            "",
            (String) null,  // No HAVING filter
            pageable,
            rsqlContext,
            compiler
        );

        assertThat(result).isNotNull();
        assertThat(result).hasSize(2);

        // Should be sorted DESC by typeName
        assertThat(result.get(0).get(0, String.class)).isEqualTo("Electronics");
        assertThat(result.get(1).get(0, String.class)).isEqualTo("Books");
    }

    @Test
    void testAggregateQuerySimpleCount() {
        List<AggregateField> selectFields = Arrays.asList(
            AggregateField.of("id", AggregateFunction.COUNT, "totalCount")
        );
        List<String> groupByFields = null; // No GROUP BY

        List<Tuple> result = SimpleQueryExecutor.getAggregateQueryResult(
            Product.class,
            Tuple.class,
            selectFields,
            groupByFields,
            "",
            (String) null,  // No HAVING filter
            null,  // No Pageable
            rsqlContext,
            compiler
        );

        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);

        Long totalCount = result.get(0).get(0, Long.class);
        assertThat(totalCount).isEqualTo(5);
    }

    @Test
    void testAggregateQueryMultipleGroupByFields() {
        List<AggregateField> selectFields = Arrays.asList(
            AggregateField.groupBy("productType.name", "typeName"),
            AggregateField.groupBy("status", "status"),
            AggregateField.of("id", AggregateFunction.COUNT, "productCount")
        );
        List<String> groupByFields = Arrays.asList("productType.name", "status");

        List<Tuple> result = SimpleQueryExecutor.getAggregateQueryResult(
            Product.class,
            Tuple.class,
            selectFields,
            groupByFields,
            "",
            (String) null,  // No HAVING filter
            null,  // No Pageable
            rsqlContext,
            compiler
        );

        assertThat(result).isNotNull();
        assertThat(result).hasSize(2); // 2 combinations of productType and status
    }

    // ==================== Tests for getAggregateQueryResultAsPage() ====================

    /**
     * Test getAggregateQueryResultAsPage() with shared JOIN between WHERE and GROUP BY.
     * This tests the fix for count query JOIN conflict when using Page<Tuple> with aggregates.
     *
     * CRITICAL: WHERE filter uses productType.code, GROUP BY uses productType.name.
     * Both need the same JOIN to productType, which must be shared to avoid
     * "Already registered a copy: SqmSingularJoin" error.
     */
    @Test
    void testAggregateQueryAsPage_sharedJoinBetweenWhereAndGroupBy() {
        // Given: WHERE and GROUP BY both reference productType
        List<AggregateField> selectFields = Arrays.asList(
            AggregateField.groupBy("productType.name", "typeName"),
            AggregateField.of("price", AggregateFunction.SUM, "totalPrice"),
            AggregateField.of("id", AggregateFunction.COUNT, "productCount")
        );
        List<String> groupByFields = Arrays.asList("productType.name");
        String filter = "productType.code=='TYPE1'"; // WHERE on productType
        Pageable pageable = PageRequest.of(0, 10, Sort.by("productType.name"));

        // When: Execute paginated aggregate query
        org.springframework.data.domain.Page<Tuple> page = SimpleQueryExecutor.getAggregateQueryResultAsPage(
            Product.class,
            Tuple.class,
            selectFields,
            groupByFields,
            filter,
            (String) null,  // No HAVING filter
            pageable,
            rsqlContext,
            compiler
        );

        // Then: Should return paginated results without JOIN conflict
        assertThat(page).isNotNull();
        assertThat(page.getTotalElements()).isEqualTo(1); // Only one group (Electronics/TYPE1)
        assertThat(page.getTotalPages()).isEqualTo(1);
        assertThat(page.getContent()).hasSize(1);

        // Verify aggregate data
        Tuple result = page.getContent().get(0);
        assertThat(result.get(0, String.class)).isEqualTo("Electronics");
        assertThat(result.get(2, Long.class)).isEqualTo(3); // 3 products in TYPE1
    }

    /**
     * Test getAggregateQueryResultAsPage() with pagination across multiple groups.
     * Tests page boundaries and count accuracy.
     */
    @Test
    void testAggregateQueryAsPage_pagination() {
        // Given: Page size of 1, so we get one group per page
        List<AggregateField> selectFields = Arrays.asList(
            AggregateField.groupBy("productType.name", "typeName"),
            AggregateField.of("price", AggregateFunction.SUM, "totalPrice")
        );
        List<String> groupByFields = Arrays.asList("productType.name");
        Pageable firstPage = PageRequest.of(0, 1, Sort.by(Sort.Direction.ASC, "productType.name"));

        // When: Get first page
        org.springframework.data.domain.Page<Tuple> page1 = SimpleQueryExecutor.getAggregateQueryResultAsPage(
            Product.class,
            Tuple.class,
            selectFields,
            groupByFields,
            "",
            (String) null,
            firstPage,
            rsqlContext,
            compiler
        );

        // Then: First page should have correct metadata
        assertThat(page1.getTotalElements()).isEqualTo(2); // 2 groups total
        assertThat(page1.getTotalPages()).isEqualTo(2);
        assertThat(page1.getNumber()).isEqualTo(0);
        assertThat(page1.getContent()).hasSize(1);
        assertThat(page1.getContent().get(0).get(0, String.class)).isEqualTo("Books");

        // When: Get second page
        Pageable secondPage = PageRequest.of(1, 1, Sort.by(Sort.Direction.ASC, "productType.name"));
        org.springframework.data.domain.Page<Tuple> page2 = SimpleQueryExecutor.getAggregateQueryResultAsPage(
            Product.class,
            Tuple.class,
            selectFields,
            groupByFields,
            "",
            (String) null,
            secondPage,
            rsqlContext,
            compiler
        );

        // Then: Second page should also work correctly
        assertThat(page2.getTotalElements()).isEqualTo(2);
        assertThat(page2.getTotalPages()).isEqualTo(2);
        assertThat(page2.getNumber()).isEqualTo(1);
        assertThat(page2.getContent()).hasSize(1);
        assertThat(page2.getContent().get(0).get(0, String.class)).isEqualTo("Electronics");
    }

    /**
     * Test getAggregateQueryResultAsPage() with complex WHERE filter on shared JOIN.
     */
    @Test
    void testAggregateQueryAsPage_complexFilterOnSharedJoin() {
        // Given: Complex filter using multiple conditions on productType
        List<AggregateField> selectFields = Arrays.asList(
            AggregateField.groupBy("productType.code", "typeCode"),
            AggregateField.groupBy("productType.name", "typeName"),
            AggregateField.of("price", AggregateFunction.AVG, "avgPrice"),
            AggregateField.of("id", AggregateFunction.COUNT, "count")
        );
        List<String> groupByFields = Arrays.asList("productType.code", "productType.name");
        String filter = "productType.code=='TYPE2';price=gt=40"; // TYPE2 AND price > 40
        Pageable pageable = PageRequest.of(0, 10);

        // When
        org.springframework.data.domain.Page<Tuple> page = SimpleQueryExecutor.getAggregateQueryResultAsPage(
            Product.class,
            Tuple.class,
            selectFields,
            groupByFields,
            filter,
            (String) null,
            pageable,
            rsqlContext,
            compiler
        );

        // Then: Only TYPE2 products with price > 40
        assertThat(page.getTotalElements()).isEqualTo(1);
        assertThat(page.getContent()).hasSize(1);

        Tuple result = page.getContent().get(0);
        assertThat(result.get(0, String.class)).isEqualTo("TYPE2");
        assertThat(result.get(1, String.class)).isEqualTo("Books");
        assertThat(result.get(3, Long.class)).isEqualTo(2); // Both books have price > 40
    }

    /**
     * Test getAggregateQueryResultAsPage() with HAVING clause and shared JOIN.
     * Tests that both WHERE and HAVING can share JOINs.
     */
    @Test
    void testAggregateQueryAsPage_withHavingAndSharedJoin() {
        // Given: WHERE filter on productType, HAVING filter on aggregate
        List<AggregateField> selectFields = Arrays.asList(
            AggregateField.groupBy("productType.name", "typeName"),
            AggregateField.of("price", AggregateFunction.SUM, "totalPrice"),
            AggregateField.of("id", AggregateFunction.COUNT, "count")
        );
        List<String> groupByFields = Arrays.asList("productType.name");
        String whereFilter = "status=='ACTIVE'";
        String havingFilter = "SUM(price)=gt=100"; // Total price > 100
        Pageable pageable = PageRequest.of(0, 10);

        // When
        org.springframework.data.domain.Page<Tuple> page = SimpleQueryExecutor.getAggregateQueryResultAsPage(
            Product.class,
            Tuple.class,
            selectFields,
            groupByFields,
            whereFilter,
            havingFilter,
            pageable,
            rsqlContext,
            compiler
        );

        // Then: Only groups with total price > 100
        assertThat(page.getTotalElements()).isEqualTo(1); // Only Electronics (1100)
        assertThat(page.getContent()).hasSize(1);

        Tuple result = page.getContent().get(0);
        assertThat(result.get(0, String.class)).isEqualTo("Electronics");
        assertThat(result.get(2, Long.class)).isEqualTo(3);
    }

    /**
     * Test getAggregateQueryResultAsPage() with empty result.
     * Ensures pagination metadata is correct even with no results.
     */
    @Test
    void testAggregateQueryAsPage_emptyResult() {
        // Given: Filter that matches no records
        List<AggregateField> selectFields = Arrays.asList(
            AggregateField.groupBy("productType.name", "typeName"),
            AggregateField.of("id", AggregateFunction.COUNT, "count")
        );
        List<String> groupByFields = Arrays.asList("productType.name");
        String filter = "productType.code=='NONEXISTENT'";
        Pageable pageable = PageRequest.of(0, 10);

        // When
        org.springframework.data.domain.Page<Tuple> page = SimpleQueryExecutor.getAggregateQueryResultAsPage(
            Product.class,
            Tuple.class,
            selectFields,
            groupByFields,
            filter,
            (String) null,
            pageable,
            rsqlContext,
            compiler
        );

        // Then
        assertThat(page.getTotalElements()).isEqualTo(0);
        assertThat(page.getTotalPages()).isEqualTo(0);
        assertThat(page.getContent()).isEmpty();
    }

    /**
     * Test getAggregateQueryResultAsPage() without WHERE filter but with GROUP BY on relation.
     * Tests that JOINs work correctly when only needed for GROUP BY, not WHERE.
     */
    @Test
    void testAggregateQueryAsPage_joinOnlyInGroupBy() {
        // Given: No WHERE filter, GROUP BY uses productType relation
        List<AggregateField> selectFields = Arrays.asList(
            AggregateField.groupBy("productType.code", "typeCode"),
            AggregateField.of("price", AggregateFunction.MAX, "maxPrice")
        );
        List<String> groupByFields = Arrays.asList("productType.code");
        String filter = ""; // No WHERE filter
        Pageable pageable = PageRequest.of(0, 10, Sort.by("productType.code"));

        // When
        org.springframework.data.domain.Page<Tuple> page = SimpleQueryExecutor.getAggregateQueryResultAsPage(
            Product.class,
            Tuple.class,
            selectFields,
            groupByFields,
            filter,
            (String) null,
            pageable,
            rsqlContext,
            compiler
        );

        // Then: Should return both groups
        assertThat(page.getTotalElements()).isEqualTo(2);
        assertThat(page.getContent()).hasSize(2);

        // Verify sorted by typeCode
        assertThat(page.getContent().get(0).get(0, String.class)).isEqualTo("TYPE1");
        assertThat(page.getContent().get(1).get(0, String.class)).isEqualTo("TYPE2");
    }
}

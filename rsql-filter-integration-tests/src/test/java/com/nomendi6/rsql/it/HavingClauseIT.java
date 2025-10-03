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
import rsql.RsqlCompiler;
import rsql.helper.AggregateField;
import rsql.helper.AggregateField.AggregateFunction;
import rsql.helper.SimpleQueryExecutor;
import rsql.where.RsqlContext;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for HAVING clause functionality.
 * Tests various HAVING conditions with real database queries.
 */
@IntegrationTest
public class HavingClauseIT {

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
        ProductType electronics = new ProductType()
            .withCode("ELECTRONICS")
            .withName("Electronics");

        ProductType books = new ProductType()
            .withCode("BOOKS")
            .withName("Books");

        ProductType clothing = new ProductType()
            .withCode("CLOTHING")
            .withName("Clothing");

        productTypeRepository.save(electronics);
        productTypeRepository.save(books);
        productTypeRepository.save(clothing);

        // Electronics (3 products, total price = 1100)
        productRepository.save(new Product()
            .withCode("P001")
            .withName("Laptop")
            .withPrice(new BigDecimal("1000.00"))
            .withProductType(electronics)
            .withStatus(StandardRecordStatus.ACTIVE));

        productRepository.save(new Product()
            .withCode("P002")
            .withName("Mouse")
            .withPrice(new BigDecimal("25.00"))
            .withProductType(electronics)
            .withStatus(StandardRecordStatus.ACTIVE));

        productRepository.save(new Product()
            .withCode("P003")
            .withName("Keyboard")
            .withPrice(new BigDecimal("75.00"))
            .withProductType(electronics)
            .withStatus(StandardRecordStatus.ACTIVE));

        // Books (2 products, total price = 95)
        productRepository.save(new Product()
            .withCode("P004")
            .withName("Java Book")
            .withPrice(new BigDecimal("50.00"))
            .withProductType(books)
            .withStatus(StandardRecordStatus.ACTIVE));

        productRepository.save(new Product()
            .withCode("P005")
            .withName("Python Book")
            .withPrice(new BigDecimal("45.00"))
            .withProductType(books)
            .withStatus(StandardRecordStatus.ACTIVE));

        // Clothing (1 product, total price = 30)
        productRepository.save(new Product()
            .withCode("P006")
            .withName("T-Shirt")
            .withPrice(new BigDecimal("30.00"))
            .withProductType(clothing)
            .withStatus(StandardRecordStatus.ACTIVE));
    }

    /**
     * Test HAVING with SUM > value - filter groups with total price > 100
     */
    @Test
    void testHavingWithSumGreaterThan() {
        List<AggregateField> selectFields = Arrays.asList(
            AggregateField.groupBy("productType.name", "typeName"),
            AggregateField.of("price", AggregateFunction.SUM, "totalPrice"),
            AggregateField.of("id", AggregateFunction.COUNT, "productCount")
        );
        List<String> groupByFields = Arrays.asList("productType.name");
        String havingFilter = "SUM(price)=gt=100";

        List<Tuple> result = SimpleQueryExecutor.getAggregateQueryResult(
            Product.class,
            Tuple.class,
            selectFields,
            groupByFields,
            "",
            havingFilter,
            null,
            rsqlContext,
            compiler
        );

        // Only Electronics (1100) should match, not Books (95) or Clothing (30)
        assertThat(result).hasSize(1);
        assertThat(result.get(0).get(0, String.class)).isEqualTo("Electronics");
    }

    /**
     * Test HAVING with alias comparison
     */
    @Test
    void testHavingWithAlias() {
        List<AggregateField> selectFields = Arrays.asList(
            AggregateField.groupBy("productType.name", "typeName"),
            AggregateField.of("price", AggregateFunction.SUM, "totalPrice"),
            AggregateField.of("id", AggregateFunction.COUNT, "productCount")
        );
        List<String> groupByFields = Arrays.asList("productType.name");
        String havingFilter = "totalPrice=gt=100";

        List<Tuple> result = SimpleQueryExecutor.getAggregateQueryResult(
            Product.class,
            Tuple.class,
            selectFields,
            groupByFields,
            "",
            havingFilter,
            null,
            rsqlContext,
            compiler
        );

        // Only Electronics (1100) should match
        assertThat(result).hasSize(1);
        assertThat(result.get(0).get(0, String.class)).isEqualTo("Electronics");
    }

    /**
     * Test HAVING with COUNT >= value
     */
    @Test
    void testHavingWithCountGreaterOrEqual() {
        List<AggregateField> selectFields = Arrays.asList(
            AggregateField.groupBy("productType.name", "typeName"),
            AggregateField.of("id", AggregateFunction.COUNT, "productCount")
        );
        List<String> groupByFields = Arrays.asList("productType.name");
        String havingFilter = "COUNT(id)=ge=2";

        List<Tuple> result = SimpleQueryExecutor.getAggregateQueryResult(
            Product.class,
            Tuple.class,
            selectFields,
            groupByFields,
            "",
            havingFilter,
            null,
            rsqlContext,
            compiler
        );

        // Electronics (3) and Books (2) should match, not Clothing (1)
        assertThat(result).hasSize(2);
    }

    /**
     * Test HAVING with AVG condition
     */
    @Test
    void testHavingWithAverage() {
        List<AggregateField> selectFields = Arrays.asList(
            AggregateField.groupBy("productType.name", "typeName"),
            AggregateField.of("price", AggregateFunction.AVG, "avgPrice"),
            AggregateField.of("id", AggregateFunction.COUNT, "productCount")
        );
        List<String> groupByFields = Arrays.asList("productType.name");
        String havingFilter = "AVG(price)=gt=45";

        List<Tuple> result = SimpleQueryExecutor.getAggregateQueryResult(
            Product.class,
            Tuple.class,
            selectFields,
            groupByFields,
            "",
            havingFilter,
            null,
            rsqlContext,
            compiler
        );

        // Electronics (avg ~366.67) and Books (avg 47.5) should match, not Clothing (30)
        assertThat(result).hasSize(2);
    }

    /**
     * Test HAVING with AND condition
     */
    @Test
    void testHavingWithAndCondition() {
        List<AggregateField> selectFields = Arrays.asList(
            AggregateField.groupBy("productType.name", "typeName"),
            AggregateField.of("price", AggregateFunction.SUM, "totalPrice"),
            AggregateField.of("id", AggregateFunction.COUNT, "productCount")
        );
        List<String> groupByFields = Arrays.asList("productType.name");
        String havingFilter = "SUM(price)=gt=90;COUNT(id)=ge=2";

        List<Tuple> result = SimpleQueryExecutor.getAggregateQueryResult(
            Product.class,
            Tuple.class,
            selectFields,
            groupByFields,
            "",
            havingFilter,
            null,
            rsqlContext,
            compiler
        );

        // Only Electronics (total=1100, count=3) and Books (total=95, count=2) should match
        assertThat(result).hasSize(2);
    }

    /**
     * Test HAVING with OR condition
     */
    @Test
    void testHavingWithOrCondition() {
        List<AggregateField> selectFields = Arrays.asList(
            AggregateField.groupBy("productType.name", "typeName"),
            AggregateField.of("price", AggregateFunction.SUM, "totalPrice"),
            AggregateField.of("id", AggregateFunction.COUNT, "productCount")
        );
        List<String> groupByFields = Arrays.asList("productType.name");
        String havingFilter = "SUM(price)=gt=1000,COUNT(id)==1";

        List<Tuple> result = SimpleQueryExecutor.getAggregateQueryResult(
            Product.class,
            Tuple.class,
            selectFields,
            groupByFields,
            "",
            havingFilter,
            null,
            rsqlContext,
            compiler
        );

        // Electronics (total=1100) OR Clothing (count=1) should match
        assertThat(result).hasSize(2);
    }

    /**
     * Test HAVING with BETWEEN operator
     */
    @Test
    void testHavingWithBetween() {
        List<AggregateField> selectFields = Arrays.asList(
            AggregateField.groupBy("productType.name", "typeName"),
            AggregateField.of("price", AggregateFunction.SUM, "totalPrice")
        );
        List<String> groupByFields = Arrays.asList("productType.name");
        String havingFilter = "SUM(price)=bt=(30,100)";

        List<Tuple> result = SimpleQueryExecutor.getAggregateQueryResult(
            Product.class,
            Tuple.class,
            selectFields,
            groupByFields,
            "",
            havingFilter,
            null,
            rsqlContext,
            compiler
        );

        // Only Books (95) and Clothing (30) should match, not Electronics (1100)
        assertThat(result).hasSize(2);
    }

    /**
     * Test HAVING with WHERE filter combined
     */
    @Test
    void testHavingWithWhereFilter() {
        List<AggregateField> selectFields = Arrays.asList(
            AggregateField.groupBy("productType.name", "typeName"),
            AggregateField.of("price", AggregateFunction.SUM, "totalPrice"),
            AggregateField.of("id", AggregateFunction.COUNT, "productCount")
        );
        List<String> groupByFields = Arrays.asList("productType.name");
        String whereFilter = "status=='ACTIVE'";
        String havingFilter = "COUNT(id)=ge=2";

        List<Tuple> result = SimpleQueryExecutor.getAggregateQueryResult(
            Product.class,
            Tuple.class,
            selectFields,
            groupByFields,
            whereFilter,
            havingFilter,
            null,
            rsqlContext,
            compiler
        );

        // Electronics (3) and Books (2) should match after WHERE and HAVING
        assertThat(result).hasSize(2);
    }

    /**
     * Test HAVING without pagination - verifies all matching groups are returned
     */
    @Test
    void testHavingWithoutPagination() {
        List<AggregateField> selectFields = Arrays.asList(
            AggregateField.groupBy("productType.name", "typeName"),
            AggregateField.of("price", AggregateFunction.SUM, "totalPrice")
        );
        List<String> groupByFields = Arrays.asList("productType.name");
        String havingFilter = "SUM(price)=gt=0";

        List<Tuple> result = SimpleQueryExecutor.getAggregateQueryResult(
            Product.class,
            Tuple.class,
            selectFields,
            groupByFields,
            "",
            havingFilter,
            null,
            rsqlContext,
            compiler
        );

        // All 3 product types should match (Electronics, Books, Clothing)
        assertThat(result).hasSize(3);
    }

    /**
     * Test HAVING with MIN function
     */
    @Test
    void testHavingWithMin() {
        List<AggregateField> selectFields = Arrays.asList(
            AggregateField.groupBy("productType.name", "typeName"),
            AggregateField.of("price", AggregateFunction.MIN, "minPrice")
        );
        List<String> groupByFields = Arrays.asList("productType.name");
        String havingFilter = "MIN(price)=ge=30";

        List<Tuple> result = SimpleQueryExecutor.getAggregateQueryResult(
            Product.class,
            Tuple.class,
            selectFields,
            groupByFields,
            "",
            havingFilter,
            null,
            rsqlContext,
            compiler
        );

        // All groups should match (Electronics min=25, Books min=45, Clothing min=30)
        // Only Books (45) and Clothing (30) >= 30
        assertThat(result).hasSize(2);
    }

    /**
     * Test HAVING with MAX function
     */
    @Test
    void testHavingWithMax() {
        List<AggregateField> selectFields = Arrays.asList(
            AggregateField.groupBy("productType.name", "typeName"),
            AggregateField.of("price", AggregateFunction.MAX, "maxPrice")
        );
        List<String> groupByFields = Arrays.asList("productType.name");
        String havingFilter = "MAX(price)=gt=100";

        List<Tuple> result = SimpleQueryExecutor.getAggregateQueryResult(
            Product.class,
            Tuple.class,
            selectFields,
            groupByFields,
            "",
            havingFilter,
            null,
            rsqlContext,
            compiler
        );

        // Only Electronics (max=1000) should match
        assertThat(result).hasSize(1);
        assertThat(result.get(0).get(0, String.class)).isEqualTo("Electronics");
    }

    /**
     * Test HAVING with complex parenthesized condition
     */
    @Test
    void testHavingWithParentheses() {
        List<AggregateField> selectFields = Arrays.asList(
            AggregateField.groupBy("productType.name", "typeName"),
            AggregateField.of("price", AggregateFunction.SUM, "totalPrice"),
            AggregateField.of("id", AggregateFunction.COUNT, "productCount")
        );
        List<String> groupByFields = Arrays.asList("productType.name");
        String havingFilter = "(SUM(price)=gt=1000,COUNT(id)==1);SUM(price)=le=1200";

        List<Tuple> result = SimpleQueryExecutor.getAggregateQueryResult(
            Product.class,
            Tuple.class,
            selectFields,
            groupByFields,
            "",
            havingFilter,
            null,
            rsqlContext,
            compiler
        );

        // (Electronics with total>1000 OR Clothing with count=1) AND total<=1200
        // Electronics matches: total=1100 (>1000 AND <=1200)
        // Clothing matches: count=1 AND total=30 (<=1200)
        assertThat(result).hasSize(2);
    }
}

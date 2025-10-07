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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import rsql.RsqlCompiler;
import rsql.helper.SelectExpression;
import rsql.helper.SimpleQueryExecutor;
import rsql.where.RsqlContext;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for SELECT expressions with arithmetic operations.
 * Tests the full stack: parsing, validation, and execution with real JPA entities.
 */
@IntegrationTest
public class SelectExpressionIT {

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

        // Create products with specific prices
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

    // ==================== Compilation Tests ====================

    @Test
    void testCompileSelectToExpressions_SimpleField() {
        // Given
        String selectString = "price";

        // When
        List<SelectExpression> expressions = compiler.compileSelectToExpressions(selectString, rsqlContext);

        // Then
        assertThat(expressions).hasSize(1);
        assertThat(expressions.get(0)).isInstanceOf(rsql.helper.FieldExpression.class);
    }

    @Test
    void testCompileSelectToExpressions_AggregateFunction() {
        // Given
        String selectString = "SUM(price)";

        // When
        List<SelectExpression> expressions = compiler.compileSelectToExpressions(selectString, rsqlContext);

        // Then
        assertThat(expressions).hasSize(1);
        assertThat(expressions.get(0)).isInstanceOf(rsql.helper.FunctionExpression.class);
    }

    @Test
    void testCompileSelectToExpressions_ArithmeticExpression() {
        // Given
        String selectString = "SUM(price) - 10";

        // When
        List<SelectExpression> expressions = compiler.compileSelectToExpressions(selectString, rsqlContext);

        // Then
        assertThat(expressions).hasSize(1);
        assertThat(expressions.get(0)).isInstanceOf(rsql.helper.BinaryOpExpression.class);
    }

    @Test
    void testCompileSelectToExpressions_ComplexExpression() {
        // Given
        String selectString = "(SUM(price) * 1.2):priceWithTax";

        // When
        List<SelectExpression> expressions = compiler.compileSelectToExpressions(selectString, rsqlContext);

        // Then
        assertThat(expressions).hasSize(1);
        assertThat(expressions.get(0)).isInstanceOf(rsql.helper.BinaryOpExpression.class);
        assertThat(expressions.get(0).getAlias()).isEqualTo("priceWithTax");
    }

    // ==================== Execution Tests ====================

    @Test
    void testExecuteSimpleArithmeticExpression() {
        // Given: Simple arithmetic with literal
        String selectString = "10 + 5:result";

        // When
        List<Tuple> results = SimpleQueryExecutor.getAggregateQueryResultWithSelectExpression(
            Product.class,
            Tuple.class,
            selectString,
            "",
            null,
            null,
            rsqlContext,
            compiler
        );

        // Then: Each product should have result = 15
        assertThat(results).isNotEmpty();
        for (Tuple tuple : results) {
            BigDecimal result = tuple.get(0, BigDecimal.class);
            assertThat(result).isEqualByComparingTo(new BigDecimal("15"));
        }
    }

    @Test
    void testExecuteAggregateSubtraction() {
        // Given: SUM(price) - 100 grouped by productType
        String selectString = "productType.name:typeName, SUM(price) - 100:adjustedTotal";

        // When
        List<Tuple> results = SimpleQueryExecutor.getAggregateQueryResultWithSelectExpression(
            Product.class,
            Tuple.class,
            selectString,
            "",
            null,
            null,
            rsqlContext,
            compiler
        );

        // Then
        assertThat(results).hasSize(2);

        for (Tuple tuple : results) {
            String typeName = tuple.get(0, String.class);
            BigDecimal adjustedTotal = tuple.get(1, BigDecimal.class);

            if ("Electronics".equals(typeName)) {
                // Sum: 1000 + 25 + 75 = 1100, minus 100 = 1000
                assertThat(adjustedTotal).isEqualByComparingTo(new BigDecimal("1000.00"));
            } else if ("Books".equals(typeName)) {
                // Sum: 50 + 45 = 95, minus 100 = -5
                assertThat(adjustedTotal).isEqualByComparingTo(new BigDecimal("-5.00"));
            }
        }
    }

    @Test
    void testExecuteAggregateMultiplication() {
        // Given: SUM(price) * 1.2 (adding 20% tax)
        String selectString = "productType.name:typeName, SUM(price) * 1.2:totalWithTax";

        // When
        List<Tuple> results = SimpleQueryExecutor.getAggregateQueryResultWithSelectExpression(
            Product.class,
            Tuple.class,
            selectString,
            "",
            null,
            null,
            rsqlContext,
            compiler
        );

        // Then
        assertThat(results).hasSize(2);

        for (Tuple tuple : results) {
            String typeName = tuple.get(0, String.class);
            BigDecimal totalWithTax = tuple.get(1, BigDecimal.class);

            if ("Electronics".equals(typeName)) {
                // Sum: 1100 * 1.2 = 1320
                assertThat(totalWithTax).isEqualByComparingTo(new BigDecimal("1320.00"));
            } else if ("Books".equals(typeName)) {
                // Sum: 95 * 1.2 = 114
                assertThat(totalWithTax).isEqualByComparingTo(new BigDecimal("114.00"));
            }
        }
    }

    @Test
    void testExecuteAggregateDivision() {
        // Given: SUM(price) / COUNT(*) (average price calculation)
        String selectString = "productType.name:typeName, SUM(price) / COUNT(*):avgPrice";

        // When
        List<Tuple> results = SimpleQueryExecutor.getAggregateQueryResultWithSelectExpression(
            Product.class,
            Tuple.class,
            selectString,
            "",
            null,
            null,
            rsqlContext,
            compiler
        );

        // Then
        assertThat(results).hasSize(2);

        for (Tuple tuple : results) {
            String typeName = tuple.get(0, String.class);
            BigDecimal avgPrice = tuple.get(1, BigDecimal.class);

            if ("Electronics".equals(typeName)) {
                // Sum: 1100, Count: 3, Average: 366.67 (approximately)
                assertThat(avgPrice).isGreaterThan(new BigDecimal("360"));
                assertThat(avgPrice).isLessThan(new BigDecimal("370"));
            } else if ("Books".equals(typeName)) {
                // Sum: 95, Count: 2, Average: 47.5
                assertThat(avgPrice).isEqualByComparingTo(new BigDecimal("47.5"));
            }
        }
    }

    @Test
    void testExecuteComplexExpression() {
        // Given: (SUM(price) - 50) * 2 / COUNT(*)
        String selectString = "productType.name:typeName, (SUM(price) - 50) * 2 / COUNT(*):complexMetric";

        // When
        List<Tuple> results = SimpleQueryExecutor.getAggregateQueryResultWithSelectExpression(
            Product.class,
            Tuple.class,
            selectString,
            "",
            null,
            null,
            rsqlContext,
            compiler
        );

        // Then
        assertThat(results).hasSize(2);

        for (Tuple tuple : results) {
            String typeName = tuple.get(0, String.class);
            BigDecimal complexMetric = tuple.get(1, BigDecimal.class);

            assertThat(complexMetric).isNotNull();

            if ("Electronics".equals(typeName)) {
                // (1100 - 50) * 2 / 3 = 1050 * 2 / 3 = 700
                assertThat(complexMetric).isEqualByComparingTo(new BigDecimal("700.00"));
            } else if ("Books".equals(typeName)) {
                // (95 - 50) * 2 / 2 = 45 * 2 / 2 = 45
                assertThat(complexMetric).isEqualByComparingTo(new BigDecimal("45.00"));
            }
        }
    }

    @Test
    void testExecuteWithWhereFilter() {
        // Given: Expression with WHERE filter
        String selectString = "productType.name:typeName, SUM(price):total";
        String filter = "price=gt=50"; // Only products with price > 50

        // When
        List<Tuple> results = SimpleQueryExecutor.getAggregateQueryResultWithSelectExpression(
            Product.class,
            Tuple.class,
            selectString,
            filter,
            null,
            null,
            rsqlContext,
            compiler
        );

        // Then
        assertThat(results).hasSize(1); // Only electronics

        for (Tuple tuple : results) {
            String typeName = tuple.get(0, String.class);
            BigDecimal total = tuple.get(1, BigDecimal.class);

            if ("Electronics".equals(typeName)) {
                // Only Laptop (1000) and Keyboard (75) = 1075
                assertThat(total).isEqualByComparingTo(new BigDecimal("1075.00"));
            } else if ("Books".equals(typeName)) {
                // No books with price > 50
                // Actually, this group won't appear since there are no matching records
            }
        }
    }

    @Test
    void testExecuteMultipleExpressions() {
        // Given: Multiple expressions in SELECT
        String selectString = "productType.name:typeName, " +
                             "SUM(price):total, " +
                             "COUNT(*):count, " +
                             "SUM(price) / COUNT(*):average";

        // When
        List<Tuple> results = SimpleQueryExecutor.getAggregateQueryResultWithSelectExpression(
            Product.class,
            Tuple.class,
            selectString,
            "",
            null,
            null,
            rsqlContext,
            compiler
        );

        // Then
        assertThat(results).hasSize(2);

        for (Tuple tuple : results) {
            String typeName = tuple.get(0, String.class);
            BigDecimal total = tuple.get(1, BigDecimal.class);
            Long count = tuple.get(2, Long.class);
            BigDecimal average = tuple.get(3, BigDecimal.class);

            if ("Electronics".equals(typeName)) {
                assertThat(total).isEqualByComparingTo(new BigDecimal("1100.00"));
                assertThat(count).isEqualTo(3L);
                // Average should be total / count
                assertThat(average).isGreaterThan(new BigDecimal("360"));
            } else if ("Books".equals(typeName)) {
                assertThat(total).isEqualByComparingTo(new BigDecimal("95.00"));
                assertThat(count).isEqualTo(2L);
                assertThat(average).isEqualByComparingTo(new BigDecimal("47.5"));
            }
        }
    }

    // ==================== Pagination Tests ====================

    @Test
    void testExecuteAggregateWithPagination_FirstPage() {
        // Given: Pagination - first page, size 1
        String selectString = "productType.name:typeName, COUNT(*):count, SUM(price):total";
        Pageable pageable = PageRequest.of(0, 1, Sort.by("typeName").ascending());

        // When
        Page<Tuple> page = SimpleQueryExecutor.getAggregateQueryResultAsPageWithSelectExpression(
            Product.class,
            Tuple.class,
            selectString,
            "",
            null,
            pageable,
            rsqlContext,
            compiler
        );

        // Then
        assertThat(page.getTotalElements()).isEqualTo(2); // 2 groups: Electronics, Books
        assertThat(page.getTotalPages()).isEqualTo(2);
        assertThat(page.getNumber()).isEqualTo(0); // First page
        assertThat(page.getSize()).isEqualTo(1);
        assertThat(page.getContent()).hasSize(1);

        // First page should contain "Books" (alphabetically first)
        Tuple firstResult = page.getContent().get(0);
        String typeName = (String) firstResult.get("typeName");
        assertThat(typeName).isEqualTo("Books");
    }

    @Test
    void testExecuteAggregateWithPagination_SecondPage() {
        // Given: Pagination - second page, size 1
        String selectString = "productType.name:typeName, COUNT(*):count, SUM(price):total";
        Pageable pageable = PageRequest.of(1, 1, Sort.by("typeName").ascending());

        // When
        Page<Tuple> page = SimpleQueryExecutor.getAggregateQueryResultAsPageWithSelectExpression(
            Product.class,
            Tuple.class,
            selectString,
            "",
            null,
            pageable,
            rsqlContext,
            compiler
        );

        // Then
        assertThat(page.getTotalElements()).isEqualTo(2);
        assertThat(page.getTotalPages()).isEqualTo(2);
        assertThat(page.getNumber()).isEqualTo(1); // Second page
        assertThat(page.getContent()).hasSize(1);

        // Second page should contain "Electronics"
        Tuple secondResult = page.getContent().get(0);
        String typeName = (String) secondResult.get("typeName");
        assertThat(typeName).isEqualTo("Electronics");
    }

    @Test
    void testExecuteAggregateWithPagination_WithArithmetic() {
        // Given: Pagination with arithmetic expressions
        String selectString = "productType.name:typeName, SUM(price) * 1.2:totalWithTax";
        Pageable pageable = PageRequest.of(0, 1, Sort.by("typeName").ascending());

        // When
        Page<Tuple> page = SimpleQueryExecutor.getAggregateQueryResultAsPageWithSelectExpression(
            Product.class,
            Tuple.class,
            selectString,
            "",
            null,
            pageable,
            rsqlContext,
            compiler
        );

        // Then
        assertThat(page.getTotalElements()).isEqualTo(2);
        assertThat(page.getTotalPages()).isEqualTo(2);
        assertThat(page.getContent()).hasSize(1);

        // Verify arithmetic calculation
        Tuple result = page.getContent().get(0);
        String typeName = (String) result.get("typeName");
        BigDecimal totalWithTax = (BigDecimal) result.get("totalWithTax");

        if ("Books".equals(typeName)) {
            // Books total: 50 + 45 = 95, with 20% tax: 95 * 1.2 = 114
            assertThat(totalWithTax).isEqualByComparingTo(new BigDecimal("114.00"));
        }
    }

    @Test
    void testExecuteAggregateWithPagination_WithSorting() {
        // Given: Pagination with sorting by aggregated field (descending)
        String selectString = "productType.name:typeName, SUM(price):total";
        Pageable pageable = PageRequest.of(0, 10, Sort.by("total").descending());

        // When
        Page<Tuple> page = SimpleQueryExecutor.getAggregateQueryResultAsPageWithSelectExpression(
            Product.class,
            Tuple.class,
            selectString,
            "",
            null,
            pageable,
            rsqlContext,
            compiler
        );

        // Then
        assertThat(page.getTotalElements()).isEqualTo(2);
        assertThat(page.getContent()).hasSize(2);

        // First result should be Electronics (higher total: 1100)
        Tuple firstResult = page.getContent().get(0);
        String firstType = (String) firstResult.get("typeName");
        BigDecimal firstTotal = (BigDecimal) firstResult.get("total");
        assertThat(firstType).isEqualTo("Electronics");
        assertThat(firstTotal).isEqualByComparingTo(new BigDecimal("1100.00"));

        // Second result should be Books (lower total: 95)
        Tuple secondResult = page.getContent().get(1);
        String secondType = (String) secondResult.get("typeName");
        BigDecimal secondTotal = (BigDecimal) secondResult.get("total");
        assertThat(secondType).isEqualTo("Books");
        assertThat(secondTotal).isEqualByComparingTo(new BigDecimal("95.00"));
    }

    @Test
    void testExecuteAggregateWithPagination_WithHaving() {
        // Given: Pagination with HAVING filter
        String selectString = "productType.name:typeName, COUNT(*):count, SUM(price):total";
        String havingFilter = "total=gt=100"; // Only groups with total > 100
        Pageable pageable = PageRequest.of(0, 10);

        // When
        Page<Tuple> page = SimpleQueryExecutor.getAggregateQueryResultAsPageWithSelect(
            Product.class,
            Tuple.class,
            selectString,
            "",
            havingFilter,
            pageable,
            rsqlContext,
            compiler
        );

        // Then: Only Electronics should match (total=1100), Books (total=95) filtered out
        assertThat(page.getTotalElements()).isEqualTo(1);
        assertThat(page.getContent()).hasSize(1);

        Tuple result = page.getContent().get(0);
        String typeName = (String) result.get("typeName");
        BigDecimal total = (BigDecimal) result.get("total");

        assertThat(typeName).isEqualTo("Electronics");
        assertThat(total).isEqualByComparingTo(new BigDecimal("1100.00"));
    }

    @Test
    void testExecuteAggregateWithPagination_EmptyResult() {
        // Given: WHERE filter that eliminates all records
        String selectString = "productType.name:typeName, COUNT(*):count";
        String filter = "price=gt=10000"; // No products with price > 10000
        Pageable pageable = PageRequest.of(0, 10);

        // When
        Page<Tuple> page = SimpleQueryExecutor.getAggregateQueryResultAsPageWithSelectExpression(
            Product.class,
            Tuple.class,
            selectString,
            filter,
            null,
            pageable,
            rsqlContext,
            compiler
        );

        // Then
        assertThat(page.getTotalElements()).isEqualTo(0);
        assertThat(page.getTotalPages()).isEqualTo(0);
        assertThat(page.getContent()).isEmpty();
    }
}

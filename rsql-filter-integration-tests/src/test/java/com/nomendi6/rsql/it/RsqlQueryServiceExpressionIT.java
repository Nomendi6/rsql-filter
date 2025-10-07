package com.nomendi6.rsql.it;

import com.nomendi6.rsql.it.config.IntegrationTest;
import com.nomendi6.rsql.it.domain.Product;
import com.nomendi6.rsql.it.domain.ProductType;
import com.nomendi6.rsql.it.domain.StandardRecordStatus;
import com.nomendi6.rsql.it.service.dto.ProductDTO;
import com.nomendi6.rsql.it.repository.ProductRepository;
import com.nomendi6.rsql.it.repository.ProductTypeRepository;
import com.nomendi6.rsql.it.service.mapper.ProductMapper;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Tuple;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import rsql.RsqlQueryService;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Integration tests for RsqlQueryService expression methods.
 * Tests getAggregateResultWithExpressions and getAggregateResultAsPageWithExpressions.
 */
@IntegrationTest
public class RsqlQueryServiceExpressionIT {

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
    void init() {
        queryService = new RsqlQueryService<>(productRepository, productMapper, em, Product.class);
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

        // Create products with specific prices for arithmetic testing
        productRepository.save(new Product()
            .withCode("P001")
            .withName("Laptop")
            .withPrice(new BigDecimal("1000.00"))
            .withProductType(type1)
            .withStatus(StandardRecordStatus.ACTIVE));

        productRepository.save(new Product()
            .withCode("P002")
            .withName("Mouse")
            .withPrice(new BigDecimal("50.00"))
            .withProductType(type1)
            .withStatus(StandardRecordStatus.ACTIVE));

        productRepository.save(new Product()
            .withCode("P003")
            .withName("Keyboard")
            .withPrice(new BigDecimal("50.00"))
            .withProductType(type1)
            .withStatus(StandardRecordStatus.ACTIVE));

        productRepository.save(new Product()
            .withCode("P004")
            .withName("Book 1")
            .withPrice(new BigDecimal("25.00"))
            .withProductType(type2)
            .withStatus(StandardRecordStatus.ACTIVE));

        productRepository.save(new Product()
            .withCode("P005")
            .withName("Book 2")
            .withPrice(new BigDecimal("70.00"))
            .withProductType(type2)
            .withStatus(StandardRecordStatus.ACTIVE));
    }

    // ==================== getAggregateResultWithExpressions Tests ====================

    @Test
    void testGetAggregateResultWithExpressions_SimpleArithmetic() {
        // When: Calculate total with 20% tax
        List<Tuple> results = queryService.getAggregateResultWithExpressions(
            "productType.name:typeName, SUM(price) * 1.2:totalWithTax",
            "",
            null,
            Pageable.unpaged()
        );

        // Then
        assertThat(results).hasSize(2);

        // Find Electronics result
        Tuple electronics = results.stream()
            .filter(t -> "Electronics".equals(t.get("typeName")))
            .findFirst()
            .orElseThrow();

        BigDecimal totalWithTax = (BigDecimal) electronics.get("totalWithTax");
        // Electronics: 1000 + 50 + 50 = 1100, * 1.2 = 1320
        assertThat(totalWithTax).isEqualByComparingTo(new BigDecimal("1320.00"));
    }

    @Test
    void testGetAggregateResultWithExpressions_SubtractionExpression() {
        // When: Calculate price difference (simulating debit - credit)
        // Using MAX(price) - MIN(price) to get the range
        List<Tuple> results = queryService.getAggregateResultWithExpressions(
            "productType.name:typeName, MAX(price) - MIN(price):priceRange",
            "",
            null,
            Pageable.unpaged()
        );

        // Then
        assertThat(results).hasSize(2);

        // Electronics: MAX(1000) - MIN(50) = 950
        Tuple electronics = results.stream()
            .filter(t -> "Electronics".equals(t.get("typeName")))
            .findFirst()
            .orElseThrow();

        BigDecimal range = (BigDecimal) electronics.get("priceRange");
        assertThat(range).isEqualByComparingTo(new BigDecimal("950.00"));
    }

    @Test
    void testGetAggregateResultWithExpressions_WithFilter() {
        // When: Only ACTIVE products, calculate total with tax
        List<Tuple> results = queryService.getAggregateResultWithExpressions(
            "productType.name:typeName, SUM(price) * 1.2:totalWithTax, COUNT(*):count",
            "status=='ACTIVE'",  // Enum needs quotes
            null,
            Pageable.unpaged()
        );

        // Then: Should get all products (all are ACTIVE)
        assertThat(results).hasSize(2);

        Tuple electronics = results.stream()
            .filter(t -> "Electronics".equals(t.get("typeName")))
            .findFirst()
            .orElseThrow();

        assertThat(electronics.get("count")).isEqualTo(3L);
    }

    @Test
    void testGetAggregateResultWithExpressions_WithSort() {
        // When: Sort by entity property (not alias - expression mode doesn't support alias sorting yet)
        List<Tuple> results = queryService.getAggregateResultWithExpressions(
            "productType.name:typeName, SUM(price):total",
            "",
            null,
            PageRequest.of(0, 10, Sort.by("productType.name").ascending())
        );

        // Then: Books should be first (alphabetically)
        assertThat(results).hasSize(2);
        assertThat(results.get(0).get("typeName")).isEqualTo("Books");
        assertThat(results.get(1).get("typeName")).isEqualTo("Electronics");
    }

    @Test
    void testGetAggregateResultWithExpressions_WithHaving() {
        // When: HAVING filter on aggregate field
        List<Tuple> results = queryService.getAggregateResultWithExpressions(
            "productType.name:typeName, SUM(price):total",
            "",
            "total=gt=100",  // HAVING now supported
            Pageable.unpaged()
        );

        // Then: Only Electronics (total=1100) should pass HAVING filter
        assertThat(results).hasSize(1);
        assertThat(results.get(0).get("typeName")).isEqualTo("Electronics");

        BigDecimal total = (BigDecimal) results.get(0).get("total");
        assertThat(total).isEqualByComparingTo(new BigDecimal("1100.00"));
    }

    @Test
    void testGetAggregateResultWithExpressions_HavingWithArithmetic() {
        // When: HAVING filter on arithmetic expression
        List<Tuple> results = queryService.getAggregateResultWithExpressions(
            "productType.name:typeName, SUM(price) * 1.2:totalWithTax",
            "",
            "totalWithTax=gt=200",  // Filter on arithmetic result
            Pageable.unpaged()
        );

        // Then: Only Electronics (1100 * 1.2 = 1320) should pass
        assertThat(results).hasSize(1);
        assertThat(results.get(0).get("typeName")).isEqualTo("Electronics");

        BigDecimal totalWithTax = (BigDecimal) results.get(0).get("totalWithTax");
        assertThat(totalWithTax).isEqualByComparingTo(new BigDecimal("1320.00"));
    }

    @Test
    void testGetAggregateResultWithExpressions_HavingFiltersAll() {
        // When: HAVING filter that matches nothing
        List<Tuple> results = queryService.getAggregateResultWithExpressions(
            "productType.name:typeName, SUM(price):total",
            "",
            "total=gt=10000",  // No groups have total > 10000
            Pageable.unpaged()
        );

        // Then: Empty result
        assertThat(results).isEmpty();
    }

    // ==================== getAggregateResultAsPageWithExpressions Tests ====================

    @Test
    void testGetAggregateResultAsPageWithExpressions_BasicPagination() {
        // When: First page with size 1
        Page<Tuple> page = queryService.getAggregateResultAsPageWithExpressions(
            "productType.name:typeName, SUM(price) * 1.2:totalWithTax",
            "",
            null,
            PageRequest.of(0, 1, Sort.by("typeName").ascending())
        );

        // Then
        assertThat(page.getTotalElements()).isEqualTo(2);
        assertThat(page.getTotalPages()).isEqualTo(2);
        assertThat(page.getNumber()).isEqualTo(0);
        assertThat(page.getContent()).hasSize(1);

        // First should be "Books" (alphabetically)
        assertThat(page.getContent().get(0).get("typeName")).isEqualTo("Books");
    }

    @Test
    void testGetAggregateResultAsPageWithExpressions_SecondPage() {
        // When: Second page
        Page<Tuple> page = queryService.getAggregateResultAsPageWithExpressions(
            "productType.name:typeName, SUM(price):total",
            "",
            null,
            PageRequest.of(1, 1, Sort.by("typeName").ascending())
        );

        // Then
        assertThat(page.getNumber()).isEqualTo(1);
        assertThat(page.getContent()).hasSize(1);
        assertThat(page.getContent().get(0).get("typeName")).isEqualTo("Electronics");
    }

    @Test
    void testGetAggregateResultAsPageWithExpressions_SortByCalculatedField() {
        // When: Sort by arithmetic expression result
        Page<Tuple> page = queryService.getAggregateResultAsPageWithExpressions(
            "productType.name:typeName, SUM(price) * 1.2:totalWithTax",
            "",
            null,
            PageRequest.of(0, 10, Sort.by("totalWithTax").descending())
        );

        // Then: Electronics should be first (higher total)
        assertThat(page.getContent()).hasSize(2);
        assertThat(page.getContent().get(0).get("typeName")).isEqualTo("Electronics");

        BigDecimal firstTotal = (BigDecimal) page.getContent().get(0).get("totalWithTax");
        BigDecimal secondTotal = (BigDecimal) page.getContent().get(1).get("totalWithTax");
        assertThat(firstTotal).isGreaterThan(secondTotal);
    }

    @Test
    void testGetAggregateResultAsPageWithExpressions_ComplexArithmetic() {
        // When: Multiple arithmetic operations
        Page<Tuple> page = queryService.getAggregateResultAsPageWithExpressions(
            "productType.name:typeName, (SUM(price) * 1.2 + 10):adjustedTotal, COUNT(*):count",
            "",
            null,
            PageRequest.of(0, 10)
        );

        // Then
        assertThat(page.getContent()).hasSize(2);

        Tuple electronics = page.getContent().stream()
            .filter(t -> "Electronics".equals(t.get("typeName")))
            .findFirst()
            .orElseThrow();

        // Electronics: (1100 * 1.2) + 10 = 1320 + 10 = 1330
        BigDecimal adjustedTotal = (BigDecimal) electronics.get("adjustedTotal");
        assertThat(adjustedTotal).isEqualByComparingTo(new BigDecimal("1330.00"));
    }

    @Test
    void testGetAggregateResultAsPageWithExpressions_WithFilter() {
        // When: Filter before aggregation
        Page<Tuple> page = queryService.getAggregateResultAsPageWithExpressions(
            "productType.name:typeName, SUM(price):total",
            "price=ge=50",  // Only products with price >= 50
            null,
            PageRequest.of(0, 10)
        );

        // Then
        assertThat(page.getContent()).hasSize(2);

        // Books should have 1 product (only Book 2 with price 70)
        Tuple books = page.getContent().stream()
            .filter(t -> "Books".equals(t.get("typeName")))
            .findFirst()
            .orElseThrow();

        BigDecimal booksTotal = (BigDecimal) books.get("total");
        assertThat(booksTotal).isEqualByComparingTo(new BigDecimal("70.00"));
    }

    @Test
    void testGetAggregateResultAsPageWithExpressions_EmptyResult() {
        // When: Filter that matches nothing
        Page<Tuple> page = queryService.getAggregateResultAsPageWithExpressions(
            "productType.name:typeName, SUM(price):total",
            "price=gt=10000",  // No products this expensive
            null,
            PageRequest.of(0, 10)
        );

        // Then
        assertThat(page.getTotalElements()).isEqualTo(0);
        assertThat(page.getTotalPages()).isEqualTo(0);
        assertThat(page.getContent()).isEmpty();
    }

    @Test
    void testGetAggregateResultAsPageWithExpressions_WithHaving() {
        // When: HAVING filter on aggregate field with pagination
        Page<Tuple> page = queryService.getAggregateResultAsPageWithExpressions(
            "productType.name:typeName, SUM(price):total",
            "",
            "total=gt=100",  // HAVING now supported
            PageRequest.of(0, 10)
        );

        // Then: Only Electronics (total=1100) should pass HAVING filter
        assertThat(page.getContent()).hasSize(1);
        assertThat(page.getTotalElements()).isEqualTo(1);  // Total after HAVING
        assertThat(page.getContent().get(0).get("typeName")).isEqualTo("Electronics");

        BigDecimal total = (BigDecimal) page.getContent().get(0).get("total");
        assertThat(total).isEqualByComparingTo(new BigDecimal("1100.00"));
    }

    @Test
    void testGetAggregateResultAsPageWithExpressions_HavingWithArithmetic() {
        // When: HAVING filter on arithmetic expression with pagination
        Page<Tuple> page = queryService.getAggregateResultAsPageWithExpressions(
            "productType.name:typeName, SUM(price) * 1.2:totalWithTax, COUNT(*):count",
            "",
            "totalWithTax=gt=200",  // Filter on arithmetic result
            PageRequest.of(0, 10, Sort.by("totalWithTax").descending())
        );

        // Then: Only Electronics (1100 * 1.2 = 1320) should pass
        assertThat(page.getContent()).hasSize(1);
        assertThat(page.getTotalElements()).isEqualTo(1);
        assertThat(page.getContent().get(0).get("typeName")).isEqualTo("Electronics");

        BigDecimal totalWithTax = (BigDecimal) page.getContent().get(0).get("totalWithTax");
        assertThat(totalWithTax).isEqualByComparingTo(new BigDecimal("1320.00"));
        assertThat(page.getContent().get(0).get("count")).isEqualTo(3L);
    }

    @Test
    void testGetAggregateResultAsPageWithExpressions_HavingFiltersAll() {
        // When: HAVING filter that matches nothing
        Page<Tuple> page = queryService.getAggregateResultAsPageWithExpressions(
            "productType.name:typeName, SUM(price):total",
            "",
            "total=gt=10000",  // No groups have total > 10000
            PageRequest.of(0, 10)
        );

        // Then: Empty result with correct metadata
        assertThat(page.getContent()).isEmpty();
        assertThat(page.getTotalElements()).isEqualTo(0);
        assertThat(page.getTotalPages()).isEqualTo(0);
    }

    @Test
    void testGetAggregateResultAsPageWithExpressions_ComplexHaving() {
        // When: Complex HAVING with multiple conditions on arithmetic expressions
        Page<Tuple> page = queryService.getAggregateResultAsPageWithExpressions(
            "productType.name:typeName, SUM(price):total, SUM(price) * 1.2:totalWithTax",
            "",
            "total=gt=90;totalWithTax=lt=2000",  // AND condition
            PageRequest.of(0, 10)
        );

        // Then: Both Books (total=95, totalWithTax=114) and Electronics (total=1100, totalWithTax=1320) pass
        assertThat(page.getContent()).hasSize(2);
        assertThat(page.getTotalElements()).isEqualTo(2);
    }

    @Test
    void testGetAggregateResultAsPageWithExpressions_Unpaged() {
        // When: Unpaged request
        Page<Tuple> page = queryService.getAggregateResultAsPageWithExpressions(
            "productType.name:typeName, SUM(price):total",
            "",
            null,
            Pageable.unpaged()
        );

        // Then: Should return all results
        assertThat(page.getContent()).hasSize(2);
        assertThat(page.getTotalElements()).isEqualTo(2); // Unpaged still returns total count
    }

    // ==================== Tests for Sorting by Field Paths ====================

    @Test
    void testGetAggregateResultAsPageWithExpressions_SortByFieldPath() {
        // When: Sort by field path (not alias)
        Page<Tuple> page = queryService.getAggregateResultAsPageWithExpressions(
            "productType.name:typeName, SUM(price):total",
            "",
            null,
            PageRequest.of(0, 10, Sort.by("productType.name").ascending())
        );

        // Then: Books should be first (alphabetically before Electronics)
        assertThat(page.getContent()).hasSize(2);
        assertThat(page.getContent().get(0).get("typeName")).isEqualTo("Books");
        assertThat(page.getContent().get(1).get("typeName")).isEqualTo("Electronics");
    }

    @Test
    void testGetAggregateResultAsPageWithExpressions_SortByFieldPathDescending() {
        // When: Sort by field path descending
        Page<Tuple> page = queryService.getAggregateResultAsPageWithExpressions(
            "productType.name:typeName, SUM(price):total",
            "",
            null,
            PageRequest.of(0, 10, Sort.by("productType.name").descending())
        );

        // Then: Electronics should be first (alphabetically after Books in descending order)
        assertThat(page.getContent()).hasSize(2);
        assertThat(page.getContent().get(0).get("typeName")).isEqualTo("Electronics");
        assertThat(page.getContent().get(1).get("typeName")).isEqualTo("Books");
    }

    @Test
    void testGetAggregateResultAsPageWithExpressions_SortByAggregateAlias() {
        // When: Sort by aggregate alias
        Page<Tuple> page = queryService.getAggregateResultAsPageWithExpressions(
            "productType.name:typeName, SUM(price):total",
            "",
            null,
            PageRequest.of(0, 10, Sort.by("total").descending())
        );

        // Then: Electronics should be first (higher total)
        assertThat(page.getContent()).hasSize(2);
        assertThat(page.getContent().get(0).get("typeName")).isEqualTo("Electronics");

        BigDecimal firstTotal = (BigDecimal) page.getContent().get(0).get("total");
        BigDecimal secondTotal = (BigDecimal) page.getContent().get(1).get("total");
        assertThat(firstTotal).isGreaterThan(secondTotal);
    }

    @Test
    void testGetAggregateResultAsPageWithExpressions_SortByArithmeticExpressionAlias() {
        // When: Sort by arithmetic expression alias
        Page<Tuple> page = queryService.getAggregateResultAsPageWithExpressions(
            "productType.name:typeName, SUM(price):total, SUM(price) - 50:adjusted",
            "",
            null,
            PageRequest.of(0, 10, Sort.by("adjusted").ascending())
        );

        // Then: Books should be first (smaller adjusted value)
        assertThat(page.getContent()).hasSize(2);
        assertThat(page.getContent().get(0).get("typeName")).isEqualTo("Books");

        BigDecimal firstAdjusted = (BigDecimal) page.getContent().get(0).get("adjusted");
        BigDecimal secondAdjusted = (BigDecimal) page.getContent().get(1).get("adjusted");
        assertThat(firstAdjusted).isLessThan(secondAdjusted);
    }

    @Test
    void testGetAggregateResultAsPageWithExpressions_SortByMultipleColumns() {
        // When: Sort by multiple columns (field path + aggregate alias)
        Page<Tuple> page = queryService.getAggregateResultAsPageWithExpressions(
            "productType.name:typeName, SUM(price):total, COUNT(*):count",
            "",
            null,
            PageRequest.of(0, 10, Sort.by("productType.name").ascending().and(Sort.by("total").descending()))
        );

        // Then: Should be sorted first by typeName, then by total
        assertThat(page.getContent()).hasSize(2);
        assertThat(page.getContent().get(0).get("typeName")).isEqualTo("Books");
        assertThat(page.getContent().get(1).get("typeName")).isEqualTo("Electronics");
    }

    @Test
    void testGetAggregateResultAsPage_SortByFieldPath() {
        // When: Sort by field path using AggregateField-based method (non-expression)
        Page<Tuple> page = queryService.getAggregateResultAsPage(
            "productType.name:typeName, COUNT(*):count, SUM(price):total",
            "",
            null,
            PageRequest.of(0, 10, Sort.by("productType.name").descending())
        );

        // Then: Electronics should be first (descending order)
        assertThat(page.getContent()).hasSize(2);
        assertThat(page.getContent().get(0).get("typeName")).isEqualTo("Electronics");
        assertThat(page.getContent().get(1).get("typeName")).isEqualTo("Books");
    }
}

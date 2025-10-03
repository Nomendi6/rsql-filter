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
import jakarta.persistence.Tuple;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import rsql.RsqlQueryService;
import rsql.helper.TupleConverter;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for TupleConverter utility class.
 * Tests conversion of JPA Tuple results to JSON-friendly Map format.
 */
@IntegrationTest
public class TupleConverterIT {

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
        ProductType electronics = new ProductType()
            .withCode("ELEC")
            .withName("Electronics");

        ProductType books = new ProductType()
            .withCode("BOOK")
            .withName("Books");

        productTypeRepository.save(electronics);
        productTypeRepository.save(books);

        // Create products
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
            .withName("Java Book")
            .withPrice(new BigDecimal("50.00"))
            .withProductType(books)
            .withStatus(StandardRecordStatus.ACTIVE));

        productRepository.save(new Product()
            .withCode("P004")
            .withName("Keyboard")
            .withPrice(new BigDecimal("75.00"))
            .withProductType(electronics)
            .withStatus(StandardRecordStatus.INACTIVE));
    }

    @Test
    void testToMap_SingleTuple() {
        // Given - Without explicit aliases
        List<Tuple> tuples = queryService.getTupleWithSelect(
            "code, name, price",
            "code=='P001'",
            PageRequest.of(0, 1)
        );

        assertThat(tuples).hasSize(1);

        // When
        Map<String, Object> result = TupleConverter.toMap(tuples.get(0));

        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(3);
        // Without explicit aliases, default field names are used
        assertThat(result.get("code")).isEqualTo("P001");
        assertThat(result.get("name")).isEqualTo("Laptop");
        assertThat(result.get("price")).isEqualTo(new BigDecimal("1000.00"));
    }

    @Test
    void testToMapList_MultipleTuples() {
        // Given - Without explicit aliases
        List<Tuple> tuples = queryService.getTupleWithSelect(
            "code, name, price",
            "status==#ACTIVE#",
            PageRequest.of(0, 10, Sort.by("code"))
        );

        assertThat(tuples).hasSize(3);

        // When
        List<Map<String, Object>> results = TupleConverter.toMapList(tuples);

        // Then
        assertThat(results).hasSize(3);

        Map<String, Object> firstProduct = results.get(0);
        assertThat(firstProduct.get("code")).isEqualTo("P001");
        assertThat(firstProduct.get("name")).isEqualTo("Laptop");

        Map<String, Object> secondProduct = results.get(1);
        assertThat(secondProduct.get("code")).isEqualTo("P002");
        assertThat(secondProduct.get("name")).isEqualTo("Mouse");

        Map<String, Object> thirdProduct = results.get(2);
        assertThat(thirdProduct.get("code")).isEqualTo("P003");
        assertThat(thirdProduct.get("name")).isEqualTo("Java Book");
    }

    @Test
    void testToMap_WithAliases() {
        // Given
        List<Tuple> tuples = queryService.getTupleWithSelect(
            "code:productCode, name:productName, price:productPrice",
            "code=='P001'",
            PageRequest.of(0, 1)
        );

        // When
        Map<String, Object> result = TupleConverter.toMap(tuples.get(0));

        // Then
        assertThat(result).hasSize(3);
        assertThat(result.get("productCode")).isEqualTo("P001");
        assertThat(result.get("productName")).isEqualTo("Laptop");
        assertThat(result.get("productPrice")).isEqualTo(new BigDecimal("1000.00"));

        // Verify original field names are NOT in the map
        assertThat(result).doesNotContainKey("code");
        assertThat(result).doesNotContainKey("name");
        assertThat(result).doesNotContainKey("price");
    }

    @Test
    void testToMap_WithNavigationProperties() {
        // Given - With mixed explicit and implicit aliases
        List<Tuple> tuples = queryService.getTupleWithSelect(
            "code:id, name, productType.name:category, price",
            "code=='P001'",
            PageRequest.of(0, 1)
        );

        // When
        Map<String, Object> result = TupleConverter.toMap(tuples.get(0));

        // Then
        assertThat(result).hasSize(4);
        assertThat(result.get("id")).isEqualTo("P001"); // Explicit alias
        assertThat(result.get("name")).isEqualTo("Laptop"); // Implicit alias (field name)
        assertThat(result.get("category")).isEqualTo("Electronics"); // Explicit alias
        assertThat(result.get("price")).isEqualTo(new BigDecimal("1000.00")); // Implicit alias (field name)
    }

    @Test
    void testToMap_WithAggregates() {
        // Given - Aggregate query with explicit aliases
        List<Tuple> tuples = queryService.getAggregateResult(
            "productType.name:category, COUNT(*):count, SUM(price):total, AVG(price):average",
            "status==#ACTIVE#",
            (String) null,  // No HAVING filter
            PageRequest.of(0, 10, Sort.by("productType.name"))
        );

        assertThat(tuples).hasSize(2); // Books and Electronics

        // When
        List<Map<String, Object>> results = TupleConverter.toMapList(tuples);

        // Then
        assertThat(results).hasSize(2);

        // Find Books row
        Map<String, Object> booksRow = results.stream()
            .filter(row -> "Books".equals(row.get("category")))
            .findFirst()
            .orElseThrow();

        assertThat(booksRow.get("category")).isEqualTo("Books");
        assertThat(booksRow.get("count")).isEqualTo(1L);
        assertThat(booksRow.get("total")).isEqualTo(new BigDecimal("50.00"));
        assertThat(booksRow.get("average")).isEqualTo(50.0);

        // Find Electronics row
        Map<String, Object> electronicsRow = results.stream()
            .filter(row -> "Electronics".equals(row.get("category")))
            .findFirst()
            .orElseThrow();

        assertThat(electronicsRow.get("category")).isEqualTo("Electronics");
        assertThat(electronicsRow.get("count")).isEqualTo(2L); // Laptop and Mouse (active only)
        assertThat(electronicsRow.get("total")).isEqualTo(new BigDecimal("1025.00")); // 1000 + 25
        assertThat(electronicsRow.get("average")).isEqualTo(512.5); // (1000 + 25) / 2
    }

    @Test
    void testToMap_PreservesFieldOrder() {
        // Given - Without explicit aliases, order should be preserved
        List<Tuple> tuples = queryService.getTupleWithSelect(
            "price, name, code", // Specific order
            "code=='P001'",
            PageRequest.of(0, 1)
        );

        // When
        Map<String, Object> result = TupleConverter.toMap(tuples.get(0));

        // Then
        assertThat(result).hasSize(3);

        // LinkedHashMap preserves insertion order from SELECT string
        List<String> keys = new ArrayList<>(result.keySet());
        assertThat(keys).hasSize(3);
        assertThat(keys.get(0)).isEqualTo("price");
        assertThat(keys.get(1)).isEqualTo("name");
        assertThat(keys.get(2)).isEqualTo("code");
    }

    @Test
    void testToMap_WithNullValues() {
        // Create a product without productType (null)
        Product productWithoutType = new Product()
            .withCode("P999")
            .withName("Orphan Product")
            .withPrice(new BigDecimal("99.99"))
            .withProductType(null)
            .withStatus(StandardRecordStatus.ACTIVE);

        productRepository.save(productWithoutType);

        // Given - Mix of implicit and explicit aliases
        List<Tuple> tuples = queryService.getTupleWithSelect(
            "code, name, productType.name:category",
            "code=='P999'",
            PageRequest.of(0, 1)
        );

        // When
        Map<String, Object> result = TupleConverter.toMap(tuples.get(0));

        // Then
        assertThat(result).hasSize(3);
        assertThat(result.get("code")).isEqualTo("P999");
        assertThat(result.get("name")).isEqualTo("Orphan Product");
        assertThat(result.get("category")).isNull();

        // Verify the key exists even though value is null
        assertThat(result).containsKey("category");
    }

    @Test
    void testToMapList_EmptyList() {
        // Given
        List<Tuple> emptyTuples = queryService.getTupleWithSelect(
            "code, name",
            "code=='NONEXISTENT'",
            PageRequest.of(0, 10)
        );

        assertThat(emptyTuples).isEmpty();

        // When
        List<Map<String, Object>> results = TupleConverter.toMapList(emptyTuples);

        // Then
        assertThat(results).isEmpty();
    }
}

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
}

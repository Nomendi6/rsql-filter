package com.nomendi6.rsql.it;

import com.nomendi6.rsql.it.config.IntegrationTest;
import com.nomendi6.rsql.it.domain.Product;
import com.nomendi6.rsql.it.domain.ProductType;
import com.nomendi6.rsql.it.domain.StandardRecordStatus;
import com.nomendi6.rsql.it.repository.ProductRepository;
import com.nomendi6.rsql.it.repository.ProductTypeRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Tuple;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Selection;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.tree.ParseTree;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import rsql.select.SelectAggregateSelectionVisitor;
import rsql.select.SelectFieldSelectionVisitor;
import rsql.select.SelectTreeParser;
import rsql.where.RsqlContext;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for SelectFieldSelectionVisitor and SelectAggregateSelectionVisitor.
 * Tests JPA Criteria API integration with real entities and database.
 */
@IntegrationTest
public class SelectionVisitorIT {

    @Autowired
    private EntityManager em;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ProductTypeRepository productTypeRepository;

    private RsqlContext<Product> rsqlContext;
    private CriteriaBuilder criteriaBuilder;
    private SelectTreeParser parser;

    @BeforeEach
    void init() {
        rsqlContext = new RsqlContext<>(Product.class);
        rsqlContext.defineEntityManager(em);
        criteriaBuilder = em.getCriteriaBuilder();
        parser = new SelectTreeParser();
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

    // ==================== SelectFieldSelectionVisitor Tests ====================

    @Test
    void testSelectFieldSelectionVisitor_SingleField() {
        // Given
        String selectString = "code";
        ParseTree tree = parser.parseStream(CharStreams.fromString(selectString));

        CriteriaQuery<Tuple> query = criteriaBuilder.createTupleQuery();
        Root<Product> root = query.from(Product.class);

        SelectFieldSelectionVisitor visitor = new SelectFieldSelectionVisitor();
        visitor.setContext(rsqlContext, criteriaBuilder, root);

        // When
        List<Selection<?>> selections = visitor.visit(tree);
        query.multiselect(selections);

        TypedQuery<Tuple> typedQuery = em.createQuery(query);
        List<Tuple> results = typedQuery.getResultList();

        // Then
        assertThat(selections).hasSize(1);
        assertThat(results).hasSize(4);
        assertThat(results.get(0).get(0)).isIn("P001", "P002", "P003", "P004");
    }

    @Test
    void testSelectFieldSelectionVisitor_MultipleFields() {
        // Given
        String selectString = "code, name, price";
        ParseTree tree = parser.parseStream(CharStreams.fromString(selectString));

        CriteriaQuery<Tuple> query = criteriaBuilder.createTupleQuery();
        Root<Product> root = query.from(Product.class);

        SelectFieldSelectionVisitor visitor = new SelectFieldSelectionVisitor();
        visitor.setContext(rsqlContext, criteriaBuilder, root);

        // When
        List<Selection<?>> selections = visitor.visit(tree);
        query.multiselect(selections);

        TypedQuery<Tuple> typedQuery = em.createQuery(query);
        List<Tuple> results = typedQuery.getResultList();

        // Then
        assertThat(selections).hasSize(3);
        assertThat(results).hasSize(4);

        Tuple firstRow = results.get(0);
        assertThat(firstRow.get(0)).isNotNull(); // code
        assertThat(firstRow.get(1)).isNotNull(); // name
        assertThat(firstRow.get(2)).isNotNull(); // price
    }

    @Test
    void testSelectFieldSelectionVisitor_FieldWithAlias() {
        // Given
        String selectString = "code:productCode, name:productName";
        ParseTree tree = parser.parseStream(CharStreams.fromString(selectString));

        CriteriaQuery<Tuple> query = criteriaBuilder.createTupleQuery();
        Root<Product> root = query.from(Product.class);

        SelectFieldSelectionVisitor visitor = new SelectFieldSelectionVisitor();
        visitor.setContext(rsqlContext, criteriaBuilder, root);

        // When
        List<Selection<?>> selections = visitor.visit(tree);
        query.multiselect(selections);

        TypedQuery<Tuple> typedQuery = em.createQuery(query);
        List<Tuple> results = typedQuery.getResultList();

        // Then
        assertThat(selections).hasSize(2);
        assertThat(selections.get(0).getAlias()).isEqualTo("productCode");
        assertThat(selections.get(1).getAlias()).isEqualTo("productName");

        assertThat(results).hasSize(4);

        // Verify we can access by alias
        Tuple firstRow = results.get(0);
        assertThat(firstRow.get("productCode")).isNotNull();
        assertThat(firstRow.get("productName")).isNotNull();
    }

    @Test
    void testSelectFieldSelectionVisitor_NavigationProperty() {
        // Given
        String selectString = "code, productType.name";
        ParseTree tree = parser.parseStream(CharStreams.fromString(selectString));

        CriteriaQuery<Tuple> query = criteriaBuilder.createTupleQuery();
        Root<Product> root = query.from(Product.class);

        SelectFieldSelectionVisitor visitor = new SelectFieldSelectionVisitor();
        visitor.setContext(rsqlContext, criteriaBuilder, root);

        // When
        List<Selection<?>> selections = visitor.visit(tree);
        query.multiselect(selections);

        TypedQuery<Tuple> typedQuery = em.createQuery(query);
        List<Tuple> results = typedQuery.getResultList();

        // Then
        assertThat(selections).hasSize(2);
        assertThat(results).hasSize(4);

        // Verify navigation property is resolved
        Tuple firstRow = results.get(0);
        assertThat(firstRow.get(0)).isNotNull(); // code
        assertThat(firstRow.get(1)).isIn("Electronics", "Books"); // productType.name
    }

    @Test
    void testSelectFieldSelectionVisitor_NavigationPropertyWithAlias() {
        // Given
        String selectString = "code:productCode, productType.name:typeName";
        ParseTree tree = parser.parseStream(CharStreams.fromString(selectString));

        CriteriaQuery<Tuple> query = criteriaBuilder.createTupleQuery();
        Root<Product> root = query.from(Product.class);

        SelectFieldSelectionVisitor visitor = new SelectFieldSelectionVisitor();
        visitor.setContext(rsqlContext, criteriaBuilder, root);

        // When
        List<Selection<?>> selections = visitor.visit(tree);
        query.multiselect(selections);

        TypedQuery<Tuple> typedQuery = em.createQuery(query);
        List<Tuple> results = typedQuery.getResultList();

        // Then
        assertThat(selections).hasSize(2);
        assertThat(selections.get(0).getAlias()).isEqualTo("productCode");
        assertThat(selections.get(1).getAlias()).isEqualTo("typeName");

        assertThat(results).hasSize(4);

        // Verify we can access by alias
        Tuple firstRow = results.get(0);
        assertThat(firstRow.get("productCode")).isNotNull();
        assertThat(firstRow.get("typeName")).isIn("Electronics", "Books");
    }

    // ==================== SelectAggregateSelectionVisitor Tests ====================

    @Test
    void testSelectAggregateSelectionVisitor_CountAll() {
        // Given
        String selectString = "COUNT(*)";
        ParseTree tree = parser.parseStream(CharStreams.fromString(selectString));

        CriteriaQuery<Tuple> query = criteriaBuilder.createTupleQuery();
        Root<Product> root = query.from(Product.class);

        SelectAggregateSelectionVisitor visitor = new SelectAggregateSelectionVisitor();
        visitor.setContext(rsqlContext, criteriaBuilder, root);

        // When
        List<Selection<?>> selections = visitor.visit(tree);
        query.multiselect(selections);

        TypedQuery<Tuple> typedQuery = em.createQuery(query);
        List<Tuple> results = typedQuery.getResultList();

        // Then
        assertThat(selections).hasSize(1);
        assertThat(results).hasSize(1);
        assertThat(results.get(0).get(0)).isEqualTo(4L); // 4 products total
    }

    @Test
    void testSelectAggregateSelectionVisitor_SumWithAlias() {
        // Given
        String selectString = "SUM(price):totalPrice";
        ParseTree tree = parser.parseStream(CharStreams.fromString(selectString));

        CriteriaQuery<Tuple> query = criteriaBuilder.createTupleQuery();
        Root<Product> root = query.from(Product.class);

        SelectAggregateSelectionVisitor visitor = new SelectAggregateSelectionVisitor();
        visitor.setContext(rsqlContext, criteriaBuilder, root);

        // When
        List<Selection<?>> selections = visitor.visit(tree);
        query.multiselect(selections);

        TypedQuery<Tuple> typedQuery = em.createQuery(query);
        List<Tuple> results = typedQuery.getResultList();

        // Then
        assertThat(selections).hasSize(1);
        assertThat(selections.get(0).getAlias()).isEqualTo("totalPrice");

        assertThat(results).hasSize(1);

        // 1000 + 25 + 50 + 75 = 1150
        BigDecimal totalPrice = (BigDecimal) results.get(0).get("totalPrice");
        assertThat(totalPrice).isEqualByComparingTo(new BigDecimal("1150.00"));
    }

    @Test
    void testSelectAggregateSelectionVisitor_GroupByWithAggregates() {
        // Given
        String selectString = "productType.name:type, COUNT(*):count, SUM(price):total";
        ParseTree tree = parser.parseStream(CharStreams.fromString(selectString));

        CriteriaQuery<Tuple> query = criteriaBuilder.createTupleQuery();
        Root<Product> root = query.from(Product.class);

        SelectAggregateSelectionVisitor visitor = new SelectAggregateSelectionVisitor();
        visitor.setContext(rsqlContext, criteriaBuilder, root);

        // When
        List<Selection<?>> selections = visitor.visit(tree);
        query.multiselect(selections);
        query.groupBy(root.get("productType").get("name"));

        TypedQuery<Tuple> typedQuery = em.createQuery(query);
        List<Tuple> results = typedQuery.getResultList();

        // Then
        assertThat(selections).hasSize(3);
        assertThat(selections.get(0).getAlias()).isEqualTo("type");
        assertThat(selections.get(1).getAlias()).isEqualTo("count");
        assertThat(selections.get(2).getAlias()).isEqualTo("total");

        assertThat(results).hasSize(2); // Electronics and Books

        // Verify results
        Tuple electronicsRow = results.stream()
            .filter(t -> "Electronics".equals(t.get("type")))
            .findFirst()
            .orElseThrow();

        assertThat(electronicsRow.get("count")).isEqualTo(3L); // 3 electronics products
        BigDecimal electronicsTotal = (BigDecimal) electronicsRow.get("total");
        assertThat(electronicsTotal).isEqualByComparingTo(new BigDecimal("1100.00")); // 1000 + 25 + 75

        Tuple booksRow = results.stream()
            .filter(t -> "Books".equals(t.get("type")))
            .findFirst()
            .orElseThrow();

        assertThat(booksRow.get("count")).isEqualTo(1L);
        BigDecimal booksTotal = (BigDecimal) booksRow.get("total");
        assertThat(booksTotal).isEqualByComparingTo(new BigDecimal("50.00"));
    }

    @Test
    void testSelectAggregateSelectionVisitor_MinMaxAvg() {
        // Given
        String selectString = "MIN(price):minPrice, MAX(price):maxPrice, AVG(price):avgPrice";
        ParseTree tree = parser.parseStream(CharStreams.fromString(selectString));

        CriteriaQuery<Tuple> query = criteriaBuilder.createTupleQuery();
        Root<Product> root = query.from(Product.class);

        SelectAggregateSelectionVisitor visitor = new SelectAggregateSelectionVisitor();
        visitor.setContext(rsqlContext, criteriaBuilder, root);

        // When
        List<Selection<?>> selections = visitor.visit(tree);
        query.multiselect(selections);

        TypedQuery<Tuple> typedQuery = em.createQuery(query);
        List<Tuple> results = typedQuery.getResultList();

        // Then
        assertThat(selections).hasSize(3);
        assertThat(results).hasSize(1);

        Tuple result = results.get(0);
        BigDecimal minPrice = (BigDecimal) result.get("minPrice");
        BigDecimal maxPrice = (BigDecimal) result.get("maxPrice");
        Double avgPrice = (Double) result.get("avgPrice");

        assertThat(minPrice).isEqualByComparingTo(new BigDecimal("25.00"));
        assertThat(maxPrice).isEqualByComparingTo(new BigDecimal("1000.00"));
        assertThat(avgPrice).isEqualTo(287.5); // (1000 + 25 + 50 + 75) / 4
    }

    @Test
    void testSelectAggregateSelectionVisitor_CountDistinct() {
        // Given
        String selectString = "COUNT(DIST productType.name):distinctTypes";
        ParseTree tree = parser.parseStream(CharStreams.fromString(selectString));

        CriteriaQuery<Tuple> query = criteriaBuilder.createTupleQuery();
        Root<Product> root = query.from(Product.class);

        SelectAggregateSelectionVisitor visitor = new SelectAggregateSelectionVisitor();
        visitor.setContext(rsqlContext, criteriaBuilder, root);

        // When
        List<Selection<?>> selections = visitor.visit(tree);
        query.multiselect(selections);

        TypedQuery<Tuple> typedQuery = em.createQuery(query);
        List<Tuple> results = typedQuery.getResultList();

        // Then
        assertThat(selections).hasSize(1);
        assertThat(selections.get(0).getAlias()).isEqualTo("distinctTypes");

        assertThat(results).hasSize(1);
        assertThat(results.get(0).get("distinctTypes")).isEqualTo(2L); // Electronics and Books
    }

    @Test
    void testSelectAggregateSelectionVisitor_GroupByMultipleFields() {
        // Given
        String selectString = "productType.code:typeCode, status:productStatus, COUNT(*):count";
        ParseTree tree = parser.parseStream(CharStreams.fromString(selectString));

        CriteriaQuery<Tuple> query = criteriaBuilder.createTupleQuery();
        Root<Product> root = query.from(Product.class);

        SelectAggregateSelectionVisitor visitor = new SelectAggregateSelectionVisitor();
        visitor.setContext(rsqlContext, criteriaBuilder, root);

        // When
        List<Selection<?>> selections = visitor.visit(tree);
        query.multiselect(selections);
        query.groupBy(root.get("productType").get("code"), root.get("status"));

        TypedQuery<Tuple> typedQuery = em.createQuery(query);
        List<Tuple> results = typedQuery.getResultList();

        // Then
        assertThat(selections).hasSize(3);
        assertThat(results).hasSize(3); // ELEC-ACTIVE, ELEC-INACTIVE, BOOK-ACTIVE

        // Verify one of the groups
        Tuple activeElectronics = results.stream()
            .filter(t -> "ELEC".equals(t.get("typeCode")) &&
                        StandardRecordStatus.ACTIVE.equals(t.get("productStatus")))
            .findFirst()
            .orElseThrow();

        assertThat(activeElectronics.get("count")).isEqualTo(2L); // Laptop and Mouse
    }
}

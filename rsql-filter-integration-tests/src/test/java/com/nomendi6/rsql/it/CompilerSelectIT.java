package com.nomendi6.rsql.it;

import com.nomendi6.rsql.it.config.IntegrationTest;
import com.nomendi6.rsql.it.domain.Product;
import com.nomendi6.rsql.it.domain.ProductType;
import com.nomendi6.rsql.it.domain.StandardRecordStatus;
import com.nomendi6.rsql.it.repository.ProductRepository;
import com.nomendi6.rsql.it.repository.ProductTypeRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import rsql.RsqlCompiler;
import rsql.helper.AggregateField;
import rsql.helper.AggregateField.AggregateFunction;
import rsql.select.SelectField;
import rsql.where.RsqlContext;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for SELECT compilation methods in RsqlCompiler.
 * Tests parsing SELECT strings with real JPA entities.
 */
@IntegrationTest
public class CompilerSelectIT {

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
            .withName("Java Book")
            .withPrice(new BigDecimal("50.00"))
            .withProductType(type2)
            .withStatus(StandardRecordStatus.ACTIVE));
    }

    // ==================== compileSelectToFields() Tests ====================

    @Test
    void testCompileSelectToFields_SingleField() {
        // Given
        String selectString = "code";

        // When
        List<SelectField> fields = compiler.compileSelectToFields(selectString, rsqlContext);

        // Then
        assertThat(fields).hasSize(1);
        assertThat(fields.get(0).getFieldPath()).isEqualTo("code");
        assertThat(fields.get(0).getAlias()).isNull();
    }

    @Test
    void testCompileSelectToFields_SingleFieldWithAlias() {
        // Given
        String selectString = "code:productCode";

        // When
        List<SelectField> fields = compiler.compileSelectToFields(selectString, rsqlContext);

        // Then
        assertThat(fields).hasSize(1);
        assertThat(fields.get(0).getFieldPath()).isEqualTo("code");
        assertThat(fields.get(0).getAlias()).isEqualTo("productCode");
    }

    @Test
    void testCompileSelectToFields_MultipleFields() {
        // Given
        String selectString = "code, name, price";

        // When
        List<SelectField> fields = compiler.compileSelectToFields(selectString, rsqlContext);

        // Then
        assertThat(fields).hasSize(3);
        assertThat(fields.get(0).getFieldPath()).isEqualTo("code");
        assertThat(fields.get(1).getFieldPath()).isEqualTo("name");
        assertThat(fields.get(2).getFieldPath()).isEqualTo("price");
    }

    @Test
    void testCompileSelectToFields_MultipleFieldsWithAliases() {
        // Given
        String selectString = "code:productCode, name:productName, price:amount";

        // When
        List<SelectField> fields = compiler.compileSelectToFields(selectString, rsqlContext);

        // Then
        assertThat(fields).hasSize(3);
        assertThat(fields.get(0).getFieldPath()).isEqualTo("code");
        assertThat(fields.get(0).getAlias()).isEqualTo("productCode");
        assertThat(fields.get(1).getFieldPath()).isEqualTo("name");
        assertThat(fields.get(1).getAlias()).isEqualTo("productName");
        assertThat(fields.get(2).getFieldPath()).isEqualTo("price");
        assertThat(fields.get(2).getAlias()).isEqualTo("amount");
    }

    @Test
    void testCompileSelectToFields_NavigationProperty() {
        // Given
        String selectString = "productType.name";

        // When
        List<SelectField> fields = compiler.compileSelectToFields(selectString, rsqlContext);

        // Then
        assertThat(fields).hasSize(1);
        assertThat(fields.get(0).getFieldPath()).isEqualTo("productType.name");
        assertThat(fields.get(0).getAlias()).isNull();
    }

    @Test
    void testCompileSelectToFields_NavigationPropertyWithAlias() {
        // Given
        String selectString = "productType.name:typeName, productType.code:typeCode";

        // When
        List<SelectField> fields = compiler.compileSelectToFields(selectString, rsqlContext);

        // Then
        assertThat(fields).hasSize(2);
        assertThat(fields.get(0).getFieldPath()).isEqualTo("productType.name");
        assertThat(fields.get(0).getAlias()).isEqualTo("typeName");
        assertThat(fields.get(1).getFieldPath()).isEqualTo("productType.code");
        assertThat(fields.get(1).getAlias()).isEqualTo("typeCode");
    }

    @Test
    void testCompileSelectToFields_EmptyString() {
        // Given
        String selectString = "";

        // When
        List<SelectField> fields = compiler.compileSelectToFields(selectString, rsqlContext);

        // Then
        assertThat(fields).isEmpty();
    }

    // ==================== compileSelectToFieldPaths() Tests ====================

    @Test
    void testCompileSelectToFieldPaths_IgnoresAliases() {
        // Given
        String selectString = "code:productCode, name:productName";

        // When
        List<String> fieldPaths = compiler.compileSelectToFieldPaths(selectString, rsqlContext);

        // Then
        assertThat(fieldPaths).hasSize(2);
        assertThat(fieldPaths).containsExactly("code", "name");
    }

    // ==================== compileSelectToAggregateFields() Tests ====================

    @Test
    void testCompileSelectToAggregateFields_SingleGroupByField() {
        // Given
        String selectString = "productType.name";

        // When
        List<AggregateField> fields = compiler.compileSelectToAggregateFields(selectString, rsqlContext);

        // Then
        assertThat(fields).hasSize(1);
        assertThat(fields.get(0).getFieldPath()).isEqualTo("productType.name");
        assertThat(fields.get(0).getFunction()).isEqualTo(AggregateFunction.NONE);
        assertThat(fields.get(0).getAlias()).isNull();
    }

    @Test
    void testCompileSelectToAggregateFields_GroupByFieldWithAlias() {
        // Given
        String selectString = "productType.name:typeName";

        // When
        List<AggregateField> fields = compiler.compileSelectToAggregateFields(selectString, rsqlContext);

        // Then
        assertThat(fields).hasSize(1);
        assertThat(fields.get(0).getFieldPath()).isEqualTo("productType.name");
        assertThat(fields.get(0).getFunction()).isEqualTo(AggregateFunction.NONE);
        assertThat(fields.get(0).getAlias()).isEqualTo("typeName");
    }

    @Test
    void testCompileSelectToAggregateFields_SumFunction() {
        // Given
        String selectString = "SUM(price)";

        // When
        List<AggregateField> fields = compiler.compileSelectToAggregateFields(selectString, rsqlContext);

        // Then
        assertThat(fields).hasSize(1);
        assertThat(fields.get(0).getFieldPath()).isEqualTo("price");
        assertThat(fields.get(0).getFunction()).isEqualTo(AggregateFunction.SUM);
        assertThat(fields.get(0).getAlias()).isNull();
    }

    @Test
    void testCompileSelectToAggregateFields_SumFunctionWithAlias() {
        // Given
        String selectString = "SUM(price):totalPrice";

        // When
        List<AggregateField> fields = compiler.compileSelectToAggregateFields(selectString, rsqlContext);

        // Then
        assertThat(fields).hasSize(1);
        assertThat(fields.get(0).getFieldPath()).isEqualTo("price");
        assertThat(fields.get(0).getFunction()).isEqualTo(AggregateFunction.SUM);
        assertThat(fields.get(0).getAlias()).isEqualTo("totalPrice");
    }

    @Test
    void testCompileSelectToAggregateFields_MultipleFunctions() {
        // Given
        String selectString = "AVG(price), MIN(price), MAX(price)";

        // When
        List<AggregateField> fields = compiler.compileSelectToAggregateFields(selectString, rsqlContext);

        // Then
        assertThat(fields).hasSize(3);
        assertThat(fields.get(0).getFunction()).isEqualTo(AggregateFunction.AVG);
        assertThat(fields.get(1).getFunction()).isEqualTo(AggregateFunction.MIN);
        assertThat(fields.get(2).getFunction()).isEqualTo(AggregateFunction.MAX);
    }

    @Test
    void testCompileSelectToAggregateFields_CountStar() {
        // Given
        String selectString = "COUNT(*)";

        // When
        List<AggregateField> fields = compiler.compileSelectToAggregateFields(selectString, rsqlContext);

        // Then
        assertThat(fields).hasSize(1);
        assertThat(fields.get(0).getFieldPath()).isEqualTo("id");
        assertThat(fields.get(0).getFunction()).isEqualTo(AggregateFunction.COUNT);
    }

    @Test
    void testCompileSelectToAggregateFields_CountField() {
        // Given
        String selectString = "COUNT(code)";

        // When
        List<AggregateField> fields = compiler.compileSelectToAggregateFields(selectString, rsqlContext);

        // Then
        assertThat(fields).hasSize(1);
        assertThat(fields.get(0).getFieldPath()).isEqualTo("code");
        assertThat(fields.get(0).getFunction()).isEqualTo(AggregateFunction.COUNT);
    }

    @Test
    void testCompileSelectToAggregateFields_CountDistinct() {
        // Given
        String selectString = "COUNT(DIST productType.name)";

        // When
        List<AggregateField> fields = compiler.compileSelectToAggregateFields(selectString, rsqlContext);

        // Then
        assertThat(fields).hasSize(1);
        assertThat(fields.get(0).getFieldPath()).isEqualTo("productType.name");
        assertThat(fields.get(0).getFunction()).isEqualTo(AggregateFunction.COUNT_DISTINCT);
    }

    @Test
    void testCompileSelectToAggregateFields_MixedFieldsAndFunctions() {
        // Given
        String selectString = "productType.name:type, SUM(price):total, COUNT(*):count";

        // When
        List<AggregateField> fields = compiler.compileSelectToAggregateFields(selectString, rsqlContext);

        // Then
        assertThat(fields).hasSize(3);

        // GROUP BY field
        assertThat(fields.get(0).getFieldPath()).isEqualTo("productType.name");
        assertThat(fields.get(0).getFunction()).isEqualTo(AggregateFunction.NONE);
        assertThat(fields.get(0).getAlias()).isEqualTo("type");

        // SUM function
        assertThat(fields.get(1).getFieldPath()).isEqualTo("price");
        assertThat(fields.get(1).getFunction()).isEqualTo(AggregateFunction.SUM);
        assertThat(fields.get(1).getAlias()).isEqualTo("total");

        // COUNT function
        assertThat(fields.get(2).getFieldPath()).isEqualTo("id");
        assertThat(fields.get(2).getFunction()).isEqualTo(AggregateFunction.COUNT);
        assertThat(fields.get(2).getAlias()).isEqualTo("count");
    }

    @Test
    void testCompileSelectToAggregateFields_GrpFunction() {
        // Given
        String selectString = "GRP(productType.name)";

        // When
        List<AggregateField> fields = compiler.compileSelectToAggregateFields(selectString, rsqlContext);

        // Then
        assertThat(fields).hasSize(1);
        assertThat(fields.get(0).getFieldPath()).isEqualTo("productType.name");
        assertThat(fields.get(0).getFunction()).isEqualTo(AggregateFunction.NONE);
    }

    // ==================== compileSelectToGroupByFields() Tests ====================

    @Test
    void testCompileSelectToGroupByFields_ExtractsOnlyGroupByFields() {
        // Given
        String selectString = "productType.name, productType.code, SUM(price), COUNT(*)";

        // When
        List<String> groupByFields = compiler.compileSelectToGroupByFields(selectString, rsqlContext);

        // Then
        assertThat(groupByFields).hasSize(2);
        assertThat(groupByFields).containsExactly("productType.name", "productType.code");
    }

    @Test
    void testCompileSelectToGroupByFields_NoGroupByFields() {
        // Given
        String selectString = "SUM(price), AVG(price), COUNT(*)";

        // When
        List<String> groupByFields = compiler.compileSelectToGroupByFields(selectString, rsqlContext);

        // Then
        assertThat(groupByFields).isEmpty();
    }

    @Test
    void testCompileSelectToGroupByFields_OnlyGroupByFields() {
        // Given
        String selectString = "productType.name, productType.code, code";

        // When
        List<String> groupByFields = compiler.compileSelectToGroupByFields(selectString, rsqlContext);

        // Then
        assertThat(groupByFields).hasSize(3);
        assertThat(groupByFields).containsExactly("productType.name", "productType.code", "code");
    }
}

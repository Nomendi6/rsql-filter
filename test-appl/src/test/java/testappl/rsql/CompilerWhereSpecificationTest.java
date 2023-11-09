package testappl.rsql;

import com.jayway.jsonpath.internal.filter.LogicalOperator;
import jakarta.persistence.criteria.Predicate;
import org.hibernate.query.sqm.ComparisonOperator;
import org.hibernate.query.sqm.function.SelfRenderingSqmFunction;
import org.hibernate.query.sqm.tree.domain.SqmBasicValuedSimplePath;
import org.hibernate.query.sqm.tree.domain.SqmPath;
import org.hibernate.query.sqm.tree.expression.SqmExpression;
import org.hibernate.query.sqm.tree.expression.SqmLiteral;
import org.hibernate.query.sqm.tree.expression.SqmParameter;
import org.hibernate.query.sqm.tree.expression.ValueBindJpaCriteriaParameter;
import org.hibernate.query.sqm.tree.predicate.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import rsql.RsqlCompiler;
import rsql.exceptions.SyntaxErrorException;
import rsql.where.RsqlContext;
import testappl.IntegrationTest;
import testappl.domain.AppObject;
import testappl.domain.Product;
import testappl.domain.ProductType;
import testappl.domain.enumeration.StandardRecordStatus;
import testappl.repository.AppObjectRepository;
import testappl.repository.ProductRepository;
import testappl.repository.ProductTypeRepository;

import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static java.time.temporal.ChronoUnit.DAYS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;


@IntegrationTest
public class CompilerWhereSpecificationTest {

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private AppObjectRepository appObjectRepository;

    @Autowired
    private ProductTypeRepository productTypeRepository;

    @Autowired
    private ProductRepository productRepository;

    RsqlContext<AppObject> rsqlContext;
    RsqlContext<Product> rsqlContextProduct;
    RsqlCompiler<AppObject> compiler;
    RsqlCompiler<Product> compilerForProduct;

    Instant expectedInstant = Instant.parse("2023-01-01T14:01:01Z");
    LocalDate expectedDate = LocalDate.parse("2023-01-01");

    @BeforeEach
    @Transactional
    void setUp() {
        setupData();
        setupRsqlCompiler();
    }

    private void setupRsqlCompiler() {
        rsqlContext = new RsqlContext<>(AppObject.class)
            .defineEntityManager(entityManager);
        compiler = new RsqlCompiler<>();

        rsqlContextProduct = new RsqlContext<>(Product.class)
            .defineEntityManager(entityManager);
        compilerForProduct = new RsqlCompiler<>();
    }

    private void setupData() {
        if (appObjectRepository.count()<1) {
            AppObject a = new AppObject()
                .name("AAAAA")
                .code("AAAAA")
                .status(StandardRecordStatus.ACTIVE)
                .validFrom(Instant.now().minus(1, DAYS));

            appObjectRepository.save(a);
            AppObject b = new AppObject()
                .name("BBBB")
                .code("BBBB")
                .status(StandardRecordStatus.NOT_ACTIVE)
                .validFrom(Instant.now())
                .parent(a);
            appObjectRepository.save(b);
            ProductType t1 = new ProductType()
                .code("T1")
                .name("T1")
                ;
            productTypeRepository.save(t1);

            ProductType t2 = new ProductType()
                .code("T2")
                .name("T2")
                ;
            productTypeRepository.save(t2);

            ProductType t3 = new ProductType()
                .code("T3")
                .name("T3")
                ;
            productTypeRepository.save(t3);

            Product p1 = new Product()
                .code ("P1")
                .name ("P1")
                .tproduct(t1)
                ;
            productRepository.save(p1);

            Product p2 = new Product()
                .code ("P2")
                .name ("P2")
                .parent(p1)
                .tproduct(t2)
                ;
            productRepository.save(p2);

            Product p3 = new Product()
                .code ("P3")
                .name ("P3")
                .parent(p2)
                .tproduct(t3)
                ;
            productRepository.save(p3);

        }
    }

    private void compileToSpecificationAndGetWhere(String rsql) {
        final Specification<AppObject> specification = compiler.compileToSpecification(rsql, rsqlContext);
    }

    private Specification<AppObject> compileToSpecification(String rsql) {
        final Specification<AppObject> specification = compiler.compileToSpecification(rsql, rsqlContext);
        return specification;
    }

    private Specification<Product> compileToSpecificationForProduct(String rsql) {
        final Specification<Product> specification = compilerForProduct.compileToSpecification(rsql, rsqlContextProduct);
        return specification;
    }


    private static class ExpectedCondition {
        public String fieldName;
        public ComparisonOperator operator;
        public Object value;
        public LogicalOperator logicalOperator; // AND ili OR
        public String rightFieldName;
        public Object parameter;

        public ExpectedCondition(String fieldName, ComparisonOperator operator, Object value, LogicalOperator logicalOperator) {
            this.fieldName = fieldName;
            this.operator = operator;
            this.value = value;
            this.logicalOperator = logicalOperator;
        }

        public ExpectedCondition(String fieldName, ComparisonOperator operator, Object value, Object parameter, LogicalOperator logicalOperator) {
            this.fieldName = fieldName;
            this.operator = operator;
            this.value = value;
            this.logicalOperator = logicalOperator;
            this.parameter = parameter;
        }

        public ExpectedCondition(String fieldName, ComparisonOperator operator, String rightFieldName) {
            this.fieldName = fieldName;
            this.operator = operator;
            this.rightFieldName = rightFieldName;
        }
    }

    private <T> void testFieldWithOperator(
        String fieldName, String condition, ComparisonOperator expectedOperator, T expectedValue, Class<T> valueType) {
        Specification<AppObject> specification = compileToSpecification(condition);
        assertThat(specification).isNotNull();

        Predicate predicate = specification.toPredicate(rsqlContext.root, rsqlContext.criteriaQuery, rsqlContext.criteriaBuilder);
        assertThat(predicate).isNotNull();

        // Assert the operator
        ComparisonOperator sqmOperator = ((SqmComparisonPredicate) predicate).getSqmOperator();
        assertThat(sqmOperator).isNotNull();
        assertThat(sqmOperator).isEqualTo(expectedOperator);

        // Assert the left path
        SqmComparisonPredicate comparisonPredicate = (SqmComparisonPredicate) predicate;
        SqmPath<?> leftPath = (SqmPath<?>) comparisonPredicate.getLeftHandExpression();
        String leftPathAttributeName = leftPath.getNavigablePath().getLocalName();
        assertThat(leftPathAttributeName).isEqualTo(fieldName);

        // Assert the right literal or parameter value
        SqmExpression<?> rightHandExpression = comparisonPredicate.getRightHandExpression();
    T rightValue = getRightValue(rightHandExpression, valueType);
        assertThat(rightValue).isEqualTo(expectedValue);
    }

    private <T> T getRightValue(SqmExpression<?> rightHandExpression, Class<T> expectedType) {
        if (rightHandExpression instanceof SqmParameter) {
            SqmParameter<?> rightParameter = (SqmParameter<?>) rightHandExpression;
            Object value = ((ValueBindJpaCriteriaParameter) rightParameter).getValue();
        if (value != null && expectedType.isInstance(value)) {
            return expectedType.cast(value);
        } else {
            throw new AssertionError("Value is not of the expected type: " + expectedType);
        }
        } else if (rightHandExpression instanceof SqmLiteral) {
            SqmLiteral<?> rightLiteral = (SqmLiteral<?>) rightHandExpression;
            Object value = rightLiteral.getLiteralValue();
        if (expectedType.isInstance(value)) {
            return expectedType.cast(value);
        } else {
            throw new AssertionError("Value is not of the expected type: " + expectedType);
        }
        }
        throw new AssertionError("Unexpected expression type: " + rightHandExpression.getClass());
    }

    private void testFieldWithOperator(String fieldName, String condition, ComparisonOperator expectedOperator, Long expectedValue) {
        testFieldWithOperator(fieldName, condition, expectedOperator, expectedValue, Long.class);
    }

    private void testFieldWithOperator(String fieldName, String condition, ComparisonOperator expectedOperator, String expectedValue) {
        testFieldWithOperator(fieldName, condition, expectedOperator, expectedValue, String.class);
    }

    private void testFieldWithOperator(String fieldName, String condition, ComparisonOperator expectedOperator, Double expectedValue) {
        testFieldWithOperator(fieldName, condition, expectedOperator, expectedValue, Double.class);
    }

    private void testFieldWithOperator(String fieldName, String condition, ComparisonOperator expectedOperator, Instant expectedValue) {
        testFieldWithOperator(fieldName, condition, expectedOperator, expectedValue, Instant.class);
    }

    private void testFieldWithOperator(String fieldName, String condition, ComparisonOperator expectedOperator, LocalDate expectedValue) {
        testFieldWithOperator(fieldName, condition, expectedOperator, expectedValue, LocalDate.class);
    }

    private void testFieldWithOperator(String fieldName, String condition, ComparisonOperator expectedOperator, Boolean expectedValue) {
        testFieldWithOperator(fieldName, condition, expectedOperator, expectedValue, Boolean.class);
    }

    private void testFieldWithLikeOperator(String fieldName, String condition, ComparisonOperator expectedOperator, String expectedValue) {
        Specification<AppObject> specification = compileToSpecification(condition);
        assertSpecificationNotNull(specification);

        Predicate predicate = specification.toPredicate(rsqlContext.root, rsqlContext.criteriaQuery, rsqlContext.criteriaBuilder);
        assertPredicateNotNullAndSpecificType(predicate, SqmLikePredicate.class);

        // left path
        SqmLikePredicate likePredicate = (SqmLikePredicate) predicate;
        assertMatchExpression(fieldName, likePredicate.getMatchExpression());

        // Right path
        assertPatternExpression(expectedValue, likePredicate.getPattern());
    }

    private void assertSpecificationNotNull(Specification<AppObject> specification) {
        assertThat(specification).isNotNull();
    }

    private void assertPredicateNotNullAndSpecificType(Predicate predicate, Class<?> predicateType) {
        assertThat(predicate).isNotNull();
        assertThat(predicate).isInstanceOf(predicateType);
    }

    private void assertMatchExpression(String fieldName, SqmExpression<?> matchExpression) {
        if (matchExpression instanceof SelfRenderingSqmFunction) {
            assertFunctionMatch(fieldName, (SelfRenderingSqmFunction<?>) matchExpression);
        } else if (matchExpression instanceof SqmPath) {
            assertPathMatch(fieldName, (SqmPath<?>) matchExpression);
        } else {
            assertThat(false).isTrue(); // This is equivalent to fail("Unexpected match expression type.");
        }
    }

    private void assertFunctionMatch(String fieldName, SelfRenderingSqmFunction<?> function) {
        String functionName = function.getFunctionName();
        assertThat(functionName).isEqualToIgnoringCase(fieldName);
    }

    private void assertPathMatch(String fieldName, SqmPath<?> path) {
        String pathAttributeName = path.getNavigablePath().getLocalName();
        assertThat(pathAttributeName).isEqualTo(fieldName);
    }

    private void assertPatternExpression(String expectedValue, SqmExpression<?> patternExpression) {
        if (patternExpression instanceof SqmLiteral) {
            assertLiteralMatch(expectedValue, (SqmLiteral<?>) patternExpression);
        } else if (patternExpression instanceof SqmParameter) {
            assertParameterMatch(expectedValue, (SqmParameter<?>) patternExpression);
        } else {
            assertThat(false).isTrue(); // This is equivalent to fail("Unexpected pattern expression type.");
        }
    }

    private void assertLiteralMatch(String expectedValue, SqmLiteral<?> literal) {
        Object literalValue = literal.getLiteralValue();
        assertThat(literalValue).isEqualTo(expectedValue);
    }

    private void assertParameterMatch(String expectedValue, SqmParameter<?> parameter) {
        String parameterValue = (String) ((ValueBindJpaCriteriaParameter) parameter).getValue();
        assertThat(parameterValue).isEqualTo(expectedValue);
    }

    private void testSingleCondition(
        SqmComparisonPredicate predicate,
        String expectedFieldName,
        ComparisonOperator expectedOperator,
        Object expectedValue,
        Object expectedParameterName) {

        assertOperatorEquals(predicate, expectedOperator);
        assertLeftPathEquals(predicate, expectedFieldName);
        assertRightHandExpression(predicate, expectedValue, expectedParameterName);
    }

    private void assertOperatorEquals(SqmComparisonPredicate predicate, ComparisonOperator expectedOperator) {
        ComparisonOperator sqmOperator = predicate.getSqmOperator();
        assertThat(sqmOperator).isNotNull();
        assertThat(sqmOperator).isEqualTo(expectedOperator);
    }

    private void assertLeftPathEquals(SqmComparisonPredicate predicate, String expectedFieldName) {
        SqmPath<?> leftPath = (SqmPath<?>) predicate.getLeftHandExpression();
        String leftPathAttributeName = leftPath.getNavigablePath().getLocalName();
        assertThat(leftPathAttributeName).isEqualTo(expectedFieldName);
    }

    private void assertRightHandExpression(SqmComparisonPredicate predicate, Object expectedValue, Object expectedParameterName) {
        SqmExpression<?> rightHandExpression = predicate.getRightHandExpression();
        if (rightHandExpression instanceof SqmLiteral) {
            assertRightLiteralEquals(rightHandExpression, expectedValue);
        } else if (rightHandExpression instanceof SqmParameter) {
            assertRightParameterEquals(rightHandExpression, expectedValue, expectedParameterName);
        }
    }

    private void assertRightLiteralEquals(SqmExpression<?> rightHandExpression, Object expectedValue) {
        Object rightLiteralValue = ((SqmLiteral<?>) rightHandExpression).getLiteralValue();
        assertThat(rightLiteralValue).isEqualTo(expectedValue);
    }

    private void assertRightParameterEquals(SqmExpression<?> rightHandExpression, Object expectedValue, Object expectedParameterName) {
        SqmParameter<?> rightParameter = (SqmParameter<?>) rightHandExpression;
        if (expectedParameterName != null) {
            assertThat(rightParameter.getName()).isEqualTo(expectedParameterName);
        }
        if (rightParameter instanceof ValueBindJpaCriteriaParameter) {
            Object rightValue = ((ValueBindJpaCriteriaParameter) rightParameter).getValue();
            assertThat(rightValue).isEqualTo(expectedValue);
        }
    }

    private void testSingleCondition(SqmComparisonPredicate predicate, ExpectedCondition expectedCondition) {
        testSingleCondition(predicate, expectedCondition.fieldName, expectedCondition.operator,
            expectedCondition.value, expectedCondition.parameter);
    }

    private void testInListCondition(SqmInListPredicate predicate, ExpectedCondition expectedCondition) {
        assertLeftPath(predicate, expectedCondition);
        assertListValues(predicate, expectedCondition);
    }

    private void assertLeftPath(SqmInListPredicate predicate, ExpectedCondition expectedCondition) {
        SqmPath<?> leftPath = (SqmPath<?>) predicate.getTestExpression();
        String leftPathAttributeName = leftPath.getNavigablePath().getLocalName();
        assertThat(leftPathAttributeName).isEqualTo(expectedCondition.fieldName);
    }

    private void assertListValues(SqmInListPredicate predicate, ExpectedCondition expectedCondition) {
        Object expectedValues = expectedCondition.value;
        assertThat(expectedValues).isInstanceOf(List.class);

        List<?> expectedValuesList = (List<?>) expectedValues;
        List<SqmExpression<?>> listExpressions = predicate.getListExpressions();

        for (int i = 0; i < listExpressions.size(); i++) {
            SqmExpression<?> rightHandExpression = listExpressions.get(i);
            Object expectedValue = expectedValuesList.get(i);
            assertRightHandExpression(rightHandExpression, expectedValue, expectedCondition.parameter);
        }
    }

    private void assertRightHandExpression(SqmExpression<?> rightHandExpression, Object expectedValue, Object expectedParameterName) {
        if (rightHandExpression instanceof SqmLiteral) {
            assertLiteralValue(rightHandExpression, expectedValue);
        } else if (rightHandExpression instanceof SqmParameter) {
            assertParameterValue(rightHandExpression, expectedValue, expectedParameterName);
        }
    }

    private void assertLiteralValue(SqmExpression<?> rightHandExpression, Object expectedValue) {
        Object rightLiteralValue = ((SqmLiteral<?>) rightHandExpression).getLiteralValue();
        assertThat(rightLiteralValue).isEqualTo(expectedValue);
    }

    private void assertParameterValue(SqmExpression<?> rightHandExpression, Object expectedValue, Object expectedParameterName) {
        SqmParameter<?> rightParameter = (SqmParameter<?>) rightHandExpression;
        if (expectedParameterName != null) {
            assertThat(rightParameter.getName()).isEqualTo(expectedParameterName);
        }
        if (rightParameter instanceof ValueBindJpaCriteriaParameter) {
            Object rightParameterValue = ((ValueBindJpaCriteriaParameter) rightParameter).getValue();
            assertThat(rightParameterValue).isEqualTo(expectedValue);
        }
    }

    private void testNestedConditions(String condition, List<ExpectedCondition> expectedConditions) {
        Specification<AppObject> specification = compileToSpecification(condition);
        assertThat(specification).isNotNull();

        Predicate predicate = specification.toPredicate(rsqlContext.root, rsqlContext.criteriaQuery, rsqlContext.criteriaBuilder);
        assertThat(predicate).isNotNull();

        testPredicateRecursively(predicate, expectedConditions, 0, null); // Početni operator je null
    }

    private void testCondition(String condition, ExpectedCondition expectedConditions) {
        Specification<AppObject> specification = compileToSpecification(condition);
        assertThat(specification).isNotNull();

        Predicate predicate = specification.toPredicate(rsqlContext.root, rsqlContext.criteriaQuery, rsqlContext.criteriaBuilder);
        assertThat(predicate).isNotNull();
        List<ExpectedCondition> list = new ArrayList<>();
        list.add(expectedConditions);
        testPredicateRecursively(predicate, list, 0, null); // Početni operator je null
    }

    private int testPredicateRecursively(Predicate predicate, List<ExpectedCondition> expectedConditions, int index, LogicalOperator parentLogicalOperator) {
        if (predicate instanceof SqmJunctionPredicate) {
            index = testJunctionPredicate((SqmJunctionPredicate) predicate, expectedConditions, index);
        } else if (predicate instanceof SqmComparisonPredicate) {
            index = testComparisonPredicate((SqmComparisonPredicate) predicate, expectedConditions, index, parentLogicalOperator);
        } else if (predicate instanceof SqmInListPredicate) {
            index = testInListPredicate((SqmInListPredicate) predicate, expectedConditions, index);
        } else if (predicate instanceof SqmBetweenPredicate) {
            testBetweenPredicate((SqmBetweenPredicate) predicate, expectedConditions, index, parentLogicalOperator);

        } else if (predicate instanceof SqmNullnessPredicate) {
            testNullnessPredicate((SqmNullnessPredicate) predicate, expectedConditions, index, parentLogicalOperator);
        } else {
            assertThat(false).isEqualTo(true);
        }
        return index;
    }

    private int testJunctionPredicate(SqmJunctionPredicate predicate, List<ExpectedCondition> expectedConditions, int index) {
        SqmJunctionPredicate junctionPredicate = predicate;
        Predicate.BooleanOperator junctionPredicateOperator = junctionPredicate.getOperator();
        LogicalOperator currentLogicalOperator = junctionPredicateOperator == Predicate.BooleanOperator.AND ? LogicalOperator.AND : LogicalOperator.OR;

        for (SqmPredicate subPredicate : junctionPredicate.getPredicates()) {
            index = testPredicateRecursively(subPredicate, expectedConditions, index, currentLogicalOperator);
        }
        return index;
    }

    private int testComparisonPredicate(SqmComparisonPredicate predicate, List<ExpectedCondition> expectedConditions, int index, LogicalOperator parentLogicalOperator) {
        SqmComparisonPredicate comparisonPredicate = predicate;
        ExpectedCondition expectedCondition = expectedConditions.get(index);

        assertThat(expectedCondition.logicalOperator).isEqualTo(parentLogicalOperator);

        testSingleCondition(comparisonPredicate, expectedCondition);

        index++;
        return index;
    }

    private int testInListPredicate(SqmInListPredicate predicate, List<ExpectedCondition> expectedConditions, int index) {
        SqmInListPredicate inListPredicate = predicate;

        testInListCondition(inListPredicate, expectedConditions.get(index));
        index++;
        return index;
    }

    private static void testBetweenPredicate(SqmBetweenPredicate predicate, List<ExpectedCondition> expectedConditions, int index, LogicalOperator parentLogicalOperator) {
        SqmBetweenPredicate betweenPredicate = predicate;
        ExpectedCondition expectedCondition = expectedConditions.get(index);

        assertThat(expectedCondition.logicalOperator).isEqualTo(parentLogicalOperator);

        SqmPath<?> leftPath = (SqmPath<?>) betweenPredicate.getExpression();
        String leftPathAttributeName = leftPath.getNavigablePath().getLocalName();
        assertThat(leftPathAttributeName).isEqualTo(expectedCondition.fieldName);

        Object lowerBound = null;
        Object upperBound = null;
        Object expectedValues = expectedCondition.value;
        if (expectedValues instanceof List) {
            lowerBound = ((List<?>) expectedValues).get(0);
            upperBound = ((List<?>) expectedValues).get(1);
        }
        Object lowerParameter = null;
        Object upperParameter = null;
        Object expectedParameters = expectedCondition.parameter;
        if (expectedParameters instanceof List) {
            lowerParameter = ((List<?>) expectedParameters).get(0);
            upperParameter = ((List<?>) expectedParameters).get(1);
        }

        SqmExpression<?> rightHandExpression = betweenPredicate.getLowerBound();
        if (rightHandExpression instanceof SqmLiteral) {
            SqmLiteral<?> rightLiteral = (SqmLiteral<?>) rightHandExpression;
            Object rightLiteralValue = rightLiteral.getLiteralValue();
            assertThat(lowerBound).isNotNull();
            assertThat(rightLiteralValue).isEqualTo(lowerBound);
        } else if (rightHandExpression instanceof SqmParameter) {
            SqmParameter<?> rightParameter = (SqmParameter<?>) rightHandExpression;
            if (lowerParameter != null) {
                assertThat(rightParameter.getName()).isEqualTo(lowerParameter);
            }
            if (rightParameter instanceof ValueBindJpaCriteriaParameter) {
                Object rightValue = ((ValueBindJpaCriteriaParameter) rightParameter).getValue();
                assertThat(rightValue).isEqualTo(lowerBound);
            }
        } else if (rightHandExpression instanceof SqmBasicValuedSimplePath<?>){
            SqmBasicValuedSimplePath<?> rightPath = (SqmBasicValuedSimplePath<?>) rightHandExpression;
            String rightPathAttributeName = rightPath.getNavigablePath().getLocalName();
            assertThat(rightPathAttributeName).isEqualTo(lowerBound);
        } else {
            assertThat(false).isEqualTo(true);
        }

        rightHandExpression = betweenPredicate.getUpperBound();
        if (rightHandExpression instanceof SqmLiteral) {
            SqmLiteral<?> rightLiteral = (SqmLiteral<?>) rightHandExpression;
            Object rightLiteralValue = rightLiteral.getLiteralValue();
            assertThat(upperBound).isNotNull();
            assertThat(rightLiteralValue).isEqualTo(upperBound);
        } else if (rightHandExpression instanceof SqmParameter) {
            SqmParameter<?> rightParameter = (SqmParameter<?>) rightHandExpression;
            if (upperParameter != null) {
                assertThat(rightParameter.getName()).isEqualTo(upperParameter);
            }
            if (rightParameter instanceof ValueBindJpaCriteriaParameter) {
                Object rightValue = ((ValueBindJpaCriteriaParameter) rightParameter).getValue();
                assertThat(rightValue).isEqualTo(upperBound);
            }
        } else if (rightHandExpression instanceof SqmBasicValuedSimplePath<?>){
            SqmBasicValuedSimplePath<?> rightPath = (SqmBasicValuedSimplePath<?>) rightHandExpression;
            String rightPathAttributeName = rightPath.getNavigablePath().getLocalName();
            assertThat(rightPathAttributeName).isEqualTo(upperBound);
        } else {
            assertThat(false).isEqualTo(true);
        }
    }

    private static void testNullnessPredicate(SqmNullnessPredicate predicate, List<ExpectedCondition> expectedConditions, int index, LogicalOperator parentLogicalOperator) {
        SqmNullnessPredicate nullnessPredicate = predicate;
        ExpectedCondition expectedCondition = expectedConditions.get(index);

        assertThat(expectedCondition.logicalOperator).isEqualTo(parentLogicalOperator);

        SqmPath<?> leftPath = (SqmPath<?>) nullnessPredicate.getExpression();
        String leftPathAttributeName = leftPath.getNavigablePath().getLocalName();
        assertThat(leftPathAttributeName).isEqualTo(expectedCondition.fieldName);

        assertThat(expectedCondition.value).isInstanceOf(Boolean.class);
        assertThat(nullnessPredicate.isNegated()).isEqualTo(!((Boolean) expectedCondition.value));
    }

    private void testFieldToFieldComparison(String condition, ExpectedCondition expectedCondition) {
        Specification<AppObject> specification = compileToSpecification(condition);
        assertThat(specification).isNotNull();

        Predicate predicate = specification.toPredicate(rsqlContext.root, rsqlContext.criteriaQuery, rsqlContext.criteriaBuilder);
        assertThat(predicate).isNotNull();
        assertThat(predicate).isInstanceOf(SqmComparisonPredicate.class);

        SqmComparisonPredicate comparisonPredicate = (SqmComparisonPredicate) predicate;

        // Test the left-hand side of the comparison
        SqmPath<?> leftPath = (SqmPath<?>) comparisonPredicate.getLeftHandExpression();
        String leftPathAttributeName = leftPath.getNavigablePath().getLocalName();
        assertThat(leftPathAttributeName).isEqualTo(expectedCondition.fieldName);

        // Test the right-hand side of the comparison
        SqmPath<?> rightPath = (SqmPath<?>) comparisonPredicate.getRightHandExpression();
        String rightPathAttributeName = rightPath.getNavigablePath().getLocalName();
        assertThat(rightPathAttributeName).isEqualTo(expectedCondition.rightFieldName);

        // Test the operator
        ComparisonOperator sqmOperator = comparisonPredicate.getSqmOperator();
        assertThat(sqmOperator).isNotNull();
        assertThat(sqmOperator).isEqualTo(expectedCondition.operator);
    }

    private void testFieldToFieldComparisonOnProduct(String condition, ExpectedCondition expectedCondition) {
        Specification<Product> specification = compileToSpecificationForProduct(condition);
        assertThat(specification).isNotNull();

        Predicate predicate = specification.toPredicate(rsqlContextProduct.root, rsqlContextProduct.criteriaQuery, rsqlContextProduct.criteriaBuilder);
        assertThat(predicate).isNotNull();
        assertThat(predicate).isInstanceOf(SqmComparisonPredicate.class);

        SqmComparisonPredicate comparisonPredicate = (SqmComparisonPredicate) predicate;

        // Test the left-hand side of the comparison
        SqmPath<?> leftPath = (SqmPath<?>) comparisonPredicate.getLeftHandExpression();
        // String leftPathAttributeName = leftPath.getNavigablePath().getLocalName();

        String leftPathAttributeName = leftPath.getNavigablePath().getIdentifierForTableGroup().toString().replace("testappl.domain.Product.", "");

        assertThat(leftPathAttributeName).isEqualTo(expectedCondition.fieldName);

        // Test the right-hand side of the comparison
        SqmPath<?> rightPath = (SqmPath<?>) comparisonPredicate.getRightHandExpression();
        String rightPathAttributeName = rightPath.getNavigablePath().getLocalName();
        assertThat(rightPathAttributeName).isEqualTo(expectedCondition.rightFieldName);

        // Test the operator
        ComparisonOperator sqmOperator = comparisonPredicate.getSqmOperator();
        assertThat(sqmOperator).isNotNull();
        assertThat(sqmOperator).isEqualTo(expectedCondition.operator);
    }

    @Test
    void fieldEqNum() {
        testFieldWithOperator("seq", "seq==10", ComparisonOperator.EQUAL, 10L);
    }

    @Test
    void fieldEqNegNum() {
        testFieldWithOperator("seq", "seq==-10", ComparisonOperator.EQUAL, -10L);
    }

    @Test
    void fieldNotEqNum() {
        testFieldWithOperator("seq", "seq!=10", ComparisonOperator.NOT_EQUAL, 10L);
    }

    @Test
    void fieldNotEqNegNum() {
        testFieldWithOperator("seq", "seq!=-10", ComparisonOperator.NOT_EQUAL, -10L);
    }
    @Test
    void fieldGtNum() {
        testFieldWithOperator("seq", "seq=gt=10", ComparisonOperator.GREATER_THAN, 10L);
    }

    @Test
    void fieldGeNum() {
        testFieldWithOperator("seq", "seq=ge=10", ComparisonOperator.GREATER_THAN_OR_EQUAL, 10L);
    }

    @Test
    void fieldLtNum() {
        testFieldWithOperator("seq", "seq=lt=10", ComparisonOperator.LESS_THAN, 10L);
    }

    @Test
    void fieldLeNum() {
        testFieldWithOperator("seq", "seq=le=10", ComparisonOperator.LESS_THAN_OR_EQUAL, 10L);
    }

    @Test
    void errorMissingClosingParenthesis() {
        SyntaxErrorException thrown = assertThrows(SyntaxErrorException.class, () -> {
            compileToSpecificationAndGetWhere("seq==10 or seq==11)");
        });
        assertTrue(thrown.getMessage().contains("Missing opening parenthesis"));
    }

    @Test
    void errorMissingOpeningParenthesis() {
        SyntaxErrorException thrown = assertThrows(SyntaxErrorException.class, () -> {
            compileToSpecificationAndGetWhere("seq==10 or seq==11)");
        });
        assertTrue(thrown.getMessage().contains("Missing opening parenthesis"));
    }

    @Test
    void errorMissingQuote1() {
        assertThrows(SyntaxErrorException.class, () -> {
            compileToSpecificationAndGetWhere("name=='text");
        });
    }

    @Test
    void seqNotEq() {
        testFieldWithOperator("seq", "seq!=10", ComparisonOperator.NOT_EQUAL, 10L);
    }

    @Test
    void fieldEqString() {
        testFieldWithOperator("name", "name=='text'", ComparisonOperator.EQUAL, "text");
    }
    @Test
    void fieldNotEqString() {
        testFieldWithOperator("name", "name!='text'", ComparisonOperator.NOT_EQUAL, "text");
    }

    @Test
    void fieldGtString() {
        testFieldWithOperator("name", "name=gt='text'", ComparisonOperator.GREATER_THAN, "text");
    }

    @Test
    void fieldGeString() {
        testFieldWithOperator("name", "name=ge='text'", ComparisonOperator.GREATER_THAN_OR_EQUAL, "text");
    }

    @Test
    void fieldLtString() {
        testFieldWithOperator("name", "name=lt='text'", ComparisonOperator.LESS_THAN, "text");
    }

    @Test
    void fieldLeString() {
        testFieldWithOperator("name", "name=le='text'", ComparisonOperator.LESS_THAN_OR_EQUAL, "text");
    }

    @Test
    void fieldEqReal() {
        testFieldWithOperator("quantity", "quantity==1.0", ComparisonOperator.EQUAL, 1.0);
    }

    @Test
    void fieldEqNegReal() {
        testFieldWithOperator("quantity", "quantity==-1.0", ComparisonOperator.EQUAL, -1.0);
    }

    @Test
    void fieldGtReal() {
        testFieldWithOperator("quantity", "quantity=gt=1.0", ComparisonOperator.GREATER_THAN, 1.0);
    }

    @Test
    void fieldGeReal() {
        testFieldWithOperator("quantity", "quantity=ge=1.0", ComparisonOperator.GREATER_THAN_OR_EQUAL, 1.0);
    }

    @Test
    void fieldLtReal() {
        testFieldWithOperator("quantity", "quantity=lt=1.0", ComparisonOperator.LESS_THAN, 1.0);
    }

    @Test
    void fieldLeReal() {
        testFieldWithOperator("quantity", "quantity=le=1.0", ComparisonOperator.LESS_THAN_OR_EQUAL, 1.0);
    }

    @Test
    void fieldEqDatetime() {
        testFieldWithOperator("validFrom", "validFrom==#2023-01-01T14:01:01Z#", ComparisonOperator.EQUAL, expectedInstant);
    }

    @Test
    void fieldNotEqDatetime() {
        testFieldWithOperator("validFrom", "validFrom!=#2023-01-01T14:01:01Z#", ComparisonOperator.NOT_EQUAL, expectedInstant);
    }

    @Test
    void fieldGtDatetime() {
        testFieldWithOperator("validFrom", "validFrom=gt=#2023-01-01T14:01:01Z#", ComparisonOperator.GREATER_THAN, expectedInstant);
    }

    @Test
    void fieldGeDatetime() {
        testFieldWithOperator("validFrom", "validFrom=ge=#2023-01-01T14:01:01Z#", ComparisonOperator.GREATER_THAN_OR_EQUAL, expectedInstant);
    }

    @Test
    void fieldLtDatetime() {
        testFieldWithOperator("validFrom", "validFrom=lt=#2023-01-01T14:01:01Z#", ComparisonOperator.LESS_THAN, expectedInstant);
    }

    @Test
    void fieldLeDatetime() {
        testFieldWithOperator("validFrom", "validFrom=le=#2023-01-01T14:01:01Z#", ComparisonOperator.LESS_THAN_OR_EQUAL, expectedInstant);
    }
    @Test
    void fieldEqDate() {
        testFieldWithOperator("creationDate", "creationDate==#2023-01-01#", ComparisonOperator.EQUAL, expectedDate);
    }

    @Test
    void fieldNotEqDate() {
        testFieldWithOperator("creationDate", "creationDate!=#2023-01-01#", ComparisonOperator.NOT_EQUAL, expectedDate);
    }

    @Test
    void fieldGtDate() {
        testFieldWithOperator("creationDate", "creationDate=gt=#2023-01-01#", ComparisonOperator.GREATER_THAN, expectedDate);
    }

    @Test
    void fieldGeDate() {
        testFieldWithOperator("creationDate", "creationDate=ge=#2023-01-01#", ComparisonOperator.GREATER_THAN_OR_EQUAL, expectedDate);
    }

    @Test
    void fieldLtDate() {
        testFieldWithOperator("creationDate", "creationDate=lt=#2023-01-01#", ComparisonOperator.LESS_THAN, expectedDate);
    }

    @Test
    void fieldLeDate() {
        testFieldWithOperator("creationDate", "creationDate=le=#2023-01-01#", ComparisonOperator.LESS_THAN_OR_EQUAL, expectedDate);
    }

    @Test
    void errorDateMissingHash() {
        assertThrows(SyntaxErrorException.class, () -> {
            compileToSpecificationAndGetWhere("validFrom==#2023-01-01T14:01:01Z");
        });
    }

    @Test
    void fieldsAnd1() {
        List<ExpectedCondition> expectedConditions = Arrays.asList(
            new ExpectedCondition("name", ComparisonOperator.EQUAL, "text", LogicalOperator.AND),
            new ExpectedCondition("code", ComparisonOperator.EQUAL, "code", LogicalOperator.AND)
        );

        testNestedConditions("name=='text' and code=='code'", expectedConditions);
    }

    @Test
    void fieldsAnd2() {
        List<ExpectedCondition> expectedConditions = Arrays.asList(
            new ExpectedCondition("name", ComparisonOperator.EQUAL, "text", LogicalOperator.AND),
            new ExpectedCondition("code", ComparisonOperator.EQUAL, "code", LogicalOperator.AND)
        );

        testNestedConditions("name=='text';code=='code'", expectedConditions);
    }

    @Test
    void fieldsAnd3() {
        List<ExpectedCondition> expectedConditions = Arrays.asList(
            new ExpectedCondition("name", ComparisonOperator.EQUAL, "text", LogicalOperator.AND),
            new ExpectedCondition("code", ComparisonOperator.EQUAL, "code", LogicalOperator.AND)
        );

        testNestedConditions("(name=='text') and (code=='code')", expectedConditions);
    }

    @Test
    void fieldsAnd4() {
        List<ExpectedCondition> expectedConditions = Arrays.asList(
            new ExpectedCondition("name", ComparisonOperator.EQUAL, "text", LogicalOperator.AND),
            new ExpectedCondition("code", ComparisonOperator.EQUAL, "code", LogicalOperator.AND)
        );

        testNestedConditions("(name=='text');(code=='code')", expectedConditions);
    }

    @Test
    void fieldsAnd5() {
        List<ExpectedCondition> expectedConditions = new ArrayList<>();
        expectedConditions.add(new ExpectedCondition("name", ComparisonOperator.EQUAL, "text", LogicalOperator.AND));
        expectedConditions.add(new ExpectedCondition("code", ComparisonOperator.EQUAL, "code", LogicalOperator.AND));
        expectedConditions.add(new ExpectedCondition("description", ComparisonOperator.EQUAL, "a", LogicalOperator.AND));

        testNestedConditions("(((name=='text') and (code=='code')) and (description=='a'))", expectedConditions);

    }

    @Test
    void fieldsOr1() {
        List<ExpectedCondition> expectedConditions = Arrays.asList(
            new ExpectedCondition("name", ComparisonOperator.EQUAL, "text", LogicalOperator.OR),
            new ExpectedCondition("code", ComparisonOperator.EQUAL, "code", LogicalOperator.OR)
        );

        testNestedConditions("name=='text' or code=='code'", expectedConditions);
    }

    @Test
    void fieldsOr2() {
        List<ExpectedCondition> expectedConditions = Arrays.asList(
            new ExpectedCondition("name", ComparisonOperator.EQUAL, "text", LogicalOperator.OR),
            new ExpectedCondition("code", ComparisonOperator.EQUAL, "code", LogicalOperator.OR)
        );

        testNestedConditions("name=='text',code=='code'", expectedConditions);
    }

    @Test
    void fieldsOr3() {
        List<ExpectedCondition> expectedConditions = Arrays.asList(
            new ExpectedCondition("name", ComparisonOperator.EQUAL, "text", LogicalOperator.OR),
            new ExpectedCondition("code", ComparisonOperator.EQUAL, "code", LogicalOperator.OR)
        );

        testNestedConditions("(name=='text')or(code=='code')", expectedConditions);
    }

    @Test
    void fieldsOr4() {
        List<ExpectedCondition> expectedConditions = Arrays.asList(
            new ExpectedCondition("name", ComparisonOperator.EQUAL, "text", LogicalOperator.OR),
            new ExpectedCondition("code", ComparisonOperator.EQUAL, "code", LogicalOperator.OR)
        );

        testNestedConditions("(name=='text'),(code=='code')", expectedConditions);
    }

    @Test
    void fieldEqField() {
        testFieldToFieldComparison("name==code", new ExpectedCondition("name", ComparisonOperator.EQUAL, "code"));
    }

    @Test
    void fieldNotEqField() {
        testFieldToFieldComparison("name!=code", new ExpectedCondition("name", ComparisonOperator.NOT_EQUAL, "code"));
    }

    @Test
    void fieldWithDotsEqField() {
        testFieldToFieldComparisonOnProduct("tproduct.code==name", new ExpectedCondition("tproduct.code", ComparisonOperator.EQUAL, "name"));
    }

    @Test
    void fieldGeField() {
        testFieldToFieldComparison("name=ge=code", new ExpectedCondition("name", ComparisonOperator.GREATER_THAN_OR_EQUAL, "code"));
    }

    @Test
    void fieldGtField() {
        testFieldToFieldComparison("name=gt=code", new ExpectedCondition("name", ComparisonOperator.GREATER_THAN, "code"));
    }

    @Test
    void fieldLtField() {
        testFieldToFieldComparison("name=lt=code", new ExpectedCondition("name", ComparisonOperator.LESS_THAN, "code"));
    }

    @Test
    void fieldLeField() {
        testFieldToFieldComparison("name=le=code", new ExpectedCondition("name", ComparisonOperator.LESS_THAN_OR_EQUAL, "code"));
    }

    @Test
    void fieldIsNull() {
        testCondition("name==NULL", new ExpectedCondition("name", ComparisonOperator.EQUAL, true, null, null));
    }

    @Test
    void fieldIsNotNull() {
        testCondition("name!=NULL", new ExpectedCondition("name", ComparisonOperator.EQUAL, false, null, null));
    }

    @Test
    void fieldEqEnum() {
        testCondition("status==#ACTIVE#", new ExpectedCondition("status", ComparisonOperator.EQUAL, StandardRecordStatus.ACTIVE, null, null));

    }

    @Test
    void fieldNotEqEnum() {
        testCondition("status!=#ACTIVE#", new ExpectedCondition("status", ComparisonOperator.NOT_EQUAL, StandardRecordStatus.ACTIVE, null, null));
    }

    @Test
    void errorEnumMissingHash1() {
        assertThrows(SyntaxErrorException.class, () -> {
            compileToSpecificationAndGetWhere("status==ACTIVE#");
        });
    }

    @Test
    void errorEnumMissingHash2() {
        assertThrows(SyntaxErrorException.class, () -> {
            compileToSpecificationAndGetWhere("status==#ACTIVE");
        });
    }

    @Test
    void fieldEqTrue() {
        testCondition("isValid==true", new ExpectedCondition("isValid", ComparisonOperator.EQUAL, Boolean.TRUE, null, null));
    }

    @Test
    void errorGtTrue() {
        assertThrows(SyntaxErrorException.class, () -> {
            compileToSpecificationAndGetWhere("isValid=gt=true");
        });
    }

    @Test
    void errorGtNull() {
        assertThrows(SyntaxErrorException.class, () -> {
            compileToSpecificationAndGetWhere("isValid=gt=null");
        });
    }

    @Test
    void errorGtEnum() {
        assertThrows(SyntaxErrorException.class, () -> {
            compileToSpecificationAndGetWhere("status=gt=#ACTIVE#");
        });
    }

    @Test
    void fieldNotEqTrue() {
        testFieldWithOperator("isValid", "isValid!=true", ComparisonOperator.NOT_EQUAL, Boolean.TRUE);
    }

    @Test
    void fieldEqFalse() {
        testFieldWithOperator("isValid", "isValid==false", ComparisonOperator.EQUAL, Boolean.FALSE);
    }

    @Test
    void fieldNotEqFalse() {
        testFieldWithOperator("isValid", "isValid!=false", ComparisonOperator.NOT_EQUAL, Boolean.FALSE);

    }

    @Test
    void fieldEqParamString() {
        ExpectedCondition expectedCondition = new ExpectedCondition("name", ComparisonOperator.EQUAL, null, "param", null);
        testCondition("name==:param", expectedCondition);

    }
    @Test
    void fieldEqParamNum() {
        ExpectedCondition expectedCondition = new ExpectedCondition("seq", ComparisonOperator.EQUAL, null, "param", null);
        testCondition("seq==:param", expectedCondition);
    }

    @Test
    void fieldEqParamReal() {
        ExpectedCondition expectedCondition = new ExpectedCondition("quantity", ComparisonOperator.EQUAL, null, "param", null);
        testCondition("quantity==:param", expectedCondition);

    }

    @Test
    void fieldEqParamBoolean() {
        ExpectedCondition expectedCondition = new ExpectedCondition("isValid", ComparisonOperator.EQUAL, null, "param", null);
        testCondition("isValid==:param", expectedCondition);

    }

    @Test
    void fieldEqParamDate() {
        ExpectedCondition expectedCondition = new ExpectedCondition("creationDate", ComparisonOperator.EQUAL, null, "param", null);
        testCondition("creationDate==:param", expectedCondition);

    }

    @Test
    void fieldEqParamDatetime() {
        ExpectedCondition expectedCondition = new ExpectedCondition("validFrom", ComparisonOperator.EQUAL, null, "param", null);
        testCondition("validFrom==:param", expectedCondition);

    }

    @Test
    void fieldEqParamEnum() {
        ExpectedCondition expectedCondition = new ExpectedCondition("status", ComparisonOperator.EQUAL, null, "param", null);
        testCondition("status==:param", expectedCondition);
    }

    @Test
    void fieldNotEqParam() {
        ExpectedCondition expectedCondition = new ExpectedCondition("name", ComparisonOperator.NOT_EQUAL, null, "param", null);
        testCondition("name!=:param", expectedCondition);
    }
    @Test
    void fieldGtParam() {
        ExpectedCondition expectedCondition = new ExpectedCondition("name", ComparisonOperator.GREATER_THAN, null, "param", null);
        testCondition("name=gt=:param", expectedCondition);

    }
    @Test
    void fieldGeParam() {
        ExpectedCondition expectedCondition = new ExpectedCondition("name", ComparisonOperator.GREATER_THAN_OR_EQUAL, null, "param", null);
        testCondition("name=ge=:param", expectedCondition);

    }
    @Test
    void fieldLtParam() {
        ExpectedCondition expectedCondition = new ExpectedCondition("name", ComparisonOperator.LESS_THAN, null, "param", null);
        testCondition("name=lt=:param", expectedCondition);

    }
    @Test
    void fieldLeParam() {
        ExpectedCondition expectedCondition = new ExpectedCondition("name", ComparisonOperator.LESS_THAN_OR_EQUAL, null, "param", null);
        testCondition("name=le=:param", expectedCondition);

    }

    @Test
    void fieldBetweenDec() {
        List<Object> expectedValues = Arrays.asList(1L, 2L);
        testCondition("seq=bt=(1,2)", new ExpectedCondition("seq", ComparisonOperator.EQUAL, expectedValues, null, null));
    }

    @Test
    void fieldBetweenReal() {
        List<Object> expectedValues = Arrays.asList(1.0, 2.0);
        testCondition("quantity=bt=(1.0,2.0)", new ExpectedCondition("quantity", ComparisonOperator.EQUAL, expectedValues, null, null));
    }

    @Test
    void fieldBetweenString() {
        List<Object> expectedValues = Arrays.asList("A", "B");
        testCondition("name=bt=('A','B')", new ExpectedCondition("name", ComparisonOperator.EQUAL, expectedValues, null, null));

    }

    @Test
    void fieldBetweenDatetime() {
        List<Object> expectedValues = Arrays.asList(Instant.parse("2023-01-01T12:00:00Z"), Instant.parse("2023-01-02T12:00:00Z"));
        testCondition("validFrom=bt=(#2023-01-01T12:00:00Z#,#2023-01-02T12:00:00Z#)", new ExpectedCondition("validFrom", ComparisonOperator.EQUAL, expectedValues, null, null));

    }

    @Test
    void fieldBetweenDate() {
        List<Object> expectedValues = Arrays.asList(LocalDate.parse("2023-01-01"), LocalDate.parse("2023-01-02"));
        testCondition("creationDate=bt=(#2023-01-01#,#2023-01-02#)", new ExpectedCondition("creationDate", ComparisonOperator.EQUAL, expectedValues, null, null));
    }

    @Test
    void fieldBetweenFields() {
        List<Object> expectedValues = Arrays.asList("code", "description");
        testCondition("name=bt=(code,description)", new ExpectedCondition("name", ComparisonOperator.EQUAL, expectedValues, null, null));
    }

    @Test
    void fieldBetweenParams() {
        List<String> expectedParams = Arrays.asList("param1", "param2");
        testCondition("name=bt=(:param1,:param2)", new ExpectedCondition("name", ComparisonOperator.EQUAL, null, expectedParams, null));
    }

    @Test
    void errorBtClauseMissingParentheses1() {
        assertThrows(SyntaxErrorException.class, () -> {
            compileToSpecificationAndGetWhere("seq=bt=(1,2");
        });
    }

    @Test
    void errorBtClauseMissingParentheses2() {
        assertThrows(SyntaxErrorException.class, () -> {
            compileToSpecificationAndGetWhere("seq=bt=1,2)");
        });
    }

    @Test
    void fieldInNumbers() {
        List<Object> expectedValues = Arrays.asList(1L, 2L, 3L);
        testCondition("seq=in=(1,2,3)",
            new ExpectedCondition("seq", ComparisonOperator.EQUAL, expectedValues, null, null));

    }

    @Test
    void fieldInReal() {
        List<Object> expectedValues = Arrays.asList(1.0,2.0,3.0);
        testCondition("quantity=in=(1.0,2.0,3.0)",
            new ExpectedCondition("quantity", ComparisonOperator.EQUAL, expectedValues, null, null));

    }

    @Test
    void fieldInString() {
        List<Object> expectedValues = Arrays.asList("A","B","C");
        testCondition("code=in=('A','B','C')",
            new ExpectedCondition("code", ComparisonOperator.EQUAL, expectedValues, null, null));

    }

    @Test
    void fieldInEnum() {
        List<Object> expectedValues = Arrays.asList(StandardRecordStatus.ACTIVE, StandardRecordStatus.NOT_ACTIVE);
        testCondition("status=in=('ACTIVE','NOT_ACTIVE')",
            new ExpectedCondition("status", ComparisonOperator.EQUAL, expectedValues, null, null));
    }

    @Test
    void errorInClauseMissingParentheses1() {
        assertThrows(SyntaxErrorException.class, () -> {
            compileToSpecificationAndGetWhere("seq=in=(1,2,3");
        });
    }

    @Test
    void errorInClauseMissingParentheses2() {
        assertThrows(SyntaxErrorException.class, () -> {
            compileToSpecificationAndGetWhere("seq=in=1,2,3)");
        });
    }

    @Test
    void fieldNotInNumbers() {
        List<Object> expectedValues = Arrays.asList(1L, 2L, 3L);
        testCondition("seq=in=(1,2,3)",
            new ExpectedCondition("seq", ComparisonOperator.EQUAL, expectedValues, null, null));

    }

    @Test
    void fieldLikeString1() {
        testFieldWithLikeOperator("lower", "name=*'A*'", ComparisonOperator.EQUAL, "a%");

    }

    @Test
    void fieldLikeString2() {
        testFieldWithLikeOperator("lower", "name=like='A*'", ComparisonOperator.EQUAL, "a%");
    }

}

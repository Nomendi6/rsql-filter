package com.nomendi6.rsql.it;

import com.nomendi6.rsql.it.config.IntegrationTest;
import com.nomendi6.rsql.it.domain.Product;
import com.nomendi6.rsql.it.domain.ProductRelation;
import com.nomendi6.rsql.it.domain.ProductType;
import com.nomendi6.rsql.it.domain.StandardRecordStatus;
import com.nomendi6.rsql.it.repository.ProductRelationRepository;
import com.nomendi6.rsql.it.repository.ProductRepository;
import com.nomendi6.rsql.it.repository.ProductTypeRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import rsql.RsqlCompiler;
import rsql.where.RsqlContext;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

/**
 * Integration tests specifically for the root change detection fix.
 *
 * <p>These tests verify that when Spring Data JPA's {@code repository.findAll(Specification, Pageable)}
 * internally calls {@code toPredicate()} twice (once for the main query, once for the count query),
 * the library correctly detects the root change and clears the JOIN cache.</p>
 *
 * <p>Before the fix, this would cause:
 * {@code IllegalArgumentException: Already registered a copy: SqmSingularJoin(...)}</p>
 *
 * <p>The fix detects when {@code toPredicate()} is called with a different root and automatically
 * clears the {@code joinsMap} to avoid reusing JOINs from a previous query context.</p>
 */
@IntegrationTest
public class SpecificationRootChangeIT {

    @Autowired
    private EntityManager em;

    @Autowired
    private ProductTypeRepository productTypeRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ProductRelationRepository productRelationRepository;

    private RsqlCompiler<Product> productCompiler;
    private RsqlCompiler<ProductRelation> relationCompiler;

    private ProductType typeAlpha;
    private ProductType typeBeta;
    private Product productA;
    private Product productB;
    private Product productC;

    @BeforeEach
    void setup() {
        // Clean existing data
        productRelationRepository.deleteAll();
        productRepository.deleteAll();
        productTypeRepository.deleteAll();

        // Create ProductTypes
        typeAlpha = new ProductType()
            .withCode("ALPHA")
            .withName("Alpha Type")
            .withDescription("First type");
        productTypeRepository.save(typeAlpha);

        typeBeta = new ProductType()
            .withCode("BETA")
            .withName("Beta Type")
            .withDescription("Second type");
        productTypeRepository.save(typeBeta);

        ProductType typeGamma = new ProductType()
            .withCode("GAMMA")
            .withName("Gamma Type")
            .withDescription("Third type");
        productTypeRepository.save(typeGamma);

        // Create Products
        productA = new Product()
            .withCode("PROD_A")
            .withName("Product A")
            .withPrice(new BigDecimal("100.00"))
            .withStatus(StandardRecordStatus.ACTIVE)
            .withProductType(typeAlpha);
        productRepository.save(productA);

        productB = new Product()
            .withCode("PROD_B")
            .withName("Product B")
            .withPrice(new BigDecimal("200.00"))
            .withStatus(StandardRecordStatus.ACTIVE)
            .withProductType(typeBeta);
        productRepository.save(productB);

        productC = new Product()
            .withCode("PROD_C")
            .withName("Product C")
            .withPrice(new BigDecimal("300.00"))
            .withStatus(StandardRecordStatus.INACTIVE)
            .withProductType(typeGamma);
        productRepository.save(productC);

        // Create ProductRelations with multiple references to Product
        ProductRelation rel1 = new ProductRelation()
            .withCode("REL1")
            .withName("Relation 1")
            .withQuantity(new BigDecimal("10"))
            .withStatus(StandardRecordStatus.ACTIVE)
            .withProduct1(productA)
            .withProduct2(productB)
            .withProduct3(productC);
        productRelationRepository.save(rel1);

        ProductRelation rel2 = new ProductRelation()
            .withCode("REL2")
            .withName("Relation 2")
            .withQuantity(new BigDecimal("20"))
            .withStatus(StandardRecordStatus.ACTIVE)
            .withProduct1(productB)
            .withProduct2(productC)
            .withProduct3(productA);
        productRelationRepository.save(rel2);

        ProductRelation rel3 = new ProductRelation()
            .withCode("REL3")
            .withName("Relation 3")
            .withQuantity(new BigDecimal("30"))
            .withStatus(StandardRecordStatus.INACTIVE)
            .withProduct1(productC)
            .withProduct2(productA)
            .withProduct3(productB);
        productRelationRepository.save(rel3);

        // Initialize compilers
        productCompiler = new RsqlCompiler<>();
        relationCompiler = new RsqlCompiler<>();
    }

    @Nested
    @DisplayName("repository.findAll(spec, pageable) - Double toPredicate() Call")
    class FindAllWithPaginationTests {

        @Test
        @DisplayName("Single JOIN - should not throw 'Already registered a copy' exception")
        void testSingleJoinWithPagination() {
            // This filter creates a single JOIN to productType
            String filter = "productType.code=='ALPHA'";
            Pageable pageable = PageRequest.of(0, 10, Sort.by("id"));

            RsqlContext<Product> ctx = new RsqlContext<>(Product.class);
            ctx.defineEntityManager(em);

            Specification<Product> spec = productCompiler.compileToSpecification(filter, ctx);

            // This internally calls toPredicate() twice: once for query, once for count
            assertThatCode(() -> {
                Page<Product> result = productRepository.findAll(spec, pageable);
                assertThat(result.getContent()).hasSize(1);
                assertThat(result.getContent().get(0).getCode()).isEqualTo("PROD_A");
                assertThat(result.getTotalElements()).isEqualTo(1);
            }).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("Multiple JOINs to same table - should not throw exception")
        void testMultipleJoinsToSameTableWithPagination() {
            // This filter creates multiple JOINs: product1, product2, product3 all to Product table
            String filter = "product1.code=='PROD_A';product2.code=='PROD_B'";
            Pageable pageable = PageRequest.of(0, 10, Sort.by("id"));

            RsqlContext<ProductRelation> ctx = new RsqlContext<>(ProductRelation.class);
            ctx.defineEntityManager(em);

            Specification<ProductRelation> spec = relationCompiler.compileToSpecification(filter, ctx);

            assertThatCode(() -> {
                Page<ProductRelation> result = productRelationRepository.findAll(spec, pageable);
                assertThat(result.getContent()).hasSize(1);
                assertThat(result.getContent().get(0).getCode()).isEqualTo("REL1");
                assertThat(result.getTotalElements()).isEqualTo(1);
            }).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("Three JOINs to same table - should not throw exception")
        void testThreeJoinsToSameTableWithPagination() {
            String filter = "product1.code=='PROD_A';product2.code=='PROD_B';product3.code=='PROD_C'";
            Pageable pageable = PageRequest.of(0, 10, Sort.by("id"));

            RsqlContext<ProductRelation> ctx = new RsqlContext<>(ProductRelation.class);
            ctx.defineEntityManager(em);

            Specification<ProductRelation> spec = relationCompiler.compileToSpecification(filter, ctx);

            assertThatCode(() -> {
                Page<ProductRelation> result = productRelationRepository.findAll(spec, pageable);
                assertThat(result.getContent()).hasSize(1);
                assertThat(result.getContent().get(0).getCode()).isEqualTo("REL1");
            }).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("Nested JOINs (relation.entity.nestedEntity) - should not throw exception")
        void testNestedJoinsWithPagination() {
            // This creates nested JOINs: product1 -> productType, product2 -> productType
            String filter = "product1.productType.code=='ALPHA';product2.productType.code=='BETA'";
            Pageable pageable = PageRequest.of(0, 10, Sort.by("id"));

            RsqlContext<ProductRelation> ctx = new RsqlContext<>(ProductRelation.class);
            ctx.defineEntityManager(em);

            Specification<ProductRelation> spec = relationCompiler.compileToSpecification(filter, ctx);

            assertThatCode(() -> {
                Page<ProductRelation> result = productRelationRepository.findAll(spec, pageable);
                assertThat(result.getContent()).hasSize(1);
                assertThat(result.getContent().get(0).getCode()).isEqualTo("REL1");
            }).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("OR filter with same JOIN - should not throw exception")
        void testOrFilterWithSameJoinWithPagination() {
            // OR filter that references same relation multiple times
            String filter = "product1.code=='PROD_A',product1.code=='PROD_B'";
            Pageable pageable = PageRequest.of(0, 10, Sort.by("id"));

            RsqlContext<ProductRelation> ctx = new RsqlContext<>(ProductRelation.class);
            ctx.defineEntityManager(em);

            Specification<ProductRelation> spec = relationCompiler.compileToSpecification(filter, ctx);

            assertThatCode(() -> {
                Page<ProductRelation> result = productRelationRepository.findAll(spec, pageable);
                // REL1 has product1=A, REL2 has product1=B
                assertThat(result.getContent()).hasSize(2);
                assertThat(result.getTotalElements()).isEqualTo(2);
            }).doesNotThrowAnyException();
        }
    }

    @Nested
    @DisplayName("Repeated Specification Usage")
    class RepeatedSpecificationUsageTests {

        @Test
        @DisplayName("Same specification used multiple times sequentially")
        void testSameSpecificationUsedMultipleTimes() {
            String filter = "productType.code=='ALPHA'";
            Pageable pageable = PageRequest.of(0, 10, Sort.by("id"));

            RsqlContext<Product> ctx = new RsqlContext<>(Product.class);
            ctx.defineEntityManager(em);

            Specification<Product> spec = productCompiler.compileToSpecification(filter, ctx);

            // Use same spec multiple times - each call internally does query + count
            for (int i = 0; i < 5; i++) {
                final int iteration = i;
                assertThatCode(() -> {
                    Page<Product> result = productRepository.findAll(spec, pageable);
                    assertThat(result.getContent()).hasSize(1);
                }).as("Iteration %d should not throw exception", iteration).doesNotThrowAnyException();
            }
        }

        @Test
        @DisplayName("Multiple specifications created from same context")
        void testMultipleSpecsFromSameContext() {
            Pageable pageable = PageRequest.of(0, 10, Sort.by("id"));

            RsqlContext<Product> ctx = new RsqlContext<>(Product.class);
            ctx.defineEntityManager(em);

            String[] filters = {
                "productType.code=='ALPHA'",
                "productType.code=='BETA'",
                "productType.code=='GAMMA'"
            };

            for (String filter : filters) {
                Specification<Product> spec = productCompiler.compileToSpecification(filter, ctx);

                assertThatCode(() -> {
                    Page<Product> result = productRepository.findAll(spec, pageable);
                    assertThat(result.getContent()).hasSize(1);
                }).as("Filter '%s' should not throw exception", filter).doesNotThrowAnyException();
            }
        }
    }

    @Nested
    @DisplayName("Edge Cases")
    class EdgeCaseTests {

        @Test
        @DisplayName("Empty result set with JOINs")
        void testEmptyResultWithJoins() {
            String filter = "productType.code=='NONEXISTENT'";
            Pageable pageable = PageRequest.of(0, 10, Sort.by("id"));

            RsqlContext<Product> ctx = new RsqlContext<>(Product.class);
            ctx.defineEntityManager(em);

            Specification<Product> spec = productCompiler.compileToSpecification(filter, ctx);

            assertThatCode(() -> {
                Page<Product> result = productRepository.findAll(spec, pageable);
                assertThat(result.getContent()).isEmpty();
                assertThat(result.getTotalElements()).isZero();
            }).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("First page with small page size")
        void testSmallPageSize() {
            String filter = "product1.productType.code=='ALPHA',product1.productType.code=='BETA',product1.productType.code=='GAMMA'";
            Pageable pageable = PageRequest.of(0, 1, Sort.by("id")); // Page size 1

            RsqlContext<ProductRelation> ctx = new RsqlContext<>(ProductRelation.class);
            ctx.defineEntityManager(em);

            Specification<ProductRelation> spec = relationCompiler.compileToSpecification(filter, ctx);

            assertThatCode(() -> {
                Page<ProductRelation> result = productRelationRepository.findAll(spec, pageable);
                assertThat(result.getContent()).hasSize(1);
                assertThat(result.getTotalElements()).isEqualTo(3); // All 3 relations match
                assertThat(result.getTotalPages()).isEqualTo(3);
            }).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("Different pages of same query")
        void testDifferentPagesOfSameQuery() {
            String filter = "status==#ACTIVE#";

            RsqlContext<ProductRelation> ctx = new RsqlContext<>(ProductRelation.class);
            ctx.defineEntityManager(em);

            Specification<ProductRelation> spec = relationCompiler.compileToSpecification(filter, ctx);

            // First page
            assertThatCode(() -> {
                Page<ProductRelation> page0 = productRelationRepository.findAll(spec, PageRequest.of(0, 1, Sort.by("id")));
                assertThat(page0.getContent()).hasSize(1);
                assertThat(page0.getTotalElements()).isEqualTo(2); // REL1 and REL2 are ACTIVE
            }).doesNotThrowAnyException();

            // Second page
            assertThatCode(() -> {
                Page<ProductRelation> page1 = productRelationRepository.findAll(spec, PageRequest.of(1, 1, Sort.by("id")));
                assertThat(page1.getContent()).hasSize(1);
            }).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("Complex nested OR with multiple JOINs")
        void testComplexNestedOrWithMultipleJoins() {
            // Complex filter: (product1.type=ALPHA OR product2.type=ALPHA) AND status=ACTIVE
            String filter = "(product1.productType.code=='ALPHA',product2.productType.code=='ALPHA');status==#ACTIVE#";
            Pageable pageable = PageRequest.of(0, 10, Sort.by("id"));

            RsqlContext<ProductRelation> ctx = new RsqlContext<>(ProductRelation.class);
            ctx.defineEntityManager(em);

            Specification<ProductRelation> spec = relationCompiler.compileToSpecification(filter, ctx);

            assertThatCode(() -> {
                Page<ProductRelation> result = productRelationRepository.findAll(spec, pageable);
                // REL1: product1=A(ALPHA), status=ACTIVE -> matches
                // REL2: product2=C(GAMMA), status=ACTIVE -> no match
                // REL3: product2=A(ALPHA), status=INACTIVE -> no match (status)
                assertThat(result.getContent()).hasSize(1);
                assertThat(result.getContent().get(0).getCode()).isEqualTo("REL1");
            }).doesNotThrowAnyException();
        }
    }

    @Nested
    @DisplayName("Stress Tests")
    class StressTests {

        @Test
        @DisplayName("Rapid sequential paginated queries")
        void testRapidSequentialPaginatedQueries() {
            String filter = "product1.code=='PROD_A';product2.code=='PROD_B'";
            Pageable pageable = PageRequest.of(0, 10, Sort.by("id"));

            int iterations = 50;
            int errors = 0;

            for (int i = 0; i < iterations; i++) {
                try {
                    RsqlContext<ProductRelation> ctx = new RsqlContext<>(ProductRelation.class);
                    ctx.defineEntityManager(em);

                    Specification<ProductRelation> spec = relationCompiler.compileToSpecification(filter, ctx);
                    Page<ProductRelation> result = productRelationRepository.findAll(spec, pageable);

                    assertThat(result.getContent()).hasSize(1);
                } catch (Exception e) {
                    errors++;
                    System.err.println("Iteration " + i + " failed: " + e.getMessage());
                }
            }

            assertThat(errors).as("All %d iterations should succeed", iterations).isZero();
        }

        @Test
        @DisplayName("Alternating filter patterns")
        void testAlternatingFilterPatterns() {
            Pageable pageable = PageRequest.of(0, 10, Sort.by("id"));

            String[] filters = {
                "product1.code=='PROD_A'",
                "product1.code=='PROD_A';product2.code=='PROD_B'",
                "product1.productType.code=='ALPHA'",
                "product1.productType.code=='ALPHA';product2.productType.code=='BETA'",
                "status==#ACTIVE#",
                "product1.code=='PROD_A',product2.code=='PROD_A'"
            };

            int iterations = 30;
            int errors = 0;

            for (int i = 0; i < iterations; i++) {
                String filter = filters[i % filters.length];
                try {
                    RsqlContext<ProductRelation> ctx = new RsqlContext<>(ProductRelation.class);
                    ctx.defineEntityManager(em);

                    Specification<ProductRelation> spec = relationCompiler.compileToSpecification(filter, ctx);
                    Page<ProductRelation> result = productRelationRepository.findAll(spec, pageable);

                    assertThat(result).isNotNull();
                } catch (Exception e) {
                    errors++;
                    System.err.println("Iteration " + i + " (filter: " + filter + ") failed: " + e.getMessage());
                }
            }

            assertThat(errors).as("All iterations should succeed").isZero();
        }
    }
}

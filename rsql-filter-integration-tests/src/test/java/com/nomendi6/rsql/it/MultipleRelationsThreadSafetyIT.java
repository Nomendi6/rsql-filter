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
import jakarta.persistence.Tuple;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import rsql.RsqlCompiler;
import rsql.helper.SimpleQueryExecutor;
import rsql.where.RsqlContext;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for:
 * 1. Multiple relations to the same table (e.g., product1, product2, product3 -> Product)
 * 2. Thread safety when executing concurrent RSQL queries
 *
 * These tests verify that JOINs are correctly isolated between queries and that
 * Hibernate 6 SQM nodes don't leak between CriteriaQuery instances.
 */
@IntegrationTest
public class MultipleRelationsThreadSafetyIT {

    @Autowired
    private EntityManager em;

    @Autowired
    private ProductTypeRepository productTypeRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ProductRelationRepository productRelationRepository;

    private RsqlContext<ProductRelation> rsqlContext;
    private RsqlCompiler<ProductRelation> compiler;

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
        ProductType typeAlpha = new ProductType()
            .withCode("ALPHA")
            .withName("Alpha Type")
            .withDescription("First type");
        productTypeRepository.save(typeAlpha);

        ProductType typeBeta = new ProductType()
            .withCode("BETA")
            .withName("Beta Type")
            .withDescription("Second type");
        productTypeRepository.save(typeBeta);

        ProductType typeGamma = new ProductType()
            .withCode("GAMMA")
            .withName("Gamma Type")
            .withDescription("Third type");
        productTypeRepository.save(typeGamma);

        // Create Products with different types
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

        // Initialize RSQL context and compiler
        rsqlContext = new RsqlContext<>(ProductRelation.class);
        rsqlContext.defineEntityManager(em);
        compiler = new RsqlCompiler<>();
    }

    // ==================== Multiple Relations Tests ====================

    @Nested
    @DisplayName("Multiple Relations to Same Table")
    class MultipleRelationsTests {

        @Test
        @DisplayName("Filter on single relation field")
        void testFilterOnSingleRelation() {
            String filter = "product1.code=='PROD_A'";
            Pageable pageable = PageRequest.of(0, 10);

            Page<Tuple> result = SimpleQueryExecutor.getQueryResultAsPage(
                ProductRelation.class,
                Tuple.class,
                new String[]{"id", "code", "product1.code:p1Code"},
                filter,
                pageable,
                rsqlContext,
                compiler,
                productRelationRepository
            );

            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).get("code")).isEqualTo("REL1");
            System.out.println("Single relation filter: PASSED");
        }

        @Test
        @DisplayName("Filter on two different relations to same table (AND)")
        void testFilterOnTwoRelationsAnd() {
            // Find relations where product1 is PROD_A AND product2 is PROD_B
            String filter = "product1.code=='PROD_A';product2.code=='PROD_B'";
            Pageable pageable = PageRequest.of(0, 10);

            Page<Tuple> result = SimpleQueryExecutor.getQueryResultAsPage(
                ProductRelation.class,
                Tuple.class,
                new String[]{"id", "code", "product1.code:p1Code", "product2.code:p2Code"},
                filter,
                pageable,
                rsqlContext,
                compiler,
                productRelationRepository
            );

            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).get("code")).isEqualTo("REL1");
            assertThat(result.getContent().get(0).get("p1Code")).isEqualTo("PROD_A");
            assertThat(result.getContent().get(0).get("p2Code")).isEqualTo("PROD_B");
            System.out.println("Two relations AND filter: PASSED");
        }

        @Test
        @DisplayName("Filter on two different relations to same table (OR)")
        void testFilterOnTwoRelationsOr() {
            // Find relations where product1 is PROD_A OR product2 is PROD_A
            String filter = "product1.code=='PROD_A',product2.code=='PROD_A'";
            Pageable pageable = PageRequest.of(0, 10);

            Page<Tuple> result = SimpleQueryExecutor.getQueryResultAsPage(
                ProductRelation.class,
                Tuple.class,
                new String[]{"id", "code", "product1.code:p1Code", "product2.code:p2Code"},
                filter,
                pageable,
                rsqlContext,
                compiler,
                productRelationRepository
            );

            // REL1 has product1=A, REL3 has product2=A
            assertThat(result.getContent()).hasSize(2);
            System.out.println("Two relations OR filter: PASSED, found " + result.getContent().size() + " records");
        }

        @Test
        @DisplayName("Filter on all three relations")
        void testFilterOnThreeRelations() {
            // Find relations where product1=A, product2=B, product3=C
            String filter = "product1.code=='PROD_A';product2.code=='PROD_B';product3.code=='PROD_C'";
            Pageable pageable = PageRequest.of(0, 10);

            Page<Tuple> result = SimpleQueryExecutor.getQueryResultAsPage(
                ProductRelation.class,
                Tuple.class,
                new String[]{"id", "code", "product1.code:p1", "product2.code:p2", "product3.code:p3"},
                filter,
                pageable,
                rsqlContext,
                compiler,
                productRelationRepository
            );

            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).get("code")).isEqualTo("REL1");
            System.out.println("Three relations filter: PASSED");
        }

        @Test
        @DisplayName("Filter on nested relation through multiple paths")
        void testFilterOnNestedRelations() {
            // Filter on product1.productType.code and product2.productType.code
            String filter = "product1.productType.code=='ALPHA';product2.productType.code=='BETA'";
            Pageable pageable = PageRequest.of(0, 10);

            Page<Tuple> result = SimpleQueryExecutor.getQueryResultAsPage(
                ProductRelation.class,
                Tuple.class,
                new String[]{"id", "code", "product1.productType.code:type1", "product2.productType.code:type2"},
                filter,
                pageable,
                rsqlContext,
                compiler,
                productRelationRepository
            );

            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).get("code")).isEqualTo("REL1");
            assertThat(result.getContent().get(0).get("type1")).isEqualTo("ALPHA");
            assertThat(result.getContent().get(0).get("type2")).isEqualTo("BETA");
            System.out.println("Nested relations filter: PASSED");
        }

        @Test
        @DisplayName("Select fields from all three relations")
        void testSelectFromAllRelations() {
            String filter = "";  // No filter, get all
            Pageable pageable = PageRequest.of(0, 10);

            String[] properties = {
                "id",
                "code",
                "product1.id:p1Id",
                "product1.code:p1Code",
                "product1.name:p1Name",
                "product1.productType.code:p1Type",
                "product2.id:p2Id",
                "product2.code:p2Code",
                "product2.name:p2Name",
                "product2.productType.code:p2Type",
                "product3.id:p3Id",
                "product3.code:p3Code",
                "product3.name:p3Name",
                "product3.productType.code:p3Type"
            };

            Page<Tuple> result = SimpleQueryExecutor.getQueryResultAsPage(
                ProductRelation.class,
                Tuple.class,
                properties,
                filter,
                pageable,
                rsqlContext,
                compiler,
                productRelationRepository
            );

            assertThat(result.getContent()).hasSize(3);

            // Verify first record has all fields populated
            Tuple first = result.getContent().stream()
                .filter(t -> "REL1".equals(t.get("code")))
                .findFirst()
                .orElseThrow();

            assertThat(first.get("p1Code")).isEqualTo("PROD_A");
            assertThat(first.get("p2Code")).isEqualTo("PROD_B");
            assertThat(first.get("p3Code")).isEqualTo("PROD_C");
            assertThat(first.get("p1Type")).isEqualTo("ALPHA");
            assertThat(first.get("p2Type")).isEqualTo("BETA");
            assertThat(first.get("p3Type")).isEqualTo("GAMMA");

            System.out.println("Select from all relations: PASSED");
        }

        @Test
        @DisplayName("Complex OR filter on same relation field")
        void testComplexOrOnSameRelation() {
            // product1.code is A OR product1.code is B
            String filter = "product1.code=='PROD_A',product1.code=='PROD_B'";
            Pageable pageable = PageRequest.of(0, 10);

            Page<Tuple> result = SimpleQueryExecutor.getQueryResultAsPage(
                ProductRelation.class,
                Tuple.class,
                new String[]{"id", "code", "product1.code:p1Code"},
                filter,
                pageable,
                rsqlContext,
                compiler,
                productRelationRepository
            );

            // REL1 has product1=A, REL2 has product1=B
            assertThat(result.getContent()).hasSize(2);
            System.out.println("Complex OR on same relation: PASSED");
        }
    }

    // ==================== Thread Safety Tests ====================

    @Nested
    @DisplayName("Thread Safety Tests")
    class ThreadSafetyTests {

        @Test
        @DisplayName("Concurrent queries with same filter")
        void testConcurrentQueriesWithSameFilter() throws InterruptedException {
            int threadCount = 20;
            int iterationsPerThread = 10;
            ExecutorService executor = Executors.newFixedThreadPool(threadCount);
            CountDownLatch startLatch = new CountDownLatch(1);
            CountDownLatch doneLatch = new CountDownLatch(threadCount);
            AtomicInteger errors = new AtomicInteger(0);
            AtomicInteger successCount = new AtomicInteger(0);
            List<String> errorMessages = new ArrayList<>();

            String filter = "product1.code=='PROD_A';product2.code=='PROD_B'";
            Pageable pageable = PageRequest.of(0, 10);

            for (int i = 0; i < threadCount; i++) {
                final int threadId = i;
                executor.submit(() -> {
                    try {
                        startLatch.await(); // Wait for all threads to be ready

                        for (int j = 0; j < iterationsPerThread; j++) {
                            try {
                                // Each thread creates its own context
                                RsqlContext<ProductRelation> threadContext = new RsqlContext<>(ProductRelation.class);
                                threadContext.defineEntityManager(em);

                                Page<Tuple> result = SimpleQueryExecutor.getQueryResultAsPage(
                                    ProductRelation.class,
                                    Tuple.class,
                                    new String[]{"id", "code", "product1.code:p1", "product2.code:p2"},
                                    filter,
                                    pageable,
                                    threadContext,
                                    compiler,
                                    productRelationRepository
                                );

                                if (result.getContent().size() == 1) {
                                    successCount.incrementAndGet();
                                } else {
                                    synchronized (errorMessages) {
                                        errorMessages.add("Thread " + threadId + ": Expected 1 result, got " + result.getContent().size());
                                    }
                                    errors.incrementAndGet();
                                }
                            } catch (Exception e) {
                                synchronized (errorMessages) {
                                    errorMessages.add("Thread " + threadId + ": " + e.getClass().getSimpleName() + " - " + e.getMessage());
                                }
                                errors.incrementAndGet();
                            }
                        }
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    } finally {
                        doneLatch.countDown();
                    }
                });
            }

            // Start all threads simultaneously
            startLatch.countDown();

            // Wait for all threads to complete
            boolean completed = doneLatch.await(60, TimeUnit.SECONDS);
            executor.shutdown();

            // Report results
            System.out.println("Concurrent queries test:");
            System.out.println("  Total executions: " + (threadCount * iterationsPerThread));
            System.out.println("  Successful: " + successCount.get());
            System.out.println("  Errors: " + errors.get());

            if (!errorMessages.isEmpty()) {
                System.out.println("  Error messages:");
                errorMessages.forEach(msg -> System.out.println("    - " + msg));
            }

            assertThat(completed).withFailMessage("Test timed out").isTrue();
            assertThat(errors.get()).withFailMessage(
                "Found %d errors in concurrent execution. Messages: %s",
                errors.get(),
                String.join("; ", errorMessages)
            ).isZero();
        }

        @Test
        @DisplayName("Concurrent queries with different filters")
        void testConcurrentQueriesWithDifferentFilters() throws InterruptedException {
            int threadCount = 10;
            ExecutorService executor = Executors.newFixedThreadPool(threadCount);
            CountDownLatch startLatch = new CountDownLatch(1);
            CountDownLatch doneLatch = new CountDownLatch(threadCount);
            AtomicInteger errors = new AtomicInteger(0);
            List<String> errorMessages = new ArrayList<>();

            String[] filters = {
                "product1.code=='PROD_A'",
                "product2.code=='PROD_B'",
                "product3.code=='PROD_C'",
                "product1.code=='PROD_A';product2.code=='PROD_B'",
                "product1.productType.code=='ALPHA'",
                "product2.productType.code=='BETA'",
                "product1.code=='PROD_A',product2.code=='PROD_A'",
                "status==#ACTIVE#",
                "product1.status==#ACTIVE#;product2.status==#ACTIVE#",
                "product3.productType.code=='GAMMA'"
            };

            Pageable pageable = PageRequest.of(0, 10);

            for (int i = 0; i < threadCount; i++) {
                final int threadId = i;
                final String filter = filters[i % filters.length];

                executor.submit(() -> {
                    try {
                        startLatch.await();

                        // Each thread creates its own context
                        RsqlContext<ProductRelation> threadContext = new RsqlContext<>(ProductRelation.class);
                        threadContext.defineEntityManager(em);

                        Page<Tuple> result = SimpleQueryExecutor.getQueryResultAsPage(
                            ProductRelation.class,
                            Tuple.class,
                            new String[]{"id", "code"},
                            filter,
                            pageable,
                            threadContext,
                            compiler,
                            productRelationRepository
                        );

                        // Just verify no exception was thrown
                        if (result == null) {
                            synchronized (errorMessages) {
                                errorMessages.add("Thread " + threadId + ": Got null result for filter: " + filter);
                            }
                            errors.incrementAndGet();
                        }
                    } catch (Exception e) {
                        synchronized (errorMessages) {
                            errorMessages.add("Thread " + threadId + " (filter: " + filter + "): " +
                                e.getClass().getSimpleName() + " - " + e.getMessage());
                        }
                        errors.incrementAndGet();
                    } finally {
                        doneLatch.countDown();
                    }
                });
            }

            startLatch.countDown();
            boolean completed = doneLatch.await(60, TimeUnit.SECONDS);
            executor.shutdown();

            System.out.println("Concurrent different filters test:");
            System.out.println("  Threads: " + threadCount);
            System.out.println("  Errors: " + errors.get());

            if (!errorMessages.isEmpty()) {
                System.out.println("  Error messages:");
                errorMessages.forEach(msg -> System.out.println("    - " + msg));
            }

            assertThat(completed).withFailMessage("Test timed out").isTrue();
            assertThat(errors.get()).withFailMessage(
                "Found %d errors. Messages: %s",
                errors.get(),
                String.join("; ", errorMessages)
            ).isZero();
        }

        @Test
        @DisplayName("Rapid sequential queries (stress test)")
        void testRapidSequentialQueries() {
            int iterations = 100;
            AtomicInteger errors = new AtomicInteger(0);
            List<String> errorMessages = new ArrayList<>();

            Pageable pageable = PageRequest.of(0, 10);
            String[] filters = {
                "product1.code=='PROD_A'",
                "product1.code=='PROD_A';product2.code=='PROD_B'",
                "product1.productType.code=='ALPHA'"
            };

            for (int i = 0; i < iterations; i++) {
                try {
                    String filter = filters[i % filters.length];

                    // Create fresh context for each query
                    RsqlContext<ProductRelation> ctx = new RsqlContext<>(ProductRelation.class);
                    ctx.defineEntityManager(em);

                    Page<Tuple> result = SimpleQueryExecutor.getQueryResultAsPage(
                        ProductRelation.class,
                        Tuple.class,
                        new String[]{"id", "code", "product1.code:p1", "product2.code:p2"},
                        filter,
                        pageable,
                        ctx,
                        compiler,
                        productRelationRepository
                    );

                    assertThat(result).isNotNull();
                } catch (Exception e) {
                    errorMessages.add("Iteration " + i + ": " + e.getClass().getSimpleName() + " - " + e.getMessage());
                    errors.incrementAndGet();
                }
            }

            System.out.println("Rapid sequential queries test:");
            System.out.println("  Iterations: " + iterations);
            System.out.println("  Errors: " + errors.get());

            if (!errorMessages.isEmpty()) {
                errorMessages.stream().limit(10).forEach(msg -> System.out.println("    - " + msg));
            }

            assertThat(errors.get()).withFailMessage(
                "Found %d errors in %d iterations",
                errors.get(),
                iterations
            ).isZero();
        }

        @Test
        @DisplayName("Repository.findAll with Specification reuse check")
        void testSpecificationReuseInRepository() {
            // This test specifically checks if using the same Specification
            // for both findAll and count (which JPA does internally) causes issues

            String filter = "product1.code=='PROD_A';product2.code=='PROD_B'";

            // Create specification
            RsqlContext<ProductRelation> ctx = new RsqlContext<>(ProductRelation.class);
            ctx.defineEntityManager(em);

            Specification<ProductRelation> spec = compiler.compileToSpecification(filter, ctx);

            // Use specification for findAll with pagination
            // This internally calls toPredicate twice (for query and count)
            try {
                Page<ProductRelation> result = productRelationRepository.findAll(spec, PageRequest.of(0, 10));

                assertThat(result).isNotNull();
                assertThat(result.getContent()).hasSize(1);
                assertThat(result.getContent().get(0).getCode()).isEqualTo("REL1");

                System.out.println("Specification reuse test: PASSED");
                System.out.println("  Found " + result.getContent().size() + " records");
                System.out.println("  Total elements: " + result.getTotalElements());
            } catch (Exception e) {
                System.err.println("Specification reuse test: FAILED");
                System.err.println("  Exception: " + e.getClass().getSimpleName());
                System.err.println("  Message: " + e.getMessage());
                throw e;
            }
        }
    }
}

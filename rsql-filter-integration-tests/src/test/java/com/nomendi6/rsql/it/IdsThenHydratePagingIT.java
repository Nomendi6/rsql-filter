package com.nomendi6.rsql.it;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.nomendi6.rsql.it.config.IntegrationTest;
import com.nomendi6.rsql.it.config.SqlStatementCapture;
import com.nomendi6.rsql.it.domain.Product;
import com.nomendi6.rsql.it.domain.ProductType;
import com.nomendi6.rsql.it.domain.idshortcut.ShortcutChild;
import com.nomendi6.rsql.it.domain.idshortcut.ShortcutEmbeddedIdKey;
import com.nomendi6.rsql.it.domain.idshortcut.ShortcutEmbeddedIdTarget;
import com.nomendi6.rsql.it.domain.idshortcut.ShortcutRoot;
import com.nomendi6.rsql.it.repository.ProductRepository;
import com.nomendi6.rsql.it.repository.ProductTypeRepository;
import com.nomendi6.rsql.it.repository.ShortcutRootRepository;
import com.nomendi6.rsql.it.service.dto.ProductDTO;
import com.nomendi6.rsql.it.service.mapper.ProductMapper;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.TestPropertySource;
import rsql.PagingStrategy;
import rsql.RsqlCompiler;
import rsql.RsqlQueryService;
import rsql.helper.SimpleQueryExecutor;
import rsql.where.RsqlContext;

/**
 * {@link PagingStrategy#IDS_THEN_HYDRATE}: the identifiers of a page first, the caller's select second.
 *
 * <p>The tests compare it against the single statement it replaces - same rows, same total, same order -
 * and assert on the statements Hibernate actually sends, because the point of the strategy is their
 * shape: a narrow first statement carrying the offset and the limit, and a second one carrying neither.</p>
 */
@IntegrationTest
@TestPropertySource(
    properties = {
        "spring.jpa.properties.hibernate.session_factory.statement_inspector=com.nomendi6.rsql.it.config.SqlStatementCapture",
    }
)
public class IdsThenHydratePagingIT {

    /** A select new over a to-one join: the shape the strategy exists for. */
    private static final String SELECT_NEW =
        "select new Product(a0.id, a0.code, a0.name, a0.productType.name) from Product a0 left join a0.productType";
    private static final String COUNT = "select count(a0) from Product a0";
    private static final int PRODUCTS = 7;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ProductTypeRepository productTypeRepository;

    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private ShortcutRootRepository shortcutRootRepository;

    private final RsqlCompiler<Product> compiler = new RsqlCompiler<>();

    @BeforeEach
    @Transactional
    void seed() {
        productRepository.deleteAll();
        productTypeRepository.deleteAll();
        ProductType typeOne = new ProductType();
        typeOne.setCode("T1");
        typeOne.setName("Type one");
        productTypeRepository.saveAndFlush(typeOne);
        ProductType typeTwo = new ProductType();
        typeTwo.setCode("T2");
        typeTwo.setName("Type two");
        productTypeRepository.saveAndFlush(typeTwo);

        // Names sort in the same order as identifiers, so "name desc" is the reverse of what an unordered
        // hydration returns; seq sorts the other way; products 5-7 have no type at all.
        for (int i = 1; i <= PRODUCTS; i++) {
            Product product = new Product();
            product.setCode("C" + i);
            product.setName("name" + i);
            product.setSeq((long) (PRODUCTS - i));
            product.setProductType(i <= 2 ? typeOne : i <= 4 ? typeTwo : null);
            productRepository.saveAndFlush(product);
        }
        entityManager.flush();
        entityManager.clear();
    }

    private <E> RsqlContext<E> context(Class<E> type) {
        RsqlContext<E> context = new RsqlContext<>(type).defineEntityManager(entityManager);
        context.root.alias("a0");
        return context;
    }

    private Page<Product> twoPhase(String jpql, String count, String filter, Pageable pageable, Function<Product, ?> idOf, int chunk) {
        SqlStatementCapture.reset();
        return SimpleQueryExecutor.getJpqlQueryResultAsPageIdsThenHydrate(
            Product.class, Product.class, jpql, "a0", count, "a0", filter, pageable, context(Product.class), compiler, idOf, chunk);
    }

    private Page<Product> twoPhase(String filter, Pageable pageable) {
        return twoPhase(SELECT_NEW, COUNT, filter, pageable, null, SimpleQueryExecutor.DEFAULT_HYDRATION_CHUNK_SIZE);
    }

    private Page<Product> single(String filter, Pageable pageable) {
        SqlStatementCapture.reset();
        return SimpleQueryExecutor.getJpqlQueryResultAsPage(
            Product.class, Product.class, SELECT_NEW, "a0", COUNT, "a0", filter, pageable, context(Product.class), compiler);
    }

    private static List<Long> ids(Page<Product> page) {
        return page.getContent().stream().map(Product::getId).collect(Collectors.toList());
    }

    /** Identifiers are sequence-generated, so assertions on specific rows use the codes. */
    private static List<String> codes(Page<Product> page) {
        return page.getContent().stream().map(Product::getCode).collect(Collectors.toList());
    }

    private static List<String> selects() {
        return SqlStatementCapture.statements().stream().filter(sql -> sql.toLowerCase().startsWith("select")).collect(Collectors.toList());
    }

    private static boolean isCount(String sql) {
        return sql.toLowerCase().contains("count(");
    }

    private static boolean isHydration(String sql) {
        return sql.toLowerCase().contains(" in (");
    }

    private static boolean isLimited(String sql) {
        String lower = sql.toLowerCase();
        return lower.contains("offset") || lower.contains("fetch first") || lower.contains("limit");
    }

    // ------------------------------------------------------------------
    // Same rows as the single statement
    // ------------------------------------------------------------------

    /**
     * A sort and whether its keys are unique. With ties, the single statement does not define a total order:
     * the database may order tied rows differently on every execution, so its pages need not even partition
     * the result. Against such a sort only the two-phase pages' own consistency can be asserted.
     */
    private record SortCase(Sort sort, boolean unique) {}

    private static final List<SortCase> SORTS = List.of(
        new SortCase(Sort.unsorted(), false),
        new SortCase(Sort.by("name"), true),
        new SortCase(Sort.by(Sort.Order.desc("name")), true),
        new SortCase(Sort.by(Sort.Order.desc("seq"), Sort.Order.asc("code")), true),
        new SortCase(Sort.by(Sort.Order.asc("productType.name"), Sort.Order.desc("id")), true),
        new SortCase(Sort.by(Sort.Order.asc("productType.name")), false),
        new SortCase(Sort.by(Sort.Order.desc("id")), true)
    );

    private static final List<String> FILTERS = List.of(
        "",
        "productType.name==null",
        "productType.name=='Type one'",
        "code=in=('C1','C3','C5','C7')",
        "name=*'name*';seq=ge=2",
        "productType.name!='Type one',code=='C1'"
    );

    @Test
    @Transactional
    @DisplayName("with a total order, every page matches the single statement across filters and page sizes")
    void sameRowsAsTheSingleStatement() {
        for (SortCase sortCase : SORTS) {
            if (!sortCase.unique()) continue;
            for (String filter : FILTERS) {
                for (int size : new int[] { 3, 100 }) {
                    for (int page = 0; page < 3; page++) {
                        Pageable pageable = PageRequest.of(page, size, sortCase.sort());
                        String because = "sort=" + sortCase.sort() + " filter=[" + filter + "] page=" + page + " size=" + size;

                        Page<Product> expected = single(filter, pageable);
                        Page<Product> actual = twoPhase(filter, pageable);

                        assertThat(actual.getTotalElements()).as("total, %s", because).isEqualTo(expected.getTotalElements());
                        assertThat(ids(actual)).as("rows, %s", because).isEqualTo(ids(expected));
                        // Every row is a real select-new result, not just an identifier.
                        assertThat(actual.getContent()).allSatisfy(product -> assertThat(product.getCode()).startsWith("C"));
                    }
                }
            }
        }
    }

    @Test
    @Transactional
    @DisplayName("with ties in the sort, the pages still partition the result: every row once, none twice")
    void tiedSortKeysStillPartitionTheResult() {
        // The single statement cannot promise this. Ordered by productType.name alone, H2 was observed to
        // return [C2, C1, C3] as page 1 and [C3] as page 2 - one row on two pages, another on none - because
        // each execution orders the tied rows afresh. The id page ends its sort with the identifier, so its
        // order is total and its pages are a partition.
        for (SortCase sortCase : SORTS) {
            if (sortCase.unique()) continue;
            for (String filter : FILTERS) {
                Set<Long> all = Set.copyOf(ids(single(filter, PageRequest.of(0, 100, sortCase.sort()))));
                List<Long> seen = new java.util.ArrayList<>();
                for (int page = 0; page * 2 < all.size() + 2; page++) {
                    seen.addAll(ids(twoPhase(filter, PageRequest.of(page, 2, sortCase.sort()))));
                }
                String because = "sort=" + sortCase.sort() + " filter=[" + filter + "]";
                assertThat(seen).as("no row twice, %s", because).doesNotHaveDuplicates();
                assertThat(Set.copyOf(seen)).as("every row once, %s", because).isEqualTo(all);
            }
        }
    }

    @Test
    @Transactional
    @DisplayName("a filter on a nullable to-one path keeps the left join, so the rows without a target still match")
    void nullPathFilterKeepsTheLeftJoin() {
        // Hibernate reuses the explicit `left join a0.productType` for the implicit path in the filter. Were
        // the id page built on a bare `from Product a0`, the same path would be an inner join and these three
        // rows would vanish - which is why the caller's from clause is reused rather than rebuilt.
        Page<Product> page = twoPhase("productType.name==null", PageRequest.of(0, 10, Sort.by("id")));

        assertThat(codes(page)).containsExactly("C5", "C6", "C7");
        assertThat(page.getTotalElements()).isEqualTo(3);
        String idPage = selects().stream().filter(sql -> !isCount(sql) && !isHydration(sql)).findFirst().orElseThrow();
        assertThat(idPage).containsIgnoringCase("left join");
    }

    // ------------------------------------------------------------------
    // The shape of the statements
    // ------------------------------------------------------------------

    @Test
    @Transactional
    @DisplayName("three statements: one count, a limited id page, an unlimited hydration")
    void threeStatementsOfTheRightShape() {
        twoPhase("seq=ge=0", PageRequest.of(1, 2, Sort.by(Sort.Order.desc("name"))));

        List<String> selects = selects();
        assertThat(selects).hasSize(3);
        assertThat(selects.stream().filter(IdsThenHydratePagingIT::isCount)).as("count runs exactly once").hasSize(1);

        String idPage = selects.stream().filter(sql -> !isCount(sql) && !isHydration(sql)).findFirst().orElseThrow();
        assertThat(isLimited(idPage)).as("the id page carries offset/limit: %s", idPage).isTrue();
        assertThat(idPage).containsIgnoringCase("distinct").doesNotContain("code");

        String hydration = selects.stream().filter(IdsThenHydratePagingIT::isHydration).findFirst().orElseThrow();
        assertThat(isLimited(hydration)).as("the hydration carries no offset/limit: %s", hydration).isFalse();
        assertThat(hydration).doesNotContainIgnoringCase("order by").containsIgnoringCase("left join");
    }

    @Test
    @Transactional
    @DisplayName("the hydration returns rows in database order and the page puts them back in sort order")
    void rowsArePutBackInSortOrder() {
        Page<Product> page = twoPhase("", PageRequest.of(0, 4, Sort.by(Sort.Order.desc("name"))));

        assertThat(codes(page)).containsExactly("C7", "C6", "C5", "C4");
        assertThat(page.getContent()).extracting(Product::getName).containsExactly("name7", "name6", "name5", "name4");
    }

    @Test
    @Transactional
    @DisplayName("a page past the end is empty, carries the total, and skips the hydration")
    void pagePastTheEnd() {
        Page<Product> page = twoPhase("", PageRequest.of(50, 10, Sort.by("name")));

        assertThat(page.getContent()).isEmpty();
        assertThat(page.getTotalElements()).isEqualTo(PRODUCTS);
        assertThat(selects()).hasSize(2).noneMatch(IdsThenHydratePagingIT::isHydration);
    }

    @Test
    @Transactional
    @DisplayName("a page longer than the chunk size is hydrated in several statements, in order")
    void hydrationIsChunked() {
        Page<Product> page = twoPhase(SELECT_NEW, COUNT, "", PageRequest.of(0, 5, Sort.by(Sort.Order.desc("name"))), null, 2);

        assertThat(codes(page)).containsExactly("C7", "C6", "C5", "C4", "C3");
        assertThat(selects().stream().filter(IdsThenHydratePagingIT::isHydration)).as("ceil(5 / 2) hydration statements").hasSize(3);
    }

    // ------------------------------------------------------------------
    // What the caller's select may contain
    // ------------------------------------------------------------------

    @Test
    @Transactional
    @DisplayName("join fetch in the caller's select is dropped for the id page and kept for the hydration")
    void joinFetchIsHandled() {
        String withFetch = "select a0 from Product a0 left join fetch a0.productType";
        Page<Product> page = twoPhase(withFetch, COUNT, "productType.name=='Type two'", PageRequest.of(0, 10, Sort.by("id")), null, 1000);

        assertThat(codes(page)).containsExactly("C3", "C4");
        assertThat(page.getContent()).allSatisfy(product -> assertThat(product.getProductType().getName()).isEqualTo("Type two"));
    }

    @Test
    @Transactional
    @DisplayName("a to-many join in the caller's select does not shorten the page")
    void toManyJoinDoesNotShortenThePage() {
        shortcutRootRepository.deleteAll();
        entityManager.createQuery("delete from ShortcutChild").executeUpdate();
        ShortcutRoot withChildren = entityManager.merge(new ShortcutRoot(1L, "a"));
        entityManager.merge(new ShortcutRoot(2L, "b"));
        entityManager.merge(new ShortcutRoot(3L, "c"));
        for (long i = 10; i < 13; i++) {
            entityManager.merge(new ShortcutChild(i, "child" + i, withChildren));
        }
        entityManager.flush();
        entityManager.clear();

        // Root 1 is three rows through the join. A page of two must still be two distinct roots.
        RsqlContext<ShortcutRoot> context = context(ShortcutRoot.class);
        SqlStatementCapture.reset();
        Page<ShortcutRoot> page = SimpleQueryExecutor.getJpqlQueryResultAsPageIdsThenHydrate(
            ShortcutRoot.class, ShortcutRoot.class,
            "select a0 from ShortcutRoot a0 left join a0.children c", "a0",
            "select count(distinct a0) from ShortcutRoot a0 left join a0.children c", "a0",
            "", PageRequest.of(0, 2, Sort.by("name")), context, new RsqlCompiler<>(), null, 1000);

        assertThat(page.getContent()).extracting(ShortcutRoot::getId).containsExactly(1L, 2L);
        assertThat(page.getTotalElements()).isEqualTo(3);
    }

    // ------------------------------------------------------------------
    // Fallbacks
    // ------------------------------------------------------------------

    @Test
    @Transactional
    @DisplayName("an unpaged request runs the single statement")
    void unpagedFallsBack() {
        Page<Product> page = twoPhase("", Pageable.unpaged());

        assertThat(page.getContent()).hasSize(PRODUCTS);
        assertThat(selects()).hasSize(1).noneMatch(IdsThenHydratePagingIT::isHydration);
    }

    @Test
    @Transactional
    @DisplayName("an entity with a composite identifier runs the single statement")
    void compositeIdentifierFallsBack() {
        entityManager.createQuery("delete from ShortcutEmbeddedIdTarget").executeUpdate();
        entityManager.merge(new ShortcutEmbeddedIdTarget(new ShortcutEmbeddedIdKey(1L, 1L), "one"));
        entityManager.merge(new ShortcutEmbeddedIdTarget(new ShortcutEmbeddedIdKey(1L, 2L), "two"));
        entityManager.flush();
        entityManager.clear();

        RsqlContext<ShortcutEmbeddedIdTarget> context = context(ShortcutEmbeddedIdTarget.class);
        SqlStatementCapture.reset();
        Page<ShortcutEmbeddedIdTarget> page = SimpleQueryExecutor.getJpqlQueryResultAsPageIdsThenHydrate(
            ShortcutEmbeddedIdTarget.class, ShortcutEmbeddedIdTarget.class,
            "select a0 from ShortcutEmbeddedIdTarget a0", "a0",
            "select count(a0) from ShortcutEmbeddedIdTarget a0", "a0",
            "", PageRequest.of(0, 10, Sort.by("name")), context, new RsqlCompiler<>(), null, 1000);

        assertThat(page.getContent()).hasSize(2);
        assertThat(selects()).hasSize(2).noneMatch(IdsThenHydratePagingIT::isHydration);
    }

    @Test
    @Transactional
    @DisplayName("a select whose from clause cannot be found runs the single statement")
    void missingFromClauseFallsBack() {
        // Hibernate accepts a select without a from for an entity query; the id page cannot be built from it.
        RsqlContext<Product> context = context(Product.class);
        SqlStatementCapture.reset();
        assertThatThrownBy(() -> SimpleQueryExecutor.getJpqlQueryResultAsPageIdsThenHydrate(
            Product.class, Product.class, "select a0", "a0", COUNT, "a0", "",
            PageRequest.of(0, 10), context, compiler, null, 1000))
            .isInstanceOf(RuntimeException.class);
        assertThat(selects()).noneMatch(IdsThenHydratePagingIT::isHydration);
    }

    // ------------------------------------------------------------------
    // Reading the identifier off a hydrated row
    // ------------------------------------------------------------------

    @Test
    @Transactional
    @DisplayName("an explicit row id extractor is used when given")
    void explicitRowIdExtractor() {
        Page<Product> page = twoPhase(SELECT_NEW, COUNT, "", PageRequest.of(0, 3, Sort.by(Sort.Order.desc("name"))), Product::getId, 1000);

        assertThat(codes(page)).containsExactly("C7", "C6", "C5");
    }

    @Test
    @Transactional
    @DisplayName("a select-new row is an entity to the persistence unit, so no extractor is needed")
    void selectNewRowNeedsNoExtractor() {
        Page<Product> page = twoPhase(SELECT_NEW, COUNT, "", PageRequest.of(0, 3, Sort.by(Sort.Order.desc("name"))), null, 1000);

        assertThat(codes(page)).containsExactly("C7", "C6", "C5");
    }

    // ------------------------------------------------------------------
    // Through the service
    // ------------------------------------------------------------------

    private RsqlQueryService<Product, ProductDTO, ProductRepository, ProductMapper> service() {
        return new RsqlQueryService<>(productRepository, productMapper, entityManager, Product.class, SELECT_NEW, COUNT);
    }

    @Test
    @Transactional
    @DisplayName("the strategy defaults to the single statement and is set fluently")
    void serviceDefaultsAndFluentSetter() {
        RsqlQueryService<Product, ProductDTO, ProductRepository, ProductMapper> service = service();
        assertThat(service.getPagingStrategy()).isEqualTo(PagingStrategy.SINGLE_QUERY);

        assertThat(service.withPagingStrategy(PagingStrategy.IDS_THEN_HYDRATE)).isSameAs(service);
        assertThat(service.getPagingStrategy()).isEqualTo(PagingStrategy.IDS_THEN_HYDRATE);

        assertThatThrownBy(() -> service.setPagingStrategy(null)).isInstanceOf(NullPointerException.class);
    }

    @Test
    @Transactional
    @DisplayName("findByFilter, findEntitiesByFilter and getJpqlQueryResultAsPage all follow the strategy")
    void everyPagedJpqlMethodFollowsTheStrategy() {
        RsqlQueryService<Product, ProductDTO, ProductRepository, ProductMapper> service = service();
        Pageable pageable = PageRequest.of(0, 3, Sort.by(Sort.Order.desc("name")));

        SqlStatementCapture.reset();
        Page<ProductDTO> singleDto = service.findByFilter("seq=ge=0", pageable);
        assertThat(selects()).hasSize(2).noneMatch(IdsThenHydratePagingIT::isHydration);

        service.withPagingStrategy(PagingStrategy.IDS_THEN_HYDRATE);

        SqlStatementCapture.reset();
        Page<ProductDTO> dtos = service.findByFilter("seq=ge=0", pageable);
        assertThat(selects()).hasSize(3).anyMatch(IdsThenHydratePagingIT::isHydration);
        assertThat(dtos.getContent()).extracting(ProductDTO::getId).isEqualTo(singleDto.getContent().stream().map(ProductDTO::getId).collect(Collectors.toList()));
        assertThat(dtos.getTotalElements()).isEqualTo(PRODUCTS);

        SqlStatementCapture.reset();
        Page<Product> entities = service.findEntitiesByFilter("seq=ge=0", pageable);
        assertThat(selects()).hasSize(3).anyMatch(IdsThenHydratePagingIT::isHydration);
        assertThat(codes(entities)).containsExactly("C7", "C6", "C5");

        SqlStatementCapture.reset();
        Page<ProductDTO> explicitJpql = service.getJpqlQueryResultAsPage(SELECT_NEW, COUNT, "seq=ge=0", pageable);
        assertThat(selects()).hasSize(3).anyMatch(IdsThenHydratePagingIT::isHydration);
        assertThat(explicitJpql.getContent()).extracting(ProductDTO::getId).isEqualTo(ids(entities));
    }

    @Test
    @Transactional
    @DisplayName("the Specification path is untouched by the strategy")
    void specificationPathIsUntouched() {
        RsqlQueryService<Product, ProductDTO, ProductRepository, ProductMapper> service =
            new RsqlQueryService<>(productRepository, productMapper, entityManager, Product.class)
                .withPagingStrategy(PagingStrategy.IDS_THEN_HYDRATE);

        SqlStatementCapture.reset();
        Page<ProductDTO> page = service.findByFilter("seq=ge=0", PageRequest.of(0, 3, Sort.by("name")));

        assertThat(page.getContent()).hasSize(3);
        assertThat(selects()).noneMatch(IdsThenHydratePagingIT::isHydration);
    }
}

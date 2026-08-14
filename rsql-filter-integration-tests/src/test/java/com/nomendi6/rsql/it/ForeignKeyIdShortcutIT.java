package com.nomendi6.rsql.it;

import static org.assertj.core.api.Assertions.assertThat;

import com.nomendi6.rsql.it.config.IntegrationTest;
import com.nomendi6.rsql.it.config.SqlStatementCapture;
import com.nomendi6.rsql.it.domain.Product;
import com.nomendi6.rsql.it.domain.ProductType;
import com.nomendi6.rsql.it.domain.idshortcut.*;
import com.nomendi6.rsql.it.repository.ProductRepository;
import com.nomendi6.rsql.it.repository.ProductTypeRepository;
import com.nomendi6.rsql.it.repository.ShortcutRootRepository;
import com.nomendi6.rsql.it.service.dto.ProductDTO;
import com.nomendi6.rsql.it.service.mapper.ProductMapper;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.test.context.TestPropertySource;
import rsql.RsqlCompiler;
import rsql.RsqlQueryService;
import rsql.where.RsqlContext;

/**
 * A selector ending in the identifier of a to-one association is resolved against the querying table's own
 * foreign key column rather than through a join.
 *
 * <p><strong>On this line the result is that nothing changes.</strong> Hibernate 6.5 already drops a LEFT JOIN
 * whose only use is the target's identifier, so {@code assoc.id==5} reached the foreign key column before this
 * change too. The tests therefore assert two things: that the generated SQL is <em>identical</em> whichever way
 * {@link RsqlContext#useForeignKeyIdShortcut} is set, and that the shape it has is the join-free one.</p>
 *
 * <p>Hibernate 7, on the 0.7.x line, honours an explicit {@code join()} literally and does emit the extra join,
 * which is where the change earns its keep. What is shared between the lines is the set of mappings the
 * shortcut must decline - a collection, the inverse side of a {@code OneToOne}, {@code @NotFound}, a composite
 * identifier - because taking it there would reach the identifier through an implicit, and therefore inner,
 * join. Those are pinned down here as well, since a guard that only works on one line is not a guard.</p>
 */
@IntegrationTest
@TestPropertySource(
    properties = {
        "spring.jpa.properties.hibernate.session_factory.statement_inspector=com.nomendi6.rsql.it.config.SqlStatementCapture",
    }
)
public class ForeignKeyIdShortcutIT {

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private ShortcutRootRepository shortcutRootRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ProductTypeRepository productTypeRepository;

    @Autowired
    private ProductMapper productMapper;

    private final RsqlCompiler<ShortcutRoot> compiler = new RsqlCompiler<>();
    private final RsqlCompiler<Product> productCompiler = new RsqlCompiler<>();

    /** The SQL and the rows a filter produces, so a test can assert on both from one run. */
    private record Run(String sql, List<Long> ids) {
        int joins() {
            return SqlStatementCapture.countJoins(sql);
        }
    }

    private Run run(String filter, boolean shortcut) {
        RsqlContext<ShortcutRoot> context = new RsqlContext<>(ShortcutRoot.class).defineEntityManager(entityManager);
        context.useForeignKeyIdShortcut = shortcut;
        Specification<ShortcutRoot> specification = compiler.compileToSpecification(filter, context);
        SqlStatementCapture.reset();
        List<Long> ids = shortcutRootRepository.findAll(specification).stream().map(ShortcutRoot::getId).sorted().toList();
        return new Run(SqlStatementCapture.firstStatement(), ids);
    }

    private Run runOnProduct(String filter, boolean shortcut) {
        RsqlContext<Product> context = new RsqlContext<>(Product.class).defineEntityManager(entityManager);
        context.useForeignKeyIdShortcut = shortcut;
        Specification<Product> specification = productCompiler.compileToSpecification(filter, context);
        SqlStatementCapture.reset();
        List<Long> ids = productRepository.findAll(specification).stream().map(Product::getId).sorted().toList();
        return new Run(SqlStatementCapture.firstStatement(), ids);
    }

    /**
     * Run a filter both ways and assert the two are indistinguishable - same SQL, same rows.
     *
     * @return the run, so a test can go on to assert the shape of the SQL
     */
    private Run bothWaysAgree(String filter) {
        Run on = run(filter, true);
        Run off = run(filter, false);
        assertThat(on.sql()).as("SQL for %s", filter).isEqualTo(off.sql());
        assertThat(on.ids()).as("rows for %s", filter).isEqualTo(off.ids());
        return on;
    }

    @BeforeEach
    @Transactional
    void seed() {
        shortcutRootRepository.deleteAll();
        entityManager.createQuery("delete from ShortcutChild").executeUpdate();
        entityManager.createQuery("delete from ShortcutInverseSide").executeUpdate();
        entityManager.createQuery("delete from ShortcutSimpleTarget").executeUpdate();
        entityManager.createQuery("delete from ShortcutCustomIdTarget").executeUpdate();
        entityManager.createQuery("delete from ShortcutEmbeddedIdTarget").executeUpdate();
        entityManager.createQuery("delete from ShortcutRestrictedTarget").executeUpdate();

        ShortcutSimpleTarget targetOne = entityManager.merge(new ShortcutSimpleTarget(1L, "one"));
        ShortcutSimpleTarget targetTwo = entityManager.merge(new ShortcutSimpleTarget(2L, "two"));
        ShortcutCustomIdTarget customTarget = entityManager.merge(new ShortcutCustomIdTarget(7L, "custom"));
        ShortcutEmbeddedIdTarget embeddedTarget = entityManager.merge(
            new ShortcutEmbeddedIdTarget(new ShortcutEmbeddedIdKey(3L, 4L), "embedded")
        );
        ShortcutRestrictedTarget liveTarget = entityManager.merge(new ShortcutRestrictedTarget(10L, "live", false));
        ShortcutRestrictedTarget archivedTarget = entityManager.merge(new ShortcutRestrictedTarget(11L, "archived", true));

        // 100 points at target one, 200 at target two, 300 at nothing at all.
        ShortcutRoot first = new ShortcutRoot(100L, "first");
        first.setSimpleTarget(targetOne);
        first.setOwnedOneToOne(targetOne);
        first.setCustomIdTarget(customTarget);
        first.setEmbeddedIdTarget(embeddedTarget);
        first.setRestrictedTarget(liveTarget);

        ShortcutRoot second = new ShortcutRoot(200L, "second");
        second.setSimpleTarget(targetTwo);
        second.setRestrictedTarget(archivedTarget);

        ShortcutRoot third = new ShortcutRoot(300L, "third");

        entityManager.merge(first);
        entityManager.merge(second);
        entityManager.merge(third);

        entityManager.merge(new ShortcutChild(1000L, "child", first));
        entityManager.merge(new ShortcutInverseSide(2000L, "inverse", first));

        // Products, for the cases that need a SELECT over an association and a three-segment path.
        productRepository.deleteAll();
        productTypeRepository.deleteAll();
        ProductType productType = new ProductType();
        productType.setCode("PT");
        productType.setName("A type");
        productTypeRepository.saveAndFlush(productType);

        Product grandparent = newProduct("GP", "grandparent", productType, null);
        Product parent = newProduct("P", "parent", null, grandparent);
        newProduct("C", "child", null, parent);

        entityManager.flush();
        entityManager.clear();
    }

    private Product newProduct(String code, String name, ProductType type, Product parent) {
        Product product = new Product();
        product.setCode(code);
        product.setName(name);
        product.setProductType(type);
        product.setParent(parent);
        return productRepository.saveAndFlush(product);
    }

    // ------------------------------------------------------------------
    // The shape a to-one id filter has on this line
    // ------------------------------------------------------------------

    @Test
    @Transactional
    @DisplayName("a ManyToOne id selector reads the foreign key column and creates no join, either way")
    void manyToOneIdNeedsNoJoin() {
        Run result = bothWaysAgree("simpleTarget.id==1");

        assertThat(result.joins()).isZero();
        assertThat(result.sql()).contains("sr1_0.simple_target_id=?").doesNotContainIgnoringCase("join");
        assertThat(result.ids()).containsExactly(100L);
    }

    @Test
    @Transactional
    @DisplayName("an =in= list over a ManyToOne id creates no join either")
    void manyToOneIdInListNeedsNoJoin() {
        Run result = bothWaysAgree("simpleTarget.id=in=(1,2)");

        assertThat(result.joins()).isZero();
        assertThat(result.sql()).contains("sr1_0.simple_target_id in (?,?)");
        assertThat(result.ids()).containsExactly(100L, 200L);
    }

    @Test
    @Transactional
    @DisplayName("the owning side of a OneToOne carries its foreign key too")
    void owningOneToOneIdNeedsNoJoin() {
        Run result = bothWaysAgree("ownedOneToOne.id==1");

        assertThat(result.joins()).isZero();
        assertThat(result.sql()).contains("sr1_0.owned_one_to_one_id=?");
        assertThat(result.ids()).containsExactly(100L);
    }

    @Test
    @Transactional
    @DisplayName("the identifier is found under whatever name it has, not just 'id'")
    void identifierNeedNotBeCalledId() {
        Run result = bothWaysAgree("customIdTarget.objectId==7");

        assertThat(result.joins()).isZero();
        assertThat(result.sql()).contains("sr1_0.custom_id_target_id=?");
        assertThat(result.ids()).containsExactly(100L);
    }

    @Test
    @Transactional
    @DisplayName("only the last segment is read from a foreign key: a.b.c.id still joins a and b")
    void onlyTheLastSegmentIsShortCircuited() {
        Run on = runOnProduct("parent.parent.productType.id==1", true);
        Run off = runOnProduct("parent.parent.productType.id==1", false);

        assertThat(on.sql()).isEqualTo(off.sql());
        assertThat(on.ids()).isEqualTo(off.ids());
        assertThat(on.joins()).isEqualTo(2);
    }

    // ------------------------------------------------------------------
    // The mappings the shortcut must decline, on either line
    // ------------------------------------------------------------------

    @Test
    @Transactional
    @DisplayName("a non-identifier selector still joins")
    void nonIdentifierSelectorStillJoins() {
        Run result = bothWaysAgree("simpleTarget.name=='one'");

        assertThat(result.joins()).isOne();
        assertThat(result.ids()).containsExactly(100L);
    }

    @Test
    @Transactional
    @DisplayName("a OneToMany keeps its LEFT join: the foreign key is on the other table")
    void oneToManyKeepsItsJoin() {
        Run result = bothWaysAgree("children.id==1000");

        assertThat(result.joins()).isOne();
        assertThat(result.sql()).containsIgnoringCase("left join");
        assertThat(result.ids()).containsExactly(100L);
    }

    @Test
    @Transactional
    @DisplayName("the inverse side of a OneToOne keeps its join, and keeps it a LEFT join")
    void inverseOneToOneKeepsItsLeftJoin() {
        // The persistent attribute type is ONE_TO_ONE on both sides, but only the owning side stores the
        // foreign key. Reaching the identifier with get() here would go through an implicit - and therefore
        // inner - join, silently narrowing the filter.
        Run result = bothWaysAgree("inverseOneToOne.id==2000");

        assertThat(result.joins()).isOne();
        assertThat(result.sql()).containsIgnoringCase("left join");
        assertThat(result.ids()).containsExactly(100L);
    }

    @Test
    @Transactional
    @DisplayName("a foreign key that may point at nothing keeps its LEFT join")
    void notFoundAssociationKeepsItsLeftJoin() {
        // @NotFound means the foreign key may name a row that does not exist, so the target has to be looked
        // up to find out. Hibernate cannot answer this one from the foreign key, on either line.
        Run result = bothWaysAgree("notFoundTarget.id==5");

        assertThat(result.joins()).isOne();
        assertThat(result.sql()).containsIgnoringCase("left join");
    }

    @Test
    @Transactional
    @DisplayName("a composite identifier is left alone: it is not one foreign key column")
    void compositeIdentifierIsLeftAlone() {
        Run result = bothWaysAgree("embeddedIdTarget.id.partA==3");

        assertThat(result.ids()).containsExactly(100L);
    }

    // ------------------------------------------------------------------
    // Clause order, and the promise that the join count never grows
    // ------------------------------------------------------------------

    @Test
    @Transactional
    @DisplayName("clause order does not change the rows or the join count")
    void clauseOrderChangesNeitherRowsNorJoinCount() {
        Run idFirst = run("simpleTarget.id==1 and simpleTarget.name=='one'", true);
        Run nameFirst = run("simpleTarget.name=='one' and simpleTarget.id==1", true);

        assertThat(idFirst.joins()).isEqualTo(nameFirst.joins()).isEqualTo(1);
        assertThat(idFirst.ids()).isEqualTo(nameFirst.ids()).containsExactly(100L);
    }

    @Test
    @Transactional
    @DisplayName("no filter shape changes its SQL or its rows when the setting is flipped")
    void nothingChangesEitherWay() {
        String[] filters = {
            "simpleTarget.id==1",
            "simpleTarget.id=in=(1,2)",
            "simpleTarget.id==null",
            "simpleTarget.id!=1",
            "simpleTarget.id=nin=(1)",
            "simpleTarget.name=='one'",
            "children.id==1000",
            "inverseOneToOne.id==2000",
            "notFoundTarget.id==5",
            "restrictedTarget.id==10",
            "simpleTarget.id==1 and simpleTarget.name=='one'",
            "simpleTarget.name=='one' and simpleTarget.id==1",
            "simpleTarget.id==1 or simpleTarget.name=='one'",
        };
        for (String filter : filters) {
            bothWaysAgree(filter);
        }
    }

    // ------------------------------------------------------------------
    // Filtering on relation.id with and without relation fields in SELECT
    // ------------------------------------------------------------------

    @Test
    @Transactional
    @DisplayName("filtering on relation.id: the join survives exactly when the SELECT needs it")
    void filterOnRelationIdAcrossSelectShapes() {
        RsqlQueryService<Product, ProductDTO, ProductRepository, ProductMapper> service = new RsqlQueryService<>(
            productRepository,
            productMapper,
            entityManager,
            Product.class
        );
        ProductType type = productTypeRepository.findAll().stream().findFirst().orElseThrow();
        String filter = "productType.id==" + type.getId();

        record SelectCase(String select, int expectedJoins) {}
        List<SelectCase> cases = List.of(
            // SELECT names no field of the association: nothing needs the join, so there is none.
            new SelectCase("code, COUNT(*):total", 0),
            new SelectCase("code, name, COUNT(*):total", 0),
            // SELECT names a field of the association other than the identifier: it needs the join for
            // itself, so the join is there and the filter reads the foreign key column beside it.
            new SelectCase("productType.name:tn, COUNT(*):total", 1),
            new SelectCase("productType.code:tc, productType.name:tn, COUNT(*):total", 1),
            // SELECT names the identifier itself: that is a foreign key column too, so it needs no join.
            new SelectCase("productType.id:tid, COUNT(*):total", 0),
            new SelectCase("productType.name:tn, productType.id:tid, COUNT(*):total", 1)
        );

        for (SelectCase testCase : cases) {
            String because = "SELECT [" + testCase.select() + "]";

            service.setUseForeignKeyIdShortcut(true);
            SqlStatementCapture.reset();
            List<jakarta.persistence.Tuple> withShortcut = service.getAggregateResultWithExpressions(
                testCase.select(),
                filter,
                null,
                null
            );
            String shortcutSql = SqlStatementCapture.firstStatement();

            service.setUseForeignKeyIdShortcut(false);
            SqlStatementCapture.reset();
            List<jakarta.persistence.Tuple> withoutShortcut = service.getAggregateResultWithExpressions(
                testCase.select(),
                filter,
                null,
                null
            );
            String joinedSql = SqlStatementCapture.firstStatement();

            assertThat(rowsOf(withShortcut)).as("rows, %s", because).isEqualTo(rowsOf(withoutShortcut)).isNotEmpty();
            assertThat(SqlStatementCapture.countJoins(shortcutSql)).as("joins, %s", because).isEqualTo(testCase.expectedJoins());

            // Unlike the WHERE side, the SELECT side is a real change on this line: Hibernate 6.5 drops a
            // join nothing uses, but selecting the joined table's key column counts as using it, so
            // productType.id in a SELECT kept the join until now. The setting never adds one.
            assertThat(SqlStatementCapture.countJoins(joinedSql))
                .as("joins without the shortcut, %s", because)
                .isGreaterThanOrEqualTo(testCase.expectedJoins());

            // The filter reads the base table's foreign key column. A WHERE condition never has to appear in
            // the GROUP BY, so grouping by a joined column while filtering on the foreign key stays valid.
            //
            // Only the ON side is pinned down here. With the shortcut off, Hibernate 6.5 decides for itself
            // whether to read the identifier off the join or off the foreign key - it does the latter unless
            // the SELECT projects that identifier - and which one it picks is not this library's contract.
            assertThat(shortcutSql).as("predicate, %s", because).contains("where p1_0.product_type_id=?").contains("group by");
        }
    }

    @Test
    @Transactional
    @DisplayName("the plain, non-aggregate SELECT path behaves the same way")
    void filterOnRelationIdOnThePlainSelectPath() {
        RsqlQueryService<Product, ProductDTO, ProductRepository, ProductMapper> service = new RsqlQueryService<>(
            productRepository,
            productMapper,
            entityManager,
            Product.class
        );
        ProductType type = productTypeRepository.findAll().stream().findFirst().orElseThrow();
        String filter = "productType.id==" + type.getId();

        record Case(String select, int expectedJoins) {}
        List<Case> cases = List.of(new Case("id, code, name", 0), new Case("id, code, productType.name", 1));

        for (Case testCase : cases) {
            String because = "SELECT [" + testCase.select() + "]";

            service.setUseForeignKeyIdShortcut(true);
            SqlStatementCapture.reset();
            List<?> rows = service.getLOVWithSelect(testCase.select(), filter, org.springframework.data.domain.PageRequest.of(0, 10));
            String sql = SqlStatementCapture.firstStatement();

            assertThat(rows).as("rows, %s", because).isNotEmpty();
            assertThat(SqlStatementCapture.countJoins(sql)).as("joins, %s", because).isEqualTo(testCase.expectedJoins());
            assertThat(sql).as("predicate, %s", because).contains("p1_0.product_type_id=?");
        }
    }

    private static List<String> rowsOf(List<jakarta.persistence.Tuple> tuples) {
        return tuples
            .stream()
            .map(tuple -> {
                StringBuilder line = new StringBuilder();
                for (int i = 0; i < tuple.getElements().size(); i++) {
                    line.append(tuple.get(i)).append('|');
                }
                return line.toString();
            })
            .sorted()
            .toList();
    }

    // ------------------------------------------------------------------
    // The configuration surface, which is shared with the 0.7.x line
    // ------------------------------------------------------------------

    @Test
    @Transactional
    @DisplayName("the flag is on by default")
    void flagDefaultsToOn() {
        assertThat(new RsqlContext<>(ShortcutRoot.class).useForeignKeyIdShortcut).isTrue();
        assertThat(new RsqlContext<>(ShortcutRoot.class).isForeignKeyIdShortcutEnabledFor("simpleTarget")).isTrue();
    }

    @Test
    @Transactional
    @DisplayName("a per-association override answers for its own path and leaves the rest to the default")
    void perAssociationOverridesDecideOnlyForThemselves() {
        RsqlContext<ShortcutRoot> onlyTwo = new RsqlContext<>(ShortcutRoot.class).defineEntityManager(entityManager);
        onlyTwo.useForeignKeyIdShortcut = false;
        onlyTwo.withForeignKeyIdShortcutFor("simpleTarget", "ownedOneToOne");
        assertThat(onlyTwo.isForeignKeyIdShortcutEnabledFor("simpleTarget")).isTrue();
        assertThat(onlyTwo.isForeignKeyIdShortcutEnabledFor("ownedOneToOne")).isTrue();
        assertThat(onlyTwo.isForeignKeyIdShortcutEnabledFor("customIdTarget")).isFalse();

        RsqlContext<ShortcutRoot> allButOne = new RsqlContext<>(ShortcutRoot.class).defineEntityManager(entityManager);
        allButOne.withoutForeignKeyIdShortcutFor("simpleTarget");
        assertThat(allButOne.isForeignKeyIdShortcutEnabledFor("simpleTarget")).isFalse();
        assertThat(allButOne.isForeignKeyIdShortcutEnabledFor("ownedOneToOne")).isTrue();

        // The key is the whole path the filter writes, not the last segment of it.
        RsqlContext<Product> nested = new RsqlContext<>(Product.class).defineEntityManager(entityManager);
        nested.withoutForeignKeyIdShortcutFor("parent.parent.productType");
        assertThat(nested.isForeignKeyIdShortcutEnabledFor("parent.parent.productType")).isFalse();
        assertThat(nested.isForeignKeyIdShortcutEnabledFor("productType")).isTrue();
    }

    @Test
    @Transactional
    @DisplayName("createNewInstance carries the settings over, and copies the overrides")
    void createNewInstanceCarriesTheSettings() {
        RsqlContext<ShortcutRoot> context = new RsqlContext<>(ShortcutRoot.class).defineEntityManager(entityManager);
        context.useForeignKeyIdShortcut = false;
        assertThat(context.createNewInstance().useForeignKeyIdShortcut).isFalse();

        context.useForeignKeyIdShortcut = true;
        context.withoutForeignKeyIdShortcutFor("simpleTarget");
        RsqlContext<ShortcutRoot> derived = context.createNewInstance();
        assertThat(derived.useForeignKeyIdShortcut).isTrue();
        assertThat(derived.isForeignKeyIdShortcutEnabledFor("simpleTarget")).isFalse();
        assertThat(derived.isForeignKeyIdShortcutEnabledFor("ownedOneToOne")).isTrue();

        // A copy, not the same map: one query's context must not be able to reconfigure the template.
        derived.withoutForeignKeyIdShortcutFor("ownedOneToOne");
        assertThat(context.isForeignKeyIdShortcutEnabledFor("ownedOneToOne")).isTrue();
    }

    @Test
    @Transactional
    @DisplayName("the query service exposes the same settings and hands them to every query it runs")
    void queryServiceExposesTheSettings() {
        RsqlQueryService<Product, ProductDTO, ProductRepository, ProductMapper> service = new RsqlQueryService<>(
            productRepository,
            productMapper,
            entityManager,
            Product.class
        );
        assertThat(service.getUseForeignKeyIdShortcut()).isTrue();

        service.setUseForeignKeyIdShortcut(false);
        assertThat(service.getUseForeignKeyIdShortcut()).isFalse();

        // The service derives a fresh context per query, so the setting has to survive that derivation -
        // which is the only thing that makes it reachable from findByFilter at all.
        service.withForeignKeyIdShortcutFor("productType");
        assertThat(service.findByFilter("productType.id==1")).isNotNull();

        service.setUseForeignKeyIdShortcut(true);
        assertThat(service.getUseForeignKeyIdShortcut()).isTrue();
    }
}

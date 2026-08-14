package com.nomendi6.rsql.it;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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
import rsql.RsqlQueryService;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.test.context.TestPropertySource;
import rsql.RsqlCompiler;
import rsql.where.RsqlContext;

/**
 * A selector ending in the identifier of a to-one association names a value the queried table already
 * carries in its foreign key column, so it is resolved there instead of through a LEFT JOIN.
 *
 * <p>These tests assert on the SQL Hibernate actually emits, not only on the rows it returns, because the
 * whole point of the change is the shape of the statement. Every case is run twice - once with
 * {@link RsqlContext#useForeignKeyIdShortcut} on and once off - so each test states both what changed and
 * what did not.</p>
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
    // What the shortcut is for
    // ------------------------------------------------------------------

    @Test
    @Transactional
    @DisplayName("a ManyToOne id selector reads the foreign key column and creates no join")
    void manyToOneIdNeedsNoJoin() {
        Run on = run("simpleTarget.id==1", true);
        Run off = run("simpleTarget.id==1", false);

        assertThat(on.joins()).isZero();
        assertThat(on.sql()).contains("sr1_0.simple_target_id=?").doesNotContainIgnoringCase("join");
        assertThat(off.joins()).isOne();
        assertThat(on.ids()).isEqualTo(off.ids()).containsExactly(100L);
    }

    @Test
    @Transactional
    @DisplayName("an =in= list over a ManyToOne id creates no join either")
    void manyToOneIdInListNeedsNoJoin() {
        Run on = run("simpleTarget.id=in=(1,2)", true);
        Run off = run("simpleTarget.id=in=(1,2)", false);

        assertThat(on.joins()).isZero();
        assertThat(on.sql()).contains("sr1_0.simple_target_id in (?,?)");
        assertThat(off.joins()).isOne();
        assertThat(on.ids()).isEqualTo(off.ids()).containsExactly(100L, 200L);
    }

    @Test
    @Transactional
    @DisplayName("the owning side of a OneToOne carries its foreign key too, so it also needs no join")
    void owningOneToOneIdNeedsNoJoin() {
        Run on = run("ownedOneToOne.id==1", true);
        Run off = run("ownedOneToOne.id==1", false);

        assertThat(on.joins()).isZero();
        assertThat(on.sql()).contains("sr1_0.owned_one_to_one_id=?");
        assertThat(off.joins()).isOne();
        assertThat(on.ids()).isEqualTo(off.ids()).containsExactly(100L);
    }

    @Test
    @Transactional
    @DisplayName("the shortcut finds the identifier under whatever name it has, not just 'id'")
    void identifierNeedNotBeCalledId() {
        Run on = run("customIdTarget.objectId==7", true);
        Run off = run("customIdTarget.objectId==7", false);

        assertThat(on.joins()).isZero();
        assertThat(on.sql()).contains("sr1_0.custom_id_target_id=?");
        assertThat(off.joins()).isOne();
        assertThat(on.ids()).isEqualTo(off.ids()).containsExactly(100L);
    }

    @Test
    @Transactional
    @DisplayName("only the last segment takes the shortcut: a.b.c.id still joins a and b")
    void onlyTheLastSegmentIsShortCircuited() {
        Run on = runOnProduct("parent.parent.productType.id==1", true);
        Run off = runOnProduct("parent.parent.productType.id==1", false);

        assertThat(on.joins()).isEqualTo(2);
        assertThat(off.joins()).isEqualTo(3);
        assertThat(on.ids()).isEqualTo(off.ids());
    }

    // ------------------------------------------------------------------
    // What the shortcut must leave alone
    // ------------------------------------------------------------------

    @Test
    @Transactional
    @DisplayName("a non-identifier selector still joins")
    void nonIdentifierSelectorStillJoins() {
        Run on = run("simpleTarget.name=='one'", true);
        Run off = run("simpleTarget.name=='one'", false);

        assertThat(on.joins()).isOne();
        assertThat(on.sql()).isEqualTo(off.sql());
        assertThat(on.ids()).isEqualTo(off.ids()).containsExactly(100L);
    }

    @Test
    @Transactional
    @DisplayName("a OneToMany keeps its join: the foreign key is on the other table")
    void oneToManyKeepsItsJoin() {
        Run on = run("children.id==1000", true);
        Run off = run("children.id==1000", false);

        assertThat(on.joins()).isOne();
        assertThat(on.sql()).isEqualTo(off.sql());
        assertThat(on.ids()).isEqualTo(off.ids()).containsExactly(100L);
    }

    @Test
    @Transactional
    @DisplayName("the inverse side of a OneToOne keeps its join, and keeps it a LEFT join")
    void inverseOneToOneKeepsItsLeftJoin() {
        Run on = run("inverseOneToOne.id==2000", true);
        Run off = run("inverseOneToOne.id==2000", false);

        // The persistent attribute type is ONE_TO_ONE on both sides, but only the owning side stores the
        // foreign key. Taking the shortcut here would make Hibernate reach the identifier through an
        // implicit - and therefore inner - join, silently narrowing the filter.
        assertThat(on.sql()).isEqualTo(off.sql());
        assertThat(on.sql()).containsIgnoringCase("left join");
        assertThat(on.ids()).isEqualTo(off.ids()).containsExactly(100L);
    }

    @Test
    @Transactional
    @DisplayName("a composite identifier is left alone: it is not one foreign key column")
    void compositeIdentifierIsLeftAlone() {
        Run on = run("embeddedIdTarget.id.partA==3", true);
        Run off = run("embeddedIdTarget.id.partA==3", false);

        assertThat(on.sql()).isEqualTo(off.sql());
        assertThat(on.ids()).isEqualTo(off.ids()).containsExactly(100L);

        // And comparing the composite identifier as a whole fails the same way it did before.
        assertThatThrownBy(() -> run("embeddedIdTarget.id==3", true)).isInstanceOf(Exception.class);
        assertThatThrownBy(() -> run("embeddedIdTarget.id==3", false)).isInstanceOf(Exception.class);
    }

    @Test
    @Transactional
    @DisplayName("a target under @SQLRestriction keeps its join, so the restriction still applies")
    void restrictedTargetKeepsItsJoin() {
        Run on = run("restrictedTarget.id==11", true);
        Run off = run("restrictedTarget.id==11", false);

        // Root 200 points at target 11, which is archived. The restriction lives on the join, so without
        // the join the row would start matching. It must not.
        assertThat(on.sql()).isEqualTo(off.sql());
        assertThat(on.sql()).contains("archived = false");
        assertThat(on.ids()).isEqualTo(off.ids()).isEmpty();

        Run live = run("restrictedTarget.id==10", true);
        assertThat(live.ids()).containsExactly(100L);
    }

    // ------------------------------------------------------------------
    // The order dependence the change introduces
    // ------------------------------------------------------------------

    @Test
    @Transactional
    @DisplayName("clause order decides which column the identifier is read from, but not the rows")
    void clauseOrderChangesTheSqlButNotTheResult() {
        Run idFirst = run("simpleTarget.id==1 and simpleTarget.name=='one'", true);
        Run nameFirst = run("simpleTarget.name=='one' and simpleTarget.id==1", true);

        // Same join count either way: the shortcut never adds a join, and when one already exists for the
        // association the cached join is used instead of the shortcut.
        assertThat(idFirst.joins()).isEqualTo(nameFirst.joins()).isEqualTo(1);
        assertThat(idFirst.ids()).isEqualTo(nameFirst.ids()).containsExactly(100L);

        // The SQL genuinely differs: the id is read off the foreign key when it is resolved first, and off
        // the join when the join is already there.
        assertThat(idFirst.sql()).contains("sr1_0.simple_target_id=?");
        assertThat(nameFirst.sql()).doesNotContain("sr1_0.simple_target_id=?");
        assertThat(idFirst.sql()).isNotEqualTo(nameFirst.sql());
    }

    @Test
    @Transactional
    @DisplayName("the shortcut never increases the join count")
    void joinCountNeverGrows() {
        String[] filters = {
            "simpleTarget.id==1",
            "simpleTarget.id=in=(1,2)",
            "simpleTarget.name=='one'",
            "children.id==1000",
            "inverseOneToOne.id==2000",
            "restrictedTarget.id==10",
            "simpleTarget.id==1 and simpleTarget.name=='one'",
            "simpleTarget.name=='one' and simpleTarget.id==1",
            "simpleTarget.id==1 or simpleTarget.name=='one'",
        };
        for (String filter : filters) {
            Run on = run(filter, true);
            Run off = run(filter, false);
            assertThat(on.joins()).as("join count for %s", filter).isLessThanOrEqualTo(off.joins());
            assertThat(on.ids()).as("rows for %s", filter).isEqualTo(off.ids());
        }
    }

    // ------------------------------------------------------------------
    // The flag itself
    // ------------------------------------------------------------------

    @Test
    @Transactional
    @DisplayName("the flag is on by default")
    void flagDefaultsToOn() {
        assertThat(new RsqlContext<>(ShortcutRoot.class).useForeignKeyIdShortcut).isTrue();
    }

    @Test
    @Transactional
    @DisplayName("turning the flag off restores the join for every shape")
    void flagOffRestoresTheJoin() {
        assertThat(run("simpleTarget.id==1", false).joins()).isOne();
        assertThat(run("ownedOneToOne.id==1", false).joins()).isOne();
        assertThat(run("customIdTarget.objectId==7", false).joins()).isOne();
        assertThat(runOnProduct("parent.parent.productType.id==1", false).joins()).isEqualTo(3);
    }

    @Test
    @Transactional
    @DisplayName("RsqlQueryService exposes the flag, and it reaches the queries it runs")
    void queryServiceCanTurnTheShortcutOff() {
        RsqlQueryService<Product, ProductDTO, ProductRepository, ProductMapper> service = new RsqlQueryService<>(
            productRepository,
            productMapper,
            entityManager,
            Product.class
        );
        assertThat(service.getUseForeignKeyIdShortcut()).isTrue();

        // The service hands each query a context derived from its own, so the setting has to survive that
        // derivation - which is the only thing that makes the escape hatch reachable from findByFilter.
        service.setUseForeignKeyIdShortcut(false);
        assertThat(service.getUseForeignKeyIdShortcut()).isFalse();

        SqlStatementCapture.reset();
        service.findByFilter("productType.id==1");
        assertThat(SqlStatementCapture.countJoins(SqlStatementCapture.firstStatement())).isOne();

        service.setUseForeignKeyIdShortcut(true);
        SqlStatementCapture.reset();
        service.findByFilter("productType.id==1");
        assertThat(SqlStatementCapture.countJoins(SqlStatementCapture.firstStatement())).isZero();
    }

    // ------------------------------------------------------------------
    // What happens when SELECT already needs the association
    // ------------------------------------------------------------------

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

    @Test
    @Transactional
    @DisplayName("only the named associations read the foreign key; the rest still join")
    void theShortcutCanBeAllowedPerAssociation() {
        // The shape the OrgSec filter wants: two owner associations resolved off the base entity's own
        // columns, everything else left as it was.
        RsqlContext<ShortcutRoot> context = new RsqlContext<>(ShortcutRoot.class).defineEntityManager(entityManager);
        context.useForeignKeyIdShortcut = false;
        context.withForeignKeyIdShortcutFor("simpleTarget", "ownedOneToOne");

        assertThat(joinsFor("simpleTarget.id==1", context)).isZero();
        assertThat(joinsFor("ownedOneToOne.id==1", context)).isZero();
        assertThat(joinsFor("customIdTarget.objectId==7", context)).isOne();
        assertThat(joinsFor("restrictedTarget.id==10", context)).isOne();
    }

    @Test
    @Transactional
    @DisplayName("a single association can be held back while the rest take the shortcut")
    void theShortcutCanBeDeniedPerAssociation() {
        RsqlContext<ShortcutRoot> context = new RsqlContext<>(ShortcutRoot.class).defineEntityManager(entityManager);
        context.withoutForeignKeyIdShortcutFor("simpleTarget");

        assertThat(joinsFor("simpleTarget.id==1", context)).isOne();
        assertThat(joinsFor("ownedOneToOne.id==1", context)).isZero();
        assertThat(joinsFor("customIdTarget.objectId==7", context)).isZero();
    }

    @Test
    @Transactional
    @DisplayName("an override names the whole path, so it reaches a nested association too")
    void anOverrideNamesTheWholePath() {
        RsqlContext<Product> shortcut = new RsqlContext<>(Product.class).defineEntityManager(entityManager);
        assertThat(joinsForProduct("parent.parent.productType.id==1", shortcut)).isEqualTo(2);

        RsqlContext<Product> held = new RsqlContext<>(Product.class).defineEntityManager(entityManager);
        held.withoutForeignKeyIdShortcutFor("parent.parent.productType");
        assertThat(joinsForProduct("parent.parent.productType.id==1", held)).isEqualTo(3);

        // Naming only the last segment does not match: the key is the path the filter writes.
        RsqlContext<Product> mismatched = new RsqlContext<>(Product.class).defineEntityManager(entityManager);
        mismatched.withoutForeignKeyIdShortcutFor("productType");
        assertThat(joinsForProduct("parent.parent.productType.id==1", mismatched)).isEqualTo(2);
    }

    @Test
    @Transactional
    @DisplayName("the query service takes the same per-association configuration")
    void queryServiceTakesPerAssociationConfiguration() {
        RsqlQueryService<Product, ProductDTO, ProductRepository, ProductMapper> service = new RsqlQueryService<>(
            productRepository,
            productMapper,
            entityManager,
            Product.class
        );
        service.setUseForeignKeyIdShortcut(false);
        service.withForeignKeyIdShortcutFor("productType");

        SqlStatementCapture.reset();
        service.findByFilter("productType.id==1");
        assertThat(SqlStatementCapture.countJoins(SqlStatementCapture.firstStatement())).isZero();

        SqlStatementCapture.reset();
        service.findByFilter("parent.id==1");
        assertThat(SqlStatementCapture.countJoins(SqlStatementCapture.firstStatement())).isOne();
    }

    @Test
    @Transactional
    @DisplayName("createNewInstance carries the per-association overrides over, and copies them")
    void createNewInstanceCarriesTheOverrides() {
        RsqlContext<ShortcutRoot> context = new RsqlContext<>(ShortcutRoot.class).defineEntityManager(entityManager);
        context.withoutForeignKeyIdShortcutFor("simpleTarget");

        RsqlContext<ShortcutRoot> derived = context.createNewInstance();
        assertThat(derived.isForeignKeyIdShortcutEnabledFor("simpleTarget")).isFalse();
        assertThat(derived.isForeignKeyIdShortcutEnabledFor("ownedOneToOne")).isTrue();

        // A copy, not the same map: one query's context must not be able to reconfigure the template.
        derived.withoutForeignKeyIdShortcutFor("ownedOneToOne");
        assertThat(context.isForeignKeyIdShortcutEnabledFor("ownedOneToOne")).isTrue();
    }

    @Test
    @Transactional
    @DisplayName("the count query of a paged result resolves selectors the same way as the page")
    void countQueryFollowsTheSameSetting() {
        RsqlQueryService<Product, ProductDTO, ProductRepository, ProductMapper> service = new RsqlQueryService<>(
            productRepository,
            productMapper,
            entityManager,
            Product.class
        );
        ProductType type = productTypeRepository.findAll().stream().findFirst().orElseThrow();
        String filter = "productType.id==" + type.getId();

        // A paged aggregate builds a second context for its count, with its own root and joins map. If that
        // context kept the default instead of the setting, the count would be counting a differently
        // resolved filter than the page shows.
        service.setUseForeignKeyIdShortcut(false);
        SqlStatementCapture.reset();
        service.getAggregateResultAsPageWithExpressions("code, COUNT(*):total", filter, null, PageRequest.of(0, 1));
        List<String> joined = countStatements();
        assertThat(joined).as("the paged aggregate issues a count").isNotEmpty();
        assertThat(joined).allSatisfy(sql -> assertThat(SqlStatementCapture.countJoins(sql)).isOne());

        service.setUseForeignKeyIdShortcut(true);
        SqlStatementCapture.reset();
        service.getAggregateResultAsPageWithExpressions("code, COUNT(*):total", filter, null, PageRequest.of(0, 1));
        assertThat(countStatements()).allSatisfy(sql -> assertThat(SqlStatementCapture.countJoins(sql)).isZero());
    }

    /**
     * A SELECT clause and how many joins it needs for its own sake, independently of the filter.
     *
     * @param select         the SELECT string
     * @param joinsOfItsOwn  joins the SELECT itself requires - 0 when it names no field of the association
     */
    private record SelectCase(String select, int joinsOfItsOwn) {}

    @Test
    @Transactional
    @DisplayName("filtering on relation.id: what the SELECT clause needs decides whether the join survives")
    void filterOnRelationIdAcrossSelectShapes() {
        RsqlQueryService<Product, ProductDTO, ProductRepository, ProductMapper> service = new RsqlQueryService<>(
            productRepository,
            productMapper,
            entityManager,
            Product.class
        );
        ProductType type = productTypeRepository.findAll().stream().findFirst().orElseThrow();
        String filter = "productType.id==" + type.getId();

        List<SelectCase> cases = List.of(
            // SELECT names no field of the association: nothing else needs the join, so it disappears.
            new SelectCase("code, COUNT(*):total", 0),
            new SelectCase("code, name, COUNT(*):total", 0),
            // SELECT names a field of the association other than the identifier: it needs the join for
            // itself, so the join stays and the filter is applied to the foreign key column instead.
            new SelectCase("productType.name:tn, COUNT(*):total", 1),
            new SelectCase("productType.code:tc, productType.name:tn, COUNT(*):total", 1),
            // SELECT names the identifier itself. The SELECT path has no shortcut of its own, so it joins
            // and groups by the joined column - the filter is the only thing that changes.
            new SelectCase("productType.id:tid, COUNT(*):total", 1),
            new SelectCase("productType.name:tn, productType.id:tid, COUNT(*):total", 1)
        );

        for (SelectCase testCase : cases) {
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

            String because = "SELECT [" + testCase.select() + "]";

            // The rows are what matters, and they never move.
            assertThat(rowsOf(withShortcut)).as("rows, %s", because).isEqualTo(rowsOf(withoutShortcut)).isNotEmpty();

            // The join survives exactly when the SELECT needs it, and the filter never adds one.
            assertThat(SqlStatementCapture.countJoins(shortcutSql))
                .as("joins with the shortcut, %s", because)
                .isEqualTo(testCase.joinsOfItsOwn());
            assertThat(SqlStatementCapture.countJoins(joinedSql)).as("joins without the shortcut, %s", because).isOne();

            // Wherever the join survives it is the filter, not the join, that moved: the predicate reads the
            // base table's foreign key column. A WHERE condition never has to appear in the GROUP BY, so
            // grouping by a joined column alongside it stays valid.
            assertThat(shortcutSql).as("predicate, %s", because).contains("where p1_0.product_type_id=?").contains("group by");
            assertThat(joinedSql).as("predicate without the shortcut, %s", because).contains("where pt1_0.id=?");
            if (testCase.joinsOfItsOwn() > 0) {
                assertThat(shortcutSql).as("the SELECT keeps its own join, %s", because).contains("left join product_type");
            }
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

        // getLOVWithSelect goes through a different selection visitor than the aggregate methods, so the
        // interaction with the shared joins map is worth stating separately rather than assuming.
        record Case(String select, int joinsWithShortcut) {}
        List<Case> cases = List.of(
            new Case("id, code, name", 0),
            new Case("id, code, productType.name", 1)
        );

        for (Case testCase : cases) {
            service.setUseForeignKeyIdShortcut(true);
            SqlStatementCapture.reset();
            List<?> withShortcut = service.getLOVWithSelect(testCase.select(), filter, PageRequest.of(0, 10));
            String shortcutSql = SqlStatementCapture.firstStatement();

            service.setUseForeignKeyIdShortcut(false);
            SqlStatementCapture.reset();
            List<?> withoutShortcut = service.getLOVWithSelect(testCase.select(), filter, PageRequest.of(0, 10));
            String joinedSql = SqlStatementCapture.firstStatement();

            String because = "SELECT [" + testCase.select() + "]";
            assertThat(withShortcut).as("rows, %s", because).hasSameSizeAs(withoutShortcut).isNotEmpty();
            assertThat(SqlStatementCapture.countJoins(shortcutSql))
                .as("joins with the shortcut, %s", because)
                .isEqualTo(testCase.joinsWithShortcut());
            assertThat(SqlStatementCapture.countJoins(joinedSql))
                .as("joins without the shortcut, %s", because)
                .isGreaterThanOrEqualTo(testCase.joinsWithShortcut());
            assertThat(shortcutSql).as("predicate, %s", because).contains("p1_0.product_type_id=?");
        }
    }

    private static List<String> countStatements() {
        return SqlStatementCapture.statements().stream().filter(sql -> sql.toLowerCase().contains("count(")).toList();
    }

    private int joinsFor(String filter, RsqlContext<ShortcutRoot> context) {
        Specification<ShortcutRoot> specification = compiler.compileToSpecification(filter, context.createNewInstance());
        SqlStatementCapture.reset();
        shortcutRootRepository.findAll(specification);
        return SqlStatementCapture.countJoins(SqlStatementCapture.firstStatement());
    }

    private int joinsForProduct(String filter, RsqlContext<Product> context) {
        Specification<Product> specification = productCompiler.compileToSpecification(filter, context.createNewInstance());
        SqlStatementCapture.reset();
        productRepository.findAll(specification);
        return SqlStatementCapture.countJoins(SqlStatementCapture.firstStatement());
    }

    @Test
    @Transactional
    @DisplayName("createNewInstance carries the flag over")
    void createNewInstanceCarriesTheFlag() {
        RsqlContext<ShortcutRoot> context = new RsqlContext<>(ShortcutRoot.class).defineEntityManager(entityManager);

        context.useForeignKeyIdShortcut = false;
        assertThat(context.createNewInstance().useForeignKeyIdShortcut).isFalse();

        context.useForeignKeyIdShortcut = true;
        assertThat(context.createNewInstance().useForeignKeyIdShortcut).isTrue();
    }

    @Test
    @Transactional
    @DisplayName("a filter that finds rows through the shortcut finds the same rows as before")
    void resultsAreUnchangedAcrossTheWholeMatrix() {
        ProductType type = productTypeRepository.findAll().stream().findFirst().orElse(null);
        if (type != null) {
            String filter = "productType.id==" + type.getId();
            assertThat(runOnProduct(filter, true).ids()).isEqualTo(runOnProduct(filter, false).ids());
        }
        assertThat(run("simpleTarget.id==99", true).ids()).isEqualTo(run("simpleTarget.id==99", false).ids()).isEmpty();
        assertThat(run("simpleTarget.id!=1", true).ids()).isEqualTo(run("simpleTarget.id!=1", false).ids());
        assertThat(run("simpleTarget.id=nin=(1)", true).ids()).isEqualTo(run("simpleTarget.id=nin=(1)", false).ids());
    }
}

package com.nomendi6.rsql.it;

import static org.assertj.core.api.Assertions.assertThat;

import com.nomendi6.rsql.it.config.IntegrationTest;
import com.nomendi6.rsql.it.config.SqlStatementCapture;
import com.nomendi6.rsql.it.domain.idshortcut.ShortcutCat;
import com.nomendi6.rsql.it.domain.idshortcut.ShortcutDog;
import com.nomendi6.rsql.it.domain.idshortcut.ShortcutRoot;
import com.nomendi6.rsql.it.repository.ShortcutRootRepository;
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
import rsql.where.RsqlContext;

/**
 * An association typed to one subtype of an inheritance hierarchy keeps its join.
 *
 * <p>Joining such an entity is not merely a lookup by identifier: the join carries the restriction that
 * selects the subtype, a discriminator predicate here. The foreign key column carries no such restriction -
 * it is constrained to the hierarchy's shared table, so it can hold the identifier of a row of a different
 * subtype. Reading the identifier there would match rows the join excludes.</p>
 *
 * <p>The data these tests set up is exactly that: a foreign key typed to {@code ShortcutCat} holding the
 * identifier of a {@code ShortcutDog}. It is written natively, the way a migration or another application
 * would write it, because no mapping-level API would let it happen. Every referential constraint is
 * satisfied - the row exists in the shared table - which is what makes this different from the dangling
 * foreign key the documentation already calls out.</p>
 */
@IntegrationTest
@TestPropertySource(
    properties = {
        "spring.jpa.properties.hibernate.session_factory.statement_inspector=com.nomendi6.rsql.it.config.SqlStatementCapture",
    }
)
public class ForeignKeyIdShortcutInheritanceIT {

    private static final long CAT_ID = 901L;
    private static final long DOG_ID = 900L;
    private static final long POINTS_AT_CAT = 1L;
    private static final long POINTS_AT_DOG = 2L;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private ShortcutRootRepository shortcutRootRepository;

    private final RsqlCompiler<ShortcutRoot> compiler = new RsqlCompiler<>();

    @BeforeEach
    @Transactional
    void seed() {
        shortcutRootRepository.deleteAll();
        entityManager.createQuery("delete from ShortcutAnimal").executeUpdate();
        entityManager.merge(new ShortcutCat(CAT_ID, "cat"));
        entityManager.merge(new ShortcutDog(DOG_ID, "dog"));
        entityManager.merge(new ShortcutRoot(POINTS_AT_CAT, "points at cat"));
        entityManager.merge(new ShortcutRoot(POINTS_AT_DOG, "points at dog"));
        entityManager.flush();

        entityManager
            .createNativeQuery("update shortcut_root set cat_target_id = " + CAT_ID + " where id = " + POINTS_AT_CAT)
            .executeUpdate();
        entityManager
            .createNativeQuery("update shortcut_root set cat_target_id = " + DOG_ID + " where id = " + POINTS_AT_DOG)
            .executeUpdate();
        entityManager.flush();
        entityManager.clear();
    }

    private List<Long> ids(String filter, boolean shortcut) {
        RsqlContext<ShortcutRoot> context = new RsqlContext<>(ShortcutRoot.class).defineEntityManager(entityManager);
        context.useForeignKeyIdShortcut = shortcut;
        Specification<ShortcutRoot> specification = compiler.compileToSpecification(filter, context);
        SqlStatementCapture.reset();
        return shortcutRootRepository.findAll(specification).stream().map(ShortcutRoot::getId).sorted().toList();
    }

    /** Run a filter both ways and assert the shortcut changed nothing about it. */
    private void unchangedBySetting(String filter, Long... expected) {
        List<Long> on = ids(filter, true);
        String onSql = SqlStatementCapture.firstStatement();
        List<Long> off = ids(filter, false);
        String offSql = SqlStatementCapture.firstStatement();

        assertThat(on).as("rows for %s", filter).isEqualTo(off).containsExactly(expected);
        assertThat(onSql).as("SQL for %s", filter).isEqualTo(offSql);
        assertThat(SqlStatementCapture.countJoins(onSql)).as("the join is kept for %s", filter).isOne();
        assertThat(onSql).as("the subtype restriction survives for %s", filter).contains("kind=");
    }

    @Test
    @Transactional
    @DisplayName("a foreign key holding the identifier of another subtype does not start matching")
    void wrongSubtypeDoesNotMatch() {
        // Without the join this reads cat_target_id=900 and finds the row pointing at the dog.
        unchangedBySetting("catTarget.id==" + DOG_ID);
    }

    @Test
    @Transactional
    @DisplayName("the right subtype still matches")
    void rightSubtypeStillMatches() {
        unchangedBySetting("catTarget.id==" + CAT_ID, POINTS_AT_CAT);
    }

    @Test
    @Transactional
    @DisplayName("a negated comparison is not widened")
    void negatedComparisonIsNotWidened() {
        unchangedBySetting("catTarget.id!=" + CAT_ID);
    }

    @Test
    @Transactional
    @DisplayName("an =in= list spanning two subtypes still selects only the right one")
    void inListSpanningSubtypes() {
        unchangedBySetting("catTarget.id=in=(" + DOG_ID + "," + CAT_ID + ")", POINTS_AT_CAT);
    }

    @Test
    @Transactional
    @DisplayName("==null still means 'no target of this subtype', not 'no foreign key'")
    void isNullKeepsItsMeaning() {
        // The row pointing at the dog has a foreign key, but no ShortcutCat, so it belongs here.
        unchangedBySetting("catTarget.id==null", POINTS_AT_DOG);
    }

    @Test
    @Transactional
    @DisplayName("a non-identifier selector on a subtype target is unaffected")
    void nonIdentifierSelectorIsUnaffected() {
        unchangedBySetting("catTarget.name=='dog'");
    }
}

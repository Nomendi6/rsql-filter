package com.nomendi6.rsql.it;

import static org.assertj.core.api.Assertions.assertThat;

import com.nomendi6.rsql.it.config.HibernateLine;
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
 * An association typed to one subtype of an inheritance hierarchy, where the foreign key holds the identifier
 * of a row of a different subtype.
 *
 * <p>Joining such an entity is not merely a lookup by identifier: the join carries the restriction that
 * selects the subtype. The foreign key column carries no such restriction - it is constrained only to the
 * hierarchy's shared table - so reading the identifier there matches rows the join would exclude.</p>
 *
 * <p>The library declines the shortcut for a subtype target, so the setting never changes these rows. What
 * Hibernate does with the join does. <strong>Hibernate 6.5 drops it by itself</strong> - a LEFT JOIN whose only
 * use is the target's identifier, even one the library asks for explicitly - and the subtype restriction goes
 * with it, so {@code catTarget.id==<a dog>} matches there. From 6.6 on Hibernate keeps the join, as Hibernate 7
 * does, and the restriction survives. The tests hold the 6.6 rows as the right ones and pin the 6.5 rows as the
 * known Hibernate 6.5 behaviour - see {@link HibernateLine}.</p>
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

        // No mapping-level API would let a ShortcutCat reference hold a ShortcutDog identifier, so this is
        // written natively - the way a migration or another application would write it. Every referential
        // constraint is satisfied: the row does exist in the hierarchy's shared table.
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

    /**
     * Run a filter both ways, assert the setting changed nothing about it, and assert the rows the Hibernate at
     * hand gives: {@code before66} on Hibernate 6.5, which drops the join and the subtype restriction with it,
     * {@code from66} from 6.6 on, which keeps both.
     */
    private void unchangedBySetting(String filter, List<Long> before66, List<Long> from66) {
        List<Long> on = ids(filter, true);
        String onSql = SqlStatementCapture.firstStatement();
        List<Long> off = ids(filter, false);
        String offSql = SqlStatementCapture.firstStatement();

        assertThat(on).as("rows for %s", filter).isEqualTo(off);
        assertThat(onSql).as("SQL for %s", filter).isEqualTo(offSql);
        if (HibernateLine.dropsIdentifierOnlyJoins()) {
            assertThat(on).as("rows for %s on Hibernate 6.5", filter).isEqualTo(before66);
        } else {
            assertThat(on).as("rows for %s", filter).isEqualTo(from66);
            assertThat(SqlStatementCapture.countJoins(onSql)).as("the join is kept for %s", filter).isOne();
            assertThat(onSql).as("the subtype restriction survives for %s", filter).contains("kind=");
        }
    }

    @Test
    @Transactional
    @DisplayName("a foreign key holding the identifier of another subtype does not match from Hibernate 6.6 on")
    void wrongSubtype() {
        // Without the join this reads cat_target_id=900 and finds the row pointing at the dog, which is what
        // Hibernate 6.5 does whatever the setting.
        unchangedBySetting("catTarget.id==" + DOG_ID, List.of(POINTS_AT_DOG), List.of());
    }

    @Test
    @Transactional
    @DisplayName("the right subtype matches on every Hibernate")
    void rightSubtypeStillMatches() {
        unchangedBySetting("catTarget.id==" + CAT_ID, List.of(POINTS_AT_CAT), List.of(POINTS_AT_CAT));
    }

    @Test
    @Transactional
    @DisplayName("a negated comparison is not widened from Hibernate 6.6 on")
    void negatedComparison() {
        unchangedBySetting("catTarget.id!=" + CAT_ID, List.of(POINTS_AT_DOG), List.of());
    }

    @Test
    @Transactional
    @DisplayName("an =in= list spanning two subtypes selects only the right one from Hibernate 6.6 on")
    void inListSpanningSubtypes() {
        unchangedBySetting("catTarget.id=in=(" + DOG_ID + "," + CAT_ID + ")", List.of(POINTS_AT_CAT, POINTS_AT_DOG), List.of(POINTS_AT_CAT));
    }

    @Test
    @Transactional
    @DisplayName("==null means 'no target of this subtype' from Hibernate 6.6 on, 'no foreign key' on 6.5")
    void isNull() {
        // The row pointing at the dog has a foreign key, but no ShortcutCat.
        unchangedBySetting("catTarget.id==null", List.of(), List.of(POINTS_AT_DOG));
    }

    @Test
    @Transactional
    @DisplayName("a non-identifier selector keeps its join, and with it the subtype restriction, on every Hibernate")
    void nonIdentifierSelectorKeepsTheRestriction() {
        List<Long> on = ids("catTarget.name=='dog'", true);
        String sql = SqlStatementCapture.firstStatement();

        assertThat(on).isEqualTo(ids("catTarget.name=='dog'", false)).isEmpty();
        assertThat(SqlStatementCapture.countJoins(sql)).isOne();
        assertThat(sql).contains("kind=");
    }
}

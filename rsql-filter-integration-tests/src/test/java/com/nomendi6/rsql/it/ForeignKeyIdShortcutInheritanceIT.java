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
 * An association typed to one subtype of an inheritance hierarchy, where the foreign key holds the identifier
 * of a row of a different subtype.
 *
 * <p>Joining such an entity is not merely a lookup by identifier: the join carries the restriction that
 * selects the subtype. The foreign key column carries no such restriction - it is constrained only to the
 * hierarchy's shared table - so reading the identifier there matches rows the join would exclude.</p>
 *
 * <p><strong>On this line Hibernate does that by itself, and has always done so.</strong> Hibernate 6.5 drops
 * a LEFT JOIN whose only use is the target's identifier even when the library asks for one explicitly, and
 * the subtype restriction goes with it. So {@code catTarget.id==<a dog>} matches here, in 0.6.21 as much as in
 * 0.6.22, whatever {@link RsqlContext#useForeignKeyIdShortcut} is set to. These tests pin that down as the
 * known difference between the lines rather than leave it to be discovered.</p>
 *
 * <p>The 0.7.x line does not behave this way: Hibernate 7 honours the explicit join, and the library declines
 * the shortcut for a subtype target, so the restriction survives there.</p>
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
     * Run a filter both ways and assert the setting changed nothing about it - which is what this release
     * guarantees on this line.
     *
     * @return the SQL, so a test can go on to state the shape Hibernate chose
     */
    private String unchangedBySetting(String filter, Long... expected) {
        List<Long> on = ids(filter, true);
        String onSql = SqlStatementCapture.firstStatement();
        List<Long> off = ids(filter, false);
        String offSql = SqlStatementCapture.firstStatement();

        assertThat(on).as("rows for %s", filter).isEqualTo(off).containsExactly(expected);
        assertThat(onSql).as("SQL for %s", filter).isEqualTo(offSql);
        return onSql;
    }

    @Test
    @Transactional
    @DisplayName("Hibernate drops the join and the subtype restriction with it, whatever the setting")
    void hibernateDropsTheSubtypeRestrictionOnThisLine() {
        // The row pointing at the dog matches a filter on a Cat-typed association, because no join is left to
        // carry kind='CAT'. This is Hibernate's own optimisation, not the library's shortcut: it happens with
        // the setting off, which is the code path 0.6.21 took.
        String sql = unchangedBySetting("catTarget.id==" + DOG_ID, POINTS_AT_DOG);

        assertThat(SqlStatementCapture.countJoins(sql)).isZero();
        assertThat(sql).contains("sr1_0.cat_target_id=?").doesNotContain("kind=");
    }

    @Test
    @Transactional
    @DisplayName("the right subtype matches, as it always did")
    void rightSubtypeStillMatches() {
        unchangedBySetting("catTarget.id==" + CAT_ID, POINTS_AT_CAT);
    }

    @Test
    @Transactional
    @DisplayName("a negated comparison behaves the same either way")
    void negatedComparisonIsUnchanged() {
        unchangedBySetting("catTarget.id!=" + CAT_ID, POINTS_AT_DOG);
    }

    @Test
    @Transactional
    @DisplayName("an =in= list spanning two subtypes behaves the same either way")
    void inListSpanningSubtypesIsUnchanged() {
        unchangedBySetting("catTarget.id=in=(" + DOG_ID + "," + CAT_ID + ")", POINTS_AT_CAT, POINTS_AT_DOG);
    }

    @Test
    @Transactional
    @DisplayName("==null reads the foreign key column, so a wrong-subtype reference is not null")
    void isNullReadsTheForeignKey() {
        unchangedBySetting("catTarget.id==null");
    }

    @Test
    @Transactional
    @DisplayName("a non-identifier selector keeps its join, and with it the subtype restriction")
    void nonIdentifierSelectorKeepsTheRestriction() {
        String sql = unchangedBySetting("catTarget.name=='dog'");

        assertThat(SqlStatementCapture.countJoins(sql)).isOne();
        assertThat(sql).contains("kind=");
    }
}

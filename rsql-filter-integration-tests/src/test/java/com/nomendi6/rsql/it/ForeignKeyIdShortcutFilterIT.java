package com.nomendi6.rsql.it;

import static org.assertj.core.api.Assertions.assertThat;

import com.nomendi6.rsql.it.config.IntegrationTest;
import com.nomendi6.rsql.it.config.SqlStatementCapture;
import com.nomendi6.rsql.it.domain.idshortcut.ShortcutFilteredTarget;
import com.nomendi6.rsql.it.domain.idshortcut.ShortcutRoot;
import com.nomendi6.rsql.it.repository.ShortcutRootRepository;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import java.util.List;
import org.hibernate.Session;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.test.context.TestPropertySource;
import rsql.RsqlCompiler;
import rsql.where.RsqlContext;

/**
 * A target scoped by an enabled {@code @Filter} keeps its join.
 *
 * <p>A {@code @Filter} declared with {@code applyToLoadByKey = true} is the idiomatic way to express tenant or
 * row-level scoping, and Hibernate renders it as a condition on the join to that entity. Reading the
 * identifier off the foreign key column instead leaves nowhere for that condition to attach, so a row scoped
 * to another tenant would start matching - the shortcut would widen exactly the kind of authorization filter
 * it was asked for.</p>
 *
 * <p>Hibernate's own {@code isFkOptimizationAllowed()} does not consider filters, so this has to be declined
 * separately.</p>
 */
@IntegrationTest
@TestPropertySource(
    properties = {
        "spring.jpa.properties.hibernate.session_factory.statement_inspector=com.nomendi6.rsql.it.config.SqlStatementCapture",
    }
)
public class ForeignKeyIdShortcutFilterIT {

    private static final long OURS = 10L;
    private static final long THEIRS = 11L;
    private static final long OUR_TENANT = 1L;
    private static final long THEIR_TENANT = 2L;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private ShortcutRootRepository shortcutRootRepository;

    private final RsqlCompiler<ShortcutRoot> compiler = new RsqlCompiler<>();

    @BeforeEach
    @Transactional
    void seed() {
        shortcutRootRepository.deleteAll();
        entityManager.createQuery("delete from ShortcutFilteredTarget").executeUpdate();

        ShortcutFilteredTarget ours = entityManager.merge(new ShortcutFilteredTarget(OURS, "ours", OUR_TENANT));
        ShortcutFilteredTarget theirs = entityManager.merge(new ShortcutFilteredTarget(THEIRS, "theirs", THEIR_TENANT));

        ShortcutRoot pointsAtOurs = new ShortcutRoot(1L, "points at ours");
        pointsAtOurs.setFilteredTarget(ours);
        ShortcutRoot pointsAtTheirs = new ShortcutRoot(2L, "points at theirs");
        pointsAtTheirs.setFilteredTarget(theirs);
        entityManager.merge(pointsAtOurs);
        entityManager.merge(pointsAtTheirs);

        entityManager.flush();
        entityManager.clear();
    }

    private List<Long> ids(String filter, boolean shortcut) {
        entityManager.unwrap(Session.class).enableFilter("tenantScope").setParameter("tenant", OUR_TENANT);
        RsqlContext<ShortcutRoot> context = new RsqlContext<>(ShortcutRoot.class).defineEntityManager(entityManager);
        context.useForeignKeyIdShortcut = shortcut;
        Specification<ShortcutRoot> specification = compiler.compileToSpecification(filter, context);
        SqlStatementCapture.reset();
        return shortcutRootRepository.findAll(specification).stream().map(ShortcutRoot::getId).sorted().toList();
    }

    @Test
    @Transactional
    @DisplayName("a target scoped to another tenant does not start matching")
    void scopedOutTargetDoesNotMatch() {
        List<Long> on = ids("filteredTarget.id==" + THEIRS, true);
        String onSql = SqlStatementCapture.firstStatement();
        List<Long> off = ids("filteredTarget.id==" + THEIRS, false);
        String offSql = SqlStatementCapture.firstStatement();

        // The row pointing at the other tenant's target has that foreign key value, so without the join it
        // would match. The filter condition lives on the join, so the join has to stay.
        assertThat(on).isEqualTo(off).isEmpty();
        assertThat(onSql).isEqualTo(offSql);
        assertThat(SqlStatementCapture.countJoins(onSql)).isOne();
        assertThat(onSql).contains("tenant_id = ?");
    }

    @Test
    @Transactional
    @DisplayName("a target inside the tenant scope still matches")
    void inScopeTargetStillMatches() {
        List<Long> on = ids("filteredTarget.id==" + OURS, true);
        List<Long> off = ids("filteredTarget.id==" + OURS, false);

        assertThat(on).isEqualTo(off).containsExactly(1L);
    }
}

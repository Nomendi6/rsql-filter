package com.nomendi6.rsql.it;

import com.nomendi6.rsql.it.config.IntegrationTest;
import com.nomendi6.rsql.it.domain.AppObject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Does the {@code escape '\'} clause that every generated LIKE predicate carries since 0.6.20 actually
 * survive JPQL parsing, and does it match what it claims to match?
 */
@IntegrationTest
@Transactional
public class EscapeClauseJpqlIT {

    @Autowired
    private EntityManager entityManager;

    /** The exact shape the library emits: escape character written as a single backslash. */
    @Test
    void escapeClauseWithSingleBackslashIsValidJpql() {
        TypedQuery<AppObject> q = entityManager.createQuery(
            "select a0 from AppObject a0 where lower(a0.name) like :p1 escape '\\'", AppObject.class);
        q.setParameter("p1", "a%");
        assertThat(q.getResultList()).isNotNull();      // executes, so it also reaches the database
    }

    /** A doubled backslash is NOT accepted - Hibernate's QuotingHelper does not unescape it. */
    @Test
    void escapeClauseWithDoubledBackslashIsRejected() {
        assertThatThrownBy(() -> entityManager.createQuery(
            "select a0 from AppObject a0 where lower(a0.name) like :p1 escape '\\\\'", AppObject.class))
            .isInstanceOf(Exception.class);
    }

    /** The pattern doubles backslashes, the clause declares '\' as escape - a literal backslash matches. */
    @Test
    void doubledBackslashInPatternMatchesOneLiteralBackslash() {
        AppObject withBackslash = new AppObject();
        withBackslash.setName("C:\\temp");
        withBackslash.setCode("ESC-1");
        entityManager.persist(withBackslash);

        AppObject withoutBackslash = new AppObject();
        withoutBackslash.setName("C:temp");
        withoutBackslash.setCode("ESC-2");
        entityManager.persist(withoutBackslash);
        entityManager.flush();

        TypedQuery<AppObject> q = entityManager.createQuery(
            "select a0 from AppObject a0 where lower(a0.name) like :p1 escape '\\'", AppObject.class);
        q.setParameter("p1", "%c:\\\\temp%");           // what escapeLikePattern produces for *C:\temp*

        List<String> codes = q.getResultList().stream().map(AppObject::getCode).toList();
        assertThat(codes).contains("ESC-1").doesNotContain("ESC-2");
    }
}

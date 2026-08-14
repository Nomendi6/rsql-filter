package com.nomendi6.rsql.it.config;

import org.hibernate.resource.jdbc.spi.StatementInspector;

import java.util.ArrayList;
import java.util.List;

/**
 * Records the SQL Hibernate actually sends, so a test can assert on the shape of the generated
 * statement rather than only on the rows it returns.
 *
 * <p>Register it with
 * {@code spring.jpa.properties.hibernate.session_factory.statement_inspector=com.nomendi6.rsql.it.config.SqlStatementCapture}.
 * Hibernate instantiates it once per session factory, so the recording is static; it is per thread
 * to keep concurrent tests apart.</p>
 */
public class SqlStatementCapture implements StatementInspector {

    private static final ThreadLocal<List<String>> STATEMENTS = ThreadLocal.withInitial(ArrayList::new);

    /** Forget everything recorded so far on this thread. Call before the statement under test. */
    public static void reset() {
        STATEMENTS.get().clear();
    }

    /** Every statement recorded on this thread since the last {@link #reset()}. */
    public static List<String> statements() {
        return List.copyOf(STATEMENTS.get());
    }

    /**
     * The first statement recorded on this thread, which is the query under test.
     *
     * <p>Reading the returned entities can trigger further selects - an inverse {@code OneToOne} is eager
     * and cannot be proxied, so Hibernate fetches it per row. Those follow the query and are not what these
     * tests measure.</p>
     *
     * @throws AssertionError when nothing was recorded, which means the test measured nothing at all
     */
    public static String firstStatement() {
        List<String> statements = statements();
        if (statements.isEmpty()) {
            throw new AssertionError("No statement was recorded");
        }
        return statements.get(0);
    }

    /** How many times {@code join} appears in the statement, ignoring case. */
    public static int countJoins(String sql) {
        int count = 0;
        int at = 0;
        String lower = sql.toLowerCase();
        while ((at = lower.indexOf("join", at)) >= 0) {
            count++;
            at += 4;
        }
        return count;
    }

    @Override
    public String inspect(String sql) {
        STATEMENTS.get().add(sql);
        return sql;
    }
}

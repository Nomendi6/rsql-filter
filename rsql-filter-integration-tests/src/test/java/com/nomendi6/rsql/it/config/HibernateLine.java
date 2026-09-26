package com.nomendi6.rsql.it.config;

import org.hibernate.Version;

/**
 * The Hibernate the tests run on, for the few places where 6.5 and 6.6 build a different statement.
 *
 * <p>The default build runs 6.6, the Hibernate Spring Boot 3.4 manages; {@code -Pboot-3.3} runs 6.5.</p>
 */
public final class HibernateLine {

    private HibernateLine() {}

    /**
     * Whether Hibernate drops a LEFT JOIN whose only use is the target's identifier, even one the library asks
     * for explicitly - and the target's restrictions with it: a subtype's discriminator, an {@code @SQLRestriction},
     * soft delete. Hibernate 6.5 does; from 6.6 on the join is kept, as in Hibernate 7, and the foreign key shortcut
     * is what removes it where that is safe.
     */
    public static boolean dropsIdentifierOnlyJoins() {
        return Version.getVersionString().startsWith("6.5.");
    }
}

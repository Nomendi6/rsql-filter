package com.nomendi6.rsql.it;

import com.nomendi6.rsql.it.config.IntegrationTest;
import org.springframework.test.context.TestPropertySource;

/**
 * {@link EmbeddableKeyFilterContract} with Hibernate's SQL Server dialect, over H2 in its SQL Server mode. The
 * dialect has no row values, so Hibernate writes a whole-key comparison out column by column; the emulated SQL is
 * plain boolean SQL, so H2 evaluates it exactly as SQL Server would. Hibernate is told the database instead of
 * asking for it, because the dialect's own metadata queries need SQL Server's system schema.
 */
@IntegrationTest
@TestPropertySource(properties = { "spring.datasource.url=jdbc:h2:mem:embeddable_mssql;MODE=MSSQLServer;DB_CLOSE_DELAY=-1", "spring.jpa.database-platform=org.hibernate.dialect.SQLServerDialect", "spring.jpa.properties.hibernate.boot.allow_jdbc_metadata_access=false", "spring.jpa.properties.jakarta.persistence.database-product-name=Microsoft SQL Server", "spring.jpa.properties.jakarta.persistence.database-major-version=16" })
public class EmbeddableKeySqlServerDialectIT extends EmbeddableKeyFilterContract {

    /** Not run here: the service counts with SQL Server's count_big, which H2 does not have. */
    @Override
    void throughTheService() {}
}

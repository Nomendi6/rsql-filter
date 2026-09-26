package com.nomendi6.rsql.it;

import com.nomendi6.rsql.it.config.IntegrationTest;
import org.springframework.test.context.TestPropertySource;

/** {@link EmbeddableKeyFilterContract} with Hibernate's DB2 dialect, over H2 in its DB2 mode. */
@IntegrationTest
@TestPropertySource(properties = { "spring.datasource.url=jdbc:h2:mem:embeddable_db2;MODE=DB2;DB_CLOSE_DELAY=-1", "spring.jpa.database-platform=org.hibernate.dialect.DB2Dialect" })
public class EmbeddableKeyDb2DialectIT extends EmbeddableKeyFilterContract {}

package com.nomendi6.rsql.it;

import com.nomendi6.rsql.it.config.IntegrationTest;
import org.springframework.test.context.TestPropertySource;

/** {@link TemporalFilterContract} under Hibernate's defaults - no JDBC time zone, so the driver works in the JVM's. */
@IntegrationTest
@TestPropertySource(properties = { "spring.jpa.properties.hibernate.format_sql=false" })
public class TemporalFilterDefaultJdbcIT extends TemporalFilterContract {}

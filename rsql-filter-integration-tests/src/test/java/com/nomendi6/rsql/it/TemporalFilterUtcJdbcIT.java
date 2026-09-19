package com.nomendi6.rsql.it;

import com.nomendi6.rsql.it.config.IntegrationTest;
import org.springframework.test.context.TestPropertySource;

/** {@link TemporalFilterContract} under the reporter's settings - JDBC in UTC, normalised storage, Instant as TIMESTAMP. */
@IntegrationTest
@TestPropertySource(properties = { "spring.jpa.properties.hibernate.jdbc.time_zone=UTC", "spring.jpa.properties.hibernate.timezone.default_storage=NORMALIZE", "spring.jpa.properties.hibernate.type.preferred_instant_jdbc_type=TIMESTAMP" })
public class TemporalFilterUtcJdbcIT extends TemporalFilterContract {}

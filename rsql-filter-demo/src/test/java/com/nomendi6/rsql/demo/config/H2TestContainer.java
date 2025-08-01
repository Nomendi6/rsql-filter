package com.nomendi6.rsql.demo.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.JdbcDatabaseContainer;
import org.testcontainers.utility.DockerImageName;

/**
 * H2 database container implementation for tests.
 */
public class H2TestContainer implements SqlTestContainer {

    private static final Logger log = LoggerFactory.getLogger(H2TestContainer.class);
    private static final String H2_JDBC_URL = "jdbc:h2:mem:rsql-filter-demo;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE;MODE=LEGACY";
    private static final String H2_USERNAME = "rsql-filter-demo";
    private static final String H2_PASSWORD = "";

    private JdbcDatabaseContainer<?> h2Container;

    @Override
    public void destroy() {
        // H2 is in-memory, nothing to destroy
    }

    @Override
    public void afterPropertiesSet() {
        log.info("Starting H2 database for testing");
        h2Container = new MockJdbcDatabaseContainer();
    }

    @Override
    public JdbcDatabaseContainer<?> getTestContainer() {
        return h2Container;
    }

    /**
     * Mock implementation of JdbcDatabaseContainer for H2.
     */
    private static class MockJdbcDatabaseContainer extends JdbcDatabaseContainer<MockJdbcDatabaseContainer> {
        
        public MockJdbcDatabaseContainer() {
            super(DockerImageName.parse("h2:latest"));
            // This is a mock container - it doesn't really use Docker
            // but the testcontainers API requires a Docker image name
        }

        @Override
        public String getDriverClassName() {
            return "org.h2.Driver";
        }

        @Override
        public String getJdbcUrl() {
            return H2_JDBC_URL;
        }

        @Override
        public String getUsername() {
            return H2_USERNAME;
        }

        @Override
        public String getPassword() {
            return H2_PASSWORD;
        }

        @Override
        public String getTestQueryString() {
            return "SELECT 1";
        }

        @Override
        protected void configure() {
            // No configuration needed for H2
        }

        @Override
        public boolean isRunning() {
            return true; // H2 in-memory is always "running"
        }

        @Override
        public void start() {
            // No need to start an in-memory database
        }

        @Override
        public void stop() {
            // No need to stop an in-memory database
        }
    }
}

package com.nomendi6.rsql.demo;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import com.nomendi6.rsql.demo.config.AsyncSyncConfiguration;
import com.nomendi6.rsql.demo.config.EmbeddedSQL;
import com.nomendi6.rsql.demo.config.JacksonConfiguration;

/**
 * Base composite annotation for integration tests.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@SpringBootTest(classes = { RsqlFilterDemoApp.class, JacksonConfiguration.class, AsyncSyncConfiguration.class })
@ActiveProfiles({"test"})
@EmbeddedSQL
public @interface IntegrationTest {
}

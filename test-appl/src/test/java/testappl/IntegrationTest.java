package testappl;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import testappl.config.AsyncSyncConfiguration;
import testappl.config.EmbeddedSQL;
import testappl.config.JacksonConfiguration;

/**
 * Base composite annotation for integration tests.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@SpringBootTest(classes = { TestapplApp.class, JacksonConfiguration.class, AsyncSyncConfiguration.class })
@ActiveProfiles({"test"})
@EmbeddedSQL
public @interface IntegrationTest {
}

package com.emersonrt.taskmanager.support;

import io.zonky.test.db.AutoConfigureEmbeddedDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static io.zonky.test.db.AutoConfigureEmbeddedDatabase.RefreshMode.AFTER_EACH_TEST_METHOD;

/**
 * Boots the whole application against an embedded PostgreSQL (no Docker needed).
 * The provider is set in src/test/resources/application.yaml.
 * Flyway runs the real migrations, and the database is reset after each test
 * from a template, so tests are isolated and still fast.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureEmbeddedDatabase(refresh = AFTER_EACH_TEST_METHOD)
public @interface IntegrationTest {
}

package org.kergru.library;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import static org.kergru.library.TestContainersConfig.KEYCLOAK;

@Import(TestContainersConfig.class)
@SpringBootTest
class LibraryFrontendApplicationTests {


  @DynamicPropertySource
  static void registerProperties(DynamicPropertyRegistry registry) {
    registry.add(
        "spring.security.oauth2.client.provider.keycloak.issuer-uri",
        () -> KEYCLOAK.getAuthServerUrl() + "/realms/library"
    );
  }

  @BeforeAll
  static void setup() {
    KEYCLOAK.start();
  }

  @AfterAll
  static void teardown() {
    KEYCLOAK.stop();
  }

  @Test
  void contextLoads() {
  }
}

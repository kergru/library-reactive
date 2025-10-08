package org.kergru.library;

import org.junit.jupiter.api.Test;
import org.kergru.library.util.KeycloakTestConfig;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

@Import(KeycloakTestConfig.class)
@SpringBootTest
class LibraryFrontendApplicationTests {

  @Test
  void contextLoads() {
  }
}

package org.kergru.library;

import org.junit.jupiter.api.Test;
import org.kergru.library.util.MockOAuthClientConfig;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

@Import(MockOAuthClientConfig.class)
@SpringBootTest
class LibraryFrontendApplicationTests {

  @Test
  void contextLoads() {
  }
}

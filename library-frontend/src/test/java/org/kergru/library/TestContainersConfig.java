package org.kergru.library;

import dasniko.testcontainers.keycloak.KeycloakContainer;
import java.time.Duration;
import org.kergru.library.util.RealmPathResolver;
import org.springframework.boot.test.context.TestConfiguration;
import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.wait.strategy.Wait;

@TestConfiguration(proxyBeanMethods = false)
public class TestContainersConfig {

  public static final KeycloakContainer KEYCLOAK = new KeycloakContainer("quay.io/keycloak/keycloak:22.0.1")
      .withBootstrapAdminDisabled()
      .withFileSystemBind(
          RealmPathResolver.resolveRealmImportDir().toString(),
          "/opt/keycloak/data/import",
          BindMode.READ_ONLY
      )
      .waitingFor(
          Wait.forLogMessage(".*Import finished successfully.*\\n", 1)
              .withStartupTimeout(Duration.ofMinutes(3))
      );
}


package org.kergru.library.util;

import com.github.dockerjava.api.model.ExposedPort;
import com.github.dockerjava.api.model.PortBinding;
import com.github.dockerjava.api.model.Ports;
import dasniko.testcontainers.keycloak.KeycloakContainer;
import java.time.Duration;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.MountableFile;

@TestConfiguration
@Testcontainers
public class KeycloakTestConfig {

  @Container
  static KeycloakContainer keycloak =
      new KeycloakContainer("quay.io/keycloak/keycloak:26.3.1")
          .withCopyFileToContainer(
              MountableFile.forHostPath("../docker/keycloak-init/library-realm.json"),
              "/opt/keycloak/data/import/library-realm.json")
          .withEnv("KEYCLOAK_ADMIN", "admin")
          .withEnv("KEYCLOAK_ADMIN_PASSWORD", "pwd")
          .withEnv("KC_HTTP_PORT", "8080")
          .withEnv("KC_IMPORT", "/opt/keycloak/data/import/library-realm.json")
          .withExposedPorts(8080)
          .waitingFor(
              Wait.forHttp("/realms/library/.well-known/openid-configuration")
                  .forStatusCode(200)
                  .withStartupTimeout(Duration.ofMinutes(2)))
          .withCreateContainerCmdModifier(cmd ->
              cmd.getHostConfig().withPortBindings(
                  new PortBinding(Ports.Binding.bindPort(8085), new ExposedPort(8080)))
          );

  static {
    keycloak.start();
  }

  @Bean
  public KeycloakContainer keycloakContainer() {
    return keycloak;
  }
}

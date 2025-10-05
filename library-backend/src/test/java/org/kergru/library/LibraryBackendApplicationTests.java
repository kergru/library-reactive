package org.kergru.library;

import static org.assertj.core.api.Assertions.assertThat;

import com.github.dockerjava.api.model.ExposedPort;
import com.github.dockerjava.api.model.PortBinding;
import com.github.dockerjava.api.model.Ports;
import dasniko.testcontainers.keycloak.KeycloakContainer;
import java.time.Duration;
import org.junit.jupiter.api.Test;
import org.kergru.library.model.UserDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.MountableFile;

@Testcontainers
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
class LibraryBackendApplicationTests {

  @Container
  public static MySQLContainer<?> mysqlContainer = new MySQLContainer<>("mysql:8.0")
      .withDatabaseName("library")
      .withUsername("admin")
      .withPassword("pwd")
      .withCopyFileToContainer(
          MountableFile.forHostPath("../docker/mysql-init/library_schema.sql"),
          "/docker-entrypoint-initdb.d/library_schema.sql"
      );

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

  @DynamicPropertySource
  static void jwtProps(DynamicPropertyRegistry registry) {
    JwtTestUtils.waitForJwks(); // ensure JWKS endpoint is reachable
    String base = keycloak.getAuthServerUrl().replaceAll("/$", "");
    registry.add("spring.security.oauth2.resourceserver.jwt.jwk-set-uri",
        () -> base + "/realms/library/protocol/openid-connect/certs");

    registry.add("spring.r2dbc.url",
        () -> "r2dbc:mysql://localhost:" + mysqlContainer.getMappedPort(3306) + "/library");
    registry.add("spring.r2dbc.username", mysqlContainer::getUsername);
    registry.add("spring.r2dbc.password", mysqlContainer::getPassword);
  }

  @Autowired
  private WebTestClient webTestClient;

  @Test
  void contextLoads() {
  }

  @Test
  void expectLoadUserWithRoleLibrarianShouldReturnUser() {
    String token = JwtTestUtils.getAccessToken("librarian", "pwd");

    webTestClient.get()
        .uri("/library/api/users/demo_user_1")
        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
        .exchange()
        .expectStatus().isOk()
        .expectBody(UserDto.class)
        .value(user -> {
          assertThat(user).isNotNull();
          assertThat(user.userName()).isEqualTo("demo_user_1");
        });
  }

  @Test
  void expectLoadUnknownUserWithRoleLibrarianShouldReturnNotFound() {
    String token = JwtTestUtils.getAccessToken("librarian", "pwd");

    webTestClient.get()
        .uri("/library/api/users/UNKNOWN_USER")
        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
        .exchange()
        .expectStatus().isNotFound();
  }

  @Test
  void expectLoadMyUserWithNotRoleLibrarianShouldReturnUser() {
    String token = JwtTestUtils.getAccessToken("demo_user_1", "pwd");

    webTestClient.get()
        .uri("/library/api/users/demo_user_1")
        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
        .exchange()
        .expectStatus().isOk()
        .expectBody(UserDto.class)
        .value(user -> {
          assertThat(user).isNotNull();
          assertThat(user.userName()).isEqualTo("demo_user_1");
        });
  }

  @Test
  void expectLoadForeignUserWithNotRoleLibrarianShouldReturnForbidden() {
    String token = JwtTestUtils.getAccessToken("demo_user_1", "pwd");

    webTestClient.get()
        .uri("/library/api/users/bob")
        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
        .exchange()
        .expectStatus().isForbidden();
  }
}

package org.kergru.library;

import static org.assertj.core.api.Assertions.assertThat;
import static org.kergru.library.JwtTestUtils.createMockJwt;
import static org.kergru.library.JwtTestUtils.createMockJwtWithRoleLibrarian;

import java.time.Duration;
import org.junit.jupiter.api.Test;
import org.kergru.library.model.PageResponseDto;
import org.kergru.library.model.UserDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.containers.MySQLR2DBCDatabaseContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.MountableFile;

/**
 * Integration test for the {@link LibraryBackendApplication}. KeyCloak is mocked using mockJwt(), no KeyCloak container required MySQL is mocked using Testcontainers Webclient is
 * configured to use a mock JWT
 */
@AutoConfigureWebTestClient
@Testcontainers
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
class LibraryBackendApplicationTests {

  private static final MySQLContainer<?> mysqlContainer = new MySQLContainer<>("mysql:8.0")
      .withDatabaseName("library")
      .withUsername("admin")
      .withPassword("pwd")
      .withCopyFileToContainer(
          MountableFile.forHostPath("../docker/mysql-init/library_schema.sql"),
          "/docker-entrypoint-initdb.d/library_schema.sql"
      )
      .withStartupTimeout(Duration.ofMinutes(2));

  @Container
  private static final MySQLR2DBCDatabaseContainer r2dbcContainer = new MySQLR2DBCDatabaseContainer(mysqlContainer);

  @DynamicPropertySource
  static void registerProperties(DynamicPropertyRegistry registry) {
    registry.add("spring.r2dbc.url",
        () -> "r2dbc:mysql://localhost:" + mysqlContainer.getMappedPort(3306) + "/library");
    registry.add("spring.r2dbc.username", mysqlContainer::getUsername);
    registry.add("spring.r2dbc.password", mysqlContainer::getPassword);

    // Hack to force creation of ReactiveJwtDecoder, when mocking JWT with JwtMutator no ReactiveJwtDecoder is created
    registry.add("spring.security.oauth2.resourceserver.jwt.jwk-set-uri",
        () -> "http://localhost:8085/realms/library/protocol/openid-connect/certs");
  }

  @Autowired
  private WebTestClient webTestClient;

  @Test
  void contextLoads() {
  }

  @Test
  void expectGetUserWithRoleLibrarianShouldReturnUser() {

    webTestClient
        .mutateWith(createMockJwtWithRoleLibrarian("librarian"))
        .get()
        .uri("/library/api/users/demo_user_1")
        .exchange()
        .expectStatus().isOk()
        .expectBody(UserDto.class)
        .value(user -> {
          assertThat(user).isNotNull();
          assertThat(user.userName()).isEqualTo("demo_user_1");
        });
  }

  @Test
  void expectGetUnknownUserWithRoleLibrarianShouldReturnNotFound() {

    webTestClient
        .mutateWith(createMockJwtWithRoleLibrarian("librarian"))
        .get()
        .uri("/library/api/users/UNKNOWN_USER")
        .exchange()
        .expectStatus().isNotFound();
  }

  @Test
  void expectSearchUsersWithRoleLibrarianShouldReturnUsers() {

    webTestClient
        .mutateWith(createMockJwtWithRoleLibrarian("librarian"))
        .get()
        .uri("/library/api/users?searchString=demo_user_1&page=0&size=10&sortBy=userName")
        .exchange()
        .expectStatus().isOk()
        .expectBody(new ParameterizedTypeReference<PageResponseDto<UserDto>>() {})
        .value(response -> {
          assertThat(response).isNotNull();
          assertThat(response.content()).isNotEmpty();
          assertThat(response.content().getFirst().userName()).isEqualTo("demo_user_1");
        });
  }

  @Test
  void expectGetMyUserWithNotRoleLibrarianShouldReturnUser() {

    webTestClient
        .mutateWith(createMockJwt("demo_user_1"))
        .get()
        .uri("/library/api/users/demo_user_1")
        .exchange()
        .expectStatus().isOk()
        .expectBody(UserDto.class)
        .value(user -> {
          assertThat(user).isNotNull();
          assertThat(user.userName()).isEqualTo("demo_user_1");
        });
  }

  @Test
  void expectGetForeignUserWithNotRoleLibrarianShouldReturnForbidden() {

    webTestClient
        .mutateWith(createMockJwt("demo_user_1"))
        .get()
        .uri("/library/api/users/demo_user_2")
        .exchange()
        .expectStatus().isForbidden();
  }

  @Test
  void expectSearchUsersWithNotRoleLibrarianShouldReturnForbidden() {

    webTestClient
        .mutateWith(createMockJwt("demo_user_1"))
        .get()
        .uri("/library/api/users?searchString=demo_user_1&page=0&size=10&sortBy=userName")
        .exchange()
        .expectStatus().isForbidden();
  }
}

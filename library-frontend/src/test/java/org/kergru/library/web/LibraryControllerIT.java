package org.kergru.library.web;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static org.assertj.core.api.Assertions.assertThat;
import static org.kergru.library.TestContainersConfig.KEYCLOAK;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.mockOidcLogin;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.kergru.library.TestContainersConfig;
import org.kergru.library.client.LibraryBackendClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.OidcLoginMutator;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@Import({TestContainersConfig.class, LibraryBackendClient.class})
public class LibraryControllerIT {

  private static WireMockServer backendMock;

  @Autowired
  private WebTestClient webTestClient;

  @DynamicPropertySource
  static void registerProperties(DynamicPropertyRegistry registry) {
    registry.add(
        "spring.security.oauth2.client.provider.keycloak.issuer-uri",
        () -> KEYCLOAK.getAuthServerUrl() + "/realms/library"
    );
  }

  private static String readFile(String path) throws IOException {
    return Files.readString(Paths.get(new ClassPathResource(path).getURI()));
  }

  @BeforeAll
  static void setup() throws IOException {
    // Keycloak
    KEYCLOAK.start();

    // Backend-Mock
    backendMock = new WireMockServer(WireMockConfiguration.options().port(8081));
    backendMock.start();

    // /books
    backendMock.stubFor(get("/library/api/books")
        .willReturn(aResponse()
            .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .withBody(readFile("stubs/books-list.json"))));

    // /books/{isbn}
    backendMock.stubFor(get("/library/api/books/12345")
        .willReturn(aResponse()
            .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .withBody(readFile("stubs/book-details.json"))));

    // /users/{userName}
    backendMock.stubFor(get("/library/api/users/demo_user_1")
        .willReturn(aResponse()
            .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .withBody(readFile("stubs/user-details.json"))));

    // /users/{userName}/loans
    backendMock.stubFor(get("/library/api/users/demo_user_1/loans")
        .willReturn(aResponse()
            .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .withBody(readFile("stubs/loans-list.json"))));
  }


  @AfterAll
  static void teardown() {
    if (backendMock != null) backendMock.stop();
    KEYCLOAK.stop();
  }

  @Test
  void testListAllBooks() {
    webTestClient
        .mutateWith(createMockOidcLogin("demo_user_1"))
        .get()
        .uri("/library/ui/books")
        .exchange()
        .expectStatus().isOk()
        .expectBody(String.class)
        .value(body -> assertThat(body).contains("The Great Gatsby"));
  }

  @Test
  void testShowBookByIsbn() {
    webTestClient
        .mutateWith(createMockOidcLogin("demo_user_1"))
        .get()
        .uri("/library/ui/books/12345")
        .exchange()
        .expectStatus().isOk()
        .expectBody(String.class)
        .value(body -> assertThat(body).contains("The Great Gatsby"));
  }

  @Test
  void testMeEndpoint() {
    webTestClient
        .mutateWith(createMockOidcLogin("demo_user_1"))
        .get()
        .uri("/library/ui/me")
        .exchange()
        .expectStatus().isOk()
        .expectBody(String.class)
        .value(body -> assertThat(body).contains("demo_user_1"));
  }

  private OidcLoginMutator createMockOidcLogin(String username) {
    return mockOidcLogin()
        .idToken(token -> token
            .claim("sub", username)
            .claim("preferred_username", username)
        );
  }
}

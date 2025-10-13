package org.kergru.library.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.kergru.library.util.JwtTestUtils.createMockOidcLoginForUser;

import org.junit.jupiter.api.Test;
import org.kergru.library.util.MockOAuthClientConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.reactive.server.WebTestClient;

/**
 * Integration test for the {@link LibraryController}. KeyCloak is mocked using mockJwt(), no KeyCloak container required Library Backend is mocked using WireMock Webclient is
 * configured to use a mock JWT
 */
@AutoConfigureWebTestClient
@AutoConfigureWireMock(port = 8081)
@Import(MockOAuthClientConfig.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class LibraryControllerIT {

  @Autowired
  private WebTestClient webTestClient;

  @Test
  void testListAllBooks() {
    webTestClient
        .mutateWith(createMockOidcLoginForUser("demo_user_1"))
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
        .mutateWith(createMockOidcLoginForUser("demo_user_1"))
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
        .mutateWith(createMockOidcLoginForUser("demo_user_1"))
        .get()
        .uri("/library/ui/me")
        .exchange()
        .expectStatus().isOk()
        .expectBody(String.class)
        .value(body -> assertThat(body).contains("demo_user_1"))
        .value(body -> assertThat(body).contains("The Great Gatsby"));
  }

  @Test
  void expectBorrowBookReturnsLoan() throws Exception {
    webTestClient
        .mutateWith(createMockOidcLoginForUser("demo_user_1"))
        .post()
        .uri("/library/ui/me/borrowBook/success-isbn")
        .exchange()
        .expectStatus().isOk();
  }

  @Test
  void expectBorrowAlreadyBorrowedBookReturnsError() throws Exception {
    webTestClient
        .mutateWith(createMockOidcLoginForUser("demo_user_1"))
        .post()
        .uri("/library/ui/me/borrowBook/conflict-isbn")
        .exchange()
        .expectStatus().isEqualTo(HttpStatus.CONFLICT);
  }

  @Test
  void expectReturnBookReturnsOk() throws Exception {
    webTestClient
        .mutateWith(createMockOidcLoginForUser("demo_user_1"))
        .post()
        .uri("/library/ui/me/returnBook/1")
        .exchange()
        .expectStatus().isOk();
  }
}

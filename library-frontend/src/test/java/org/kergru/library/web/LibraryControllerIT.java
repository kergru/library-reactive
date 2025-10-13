package org.kergru.library.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.kergru.library.util.JwtTestUtils.createMockOidcLoginForUser;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.csrf;

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
  void testSearchBooksShouldReturnBooks() {
    webTestClient
        .mutateWith(createMockOidcLoginForUser("demo_user_1"))
        .get()
        .uri("/library/ui/books?searchString=The Great Gatsby")
        .exchange()
        .expectStatus().isOk()
        .expectBody(String.class)
        .value(body -> assertThat(body).contains("The Great Gatsby"));
  }

  @Test
  void testGetBookByIsbnShouldReturnBook() {
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
  void expectGetMeShouldReturnAuthenticatedUser() {
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
  void expectBorrowBookReturnsLoan() {
    webTestClient
        .mutateWith(createMockOidcLoginForUser("demo_user_1"))
        .mutateWith(csrf())
        .post()
        .uri("/library/ui/me/borrowBook/success-isbn")
        .exchange()
        .expectStatus().isOk();
  }

  @Test
  void expectBorrowAlreadyBorrowedBookReturnsConflict() {
    webTestClient
        .mutateWith(createMockOidcLoginForUser("demo_user_1"))
        .mutateWith(csrf())
        .post()
        .uri("/library/ui/me/borrowBook/conflict-isbn")
        .exchange()
        .expectStatus().isEqualTo(HttpStatus.CONFLICT);
  }

  @Test
  void expectReturnBookReturnsOk() {
    webTestClient
        .mutateWith(createMockOidcLoginForUser("demo_user_1"))
        .mutateWith(csrf())
        .post()
        .uri("/library/ui/me/returnBook/1")
        .exchange()
        .expectStatus().isOk();
  }
}

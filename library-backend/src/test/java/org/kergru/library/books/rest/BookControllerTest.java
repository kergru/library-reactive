package org.kergru.library.books.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.kergru.library.JwtTestUtils.createMockJwt;

import java.time.Duration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.kergru.library.books.service.BookService;
import org.kergru.library.loans.repository.LoanRepository;
import org.kergru.library.loans.service.LoanService;
import org.kergru.library.model.BookDto;
import org.kergru.library.model.PageResponseDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.containers.MySQLR2DBCDatabaseContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.MountableFile;

@AutoConfigureWebTestClient
@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class BookControllerTest {

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
  @Autowired
  private BookService bookService;

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

  @Autowired
  private LoanService loanService;

  @Autowired
  private LoanRepository loanRepository;

  @BeforeEach
  public void afterEach() {
    loanRepository.deleteAll().subscribe(); //filled because of docker init
  }

  @Test
  @WithMockUser
  public void expectGetBookShouldReturnBook() throws Exception {
    webTestClient
        .mutateWith(createMockJwt("demo_user_1"))
        .get()
        .uri("/library/api/books/9780132350884")
        .exchange()
        .expectStatus().isOk()
        .expectBody(BookDto.class)
        .value(book -> {
          assertThat(book).isNotNull();
          assertThat(book.title()).isEqualTo("Clean Code");
          assertThat(book.isbn()).isEqualTo("9780132350884");
          assertThat(book.loanStatus().available()).isEqualTo(true);
        });
  }

  @Test
  @WithMockUser
  public void expectGetBorrowedBookShouldReturnBooksWithStatusBorrowed() throws Exception {
    //borrow book
    var loan = loanService.borrowBook("9780132350884", "demo_user_1").block();
    assertThat(loan).isNotNull();

    webTestClient
        .mutateWith(createMockJwt("demo_user_1"))
        .get()
        .uri("/library/api/books/9780132350884")
        .exchange()
        .expectStatus().isOk()
        .expectBody(BookDto.class)
        .value(book -> {
          assertThat(book).isNotNull();
          assertThat(book.title()).isEqualTo("Clean Code");
          assertThat(book.isbn()).isEqualTo("9780132350884");
          assertThat(book.loanStatus().available()).isEqualTo(false);
        });
  }

  @Test
  @WithMockUser
  public void expectSearchBooksShouldReturnBooks() throws Exception {

    webTestClient
        .mutateWith(createMockJwt("demo_user_1"))
        .get()
        .uri("/library/api/books?searchString=Clean Code")
        .exchange()
        .expectStatus().isOk()
        .expectBody(new ParameterizedTypeReference<PageResponseDto<BookDto>>() {})
        .value(response -> {
          assertThat(response).isNotNull();
          assertThat(response.content()).isNotEmpty();
          assertThat(response.content().getFirst().title()).isEqualTo("Clean Code");
          assertThat(response.content().getFirst().isbn()).isEqualTo("9780132350884");
          assertThat(response.content().getFirst().loanStatus().available()).isEqualTo(true);
        });
  }
}

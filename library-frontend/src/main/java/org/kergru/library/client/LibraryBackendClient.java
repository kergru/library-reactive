package org.kergru.library.client;

import java.util.List;
import org.kergru.library.model.BookDto;
import org.kergru.library.model.LoanDto;
import org.kergru.library.model.UserDto;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
public class LibraryBackendClient {

  private final WebClient webClient;

  public LibraryBackendClient(WebClient oauth2WebClient) {
    this.webClient = oauth2WebClient;
  }

  /**
   * Retrieves all books from the backend. Using the token relay pattern.
   */
  public Mono<List<BookDto>> getAllBooks() {
    return webClient.get()
        .uri("/library/api/books")
        .retrieve()
        .onStatus(s -> s.value() == 404, resp -> reactor.core.publisher.Mono.empty())
        .onStatus(s -> s.is4xxClientError() || s.is5xxServerError(),
            ClientResponse::createException)
        .bodyToMono(new ParameterizedTypeReference<>() {
        });
  }

  /**
   * Retrieves a single book by its ISBN from the backend. Using the client credentials pattern.
   */
  public Mono<BookDto> getBookByIsbn(String isbn) {
    return webClient.get()
        .uri("/library/api/books/{isbn}", isbn)
        .retrieve()
        .onStatus(s -> s.value() == 404, resp -> reactor.core.publisher.Mono.empty())
        .onStatus(s -> s.is4xxClientError() || s.is5xxServerError(),
            ClientResponse::createException)
        .bodyToMono(BookDto.class);
  }

  public Mono<List<UserDto>> getAllUsers() {
    return webClient.get()
        .uri("/library/api/users")
        .retrieve()
        .onStatus(s -> s.value() == 404, resp -> reactor.core.publisher.Mono.empty())
        .onStatus(s -> s.is4xxClientError() || s.is5xxServerError(),
            ClientResponse::createException)
        .bodyToMono(new ParameterizedTypeReference<>() {
        });
  }

  public Mono<UserDto> getUser(String userName) {
    return webClient.get()
        .uri("/library/api/users/{userName}", userName)
        .retrieve()
        .onStatus(s -> s.value() == 404, resp -> reactor.core.publisher.Mono.empty())
        .onStatus(s -> s.is4xxClientError() || s.is5xxServerError(),
            ClientResponse::createException)
        .bodyToMono(UserDto.class);
  }

  public Mono<List<LoanDto>> getBorrowedBooksOfUser(String userName) {
    return webClient.get()
        .uri("/library/api/users/{userName}/loans", userName)
        .retrieve()
        .onStatus(s -> s.value() == 404, resp -> reactor.core.publisher.Mono.empty())
        .onStatus(s -> s.is4xxClientError() || s.is5xxServerError(),
            ClientResponse::createException)
        .bodyToFlux(LoanDto.class)
        .collectList();
  }
}
package org.kergru.library.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.kergru.library.model.BookDto;
import org.kergru.library.model.LoanDto;
import org.kergru.library.model.UserDto;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class LibraryBackendClient {

  private final WebClient webClient;

  public LibraryBackendClient(WebClient oauth2WebClient) {
    this.webClient = oauth2WebClient;
  }

  /**
   * Retrieves all books from the backend.
   * Using the token relay pattern.
   */
  public Flux<BookDto> getAllBooks() {
    return webClient.get()
        .uri("/library/api/books")
        .retrieve()
        .onStatus(s -> s.value() == 404, resp -> reactor.core.publisher.Mono.empty())
        .onStatus(s -> s.is4xxClientError() || s.is5xxServerError(),
            ClientResponse::createException)
        .bodyToFlux(BookDto.class);
  }

  /**
   * Retrieves a single book by its ISBN from the backend.
   * Using the client credentials pattern.
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

  public Flux<UserDto> getAllUsers() {
    return webClient.get()
        .uri("/library/api/users")
        .retrieve()
        .onStatus(s -> s.value() == 404, resp -> reactor.core.publisher.Mono.empty())
        .onStatus(s -> s.is4xxClientError() || s.is5xxServerError(),
            ClientResponse::createException)
        .bodyToFlux(UserDto.class);
  }

  public Mono<UserDto> getUser(String userName) {
    return webClient.get()
        .uri("/library/api/users/{userName}", "demo_user_1")
        .exchangeToMono(response -> response.bodyToMono(String.class)
            .doOnNext(body -> System.out.println("Raw JSON: " + body))
            .flatMap(body -> {
              try {
                return Mono.justOrEmpty(
                    new ObjectMapper().readValue(body, UserDto.class)
                );
              } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
              }
            }));
  }

  public Flux<LoanDto> getBorrowedBooksOfUser(String userName) {
    return webClient.get()
        .uri("/library/api/users/{userName}/loans", userName)
        .retrieve()
        .onStatus(s -> s.value() == 404, resp -> reactor.core.publisher.Mono.empty())
        .onStatus(s -> s.is4xxClientError() || s.is5xxServerError(),
            ClientResponse::createException)
        .bodyToFlux(LoanDto.class);
  }
}
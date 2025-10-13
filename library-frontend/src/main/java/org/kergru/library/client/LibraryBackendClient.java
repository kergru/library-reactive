package org.kergru.library.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.kergru.library.model.BookDto;
import org.kergru.library.model.LoanDto;
import org.kergru.library.model.PageResponseDto;
import org.kergru.library.model.UserDto;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * OAuth2 protected client for the library backend.
 * Using the token relay pattern.
 * The token is added to the request headers by the OAuth2 interceptor defined in OAuth2ExchangeFilterFunction
 *
 * @see OAuth2ExchangeFilterFunction
 * @see OAuth2WebClientConfig
 */
@Service
public class LibraryBackendClient {

  private final WebClient webClient;

  public LibraryBackendClient(WebClient oauth2WebClient) {
    this.webClient = oauth2WebClient;
  }

  /**
   * Searches books from the backend using pagination.
   */
  public Mono<PageResponseDto<BookDto>> searchBooks(String searchString, int page, int size, String sortBy) {
    return webClient.get()
        .uri(uriBuilder -> {
          var builder = uriBuilder
              .path("/library/api/books")
              .queryParam("page", page)
              .queryParam("size", size)
              .queryParam("sort", sortBy);
          if (searchString != null && !searchString.isEmpty()) {
            builder.queryParam("searchString", searchString);
          }
          return builder.build();
        })
        .retrieve()
        .onStatus(
            status -> status.is4xxClientError() || status.is5xxServerError(),
            ClientResponse::createException
        )
        .bodyToMono(new ParameterizedTypeReference<>() {});
  }

  /**
   * Retrieves a single book by its ISBN from the backend.
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

  /**
   * Searches users from the backend using pagination.
   */
  public Mono<PageResponseDto<UserDto>> searchUsers(String searchString, int page, int size, String sortBy) {
    return webClient.get()
        .uri(uriBuilder -> {
          var builder = uriBuilder
              .path("/library/api/users")
              .queryParam("page", page)
              .queryParam("size", size)
              .queryParam("sort", sortBy);
          if (searchString != null && !searchString.isEmpty()) {
            builder.queryParam("searchString", searchString);
          }
          return builder.build();
        })
        .retrieve()
        .onStatus(
            status -> status.is4xxClientError() || status.is5xxServerError(),
            ClientResponse::createException
        )
        .bodyToMono(new ParameterizedTypeReference<>() {});
  }

  /**
   * Retrieves a single book by its ISBN from the backend.
   */
  public Mono<UserDto> getUser(String userName) {
    return webClient.get()
        .uri("/library/api/users/{userName}", "demo_user_1")
        .exchangeToMono(response -> response.bodyToMono(String.class)
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

  public Mono<LoanDto> borrowBook(String isbn, String userName) {
    return webClient.post()
        .uri("/library/api/users/{userName}/loans", userName)
        .body(Mono.just(isbn), String.class)
        .retrieve()
        .onStatus(s -> s.value() == 404, resp -> reactor.core.publisher.Mono.empty())
        .onStatus(s -> s.is4xxClientError() || s.is5xxServerError(),
            ClientResponse::createException)
        .bodyToMono(LoanDto.class);
  }

  public Mono<Void> returnBook(Long loanId, String userName) {
    return webClient
        .delete()
        .uri("/library/api/users/{userName}/loans/{loanId}", userName, loanId)
        .retrieve()
        .onStatus(s -> s.value() == 404, resp -> reactor.core.publisher.Mono.empty())
        .onStatus(s -> s.is4xxClientError() || s.is5xxServerError(),
            ClientResponse::createException)
        .bodyToMono(Void.class);
  }
}

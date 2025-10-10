package org.kergru.library.service;

import org.kergru.library.client.LibraryBackendClient;
import org.kergru.library.model.BookDto;
import org.kergru.library.model.LoanDto;
import org.kergru.library.model.PageResponseDto;
import org.kergru.library.model.UserDto;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class LibraryService {

  private final LibraryBackendClient backendClient;

  public LibraryService(LibraryBackendClient oauth2WebClient) {
    this.backendClient = oauth2WebClient;
  }

  public Mono<PageResponseDto<BookDto>> searchBooks(String searchString, int page, int size, String sortBy) {
    return backendClient.searchBooks(searchString, page, size, sortBy);
  }

  public Mono<BookDto> getBookByIsbn(String isbn) {
    return backendClient.getBookByIsbn(isbn);
  }

  public Mono<PageResponseDto<UserDto>> searchUsers(String searchString, int page, int size, String sortBy) {
    return backendClient.searchUsers(searchString, page, size, sortBy);
  }

  public Mono<UserWithLoans> getUserWithLoans(String userName) {
    return getUser(userName)
        .flatMap(user ->
            getBorrowedBooksOfUser(userName)
                .collectList()
                .map(loans -> new UserWithLoans(
                    user,
                    loans
                )));
  }

  public Mono<UserDto> getUser(String userName) {
    return backendClient.getUser(userName);
  }

  public Flux<LoanDto> getBorrowedBooksOfUser(String userId) {
    return backendClient.getBorrowedBooksOfUser(userId);
  }
}
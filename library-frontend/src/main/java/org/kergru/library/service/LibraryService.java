package org.kergru.library.service;

import org.kergru.library.client.LibraryBackendClient;
import org.kergru.library.model.BookDto;
import org.kergru.library.model.LoanDto;
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

  /**
   * Retrieves all books from the backend.
   */
  public Flux<BookDto> getAllBooks() {
    return backendClient.getAllBooks();
  }

  /**
   * Retrieves a single book by its ISBN.
   */
  public Mono<BookDto> getBookByIsbn(String isbn) {
    return backendClient.getBookByIsbn(isbn);
  }

  /**
   * Retrieves all users.
   */
  public Flux<UserDto> getAllUsers() {
    return backendClient.getAllUsers();
  }

  /**
   * Retrieves a single user with his loans.
   */
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

  /**
   * Retrieves a single user by userName.
   */
  public Mono<UserDto> getUser(String userName) {
    return backendClient.getUser(userName);
  }

  /**
   * Retrieves borrowed books by user.
   */
  public Flux<LoanDto> getBorrowedBooksOfUser(String userId) {
    return backendClient.getBorrowedBooksOfUser(userId);
  }
}
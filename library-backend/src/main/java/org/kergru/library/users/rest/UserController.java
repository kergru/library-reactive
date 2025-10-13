package org.kergru.library.users.rest;

import org.kergru.library.loans.service.LoanService;
import org.kergru.library.model.LoanDto;
import org.kergru.library.model.PageResponseDto;
import org.kergru.library.model.UserDto;
import org.kergru.library.users.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/library/api")
public class UserController {

  private final UserService userService;

  private final LoanService loanService;

  public UserController(UserService userService, LoanService loansService) {
    this.userService = userService;
    this.loanService = loansService;
  }

  /**
   * Returns paged search result of users by userName, firstName, lastName, email
   */
  @GetMapping("/users")
  @PreAuthorize("hasRole('LIBRARIAN')")
  public Mono<PageResponseDto<UserDto>> searchUsers(
      @RequestParam(required = false) String searchString,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "10") int size,
      @RequestParam(defaultValue = "firstName") String sortBy
  ) {
    return userService.searchUsers(searchString, page, size, sortBy);
  }
  /**
   * Returns user profile by userName, only accessible by the librarian or the user himself
   */
  @GetMapping("/users/{userName}")
  @PreAuthorize("hasRole('LIBRARIAN') or #userName == authentication.principal.claims['preferred_username']")
  public Mono<UserDto> getUser(@PathVariable String userName) {

    return userService.getUser(userName)
        .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found")));
  }

  /**
   * All borrowed books by a user, only accessible by the librarian or the user himself
   */
  @PreAuthorize("hasRole('LIBRARIAN') or #userName == authentication.principal.claims['preferred_username']")
  @GetMapping("/users/{userName}/loans")
  public Flux<LoanDto> getBorrowedBooksByUser(@PathVariable String userName) {

    return loanService.getBorrowedBooksByUser(userName)
        .onErrorMap(e -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
  }
  /**
   * Borrows a book to a user. Endpoint is only available for the user himself.
   * If the book is already borrowed, a 409 Conflict is returned.
   */
  @PreAuthorize("#userName == authentication.principal.claims['preferred_username']")
  @PostMapping("/users/{userName}/loans")
  public Mono<LoanDto> borrowBook(@PathVariable String userName, @RequestBody String isbn) {

      return loanService.borrowBook(isbn, userName)
          .onErrorMap(e -> new ResponseStatusException(HttpStatus.CONFLICT, e.getMessage()));
  }

  /**
   * Returns a book to library by setting returnedAt date in loan. Endpoint is only available for the user himself.
   * If no loan with id found or loan not borrowed by user, a 404 NotFound is returned.
   */
  @PreAuthorize("#userName == authentication.principal.claims['preferred_username']")
  @DeleteMapping("/users/{userName}/loans/{loanId}")
  public Mono<Void> returnBook(@PathVariable String userName, @PathVariable long loanId) {

    return loanService.returnBook(loanId, userName)
        .onErrorMap(e -> new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage()));
  }
}

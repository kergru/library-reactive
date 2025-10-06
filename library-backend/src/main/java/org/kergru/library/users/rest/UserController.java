package org.kergru.library.users.rest;

import org.kergru.library.loans.service.LoansService;
import org.kergru.library.model.LoanDto;
import org.kergru.library.model.UserDto;
import org.kergru.library.users.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/library/api")
public class UserController {

  private final UserService userService;

  private final LoansService loansService;

  public UserController(UserService userService, LoansService loansService) {
    this.userService = userService;
    this.loansService = loansService;
  }

  @GetMapping("/users")
  @PreAuthorize("hasRole('LIBRARIAN')")
  public Flux<UserDto> getAllUsers() {
    return userService.findAll();
  }

  /**
   * Returns user profile by userName, only accessible by the librarian or the user himself
   */
  @GetMapping("/users/{userName}")
  @PreAuthorize("hasRole('LIBRARIAN') or #userName == authentication.principal.claims['preferred_username']")
  public Mono<UserDto> getUserByUserName(@PathVariable String userName) {

    return userService.findUserByUserName(userName)
        .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found")));
  }

  /**
   * All borrowed books by a user, only accessible by the librarian or the user himself
   */
  @PreAuthorize("hasRole('LIBRARIAN') or #userName == authentication.principal.claims['preferred_username']")
  @GetMapping("/users/{userName}/loans")
  public Flux<LoanDto> getBorrowedBooksByUser(@PathVariable String userName) {

    return loansService.findBorrowedByUser(userName)
        .onErrorMap(e -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
  }
}

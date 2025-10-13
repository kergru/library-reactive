package org.kergru.library.web;

import java.util.List;
import java.util.Map;
import org.kergru.library.client.LibraryBackendClient.BookAlreadyBorrowedException;
import org.kergru.library.model.LoanDto;
import org.kergru.library.service.LibraryService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.web.server.csrf.CsrfToken;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

@Controller
@RequestMapping("/library/ui")
public class LibraryController {

  private final LibraryService libraryService;

  public LibraryController(LibraryService libraryService) {
    this.libraryService = libraryService;
  }

  @ModelAttribute
  @SuppressWarnings("unchecked")
  public void addCommonAttributes(Model model, @AuthenticationPrincipal OidcUser user) {

    if (user != null) {
      Map<String, Object> realmAccess = (Map<String, Object>) user.getClaims().get("realm_access");
      model.addAttribute("isLibrarian",
          realmAccess != null && ((List<String>) realmAccess.get("roles")).contains("LIBRARIAN"));
      model.addAttribute("userFullName",
          user.getFullName() != null ? user.getFullName() : user.getPreferredUsername());
    }
  }

  @GetMapping("/me")
  public Mono<String> me(Model model, @AuthenticationPrincipal OidcUser user) {

    return libraryService.getUserWithLoans(user.getPreferredUsername())
        .doOnNext(userWithLoans -> model.addAttribute("userWithLoans", userWithLoans))
        .thenReturn("users/detail")
        .switchIfEmpty(Mono.defer(() -> {
          model.addAttribute("userName", user.getPreferredUsername());
          return Mono.just("error/404");
        }));
  }

  /**
   * Borrows a book to a user.
   * (Rest endpoint for AJAX calls)
   */
  @ResponseBody
  @PostMapping(
      value = "/me/borrowBook/{isbn}",
      produces = MediaType.APPLICATION_JSON_VALUE)
  public Mono<LoanDto> borrowBook(@PathVariable String isbn, @AuthenticationPrincipal OidcUser user) {

    return libraryService.borrowBook(isbn, user.getPreferredUsername())
        .onErrorResume(BookAlreadyBorrowedException.class, e -> {
          return Mono.error(new ResponseStatusException(HttpStatus.CONFLICT, e.getMessage()));
        });
  }

  /**
   * Returns a book to the library.
   * (Rest endpoint for AJAX calls)
   */
  @ResponseBody
  @PostMapping(
      value = "/me/returnBook/{loanId}",
      produces = MediaType.APPLICATION_JSON_VALUE
  )
  public Mono<Void> returnBook(@PathVariable Long loanId, @AuthenticationPrincipal OidcUser user) {

    return libraryService.returnBook(loanId, user.getPreferredUsername());
  }

  @GetMapping("/books")
  public Mono<String> searchBooks(
      Model model,
      @RequestParam(required = false) String searchString,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "5") int size,
      @RequestParam(defaultValue = "title") String sortBy
  ) {
    return libraryService.searchBooks(searchString, page, size, sortBy)
        .doOnNext(books -> model.addAttribute("booksPage", books))
        .thenReturn("books/list");
  }

  @GetMapping("/books/{isbn}")
  public Mono<String> getBook(@PathVariable String isbn, Model model) {

    return libraryService.getBookByIsbn(isbn)
        .flatMap(bookDto -> {
          model.addAttribute("book", bookDto);
          return Mono.just("books/detail");
        })
        .switchIfEmpty(Mono.defer(() -> {
          model.addAttribute("isbn", isbn);
          return Mono.just("error/404");
        }));
  }
}
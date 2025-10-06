// java
package org.kergru.library.web;

import java.util.List;
import java.util.Map;
import org.kergru.library.service.LibraryService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
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

  @GetMapping("/books")
  public Mono<String> listAllBooks(Model model) {

    return libraryService.getAllBooks()
        .doOnNext(books -> model.addAttribute("books", books))
        .thenReturn("books/list");
  }

  @GetMapping("/books/{isbn}")
  public Mono<String> showBook(@PathVariable String isbn, Model model) {

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
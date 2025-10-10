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
import org.springframework.web.bind.annotation.RequestParam;
import reactor.core.publisher.Mono;

@Controller
@RequestMapping("/library/ui/admin")
public class LibraryAdminController {

  private final LibraryService libraryService;

  public LibraryAdminController(LibraryService libraryService) {
    this.libraryService = libraryService;
  }

  @ModelAttribute
  @SuppressWarnings("unchecked")
  public void addCommonAttributes(Model model, @AuthenticationPrincipal OidcUser user) {
    if (user != null) {
      Map<String, Object> realmAccess = (Map<String, Object>) user.getClaims().get("realm_access");
      model.addAttribute("isLibrarian",
          realmAccess != null && ((List<String>) realmAccess.get("roles")).contains("LIBRARIAN"));
    }
  }

  @GetMapping("/users")
  public Mono<String> searchUsers(
      Model model,
      @RequestParam(required = false) String searchString,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "10") int size,
      @RequestParam(defaultValue = "firstName") String sortBy
  ) {
    return libraryService.searchUsers(searchString, page, size, sortBy)
        .doOnNext(users -> model.addAttribute("usersPage", users))
        .thenReturn("users/list");
  }

  @GetMapping("/users/{userName}")
  public Mono<String> getUser(@PathVariable String userName, Model model) {

    return libraryService.getUserWithLoans(userName)
        .flatMap(userWithLoans -> {
          model.addAttribute("userWithLoans", userWithLoans);
          return Mono.just("users/detail");
        })
        .switchIfEmpty(Mono.defer(() -> {
          model.addAttribute("userName", userName);
          return Mono.just("error/404");
        }));
  }
}

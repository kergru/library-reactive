package org.kergru.library.web;

import org.springframework.security.web.server.csrf.CsrfToken;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * Adds the CSRF token to the model for Thymeleaf templates.
 * This is used to prevent CSRF attacks.
 * The token is added to the model as a variable named "_csrf". and is then available in the Thymeleaf templates.
 * The token is used to append to the headers of ajax requests (borrow book, return book, etc.) to the backend.
 */
@ControllerAdvice
public class CsrfAdvice {
  @ModelAttribute("_csrf")
  Mono<CsrfToken> csrf(ServerWebExchange exchange) {
    return exchange.getAttributeOrDefault(CsrfToken.class.getName(), Mono.<CsrfToken>empty())
        .doOnNext(t -> System.out.println("CsrfAdvice -> header=" + t.getHeaderName() + ": " + t.getToken()));
  }
}

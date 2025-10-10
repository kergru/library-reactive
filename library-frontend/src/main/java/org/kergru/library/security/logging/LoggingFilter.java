package org.kergru.library.security.logging;

import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import org.thymeleaf.util.StringUtils;
import reactor.core.publisher.Mono;

@Component
public class LoggingFilter implements WebFilter {

  @Override
  public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
    if (exchange.getAttributeOrDefault("request_logged", false)) {
      return chain.filter(exchange);
    }

    // mark request as logged
    exchange.getAttributes().put("request_logged", true);

    var uri = exchange.getRequest().getURI();
    if(StringUtils.contains(uri, "/library/ui")) {
      System.out.println("Incoming request: " + exchange.getRequest().getMethod() + " " + uri);
    }
    return chain.filter(exchange);
  }
}

package org.kergru.library.security.logging;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

/**
 * Logging filter for incoming requests to log the JWT token claims.
 */
@Component
public class JwtLoggingFilter implements WebFilter {

  private final ObjectMapper objectMapper;

  public JwtLoggingFilter() {
    this.objectMapper = new ObjectMapper()
        .enable(SerializationFeature.INDENT_OUTPUT)
        .registerModule(new JavaTimeModule());
  }

  @Override
  public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
    System.out.println("Incoming request: " + exchange.getRequest().getURI());

    // Headers loggen
    exchange.getRequest().getHeaders().forEach((name, values) -> {
      if ("authorization".equalsIgnoreCase(name)) {
        System.out.println("Authorization header - " + name + ": " + values);
      }
    });

    // JWT Claims loggen
    return ReactiveSecurityContextHolder.getContext()
        .flatMap(securityContext -> {
          var authentication = securityContext.getAuthentication();
          if (authentication != null && authentication.getPrincipal() instanceof Jwt jwt) {
            try {
              String jwtJson = objectMapper.writeValueAsString(jwt.getClaims());
              System.out.println("JWT Token Claims:\n" + jwtJson);
            } catch (JsonProcessingException e) {
              System.out.println("Could not parse JWT token: " + e.getMessage());
            }
          } else {
            System.out.println("No JWT token found in security context");
          }
          return Mono.empty();
        })
        .then(chain.filter(exchange)); // request weiterleiten
  }
}

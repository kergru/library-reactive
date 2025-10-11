package org.kergru.library.security;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import org.kergru.library.security.logging.JwtLoggingFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity.CsrfSpec;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.server.SecurityWebFilterChain;
import reactor.core.publisher.Mono;

/**
 * Configures Spring Security with OAuth2 Login using OpenID Connect (OIDC) and Keycloak integration.
 *
 * <p>This configuration sets up:
 * <ul>
 *   <li>OAuth2 Login with Keycloak as the identity provider</li>
 *   <li>Role-based access control (RBAC) with custom role mapping</li>
 *   <li>Custom success and logout handlers</li>
 *   <li>CSRF protection configuration</li>
 *   <li>Exception handling for access denied scenarios</li>
 * </ul>
 *
 * <p>Key Features:
 * <ul>
 *   <li><b>OIDC Integration</b>: Authenticates users against a Keycloak server using OpenID Connect</li>
 *   <li><b>Role Mapping</b>: Maps Keycloak realm roles to Spring Security authorities</li>
 *   <li><b>Custom Redirects</b>: Redirects users based on their roles after login</li>
 *   <li><b>Global Logout</b>: Implements single sign-out with Keycloak</li>
 * </ul>
 */
@Configuration
@EnableReactiveMethodSecurity
public class OAuth2SecurityConfig {

  @Bean
  public SecurityWebFilterChain securityWebFilterChain(
      ServerHttpSecurity http,
      JwtLoggingFilter jwtLoggingFilter) {

    return http
        .csrf(CsrfSpec::disable) // service-to-service communication, token secured
        .addFilterAfter(jwtLoggingFilter, SecurityWebFiltersOrder.AUTHENTICATION)
        .authorizeExchange(exchange -> exchange
            .pathMatchers("/actuator/**").permitAll()
            .anyExchange().authenticated()
        )
        .oauth2ResourceServer(oauth2 -> oauth2
            .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter()))
        )
        .build();
  }

  /**
   * JWT authentication converter that maps the re roles from the JWT token to the Spring Security authorities.
   */
  private Converter<Jwt, Mono<AbstractAuthenticationToken>> jwtAuthenticationConverter() {
    JwtGrantedAuthoritiesConverter scopesConverter = new JwtGrantedAuthoritiesConverter();
    scopesConverter.setAuthorityPrefix("SCOPE_"); // optional

    return jwt -> {
      Collection<GrantedAuthority> authorities = new ArrayList<>(scopesConverter.convert(jwt));

      Map<String, Object> realmAccess = jwt.getClaim("realm_access");
      if (realmAccess != null && realmAccess.get("roles") instanceof Collection<?> roles) {
        roles.forEach(role -> authorities.add(new SimpleGrantedAuthority("ROLE_" + role)));
      }

      return Mono.just(new JwtAuthenticationToken(jwt, authorities));
    };
  }
}

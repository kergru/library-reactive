package org.kergru.library.security;

import java.net.URI;
import java.time.Duration;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity.CsrfSpec;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcReactiveOAuth2UserService;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.web.server.logout.OidcClientInitiatedServerLogoutSuccessHandler;
import org.springframework.security.oauth2.client.registration.ReactiveClientRegistrationRepository;
import org.springframework.security.oauth2.client.userinfo.ReactiveOAuth2UserService;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.WebFilterExchange;
import org.springframework.security.web.server.authentication.RedirectServerAuthenticationSuccessHandler;
import org.springframework.security.web.server.authentication.logout.ServerLogoutSuccessHandler;
import org.springframework.security.web.server.savedrequest.NoOpServerRequestCache;
import org.springframework.security.web.server.util.matcher.NegatedServerWebExchangeMatcher;
import org.springframework.security.web.server.util.matcher.PathPatternParserServerWebExchangeMatcher;
import org.springframework.web.server.WebSession;
import reactor.core.publisher.Mono;

@Configuration
@EnableWebFluxSecurity
public class OAuth2LoginSecurityConfig {

  @Bean
  public SecurityWebFilterChain springSecurityFilterChain(
      ServerHttpSecurity http,
      ReactiveClientRegistrationRepository clientRegistrationRepository) {

    //browser-to-service communication with session cookie, csrf per default enabled
    return http
        .authorizeExchange(exchanges -> exchanges
            .pathMatchers("/", "/public/**").permitAll()
            .pathMatchers("/library/ui/admin/**").hasAuthority("ROLE_LIBRARIAN")
            .anyExchange().authenticated()
        )
        .oauth2Login(oauth2 -> oauth2
            .authenticationSuccessHandler(getLoginSuccessHandler())
        )
        .csrf(CsrfSpec::disable) // hack cause csrf is also set for logout
        .logout(logout -> logout
            .logoutUrl("/logout")
            .logoutSuccessHandler(getLogoutSuccessHandler(clientRegistrationRepository))
        )
        .oauth2Client(withDefaults -> {
        })
        .exceptionHandling(exception -> exception
            .accessDeniedHandler((exchange, ex) -> {
              ServerHttpResponse response = exchange.getResponse();
              response.setStatusCode(HttpStatus.SEE_OTHER);
              response.getHeaders().setLocation(URI.create("/error/403"));
              return response.setComplete();
            })
        )
        .build();
  }

  private RedirectServerAuthenticationSuccessHandler getLoginSuccessHandler() {
    return new RedirectServerAuthenticationSuccessHandler() {
      @Override
      public Mono<Void> onAuthenticationSuccess(WebFilterExchange webFilterExchange, Authentication authentication) {
        boolean isLibrarian = authentication.getAuthorities().stream()
            .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("ROLE_LIBRARIAN"));

        if (isLibrarian) {
          setLocation(URI.create("/library/ui/admin/users"));
        } else {
          setLocation(URI.create("/library/ui/me"));
        }
        setRequestCache(NoOpServerRequestCache.getInstance());
        return super.onAuthenticationSuccess(webFilterExchange, authentication);
      }
    };
  }

  private ServerLogoutSuccessHandler getLogoutSuccessHandler(
      ReactiveClientRegistrationRepository repo) {

    OidcClientInitiatedServerLogoutSuccessHandler oidcHandler =
        new OidcClientInitiatedServerLogoutSuccessHandler(repo);
    oidcHandler.setPostLogoutRedirectUri("http://localhost:8080/login");

    return (exchange, authentication) -> Mono.defer(() -> {
      System.out.println("Logout success handler");
          return exchange.getExchange().getSession()
              .flatMap(WebSession::invalidate)
              .then(Mono.fromRunnable(() -> exchange.getExchange().getResponse().addCookie(
                  ResponseCookie.from("JSESSIONID", "")
                      .maxAge(Duration.ZERO)
                      .path("/")
                      .build()
              )))
              .then(oidcHandler.onLogoutSuccess(exchange, authentication)); // Keycloak-Logout-Redirect
        }
    );
  }

  /**
   * Customizes the OidcUser to include roles from the realm_access claim. Spring will detect this bean if defined and include it in the oauth2Login configuration.
   */
  @Bean
  public ReactiveOAuth2UserService<OidcUserRequest, OidcUser> keycloakOidcUserService() {
    final var delegate = new OidcReactiveOAuth2UserService();
    return (userRequest) -> delegate.loadUser(userRequest).map(oidcUser -> {
      //log OidcUserService call
      System.out.println("Response of OidcUserService.loadUser: " + oidcUser);

      var authorities = new HashSet<GrantedAuthority>(oidcUser.getAuthorities());
      var realmAccess = oidcUser.<Map<String, Object>>getClaim("realm_access");
      if (realmAccess != null) {
        @SuppressWarnings("unchecked")
        var roles = (List<String>) realmAccess.get("roles");
        if (roles != null) {
          roles.stream()
              .map(roleName -> "ROLE_" + roleName)
              .map(SimpleGrantedAuthority::new)
              .forEach(authorities::add);
        }
      }
      return new DefaultOidcUser(authorities, oidcUser.getIdToken(), oidcUser.getUserInfo());
    });
  }
}

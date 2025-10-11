package org.kergru.library.util;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.InMemoryReactiveClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.ReactiveClientRegistrationRepository;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;

/**
 * Mock OAuth client configuration.
 * Defines a mock OAuth client for testing purposes which does not require a real OAuth server.
 * The mock client is used to simulate the OAuth flow and provide a JWT token for authentication.
 */
@TestConfiguration
public class MockOAuthClientConfig {

  @Bean
  public JwtDecoder jwtDecoder() {
    return token -> {
      List<String> roles = "librarian".equals(token)
          ? List.of("librarian")
          : Collections.emptyList();

      return Jwt.withTokenValue(token)
          .header("alg", "none")
          .claim("sub", "test-user")
          .claim("preferred_username", token)
          .claim("realm_access", Map.of("roles", roles))
          .issuedAt(Instant.now())
          .expiresAt(Instant.now().plusSeconds(3600))
          .build();
    };
  }

  @Bean
  public ReactiveClientRegistrationRepository reactiveClientRegistrationRepository() {
    ClientRegistration mockRegistration = ClientRegistration.withRegistrationId("keycloak")
        .clientId("library-frontend")
        .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
        .authorizationUri("http://mock/auth")
        .tokenUri("http://mock/token")
        .redirectUri("{baseUrl}/login/oauth2/code/keycloak")
        .scope("openid")
        .build();

    return new InMemoryReactiveClientRegistrationRepository(mockRegistration);
  }
}

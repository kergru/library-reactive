package org.kergru.library.security.logging;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.endpoint.OAuth2AuthorizationCodeGrantRequest;
import org.springframework.security.oauth2.client.endpoint.ReactiveOAuth2AccessTokenResponseClient;
import org.springframework.security.oauth2.client.endpoint.WebClientReactiveAuthorizationCodeTokenResponseClient;
import org.springframework.security.oauth2.core.endpoint.OAuth2AccessTokenResponse;

/**
 * OAuth2 token debug configuration.
 * Defines a custom token response client that logs the PKCE process and the access token.
 */
@Configuration
public class OAuth2TokenDebugConfig {

  /**
   * Custom token response client that logs the access token and its properties.
   */
  @Bean
  public ReactiveOAuth2AccessTokenResponseClient<OAuth2AuthorizationCodeGrantRequest> reactiveTokenResponseClient() {
    WebClientReactiveAuthorizationCodeTokenResponseClient delegate =
        new WebClientReactiveAuthorizationCodeTokenResponseClient();

    return (grantRequest) -> {
      // PKCE-Verifier loggen
      Object verifier = grantRequest.getAuthorizationExchange()
          .getAuthorizationRequest()
          .getAttributes()
          .get("code_verifier");
      System.out.println("PKCE: code_verifier=" + verifier);
      System.out.println("PKCE: code=" + grantRequest.getAuthorizationExchange().getAuthorizationResponse().getCode());
      System.out.println("PKCE: state=" + grantRequest.getAuthorizationExchange().getAuthorizationResponse().getState());

      // Token-Request durchführen
      return delegate.getTokenResponse(grantRequest)
          .doOnNext((OAuth2AccessTokenResponse resp) -> {
            // Access Token loggen
            System.out.println("PKCE response accesstoken: " + resp.getAccessToken().getTokenValue());

            // Optional: Ablaufdatum, Scopes, Token Typ
            System.out.println("Token Expires At: " + resp.getAccessToken().getExpiresAt());
            System.out.println("Token Scopes: " + resp.getAccessToken().getScopes());
            System.out.println("Token Type: " + resp.getAccessToken().getTokenType());
          });
    };
  }
}

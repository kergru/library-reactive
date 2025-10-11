package org.kergru.library.client;

import org.kergru.library.client.logging.LoggingExchangeFilterFunction;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.ReactiveOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.ReactiveOAuth2AuthorizedClientProvider;
import org.springframework.security.oauth2.client.ReactiveOAuth2AuthorizedClientProviderBuilder;
import org.springframework.security.oauth2.client.registration.ReactiveClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.DefaultReactiveOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.web.server.ServerOAuth2AuthorizedClientRepository;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * WebClient configuration with OAuth2 and logging interceptors.
 */
@Configuration
public class OAuth2WebClientConfig {

  /**
   * Responsible for Token Lifecycle Management
   * Defines supported token flows:
   * - Authorization Code Flow
   * - Refresh Token Flow
   */
  @Bean
  ReactiveOAuth2AuthorizedClientManager authorizedClientManager(
      ReactiveClientRegistrationRepository clientRegistrations,
      ServerOAuth2AuthorizedClientRepository authorizedClients) {

    ReactiveOAuth2AuthorizedClientProvider authorizedClientProvider =
        ReactiveOAuth2AuthorizedClientProviderBuilder.builder()
            .authorizationCode()
            .refreshToken()
            .build();

    DefaultReactiveOAuth2AuthorizedClientManager manager =
        new DefaultReactiveOAuth2AuthorizedClientManager(clientRegistrations, authorizedClients);
    manager.setAuthorizedClientProvider(authorizedClientProvider);

    return manager;
  }

  /**
   * Configures WebClient with OAuth2 and logging interceptors.
   */
  @Bean
  WebClient oauth2WebClient(
      OAuth2ExchangeFilterFunction oauth2Interceptor,
      LoggingExchangeFilterFunction loggingInterceptor,
      @Value("${library.backend.baseUrl}") String backendBaseUrl) {

    return WebClient.builder()
        .baseUrl(backendBaseUrl)
        .filter(oauth2Interceptor) // interceptor for adding access token
        .filter(loggingInterceptor) // interceptor for logging
        .build();
  }
}

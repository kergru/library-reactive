package org.kergru.library.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.ReactiveOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.ReactiveOAuth2AuthorizedClientProvider;
import org.springframework.security.oauth2.client.ReactiveOAuth2AuthorizedClientProviderBuilder;
import org.springframework.security.oauth2.client.registration.ReactiveClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.DefaultReactiveOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.web.reactive.function.client.ServerOAuth2AuthorizedClientExchangeFilterFunction;
import org.springframework.security.oauth2.client.web.server.ServerOAuth2AuthorizedClientRepository;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class  OAuth2ClientConfig {

  private static final Logger logger = LoggerFactory.getLogger(OAuth2ClientConfig.class);

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

  @Bean
  WebClient oauth2WebClient(
      ReactiveOAuth2AuthorizedClientManager authorizedClientManager,
      @Value("${library.backend.baseUrl}") String backendBaseUrl) {

    ServerOAuth2AuthorizedClientExchangeFilterFunction oauth2Filter =
        new ServerOAuth2AuthorizedClientExchangeFilterFunction(authorizedClientManager);
    oauth2Filter.setDefaultOAuth2AuthorizedClient(true);
    oauth2Filter.setDefaultClientRegistrationId("keycloak");

    ExchangeFilterFunction logRequest = (clientRequest, next) -> {

      logRequest(clientRequest);

      return next.exchange(clientRequest)
          .doOnError(error -> logger.error("Failed to send request to " + clientRequest.url(), error));
    };

    return WebClient.builder()
        .baseUrl(backendBaseUrl)
        .filter(oauth2Filter)
        .filter(logRequest)
        .build();
  }

  private static void logRequest(ClientRequest clientRequest) {
    System.out.println("Outgoing ResourceServer Request to " + clientRequest.url());
    // Authorization Header auslesen
    String authHeader = clientRequest.headers().getFirst("Authorization");
    if (authHeader != null) {
      System.out.println("Bearer token: " + authHeader);
    } else {
      System.out.println("No Authorization header present");
    }
  }
}

package org.kergru.library.client;

import org.springframework.security.oauth2.client.ReactiveOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.web.reactive.function.client.ServerOAuth2AuthorizedClientExchangeFilterFunction;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.ExchangeFunction;
import reactor.core.publisher.Mono;

/**
 * Interceptor to add access token to requests
 */
@Component
public class OAuth2ExchangeFilterFunction implements ExchangeFilterFunction {

  private final ServerOAuth2AuthorizedClientExchangeFilterFunction delegate;

  public OAuth2ExchangeFilterFunction(ReactiveOAuth2AuthorizedClientManager authorizedClientManager) {
    this.delegate = new ServerOAuth2AuthorizedClientExchangeFilterFunction(authorizedClientManager);
    this.delegate.setDefaultOAuth2AuthorizedClient(true);
    this.delegate.setDefaultClientRegistrationId("keycloak");
  }

  @Override
  public Mono<ClientResponse> filter(ClientRequest request, ExchangeFunction next) {
    return delegate.filter(request, next);
  }
}
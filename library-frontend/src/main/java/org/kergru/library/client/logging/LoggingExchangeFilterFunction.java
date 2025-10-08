package org.kergru.library.client.logging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.ExchangeFunction;
import reactor.core.publisher.Mono;

@Component
public class LoggingExchangeFilterFunction implements ExchangeFilterFunction {

  private static final Logger logger = LoggerFactory.getLogger(LoggingExchangeFilterFunction.class);

  @Override
  public Mono<ClientResponse> filter(ClientRequest request, ExchangeFunction next) {

    logRequest(request);

    return next.exchange(request)
        .doOnError(error -> logger.error("Failed to send request to {}", request.url(), error));
  }

  private void logRequest(ClientRequest request) {
    System.out.println("LibraryBackendClient: Outgoing request to " + request.url());

    String authHeader = request.headers().getFirst("Authorization");
    if (authHeader != null) {
      System.out.println("Authorization header present: " + authHeader);
    } else {
      System.out.println("No Authorization header present for request to " + request.url());
    }
  }
}
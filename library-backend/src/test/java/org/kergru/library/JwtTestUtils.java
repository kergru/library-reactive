package org.kergru.library;

import com.nimbusds.jwt.SignedJWT;
import java.time.Duration;
import java.util.Map;
import org.springframework.http.MediaType;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.util.retry.Retry;

public class JwtTestUtils {

  // ---- Helper: Wait for JWKS to be available ----
  public static void waitForJwks() {
    WebClient webClient = WebClient.create();
    String jwksUrl = "http://localhost:8085/realms/library/protocol/openid-connect/certs";

    webClient.get()
        .uri(jwksUrl)
        .retrieve()
        .bodyToMono(String.class)
        .retryWhen(Retry.backoff(30, Duration.ofSeconds(1))
            .doBeforeRetry(retrySignal ->
                System.out.println("Waiting for JWKS... (try " + (retrySignal.totalRetries() + 1) + ")"))
            .onRetryExhaustedThrow((spec, signal) ->
                new RuntimeException("JWKS not available after " + signal.totalRetries() + " retries")))
        .doOnSuccess(resp -> System.out.println("JWKS available."))
        .block();
  }

  // ---- Helper: Retrieve access token from Keycloak ----
  public static String getAccessToken(String username, String password) {
    MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
    form.add("client_id", "library-frontend");
    form.add("client_secret", "secret");
    form.add("grant_type", "password");
    form.add("username", username);
    form.add("password", password);

    WebClient tokenClient = WebClient.create();

    return tokenClient.post()
        .uri("http://localhost:8085/realms/library/protocol/openid-connect/token")
        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
        .bodyValue(form)
        .retrieve()
        .bodyToMono(Map.class)
        .map(response -> {
          String token = (String) response.get("access_token");
          System.out.println("Token: " + token);
          try {
            SignedJWT jwt = SignedJWT.parse(token);
            System.out.println(jwt.getJWTClaimsSet().toJSONObject());
          } catch (Exception e) {
            e.printStackTrace();
          }
          return token;
        })
        .block();
  }
}

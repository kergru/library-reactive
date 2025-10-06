# Library Reactive Stack – Example Implementation for OAuth2 Authentication (with PKCE)
This project is a reactive library management system built with Spring WebFlux, Spring Security (Reactive), and OAuth2 / OpenID Connect.
It demonstrates a non-blocking architecture using the Authorization Code Flow with PKCE, backed by Keycloak as the identity provider.

Hint:
Although this example implementation uses Reactive WebFlux, it still renders blocking Thymeleaf templates.
Even though server-side template rendering does not represent a public client, it is nevertheless treated as a public client by enabling PKCE.

## Project Structure (MonoRepo)
| Module                   | Description                                                              |
| ------------------------ | ------------------------------------------------------------------------ |
| **library-commons**      | Shared domain classes, DTOs, and utilities                               |
| **library-backend**      | Reactive backend service acting as an OAuth2 resource server             |
| **library-frontend**     | Reactive frontend web application acting as an OAuth2 client (with PKCE) |

## Components Overwiev
| Layer                             | Component                                               | Purpose                                                                                                                              |
|-----------------------------------|---------------------------------------------------------|--------------------------------------------------------------------------------------------------------------------------------------|
| **Frontend (`library-frontend`)** | Spring Boot WebFlux + Reactive OAuth2 Client (with PKCE) | Authenticates users via OIDC using the Authorization Code Flow with PKCE, then calls the backend using `WebClient` and Bearer Tokens |
| **Backend (`library-backend`)**   | Spring Boot WebFlux Resource Server                     | Validates incoming JWTs using a `ReactiveJwtDecoder` against the JWKS from the authorization server                                  |
| **Authorization Server** (docker) | Keycloak                                                | Performs login, issues Access/ID Tokens, and provides public keys via JWKS                                                           |
| **Database** (docker)             | MySQL                                                   | Stores Keycloak and Library data                                                                                                     |~~
| **User (Browser)**                | Web Client                                              | Interacts with the application and accesses protected data                                                                           |

## Core Spring Components per Module
| Module               | Area                     | Key Classes / Beans                                                                                                                             | Purpose                                                                                           |
| -------------------- | ------------------------ | ----------------------------------------------------------------------------------------------------------------------------------------------- | ------------------------------------------------------------------------------------------------- |
| **library-frontend** | **Security (Reactive)**  | `SecurityWebFilterChain` (with `.oauth2Login()` + PKCE),<br>`ReactiveClientRegistrationRepository`,<br>`ServerOAuth2AuthorizedClientRepository` | Handles OIDC login with PKCE, stores tokens reactively within the `ReactiveSecurityContextHolder` |
|                      | **Web / Controller**     | `@Controller`, `@GetMapping`, `Mono<Rendering>`                                                                                                 | Renders HTML using Thymeleaf Reactive or returns JSON responses                                   |
|                      | **Service / API Calls**  | `WebClient` + `ServerOAuth2AuthorizedClientExchangeFilterFunction`                                                                              | Calls the `library-backend` API reactively using Bearer Tokens                                    |
|                      | **Configuration**        | `application.yml` with `spring.security.oauth2.client.registration.*`                                                                           | Defines client ID, secret, scopes, redirect URIs, and PKCE enforcement                            |
| **library-backend**  | **Security (Reactive)**  | `SecurityWebFilterChain` (with `.oauth2ResourceServer().jwt()`),<br>`ReactiveJwtDecoder` (Nimbus),<br>`ReactiveJwtAuthenticationConverter`      | Reactively validates JWTs and maps claims to authorities                                          |
|                      | **Web / REST API**       | `@RestController`, `@GetMapping`, `Mono<ResponseEntity<?>>`                                                                                     | Exposes protected reactive REST endpoints (e.g. `/api/books`, `/api/users`)                       |
|                      | **Data / Service Layer** | `@Service`, `ReactiveCrudRepository`, R2DBC entities                                                                                            | Handles business logic and reactive database access                                               |
|                      | **Configuration**        | `application.yml` with `spring.security.oauth2.resourceserver.jwt.jwk-set-uri`                                                                  | Defines JWKS URI for token signature validation                                                   |

## Flow

```mermaid
flowchart BT
    %% --- FRONTEND ---
    subgraph FRONTEND["💻 library-frontend (Spring WebFlux + OAuth2 Client + PKCE)"]
    F1["SecurityWebFilterChain + oauth2Login()"]
    F2["WebClient (mit OAuth2AuthorizedClient)"]
    F3["Thymeleaf"]
    F4["PKCE: code_challenge, code_verifier"]
    end

    %% --- BACKEND ---
    subgraph BACKEND["⚙️ library-backend (Spring WebFlux Resource Server)"]
        B1["SecurityWebFilterChain + oauth2ResourceServer().jwt()"]
        B2["ReactiveJwtDecoder (Nimbus)"]
        B3["Reactive Controllers (/api/books, /api/users)"]
    end

    %% --- AUTH SERVER ---
    subgraph AUTH["🛡️ Authorization Server (Keycloak)"]
        A1["/authorize (mit PKCE)"]
        A2["/token (verifiziert code_verifier)"]
        A3["/.well-known/jwks.json"]
    end

    %% --- USER ---
    subgraph USER["👤 Benutzer / Browser"]
        U1["Browser"]
    end

    %% --- FLOWS ---
    U1 -->|"1️⃣ GET /books"| FRONTEND
    FRONTEND -->|"2️⃣ Redirect /authorize (mit code_challenge)"| AUTH
    AUTH -->|"3️⃣ Login"| U1
    AUTH -->|"4️⃣ Authorization Code"| FRONTEND
    FRONTEND -->|"5️⃣ POST /token (mit code_verifier)"| AUTH
    AUTH -->|"6️⃣ Access/ID Token"| FRONTEND
    FRONTEND -->|"7️⃣ WebClient -> /api/books (Bearer Token)"| BACKEND
    BACKEND -->|"8️⃣ Validate JWT via JWKS"| AUTH
    BACKEND -->|"9️⃣ JSON Books"| FRONTEND
    FRONTEND -->|"🏁 Render Thymeleaf Templates"| U1

    classDef comp fill:#f6f8fa,stroke:#ccc,stroke-width:1px,rx:8px,ry:8px;
    class FRONTEND,BACKEND,AUTH comp;
```
## Architecture

```mermaid
flowchart BT
    %% ===== MODULES =====
    subgraph FRONTEND["💻 library-frontend (Spring Boot WebFlux OAuth2 Client + PKCE + Reactive UI)"]
    FE1["OIDC Login (Authorization Code Flow + PKCE)"]
    FE2["Thymeleaf Rendering"]
    FE3["WebClient (Reactive, with Bearer Token)"]
    end

    subgraph BACKEND["⚙️ library-backend (Spring Boot WebFlux Resource Server)"]
        BE1["JWT Validation (ReactiveJwtDecoder - Nimbus)"]
        BE2["Protected Reactive REST Endpoints\n/api/books, /api/users"]
    end

    subgraph AUTH["🛡️ Authorization Server (Keycloak)"]
        AS1["/authorize (PKCE supported)"]
        AS2["/token (validates code_verifier)"]
        AS3["/.well-known/jwks.json (JWKS keys for signature validation)"]
    end

    subgraph USER["👤 User / Browser"]
        U1["Browser"]
    end

    %% ===== CONNECTIONS =====
    U1 -->|"HTTP: GET /books"| FRONTEND
    FRONTEND -->|"OIDC Redirect: /authorize (with code_challenge)"| AUTH
    AUTH -->|"Authorization Code + Tokens (Access/ID)"| FRONTEND
    FRONTEND -->|"Reactive REST Call: GET /api/books\nAuthorization: Bearer <token>"| BACKEND
    BACKEND -->|"JWT validation via JWKS"| AUTH
    BACKEND -->|"JSON Response (books, users, etc.)"| FRONTEND
    FRONTEND -->|"Renders Thymeleaf"| U1

    %% ===== STYLES =====
    classDef comp fill:#f6f8fa,stroke:#aaa,stroke-width:1px,rx:8px,ry:8px;
    class FRONTEND,BACKEND,AUTH comp;
```
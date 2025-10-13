# Library Reactive Stack – Example Implementation for OAuth2 Authentication (with PKCE)

This project is a reactive library management system built with Spring WebFlux, Spring Security (Reactive), and OAuth2 / OpenID Connect.
It demonstrates a non-blocking architecture using the Authorization Code Flow with PKCE, backed by Keycloak as the identity provider.

Hint:
Although this example implementation uses Reactive WebFlux, it still renders blocking Thymeleaf templates.
Even though server-side template rendering does not represent a public client, it is nevertheless treated as a public client by enabling PKCE.

## Project Structure (MonoRepo)

| Module               | Description                                                              |
|----------------------|--------------------------------------------------------------------------|
| **library-commons**  | Shared domain classes, DTOs, and utilities                               |
| **library-backend**  | Reactive backend service acting as an OAuth2 resource server             |
| **library-frontend** | Reactive frontend web application acting as an OAuth2 client (with PKCE) |

## Components Overview

| Layer                             | Component                                                | Purpose                                                                                                                              |
|-----------------------------------|----------------------------------------------------------|--------------------------------------------------------------------------------------------------------------------------------------|
| **Frontend (`library-frontend`)** | Spring Boot WebFlux + Reactive OAuth2 Client (with PKCE) | Authenticates users via OIDC using the Authorization Code Flow with PKCE, then calls the backend using `WebClient` and Bearer Tokens |
| **Backend (`library-backend`)**   | Spring Boot WebFlux Resource Server                      | Validates incoming JWTs using a `ReactiveJwtDecoder` against the JWKS from the authorization server                                  |
| **Authorization Server** (docker) | Keycloak                                                 | Performs login, issues Access/ID Tokens, and provides public keys via JWKS                                                           |
| **Database** (docker)             | MySQL                                                    | Stores Keycloak and Library data                                                                                                     |~~
| **User (Browser)**                | Web Client                                               | Interacts with the application and accesses protected data                                                                           |

## Core Spring Components per Module

| Module               | Area                     | Key Classes / Beans                                                                                                                             | Purpose                                                                                           |
|----------------------|--------------------------|-------------------------------------------------------------------------------------------------------------------------------------------------|---------------------------------------------------------------------------------------------------|
| **library-frontend** | **Security (Reactive)**  | `SecurityWebFilterChain` (with `.oauth2Login()` + PKCE),<br>`ReactiveClientRegistrationRepository`,<br>`ServerOAuth2AuthorizedClientRepository` | Handles OIDC login with PKCE, stores tokens reactively within the `ReactiveSecurityContextHolder` |
|                      | **Web / Controller**     | `@Controller`, `@GetMapping`, `Mono<Rendering>`                                                                                                 | Renders HTML using Thymeleaf Reactive or returns JSON responses                                   |
|                      | **Service / API Calls**  | `WebClient` + `ServerOAuth2AuthorizedClientExchangeFilterFunction`                                                                              | Calls the `library-backend` API reactively using Bearer Tokens                                    |
|                      | **Configuration**        | `application.yml` with `spring.security.oauth2.client.registration.*`                                                                           | Defines client ID, secret, scopes, redirect URIs, and PKCE enforcement                            |
| **library-backend**  | **Security (Reactive)**  | `SecurityWebFilterChain` (with `.oauth2ResourceServer().jwt()`),<br>`ReactiveJwtDecoder` (Nimbus),<br>`ReactiveJwtAuthenticationConverter`      | Reactively validates JWTs and maps claims to authorities                                          |
|                      | **Web / REST API**       | `@RestController`, `@GetMapping`, `Mono<ResponseEntity<?>>`                                                                                     | Exposes protected reactive REST endpoints (e.g. `/api/books`, `/api/users`)                       |
|                      | **Data / Service Layer** | `@Service`, `ReactiveCrudRepository`, R2DBC entities                                                                                            | Handles business logic and reactive database access                                               |
|                      | **Configuration**        | `application.yml` with `spring.security.oauth2.resourceserver.jwt.jwk-set-uri`                                                                  | Defines JWKS URI for token signature validation                                                   |

## Architecture Diagram

```mermaid
flowchart TB
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
    subgraph AUTH["🛡️ Authorization Server (Keycloak / OIDC Provider)"]
        A0["/.well-known/openid-configuration"]
        A1["/authorize (mit PKCE, scope=openid profile email)"]
        A2["/token (verifiziert code_verifier)"]
        A3["/userinfo (Access Token → User Claims)"]
        A4["/.well-known/jwks.json"]
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
    AUTH -->|"6️⃣ Access Token + ID Token (OIDC)"| FRONTEND
    FRONTEND -->|"7️⃣ (optional) /userinfo (Bearer Token)"| AUTH
    FRONTEND -->|"8️⃣ WebClient → /api/books (Bearer Token)"| BACKEND
    BACKEND -->|"9️⃣ Validate JWT via JWKS"| AUTH
    BACKEND -->|"🔟 JSON Books"| FRONTEND
    FRONTEND -->|"🏁 Render Thymeleaf Templates"| U1

%% --- STYLING ---
    classDef comp fill:#f6f8fa,stroke:#ccc,stroke-width:1px,rx:8px,ry:8px;
    class FRONTEND comp;
    class BACKEND comp;
    class AUTH comp;

```
## OAuth2 Sequence Diagram

```mermaid
sequenceDiagram
    participant User
    participant Frontend as Library-Frontend (OAuth2 Client, Thymeleaf)
    participant Auth as Keycloak (Auth Server)
    participant Backend as Library-Backend (Resource Server)

%% 1. Login Flow (handled entirely in Frontend)
    User->>+Frontend: 1. Clicks Login
    Frontend->>+Frontend: 2. Generate PKCE code_verifier
    Frontend->>+Frontend: 3. Create code_challenge (S256)

    Frontend->>+Auth: 4. Redirect to /auth/realms/.../auth
    Note right of Frontend: With:<br>- client_id<br>- response_type=code<br>- code_challenge<br>- code_challenge_method=S256<br>- redirect_uri<br>- state

    Auth->>+User: 5. Shows login form
    User->>+Auth: 6. Enters credentials
    Auth->>+Frontend: 7. Redirect with Authorization Code
    Note right of Auth: - code<br>- state

    Frontend->>+Auth: 8. Token Request
    Note right of Frontend: POST /token with:<br>- grant_type=authorization_code<br>- code<br>- redirect_uri<br>- client_id<br>- code_verifier

    Auth->>+Auth: 9. Validates code_verifier
    Auth-->>-Frontend: 10. Returns tokens
    Note left of Auth: - access_token (stored in session)<br>- refresh_token<br>- id_token

    Frontend->>+Frontend: 11. Store tokens in session
    Frontend-->>-User: 12. Display protected page

%% 2. Protected Resource Access
    User->>+Frontend: 13. Requests another protected page
    Note left of User: GET /library/ui/page<br>Cookie: JSESSIONID=...

    Frontend->>+Frontend: 14. Get access_token from session
    Frontend->>+Backend: 15. Request protected resource from backend
    Note right of Frontend: GET /library/api/resource<br>Authorization: Bearer <access_token>

    Backend->>+Auth: 16. Validate token (JWKS)
    Auth-->>-Backend: 17. Token info + scopes

    Backend-->>-Frontend: 18. Send protected data
    Frontend->>+Frontend: 19. Render Thymeleaf template
    Frontend-->>-User: 20. Display protected page
```
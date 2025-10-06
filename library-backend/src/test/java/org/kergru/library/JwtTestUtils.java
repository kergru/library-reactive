package org.kergru.library;

import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.mockJwt;

import java.util.List;
import java.util.Map;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.JwtMutator;

public class JwtTestUtils {

  public static JwtMutator createMockJwt(String username) {
    return mockJwt()
        .jwt(jwt -> jwt
            .claim("sub", username)
            .claim("preferred_username", username)
            .claim("scope", "profile email")
            .issuer("http://localhost:8085/realms/library")
        );
  }

  public static JwtMutator createMockJwtWithRoleLibrarian(String username) {
    return mockJwt()
        // set authorities directly cause jwtAuthenticationConverter is not called when using mockJwt()
        .authorities(new SimpleGrantedAuthority("ROLE_LIBRARIAN"))
        .jwt(jwt -> jwt
            .claim("sub", username)
            .claim("preferred_username", username)
            .claim("realm_access", Map.of("roles", List.of("LIBRARIAN")))
            .claim("scope", "profile email")
            .issuer("http://localhost:8085/realms/library")
        );
  }
}

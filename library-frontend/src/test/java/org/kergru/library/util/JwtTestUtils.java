package org.kergru.library.util;

import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.mockOidcLogin;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.OidcLoginMutator;

public class JwtTestUtils {

  /**
   * Helper method to mock OidcLogin
   *
   * @return
   */
  public static OidcLoginMutator createMockOidcLoginForUser(String username) {
    return mockOidcLogin()
        .idToken(token -> token
            .claim("sub", username)
            .claim("preferred_username", username)
        );
  }

  /**
   * Helper method to mock OidcLogin for librarian (add role librarian to the jwt)
   *
   * @return
   */
  public static OidcLoginMutator createMockOidcLoginForLibrarian(String username) {
    return mockOidcLogin()
        .idToken(token -> token
            .claim("sub", username)
            .claim("preferred_username", username)
        )
        .authorities(new SimpleGrantedAuthority("ROLE_LIBRARIAN"));
  }
}

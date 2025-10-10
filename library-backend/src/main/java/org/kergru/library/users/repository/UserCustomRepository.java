package org.kergru.library.users.repository;

import org.springframework.data.domain.Page;
import reactor.core.publisher.Mono;

public interface UserCustomRepository {

  Mono<Page<UserEntity>> searchUsers(String searchString, int page, int size, String sortBy);
}

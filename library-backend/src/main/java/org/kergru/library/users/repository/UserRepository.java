package org.kergru.library.users.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface UserRepository extends ReactiveCrudRepository<UserEntity, Long> {

  Mono<UserEntity> findByUsername(String username);

  @Query("""
    SELECT u.*
    FROM users u
    WHERE 
        (:searchString IS NULL OR 
         u.username LIKE CONCAT('%', :searchString, '%') OR 
         u.firstname LIKE CONCAT('%', :searchString, '%') OR 
         u.lastname LIKE CONCAT('%', :searchString, '%') OR 
         u.email LIKE CONCAT('%', :searchString, '%'))
    ORDER BY 
        CASE WHEN :sortBy = 'userName' THEN u.username END ASC,
        CASE WHEN :sortBy = 'firstName' THEN u.firstname END ASC,
        CASE WHEN :sortBy = 'lastName' THEN u.lastname END ASC,
        CASE WHEN :sortBy = 'email' THEN u.email END ASC
    LIMIT :size OFFSET :offset
    """)
  Flux<UserEntity> searchUsers(
      @Param("searchString") String searchString,
      @Param("size") int size,
      @Param("offset") long offset,
      @Param("sortBy") String sortBy
  );

  @Query("""
    SELECT COUNT(u.id) FROM users u
    WHERE 
        (:searchString IS NULL OR 
         u.username LIKE CONCAT('%', :searchString, '%') OR 
         u.firstname LIKE CONCAT('%', :searchString, '%') OR 
         u.lastname LIKE CONCAT('%', :searchString, '%') OR 
         u.email LIKE CONCAT('%', :searchString, '%'))
    """)
  Mono<Long> countUsers(@Param("searchString") String searchString);

  default Mono<Page<UserEntity>> searchUsersPaged(String searchString, int page, int size, String sortBy) {
    String search = StringUtils.hasText(searchString) ? searchString : null;
    long offset = (long) page * size;

    return searchUsers(search, size, offset, sortBy)
        .collectList()
        .zipWith(countUsers(search))
        .map(tuple -> new PageImpl<>(
            tuple.getT1(),
            PageRequest.of(page, size),
            tuple.getT2()
        ));
  }
}

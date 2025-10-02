package org.kergru.library.loans.repository;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;

public interface LoanRepository extends ReactiveCrudRepository<LoanEntity, Long> {

  @Query("""
    SELECT
        l.id AS id,
        l.user_id AS user_id,
        l.book_id AS book_id,
        l.borrowed_at AS borrowed_at,
        l.returned_at AS returned_at,
        b.title AS title,
        b.author AS author,
        b.isbn AS isbn,
        b.publisher AS publisher,
        b.pages AS pages,
        b.published_at AS published_at,
        b.language AS language,
        b.description AS description
    FROM loans l
    JOIN books b ON l.book_id = b.id
    WHERE l.user_id = :userId
  """)
  Flux<LoanWithBookDto> findByUserIdWithBook(Long userId);
}

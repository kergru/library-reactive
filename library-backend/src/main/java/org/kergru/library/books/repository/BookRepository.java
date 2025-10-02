package org.kergru.library.books.repository;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface BookRepository extends ReactiveCrudRepository<BookEntity, Long> {

  @Query("""
    SELECT
        b.id AS id,
        b.isbn AS isbn,
        b.title AS title,
        b.author AS author,
        b.published_at AS publishedAt,
        b.publisher AS publisher,
        b.language AS language,
        b.description AS description,
        b.pages AS pages,
        l.id AS loan_id,
        l.borrowed_at AS borrowed_at,
        l.user_id AS borrower_id
    FROM books b
    LEFT JOIN loans l ON b.id = l.book_id AND l.returned_at IS NULL
    WHERE b.isbn = :isbn
  """)
  Mono<BookWithLoanDto> findByIsbnWithLoan(String isbn);

  @Query("""
    SELECT
        b.id AS id,
        b.isbn AS isbn,
        b.title AS title,
        b.author AS author,
        b.published_at AS publishedAt,
        b.publisher AS publisher,
        b.language AS language,
        b.description AS description,
        b.pages AS pages,
        l.id AS loan_id,
        l.borrowed_at AS borrowed_at,
        l.user_id AS borrower_id  
    FROM books b
    LEFT JOIN loans l ON b.id = l.book_id AND l.returned_at IS NULL
  """)
  Flux<BookWithLoanDto> findAllWithLoanStatus();
}


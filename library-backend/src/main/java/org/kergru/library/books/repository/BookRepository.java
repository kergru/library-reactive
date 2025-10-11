package org.kergru.library.books.repository;

import org.kergru.library.model.BookDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface BookRepository extends ReactiveCrudRepository<BookDto, Long> {

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
  Mono<BookWithLoanProjection> findByIsbn(String isbn);

  @Query("""
    SELECT b.*, l.id AS loan_id, l.borrowed_at AS borrowed_at, l.user_id AS borrower_id
    FROM books b
    LEFT JOIN loans l ON b.id = l.book_id AND l.returned_at IS NULL
    WHERE 
        (:searchString IS NULL OR 
         b.title LIKE CONCAT('%', :searchString, '%') OR 
         b.author LIKE CONCAT('%', :searchString, '%') OR 
         b.isbn LIKE CONCAT('%', :searchString, '%'))
    ORDER BY 
        CASE WHEN :sortBy = 'title' THEN b.title END ASC,
        CASE WHEN :sortBy = 'author' THEN b.author END ASC,
        CASE WHEN :sortBy = 'isbn' THEN b.isbn END ASC
    LIMIT :size OFFSET :offset
    """)
  Flux<BookWithLoanProjection> searchBooksWithJoinLoans(
      @Param("searchString") String searchString,
      @Param("size") int size,
      @Param("offset") long offset,
      @Param("sortBy") String sortBy
  );

  @Query("""
    SELECT COUNT(b.id) FROM books b
    WHERE 
        (:searchString IS NULL OR 
         b.title LIKE CONCAT('%', :searchString, '%') OR 
         b.author LIKE CONCAT('%', :searchString, '%') OR 
         b.isbn LIKE CONCAT('%', :searchString, '%'))
    """)
  Mono<Long> countBooks(@Param("searchString") String searchString);

  default Mono<Page<BookWithLoanProjection>> searchBooksPaged(String searchString, int page, int size, String sortBy) {
    String search = StringUtils.hasText(searchString) ? searchString : null;
    long offset = (long) page * size;

    return searchBooksWithJoinLoans(search, size, offset, sortBy)
        .collectList()
        .zipWith(countBooks(search))
        .map(tuple -> new PageImpl<>(
            tuple.getT1(),
            PageRequest.of(page, size),
            tuple.getT2()
        ));
  }
}

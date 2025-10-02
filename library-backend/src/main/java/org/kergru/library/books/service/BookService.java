package org.kergru.library.books.service;

import org.kergru.library.books.repository.BookRepository;
import org.kergru.library.books.repository.BookWithLoanDto;
import org.kergru.library.model.BookDto;
import org.kergru.library.model.LoanStatusDto;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class BookService {

  private final BookRepository bookRepository;

  public BookService(BookRepository bookRepository) {
    this.bookRepository = bookRepository;
  }

  public Flux<BookDto> findAll() {
    return bookRepository.findAllWithLoanStatus().map(BookService::toDto);
  }

  public Mono<BookDto> findByIsbn(String isbn) {
    return bookRepository.findByIsbnWithLoan(isbn).map(BookService::toDto);
  }

  public static BookDto toDto(BookWithLoanDto b) {
    return new BookDto(
        b.getIsbn(),
        b.getTitle(),
        b.getAuthor(),
        b.getPublishedAt(),
        b.getPublisher(),
        b.getLanguage(),
        b.getPages(),
        b.getDescription(),
        new LoanStatusDto(
          b.getLoanId() != null,
          b.getLoanId() != null ? b.getBorrowerId() : null,
          b.getLoanId() != null ? b.getBorrowedAt() : null
        )
    );
  }
}


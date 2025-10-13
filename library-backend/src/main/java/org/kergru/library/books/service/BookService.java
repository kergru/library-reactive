package org.kergru.library.books.service;

import java.util.stream.Collectors;
import org.kergru.library.books.repository.BookRepository;
import org.kergru.library.books.repository.BookWithLoanProjection;
import org.kergru.library.model.BookDto;
import org.kergru.library.model.LoanStatusDto;
import org.kergru.library.model.PageResponseDto;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class BookService {

  private final BookRepository bookRepository;

  public BookService(BookRepository bookRepository) {
    this.bookRepository = bookRepository;
  }

  public Mono<PageResponseDto<BookDto>> searchBooks(String searchStr, int page, int size, String sortBy) {

    return bookRepository.searchBooksPaged(searchStr, page, size, sortBy)
        .map(p -> new PageResponseDto<>(
            p.getContent().stream().map(this::toDto).collect(Collectors.toList()),
            p.getNumber(),
            p.getSize(),
            p.getTotalPages(),
            p.getTotalElements(),
            p.isFirst(),
            p.isLast(),
            p.getNumberOfElements(),
            p.isEmpty()
        ));
  }

  public Mono<BookDto> findByIsbn(String isbn) {
    return bookRepository.findByIsbn(isbn).map(this::toDto);
  }

  private BookDto toDto(BookWithLoanProjection b) {

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
            b.getLoanId() == null,
            b.getLoanId() != null ? b.getBorrowerId() : null,
            b.getLoanId() != null ? b.getBorrowedAt() : null
        )
    );
  }
}

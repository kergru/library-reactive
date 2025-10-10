package org.kergru.library.books.rest;

import org.kergru.library.books.service.BookService;
import org.kergru.library.model.BookDto;
import org.kergru.library.model.PageResponseDto;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/library/api")
public class BookController {

  private final BookService bookService;

  public BookController(BookService bookService) {
    this.bookService = bookService;
  }

  /**
   * Returns paged search result of books by title, author, isbn
   */
  @PreAuthorize("isAuthenticated()")
  @GetMapping("/books")
  public Mono<PageResponseDto<BookDto>> searchBooks(
      @RequestParam(required = false) String searchString,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "10") int size,
      @RequestParam(defaultValue = "title") String sortBy
  ) {
    return bookService.searchBooks(searchString, page, size, sortBy);
  }
  /**
   * Returns a single book by ISBN
   */
  @PreAuthorize("isAuthenticated()")
  @GetMapping("/books/{isbn}")
  public Mono<BookDto> getBook(@PathVariable String isbn) {
    return bookService.findByIsbn(isbn)
        .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "Book not found")));
  }
}

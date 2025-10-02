package org.kergru.library.books.rest;

import org.kergru.library.model.BookDto;
import org.kergru.library.books.service.BookService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;


@RestController
@RequestMapping("/library/api")
public class BookController {

  private final BookService bookService;

  public BookController(BookService bookService) {
    this.bookService = bookService;
  }

  /**
   * Returns all books
   */
  @PreAuthorize("isAuthenticated()")
  @GetMapping("/books")
  public Flux<BookDto> getAllBooks() {
    return bookService.findAll();
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
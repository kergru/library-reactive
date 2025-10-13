package org.kergru.library.loans.service;

import java.time.Instant;
import java.util.NoSuchElementException;
import java.util.Objects;
import org.kergru.library.books.repository.BookRepository;
import org.kergru.library.books.repository.BookWithLoanProjection;
import org.kergru.library.loans.repository.LoanEntity;
import org.kergru.library.loans.repository.LoanRepository;
import org.kergru.library.loans.repository.LoanWithBookProjection;
import org.kergru.library.model.BookDto;
import org.kergru.library.model.LoanDto;
import org.kergru.library.users.repository.UserEntity;
import org.kergru.library.users.repository.UserRepository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class LoanService {

  private final LoanRepository loanRepository;

  private final UserRepository userRepository;

  private final BookRepository bookRepository;

  public LoanService(
      LoanRepository loanRepository,
      UserRepository userRepository,
      BookRepository bookRepository
  ) {
    this.loanRepository = loanRepository;
    this.userRepository = userRepository;
    this.bookRepository = bookRepository;
  }

  public Flux<LoanDto> getBorrowedBooksByUser(String userName) {
    return userRepository.findByUsername(userName)
        .switchIfEmpty(Mono.error(new NoSuchElementException("User not found")))
        .flatMapMany(user -> loanRepository.findByUserIdWithBook(user.id))
        .map(this::toDto)
        .doOnError(err -> System.err.println("Error in findBorrowedByUser: " + err.getMessage()));
  }

  public Mono<LoanDto> borrowBook(String isbn, String userName) {
    return userRepository.findByUsername(userName)
        .switchIfEmpty(Mono.error(new NoSuchElementException("User not found")))
        .zipWhen(u -> bookRepository.findByIsbn(isbn)
            .switchIfEmpty(Mono.error(new NoSuchElementException("Book not found"))))
        .flatMap(tuple -> {
          var user = tuple.getT1();
          var book = tuple.getT2();

          return loanRepository.existsByBookIdAndReturnedAtIsNull(book.getId())
              .flatMap(exists -> {
                if (exists) return Mono.error(new IllegalStateException("Book is already borrowed"));
                var loan = new LoanEntity();
                loan.userId = user.id;
                loan.bookId = book.getId();
                loan.borrowedAt = Instant.now();
                return loanRepository.save(loan)
                    .map(saved -> toDto(saved, book));
              });
        });
  }

  public Mono<Void> returnBook(Long loanId, String userName) {
    Mono<UserEntity> userMono = userRepository.findByUsername(userName)
        .switchIfEmpty(Mono.error(new NoSuchElementException("User not found")));

    Mono<LoanEntity> loanMono = loanRepository.findById(loanId)
        .switchIfEmpty(Mono.error(new NoSuchElementException("Loan not found")));

    return userMono.zipWith(loanMono)
        .flatMap(tuple -> {
          var user = tuple.getT1();
          var loan = tuple.getT2();

          if (!Objects.equals(loan.userId, user.id)) {
            return Mono.error(new IllegalStateException("User is not the owner of the loan"));
          }

          loan.returnedAt = Instant.now();
          return loanRepository.save(loan).then(); // Mono<Void>
        });
  }

  private LoanDto toDto(LoanEntity loan, BookWithLoanProjection book) {
    return new LoanDto(
        loan.id,
        new BookDto(
            book.getIsbn(),
            book.getTitle(),
            book.getAuthor(),
            book.getPublishedAt(),
            book.getPublisher(),
            book.getLanguage(),
            book.getPages(),
            book.getDescription(),
            null
        ),
        loan.borrowedAt,
        loan.returnedAt);
  }

  private LoanDto toDto(LoanWithBookProjection loan) {
    return new LoanDto(loan.getId(),
        new BookDto(
            loan.getIsbn(),
            loan.getTitle(),
            loan.getAuthor(),
            loan.getPublishedAt(),
            loan.getPublisher(),
            loan.getLanguage(),
            loan.getPages(),
            loan.getDescription(),
            null
        ),
        loan.getBorrowedAt(),
        loan.getReturnedAt());
  }
}

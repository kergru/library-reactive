package org.kergru.library.loans.service;

import java.util.NoSuchElementException;
import org.kergru.library.loans.repository.LoanRepository;
import org.kergru.library.loans.repository.LoanWithBookDto;
import org.kergru.library.model.BookDto;
import org.kergru.library.model.LoanDto;
import org.kergru.library.users.repository.UserRepository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class LoansService {

  private final LoanRepository loanRepository;

  private final UserRepository userRepository;

  public LoansService(LoanRepository loanRepository, UserRepository userRepository) {
    this.loanRepository = loanRepository;
    this.userRepository = userRepository;
  }

  public Flux<LoanDto> findBorrowedByUser(String userName) {
    return userRepository.findByUsername(userName)
        .switchIfEmpty(Mono.error(new NoSuchElementException("User not found")))
        .flatMapMany(u -> loanRepository.findByUserIdWithBook(u.id))
        .map(this::toDto)
        .doOnError(err -> System.err.println("Error in findBorrowedByUser: " + err.getMessage()));
  }

  private LoanDto toDto(LoanWithBookDto e) {
    return new LoanDto(
        new BookDto(
            e.getIsbn(),
            e.getTitle(),
            e.getAuthor(),
            e.getPublishedAt(),
            e.getPublisher(),
            e.getLanguage(),
            e.getPages(),
            e.getDescription(),
            null
        ),
        e.getBorrowedAt(),
        e.getReturnedAt()
    );
  }
}

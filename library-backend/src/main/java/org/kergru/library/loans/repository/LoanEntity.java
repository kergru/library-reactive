package org.kergru.library.loans.repository;

import java.time.Instant;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Table(name = "loans")
public class LoanEntity {

  @Id
  public Long id;

  public Long userId;

  public Long bookId;

  public Instant borrowedAt;

  public Instant returnedAt; // null => aktuell ausgeliehen

}


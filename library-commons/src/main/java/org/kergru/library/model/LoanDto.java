package org.kergru.library.model;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public record LoanDto(
    BookDto book,
    Instant borrowedAt,
    Instant returnedAt
) {

  public String getReturnedAtFormatted() {
    if (returnedAt == null) {
      return "";
    }
    return getDateFormatted(returnedAt);
  }

  public String getBorrowedAtFormatted() {
    if (borrowedAt == null) {
      return "";
    }
    return getDateFormatted(borrowedAt);
  }

  private String getDateFormatted(Instant date) {
    return DateTimeFormatter.ofPattern("dd-MM-yyyy")
        .withZone(ZoneId.systemDefault())
        .format(date);
  }
}

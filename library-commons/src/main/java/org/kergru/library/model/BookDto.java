package org.kergru.library.model;

public record BookDto(
    String isbn,
    String title,
    String author,
    Integer publishedAt,
    String publisher,
    String language,
    Integer pages,
    String description,
    LoanStatusDto loanStatus
) {}

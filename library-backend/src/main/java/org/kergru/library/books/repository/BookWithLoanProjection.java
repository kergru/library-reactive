package org.kergru.library.books.repository;

import java.time.Instant;

public class BookWithLoanProjection {

  private Long id;
  private String isbn;
  private String title;
  private String author;
  private Integer publishedAt;
  private String publisher;
  private String language;
  private String description;
  private Integer pages;

  private Long loanId;       // null if not borrowed
  private Instant borrowedAt; // null if not borrowed
  private Long borrowerId;    // null if not borrowed

  // --- Getter / Setter ---
  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getIsbn() {
    return isbn;
  }

  public void setIsbn(String isbn) {
    this.isbn = isbn;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public String getAuthor() {
    return author;
  }

  public void setAuthor(String author) {
    this.author = author;
  }

  public Integer getPublishedAt() {
    return publishedAt;
  }

  public void setPublishedAt(Integer publishedAt) {
    this.publishedAt = publishedAt;
  }

  public String getPublisher() {
    return publisher;
  }

  public void setPublisher(String publisher) {
    this.publisher = publisher;
  }

  public String getLanguage() {
    return language;
  }

  public void setLanguage(String language) {
    this.language = language;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public Integer getPages() {
    return pages;
  }

  public void setPages(Integer pages) {
    this.pages = pages;
  }

  public Long getLoanId() {
    return loanId;
  }

  public void setLoanId(Long loanId) {
    this.loanId = loanId;
  }

  public Instant getBorrowedAt() {
    return borrowedAt;
  }

  public void setBorrowedAt(Instant borrowedAt) {
    this.borrowedAt = borrowedAt;
  }

  public Long getBorrowerId() {
    return borrowerId;
  }

  public void setBorrowerId(Long borrowerId) {
    this.borrowerId = borrowerId;
  }

  public String toString() {
    return "BookWithLoanDto{" +
        "id=" + id + ", isbn" + isbn + ", loanId=" + loanId;
  }
}

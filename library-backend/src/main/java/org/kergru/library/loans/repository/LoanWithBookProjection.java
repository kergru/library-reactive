package org.kergru.library.loans.repository;

import java.time.Instant;

public class LoanWithBookProjection {

  private Long id;
  private Long userId;
  private Long bookId;
  private Instant borrowedAt;
  private Instant returnedAt;

  private String title;
  private String author;
  private String isbn;
  private String publisher;
  private Integer pages;
  private Integer publishedAt;
  private String language;
  private String description;

  // --- Getter / Setter ---
  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public Long getUserId() {
    return userId;
  }

  public void setUserId(Long userId) {
    this.userId = userId;
  }

  public Long getBookId() {
    return bookId;
  }

  public void setBookId(Long bookId) {
    this.bookId = bookId;
  }

  public Instant getBorrowedAt() {
    return borrowedAt;
  }

  public void setBorrowedAt(Instant borrowedAt) {
    this.borrowedAt = borrowedAt;
  }

  public Instant getReturnedAt() {
    return returnedAt;
  }

  public void setReturnedAt(Instant returnedAt) {
    this.returnedAt = returnedAt;
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

  public String getIsbn() {
    return isbn;
  }

  public void setIsbn(String isbn) {
    this.isbn = isbn;
  }

  public String getPublisher() {
    return publisher;
  }

  public void setPublisher(String publisher) {
    this.publisher = publisher;
  }

  public Integer getPages() {
    return pages;
  }

  public void setPages(Integer pages) {
    this.pages = pages;
  }

  public Integer getPublishedAt() {
    return publishedAt;
  }

  public void setPublishedAt(Integer publishedAt) {
    this.publishedAt = publishedAt;
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

  // --- Optional: toString() ---
  @Override
  public String toString() {
    return "LoanWithBookDto{" +
        "id=" + id +
        ", userId=" + userId +
        ", bookId=" + bookId +
        ", borrowedAt=" + borrowedAt +
        ", returnedAt=" + returnedAt +
        ", title='" + title + '\'' +
        ", isbn='" + isbn + '\'' +
        '}';
  }
}

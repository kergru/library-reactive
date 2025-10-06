package org.kergru.library.books.repository;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Table(name = "books")
public class BookEntity {

  @Id
  public Long id;

  public String isbn;

  public String title;

  public String author;

  public Integer publishedAt;

  public String publisher;

  public String language;

  public String description;

  public Integer pages;
}
package org.kergru.library.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record PageResponseDto<T>(
    List<T> content,
    int number,
    int size,
    int totalPages,
    long totalElements,
    boolean first,
    boolean last,
    int numberOfElements,
    boolean empty
) {
  @JsonCreator
  public PageResponseDto(
      @JsonProperty("content") List<T> content,
      @JsonProperty("number") int number,
      @JsonProperty("size") int size,
      @JsonProperty("totalPages") int totalPages,
      @JsonProperty("totalElements") long totalElements,
      @JsonProperty("first") boolean first,
      @JsonProperty("last") boolean last,
      @JsonProperty("numberOfElements") int numberOfElements,
      @JsonProperty("empty") boolean empty
  ) {
    this.content = content;
    this.number = number;
    this.size = size;
    this.totalPages = totalPages;
    this.totalElements = totalElements;
    this.first = first;
    this.last = last;
    this.numberOfElements = numberOfElements;
    this.empty = empty;
  }
}

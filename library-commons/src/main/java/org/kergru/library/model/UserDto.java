package org.kergru.library.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

public record UserDto(
    String userName,
    String firstName,
    String lastName,
    String email
) {

  @JsonIgnore
  public String getFullName() {
    return firstName + " " + lastName;
  }
}

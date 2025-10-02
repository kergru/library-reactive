package org.kergru.library.model;

public record UserDto(
    String userName,
    String firstName,
    String lastName,
    String email
) {
  public String getFullName() {
    return firstName + " " + lastName;
  }
}

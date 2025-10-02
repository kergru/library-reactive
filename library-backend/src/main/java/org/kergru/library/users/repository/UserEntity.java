package org.kergru.library.users.repository;


import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Table("users")
public class UserEntity {

  @Id
  public Long id;

  public String username;

  public String firstname;

  public String lastname;

  public String email;
}
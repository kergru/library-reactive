package org.kergru.library.users.service;

import org.kergru.library.model.UserDto;
import org.kergru.library.users.repository.UserEntity;
import org.kergru.library.users.repository.UserRepository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class UserService {

  private final UserRepository repository;

  public UserService(UserRepository repository) {
    this.repository = repository;
  }

  public Mono<UserDto> findUserByUserName(String userName) {
    return repository.findByUsername(userName).map(this::toDto);
  }

  public Flux<UserDto> findAll() {
    return repository.findAll().map(this::toDto);
  }

  private UserDto toDto(UserEntity e) {
    return new UserDto(
        e.username,
        e.firstname,
        e.lastname,
        e.email
    );
  }
}

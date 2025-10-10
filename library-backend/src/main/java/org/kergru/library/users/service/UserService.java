package org.kergru.library.users.service;

import java.util.stream.Collectors;
import org.kergru.library.model.PageResponseDto;
import org.kergru.library.model.UserDto;
import org.kergru.library.users.repository.UserEntity;
import org.kergru.library.users.repository.UserRepository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class UserService {

  private final UserRepository repository;

  public UserService(UserRepository repository) {
    this.repository = repository;
  }

  public Mono<UserDto> getUser(String userName) {
    return repository.findByUsername(userName).map(this::toDto);
  }

  public Mono<PageResponseDto<UserDto>> searchUsers(String searchString, int page, int size, String sortBy) {

    return repository.searchUsersPaged(searchString, page, size, sortBy)
        .map(p -> new PageResponseDto<>(
            p.getContent().stream().map(this::toDto).collect(Collectors.toList()),
            p.getNumber(),
            p.getSize(),
            p.getTotalPages(),
            p.getTotalElements(),
            p.isFirst(),
            p.isLast(),
            p.getNumberOfElements(),
            p.isEmpty()
        ));
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

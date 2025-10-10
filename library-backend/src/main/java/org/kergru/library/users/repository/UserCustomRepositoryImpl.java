package org.kergru.library.users.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.data.relational.core.query.Criteria;
import org.springframework.data.relational.core.query.Query;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public class UserCustomRepositoryImpl implements UserCustomRepository {

  private final R2dbcEntityTemplate template;

  public UserCustomRepositoryImpl(R2dbcEntityTemplate template) {
    this.template = template;
  }

  @Override
  public Mono<Page<UserEntity>> searchUsers(String searchString, int page, int size, String sortBy) {

    Criteria criteria = Criteria.empty();
    if (StringUtils.hasText(searchString)) {
      criteria = criteria.and("username").like("%" + searchString + "%");
      criteria = criteria.or("firstname").like("%" + searchString + "%");
      criteria = criteria.or("lastname").like("%" + searchString + "%");
      criteria = criteria.or("email").like("%" + searchString + "%");
    }

    Pageable pageable = PageRequest.of(page, size, Sort.by(sortBy));
    Query pageQuery  = Query.query(criteria).with(pageable);

    Flux<UserEntity> dataFlux = template.select(UserEntity.class)
        .matching(pageQuery)
        .all();

    return Mono.zip(dataFlux.collectList(), countUsers(criteria))
        .map(tuple -> new PageImpl<>(
            tuple.getT1(),
            PageRequest.of(page, size),
            tuple.getT2()
        ));
  }

  public Mono<Long> countUsers(Criteria criteria) {
    return template.count(Query.query(criteria), UserEntity.class);
  }
}

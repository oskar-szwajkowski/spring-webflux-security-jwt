package io.rapha.spring.reactive.security.auth.repository;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public interface UserRepository extends ReactiveCrudRepository<CustomUser, String> {
	Mono<CustomUser> findByUserName(String userName);
}

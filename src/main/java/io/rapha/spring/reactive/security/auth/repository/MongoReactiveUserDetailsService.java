package io.rapha.spring.reactive.security.auth.repository;

import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import reactor.core.publisher.Mono;

public class MongoReactiveUserDetailsService implements ReactiveUserDetailsService {

	private UserRepository userRepository;

	public MongoReactiveUserDetailsService(UserRepository userRepository) {
		this.userRepository = userRepository;
	}

	@Override
	public Mono<UserDetails> findByUsername(String s) {
		return userRepository.findByUserName(s)
				.map(customUser ->
						User.withDefaultPasswordEncoder()
								.username(customUser.getUserName())
								.password(customUser.getPassword())
								.roles(customUser.getRoles().split(","))
								.disabled(customUser.isDisabled())
								.build());
	}
}

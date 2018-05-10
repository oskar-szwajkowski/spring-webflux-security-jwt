package io.rapha.spring.reactive.security.auth.repository;

import com.mongodb.reactivestreams.client.MongoClient;
import com.mongodb.reactivestreams.client.MongoClients;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.AbstractReactiveMongoConfiguration;

@Configuration
public class UserRepositoryConfiguration extends AbstractReactiveMongoConfiguration{

	@Override
	public MongoClient reactiveMongoClient() {
		return MongoClients.create("mongodb://localhost:27017");
	}

	@Override
	protected String getDatabaseName() {
		return "test";
	}
}

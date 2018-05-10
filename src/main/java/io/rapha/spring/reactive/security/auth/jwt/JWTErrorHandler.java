package io.rapha.spring.reactive.security.auth.jwt;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.security.InvalidParameterException;
import java.time.DateTimeException;

public class JWTErrorHandler {

	public Mono<Void> handleError(Throwable t, ServerWebExchange exchange) {
		switch (getExceptionType(t)){
			case JWT_VALIDATION_ERROR:
				handleJwtValidationError(exchange);
				break;
			case DATE_EXPIRED_ERROR:
				handleDateExpiredError(exchange);
				break;
			case SERVER_ERROR:
				handleServerError(exchange);
		}
		return Mono.empty();
	}

	private ErrorType getExceptionType(Throwable t){
		if (t instanceof DateTimeException)
			return ErrorType.DATE_EXPIRED_ERROR;
		else if (t instanceof InvalidParameterException)
			return ErrorType.JWT_VALIDATION_ERROR;
		else
			return ErrorType.SERVER_ERROR;
	}

	private void handleJwtValidationError(ServerWebExchange exchange){
		exchange.getResponse().setStatusCode(HttpStatus.BAD_REQUEST);
	}

	private void handleDateExpiredError(ServerWebExchange exchange){
		exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
	}

	private void handleServerError(ServerWebExchange exchange){
		exchange.getResponse().setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR);
	}

	private enum ErrorType{
		DATE_EXPIRED_ERROR, JWT_VALIDATION_ERROR, SERVER_ERROR
	}
}

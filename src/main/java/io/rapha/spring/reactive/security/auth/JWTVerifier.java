package io.rapha.spring.reactive.security.auth;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.SignedJWT;
import org.springframework.stereotype.Component;

import java.text.ParseException;
import java.util.Date;

import static io.rapha.spring.reactive.security.auth.JWTSecrets.DEFAULT_SECRET;

public class JWTVerifier {

	public static boolean verifyJWTHMAC(SignedJWT jwt){
		try {
			return jwt.verify(new MACVerifier(DEFAULT_SECRET));
		} catch (JOSEException e) {
			//TODO log this or rethrow runtime exception
			return false;
		}
	}

	public static boolean verifyExpirationDate(SignedJWT jwt) {
		try {
			return jwt.getJWTClaimsSet().getExpirationTime().after(new Date());
		} catch (ParseException e) {
			//TODO log this or rethrow runtime exception
			return false;
		}
	}
}

/*
 * Permission is hereby granted, free of charge, to any person obtaining a
 * copy of this software and associated documentation files (the "Software"),
 * to deal in the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense,
 * and/or sell copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included
 * in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NON-INFRINGEMENT. IN NO EVENT SHALL
 * THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package io.rapha.spring.reactive.security.auth;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSObject;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.springframework.security.core.GrantedAuthority;
import reactor.core.publisher.Mono;

import java.security.InvalidParameterException;
import java.text.ParseException;
import java.util.Collection;
import java.util.Date;
import java.util.stream.Collectors;

/**
 * A service to create JWT objects, this one is used when an exchange
 * provides basic authentication.
 * If authentication is successful, a token is added in the response
 */
public class JWTTokenService {

    private GuestTokenWrapper guestToken;

    public JWTTokenService(){
        guestToken = new GuestTokenWrapper(generateGuestToken());
    }

    /**
     * Create and sign a JWT object using information from the current
     * authenticated principal
     *
     * @param subject     Name of current principal
     * @param credentials Credentials of current principal
     * @param authorities A collection of granted authorities for this principal
     * @return String representing a valid token
     */
    public String generateToken(String subject, Object credentials, Collection<? extends GrantedAuthority> authorities) {
        //TODO refactor this nasty code
        // Prepare JWT with claims set
        JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
                .subject(subject)
                .issuer("some.issuer")
                .expirationTime(new Date(new Date().getTime() + 60 * 10000))
                .claim("auths", authorities.parallelStream().map(auth -> (GrantedAuthority) auth).map(GrantedAuthority::getAuthority).collect(Collectors.joining(",")))
                .build();

        return serializeSignedJWT(signToken(claimsSet));
    }

    public Mono<SignedJWT> provideGuestToken(){
        return Mono.just(guestToken.signedToken)
                .filter(p -> isGuestTokenExpired())
                .defaultIfEmpty(generateGuestToken());
        //return Mono.just(isGuestTokenExpired() ? generateGuestToken() : guestToken.signedToken);
    }

    private SignedJWT generateGuestToken(){
        JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
                .subject("guest")
                .issuer("some.issuer")
                .expirationTime(new Date(new Date().getTime() + 60 * 10000))
                .claim("auths", "ROLE_GUEST")
                .build();

        regenerateGuestToken(signToken(claimsSet));

        return guestToken.signedToken;
    }

    private synchronized void regenerateGuestToken(SignedJWT signedJWT){
        guestToken = new GuestTokenWrapper(signedJWT);
    }

    private boolean isGuestTokenExpired(){
        return guestToken.expirationTime.before(new Date());
    }

    private SignedJWT signToken(JWTClaimsSet claimsSet){
        SignedJWT signedJWT = new SignedJWT(new JWSHeader(JWSAlgorithm.HS256), claimsSet);

        // Apply the HMAC protection
        try {
            signedJWT.sign(new JWTSignerProvider().getSigner());
        } catch (JOSEException e) {
            e.printStackTrace();
        }

        return signedJWT;
    }

    private String serializeSignedJWT(SignedJWT signedJWT){
        return signedJWT.serialize();
    }

    private class GuestTokenWrapper{
        final SignedJWT signedToken;
        final String serializedToken;
        final Date expirationTime;

        private GuestTokenWrapper(SignedJWT signedToken) {
            if (!signedToken.getState().equals(JWSObject.State.SIGNED)) throw new InvalidParameterException("Token provided is not signed!");
            this.signedToken = signedToken;
            this.serializedToken = signedToken.serialize();
            Date expirationTime = null;
            try {
                expirationTime = signedToken.getJWTClaimsSet().getExpirationTime();
            } catch (ParseException e) {
                e.printStackTrace();
            } finally {
                this.expirationTime = expirationTime;
            }
        }
    }
}

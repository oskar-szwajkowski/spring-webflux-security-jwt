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
package io.rapha.spring.reactive.security.auth.jwt;

import com.nimbusds.jwt.SignedJWT;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import reactor.core.publisher.Mono;

import java.text.ParseException;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class UsernamePasswordAuthenticationFromJWTToken {

	public static Mono<Authentication> create(Mono<SignedJWT> signedJWTMono) {
//		SignedJWT signedJWT = signedJWTMono.block();
//		String subject;
//		String auths;
//		List authorities;

		//try {
		return signedJWTMono.map(jwt -> {
			try {
				return new SubjectWithAuthorities(jwt.getJWTClaimsSet().getSubject(), (String) jwt.getJWTClaimsSet().getClaim("auths"));
			} catch (ParseException e) {
				return null;
			}
		})
				.map(auth -> {
							auth.setAuthoritiesList(
									Stream.of(auth.getAuthorities().split(","))
											.map(SimpleGrantedAuthority::new)
											.collect(Collectors.toList()));
							return auth;
						})
				.map(swa -> new UsernamePasswordAuthenticationToken(swa.getSubject(), null, swa.getAuthoritiesList()));
//					.map(auth -> auth.setAuthoritiesList(Stream.of(auth.getAuthorities().split(","))
//							.map(SimpleGrantedAuthority::new)
//							.collect(Collectors.toList())))
		//.map(new UsernamePasswordAuthenticationToken());
		//subject = signedJWT.getJWTClaimsSet().getSubject();
		//auths = (String) signedJWT.getJWTClaimsSet().getClaim("auths");
		//} catch (ParseException e) {
		//return null;
		//}
		//authorities = Stream.of(auths.split(","))
		//		.map(SimpleGrantedAuthority::new)
		//		.collect(Collectors.toList());

		//return new UsernamePasswordAuthenticationToken(subject, null, authorities);
	}

	private static class SubjectWithAuthorities {

		private String subject;
		private String authorities;
		private List<SimpleGrantedAuthority> authoritiesList;

		private SubjectWithAuthorities(String subject, String authorities) {
			this.subject = subject;
			this.authorities = authorities;
		}

		public void setAuthoritiesList(List<SimpleGrantedAuthority> authoritiesList) {
			this.authoritiesList = authoritiesList;
		}

		public String getSubject() {
			return subject;
		}

		public String getAuthorities() {
			return authorities;
		}

		public List<SimpleGrantedAuthority> getAuthoritiesList() {
			return authoritiesList;
		}
	}
}

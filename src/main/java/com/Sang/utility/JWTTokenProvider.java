package com.Sang.utility;

import static com.Sang.constant.SecurityConstant.*;
import static com.auth0.jwt.algorithms.Algorithm.HMAC512;

import com.Sang.domain.UserPrincipal;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.JWTVerifier;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;

@Component
public class JWTTokenProvider {

  @Value("${jwt.secret}")
  private String secret;

  public String generateJWTToken(UserPrincipal userPrincipal) {
    String[] claims = getClaimsFromUser(userPrincipal);
    return JWT.create()
        .withIssuer(SANG_LLC)
        .withAudience(SANG_ADMINISTRATION)
        .withIssuedAt(new Date())
        .withSubject(userPrincipal
            .getUsername())
        .withArrayClaim(AUTHORITIES, claims)
        .withExpiresAt(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
        .sign(HMAC512(secret.getBytes()));
  }

  public List<GrantedAuthority> getAuthorities(String token) {
    String[] claims = getClaimsFromToken(token);
    List<GrantedAuthority> grantAuthoritiesList = new ArrayList<>();
    for (String claim : claims) {
      SimpleGrantedAuthority simpleGrantedAuthority = new SimpleGrantedAuthority(claim);
      grantAuthoritiesList.add(simpleGrantedAuthority);
    }
    return grantAuthoritiesList;
  }

  public Authentication getAuthentication(
      String username,
      List<GrantedAuthority> authorities,
      HttpServletRequest request) {
    UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken =
        new UsernamePasswordAuthenticationToken(username, null, authorities);
    usernamePasswordAuthenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
    return usernamePasswordAuthenticationToken;
  }

  public boolean isTokenValid(String username, String token) {
    JWTVerifier verifier = getJWTVerifier();
    return StringUtils.isNotEmpty(username) && !isTokenExpired(verifier, token);
  }

  public String getSubject(String token) {
    JWTVerifier verifier = getJWTVerifier();
    return verifier.verify(token).getSubject();
  }

  private boolean isTokenExpired(JWTVerifier verifier, String token) {
    Date expiration = verifier.verify(token).getExpiresAt();
    return expiration.before(new Date());
  }

  private String[] getClaimsFromToken(String token) {
    JWTVerifier verifier = getJWTVerifier();
    return verifier.verify(token).getClaim(AUTHORITIES).asArray(String.class);
  }

  private JWTVerifier getJWTVerifier() {
    JWTVerifier verifier;
    try {
      Algorithm algorithm = HMAC512(secret);
      verifier = JWT.require(algorithm).withIssuer(SANG_LLC).build();
    } catch (JWTVerificationException e) {
      throw new JWTVerificationException(TOKEN_CANNOT_BE_VERIFIED);
    }
    return verifier;
  }

  private String[] getClaimsFromUser(UserPrincipal userPrincipal) {
    List<String> authorities = new ArrayList<>();
    for (GrantedAuthority grantedAuthority : userPrincipal.getAuthorities()) {
      authorities.add(grantedAuthority.getAuthority());
    }
    return authorities.toArray(new String[0]);
  }
}
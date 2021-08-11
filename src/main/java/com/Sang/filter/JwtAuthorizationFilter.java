package com.Sang.filter;

import static com.Sang.constant.SecurityConstant.*;
import static org.springframework.http.HttpHeaders.*;
import static org.springframework.http.HttpStatus.*;

import com.Sang.utility.JWTTokenProvider;
import java.io.IOException;
import java.util.List;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@AllArgsConstructor
public class JwtAuthorizationFilter extends OncePerRequestFilter {

  private JWTTokenProvider jwtTokenProvider;

  @Override
  protected void doFilterInternal(HttpServletRequest request,
      HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {
    if (request.getMethod().equalsIgnoreCase(OPTIONS_HTTP_METHOD)) {
      response.setStatus(OK.value());
    } else {
      String authorizationHeader = request.getHeader(AUTHORIZATION);
      if (authorizationHeader == null || !authorizationHeader.startsWith(TOKEN_PREFIX)) {
        filterChain.doFilter(request, response);
        return;
      }
      String token = authorizationHeader.substring(TOKEN_PREFIX.length());
      String username = jwtTokenProvider.getSubject(token);
      if (jwtTokenProvider.isTokenValid(username, token) && SecurityContextHolder.getContext().getAuthentication() == null) {
        List<GrantedAuthority> authoritiesList = jwtTokenProvider.getAuthorities(token);
        Authentication authentication = jwtTokenProvider.getAuthentication(username, authoritiesList, request);
        SecurityContextHolder.getContext().setAuthentication(authentication);
      } else {
        SecurityContextHolder.clearContext();
      }
    }
    filterChain.doFilter(request, response);
  }
}

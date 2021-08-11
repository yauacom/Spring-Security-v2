package com.Sang.configuration;

import static com.Sang.constant.SecurityConstant.PUBLIC_URLS;
import static org.springframework.security.config.http.SessionCreationPolicy.STATELESS;

import com.Sang.filter.JwtAccessDeniedHandler;
import com.Sang.filter.JwtAuthenticationEntryPoint;
import com.Sang.filter.JwtAuthorizationFilter;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
@AllArgsConstructor
@Qualifier("userDetailsService")
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {
  private JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
  private JwtAccessDeniedHandler jwtAccessDeniedHandler;
  private JwtAuthorizationFilter jwtAuthorizationFilter;
  private UserDetailsService userDetailsService;
  private BCryptPasswordEncoder bCryptPasswordEncoder;

  @Override
  protected void configure(AuthenticationManagerBuilder auth) throws Exception {
    auth.userDetailsService(userDetailsService).passwordEncoder(bCryptPasswordEncoder);
  }

  @Override
  protected void configure(HttpSecurity http) throws Exception {
   http.csrf().disable()
       .cors()
       .and().sessionManagement().sessionCreationPolicy(STATELESS)
       .and().authorizeRequests().antMatchers(PUBLIC_URLS).permitAll()
       .anyRequest().authenticated()
       .and().exceptionHandling().accessDeniedHandler(jwtAccessDeniedHandler)
       .authenticationEntryPoint(jwtAuthenticationEntryPoint)
       .and().addFilterBefore(jwtAuthorizationFilter, UsernamePasswordAuthenticationFilter.class);
  }

  @Bean
  @Override
  public AuthenticationManager authenticationManagerBean() throws Exception {
    return super.authenticationManagerBean();
  }
}

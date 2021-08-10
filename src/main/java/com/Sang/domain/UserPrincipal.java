package com.Sang.domain;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

@AllArgsConstructor
public class UserPrincipal implements UserDetails {
  private User user;

  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {
    List<SimpleGrantedAuthority> authoritiesList = new ArrayList<>();
    for (String authority : this.user.getAuthorities()) {
      SimpleGrantedAuthority simpleGrantedAuthority = new SimpleGrantedAuthority(authority);
      authoritiesList.add(simpleGrantedAuthority);
    }
    return authoritiesList;
  }

  @Override
  public String getPassword() {
    return this.user.getPassword();
  }

  @Override
  public String getUsername() {
    return this.user.getUsername();
  }

  @Override
  public boolean isAccountNonExpired() {
    return true;
  }

  @Override
  public boolean isAccountNonLocked() {
    return this.user.isNotLocked();
  }

  @Override
  public boolean isCredentialsNonExpired() {
    return true;
  }

  @Override
  public boolean isEnabled() {
    return this.user.isActive();
  }
}

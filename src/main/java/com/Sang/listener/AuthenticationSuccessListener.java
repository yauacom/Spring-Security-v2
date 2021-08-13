package com.Sang.listener;

import com.Sang.domain.UserPrincipal;
import com.Sang.service.LoginAttemptService;
import lombok.AllArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class AuthenticationSuccessListener {
  private LoginAttemptService loginAttemptService;

  @EventListener
  public void onAuthenticationSuccess(AuthenticationSuccessEvent event) {
    Object principal = event.getAuthentication().getPrincipal();
    if (principal instanceof UserPrincipal) {
      UserPrincipal user = (UserPrincipal) principal;
      loginAttemptService.evictUserFromLoginAttemptCache(user.getUsername());
    }
  }

}

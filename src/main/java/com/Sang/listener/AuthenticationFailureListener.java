package com.Sang.listener;

import com.Sang.service.LoginAttemptService;
import java.util.concurrent.ExecutionException;
import lombok.AllArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.event.AuthenticationFailureBadCredentialsEvent;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class AuthenticationFailureListener {
  private LoginAttemptService loginAttemptService;

  @EventListener
  public void onAuthenticationFailure(AuthenticationFailureBadCredentialsEvent event) {
    Object principal = event.getAuthentication().getPrincipal();
    if (principal instanceof String) {
      String username = (String) principal;
      loginAttemptService.addUserToLoginAttemptCache(username);
    }
  }
}

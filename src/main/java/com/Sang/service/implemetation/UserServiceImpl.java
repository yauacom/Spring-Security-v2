package com.Sang.service.implemetation;

import com.Sang.domain.User;
import com.Sang.domain.UserPrincipal;
import com.Sang.repository.UserRepository;
import com.Sang.service.UserService;
import java.util.Date;
import javax.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@Transactional
@Qualifier("UserDetailsService")
@AllArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService, UserDetailsService {
  private UserRepository userRepository;

  @Override
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    User user = userRepository.findUserByUsername(username);
    if (user == null) {
      log.error("User not found by username: " + username);
      throw new UsernameNotFoundException("User not found by username: " + username);
    } else {
      user.setLastLoginDateDisplayed(user.getLastLoginDate());
      user.setLastLoginDate(new Date());
      userRepository.save(user);
      UserPrincipal userPrincipal = new UserPrincipal(user);
      log.info("Returning found user by username:" + username);
    return userPrincipal;
    }
  }
}

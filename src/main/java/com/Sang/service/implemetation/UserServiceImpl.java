package com.Sang.service.implemetation;

import static com.Sang.constant.UserImplConstant.*;
import static com.Sang.enumeration.Role.*;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

import com.Sang.domain.User;
import com.Sang.domain.UserPrincipal;
import com.Sang.exception.domain.EmailExistException;
import com.Sang.exception.domain.UserNotFoundException;
import com.Sang.exception.domain.UsernameExistException;
import com.Sang.repository.UserRepository;
import com.Sang.service.UserService;
import java.util.Date;
import java.util.List;
import javax.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@Service
@Transactional
@Qualifier("UserDetailsService")
@AllArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService, UserDetailsService {
  private UserRepository userRepository;
  private BCryptPasswordEncoder bCryptPasswordEncoder;

  @Override
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    User user = userRepository.findUserByUsername(username);
    if (user == null) {
      log.error(NO_USER_FOUND_BY_USERNAME + username);
      throw new UsernameNotFoundException(NO_USER_FOUND_BY_USERNAME + username);
    } else {
      user.setLastLoginDateDisplayed(user.getLastLoginDate());
      user.setLastLoginDate(new Date());
      userRepository.save(user);
      UserPrincipal userPrincipal = new UserPrincipal(user);
      log.info(FOUND_USER_BY_USERNAME + username);
    return userPrincipal;
    }
  }

  @Override
  public User register(String firstName, String lastName, String username, String email)
      throws UserNotFoundException, EmailExistException, UsernameExistException {
    validateNewUsernameAndEmail(EMPTY, username, email);
    User user = new User();
    user.setUserId(generateUserId());
    String password = generatePassword();
    String encodedPassword = encodePassword(password);
    user.setPassword(encodedPassword);
    user.setFirstName(firstName);
    user.setLastName(lastName);
    user.setUsername(username);
    user.setEmail(email);
    user.setJoinDate(new Date());
    user.setActive(true);
    user.setNotLocked(true);
    user.setRole(ROLE_USER.name());
    user.setAuthorities(ROLE_USER.getAuthorities());
    user.setProfileImageUrl(getTemporaryProfileImageUrl());
    userRepository.save(user);
    log.info("New user is created with password: {}", password);
    return user;
  }

  private User validateNewUsernameAndEmail(String currentUsername, String newUsername, String newEmail)
      throws UserNotFoundException, UsernameExistException, EmailExistException {

    User userByNewUsername = findUserByUsername(newUsername);
    User userByNewEmail = findUserByEmail(newEmail);

    if (isNotBlank(currentUsername)) {
      User currentUser = findUserByUsername(currentUsername);
      if (currentUser == null) {
        throw new UserNotFoundException(NO_USER_FOUND_BY_USERNAME + currentUsername);
      }
      if (userByNewUsername != null && !currentUser.getId().equals(userByNewUsername.getId())) {
        throw new UsernameExistException(USERNAME_ALREADY_EXISTS + newUsername);
      }
      if (userByNewEmail != null && !currentUser.getId().equals(userByNewEmail.getId())) {
        throw new EmailExistException(EMAIL_ALREADY_EXISTS + newEmail);
      }
      return currentUser;
    } else {
      if (userByNewUsername != null) {
        throw new UsernameExistException(USERNAME_ALREADY_EXISTS + newUsername);
      }
      if (userByNewEmail != null) {
        throw new EmailExistException(EMAIL_ALREADY_EXISTS + newEmail);
      }
      return null;
    }
  }

  @Override
  public List<User> getUsers() {
    return userRepository.findAll();
  }

  @Override
  public User findUserByUsername(String username) {
    return userRepository.findUserByUsername(username);
  }


  @Override
  public User findUserByEmail(String email) {
    return userRepository.findUserByEmail(email);
  }

  private String getTemporaryProfileImageUrl() {
    return ServletUriComponentsBuilder.fromCurrentContextPath().path(DEFAULT_USER_IMAGE_PATH).toUriString();
  }

  private String encodePassword(String password) {
    return bCryptPasswordEncoder.encode(password);
  }

  private String generatePassword() {
    return RandomStringUtils.randomAlphabetic(10);
  }

  private String generateUserId() {
    return RandomStringUtils.randomNumeric(10);
  }
}

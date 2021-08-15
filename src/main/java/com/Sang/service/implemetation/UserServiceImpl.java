package com.Sang.service.implemetation;

import static com.Sang.constant.FileConstant.DEFAULT_USER_IMAGE_PATH;
import static com.Sang.constant.FileConstant.DIRECTORY_CREATED;
import static com.Sang.constant.FileConstant.DOT;
import static com.Sang.constant.FileConstant.FILE_SAVED_IN_FILE_SYSTEM;
import static com.Sang.constant.FileConstant.FORWARD_SLASH;
import static com.Sang.constant.FileConstant.JPG_EXTENSION;
import static com.Sang.constant.FileConstant.USER_FOLDER;
import static com.Sang.constant.FileConstant.USER_IMAGE_PATH;
import static com.Sang.constant.UserImplConstant.*;
import static com.Sang.enumeration.Role.*;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

import com.Sang.domain.User;
import com.Sang.domain.UserPrincipal;
import com.Sang.enumeration.Role;
import com.Sang.exception.domain.EmailExistException;
import com.Sang.exception.domain.EmailNotFoundException;
import com.Sang.exception.domain.UserNotFoundException;
import com.Sang.exception.domain.UsernameExistException;
import com.Sang.repository.UserRepository;
import com.Sang.service.EmailService;
import com.Sang.service.LoginAttemptService;
import com.Sang.service.UserService;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@Service
@Transactional
@Qualifier("UserDetailsService")
@AllArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService, UserDetailsService {
  private UserRepository userRepository;
  private BCryptPasswordEncoder bCryptPasswordEncoder;
  private LoginAttemptService loginAttemptService;
  private EmailService emailService;

  @Override
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    User user = userRepository.findUserByUsername(username);
    if (user == null) {
      log.error(NO_USER_FOUND_BY_USERNAME + username);
      throw new UsernameNotFoundException(NO_USER_FOUND_BY_USERNAME + username);
    } else {
      validateLoginAttempt(user);
      user.setLastLoginDateDisplay(user.getLastLoginDate());
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
    user.setPassword(encodePassword(password));
    user.setFirstName(firstName);
    user.setLastName(lastName);
    user.setUsername(username);
    user.setEmail(email);
    user.setJoinDate(new Date());
    user.setActive(true);
    user.setNotLocked(true);
    user.setRole(ROLE_USER.name());
    user.setAuthorities(ROLE_USER.getAuthorities());
    user.setProfileImageUrl(getTemporaryProfileImageUrl(username));
    userRepository.save(user);
    log.info("New user is created with password: {}", password);
    emailService.sendNewPasswordEmail(firstName, password, email);
    return user;
  }

  @Override
  public User addNewUser(String firstName,
      String lastName,
      String username,
      String email,
      String role,
      boolean isActive,
      boolean isNonLocked,
      MultipartFile profileImage)
      throws UserNotFoundException, EmailExistException, UsernameExistException, IOException {
    validateNewUsernameAndEmail(EMPTY, username, email);
    User user = new User();
    user.setUserId(generateUserId());
    String password = generatePassword();
    user.setPassword(encodePassword(password));
    user.setFirstName(firstName);
    user.setLastName(lastName);
    user.setUsername(username);
    user.setEmail(email);
    user.setJoinDate(new Date());
    user.setActive(isActive);
    user.setNotLocked(isNonLocked);
    user.setRole(getRoleEnumName(role).name());
    user.setAuthorities(getRoleEnumName(role).getAuthorities());
    user.setProfileImageUrl(getTemporaryProfileImageUrl(username));
    userRepository.save(user);
    saveProfileImage(user, profileImage);
    return user;
  }

  @Override
  public User updateUser(String currentUsername,
      String newFirstName,
      String newLastName,
      String newUsername,
      String newEmail,
      String role,
      boolean isActive,
      boolean isNonLocked,
      MultipartFile profileImage)
      throws UserNotFoundException, EmailExistException, UsernameExistException, IOException {
    User currentUser = validateNewUsernameAndEmail(currentUsername, newUsername, newEmail);
    currentUser.setFirstName(newFirstName);
    currentUser.setLastName(newLastName);
    currentUser.setUsername(newUsername);
    currentUser.setEmail(newEmail);
    currentUser.setActive(isActive);
    currentUser.setNotLocked(isNonLocked);
    currentUser.setRole(getRoleEnumName(role).name());
    currentUser.setAuthorities(getRoleEnumName(role).getAuthorities());
    userRepository.save(currentUser);
    saveProfileImage(currentUser, profileImage);
    return currentUser;
  }

  @Override
  public void deleteUser(long id) {
    userRepository.deleteById(id);
  }

  @Override
  public void resetPassword(String email) throws EmailNotFoundException {
    User user = userRepository.findUserByEmail(email);
    if (user == null) {
      throw new EmailNotFoundException(NO_USER_FOUND_BY_EMAIL + email);
    }
    String password = generatePassword();
    user.setPassword(encodePassword(password));
    userRepository.save(user);
    emailService.sendNewPasswordEmail(user.getFirstName(), password, user.getEmail());
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

  @Override
  public User updateProfileImage(String username, MultipartFile profileImage)
      throws UserNotFoundException, EmailExistException, UsernameExistException, IOException {
    User user = validateNewUsernameAndEmail(username, null, null);
    saveProfileImage(user, profileImage);
    return user;
  }

  private void saveProfileImage(User user, MultipartFile profileImage) throws IOException {
    if (profileImage != null) {
      Path userFolder = Paths.get(USER_FOLDER + user.getUsername()).toAbsolutePath().normalize();
      if(!Files.exists(userFolder)) {
        Files.createDirectories(userFolder);
        log.info(DIRECTORY_CREATED + userFolder);
      }
      Files.deleteIfExists(Paths.get(userFolder + user.getUsername() + DOT + JPG_EXTENSION));
      Files.copy(profileImage.getInputStream(), userFolder.resolve(user.getUsername()+ DOT + JPG_EXTENSION), REPLACE_EXISTING);
      user.setProfileImageUrl(setProfileImageUrl(user.getUsername()));
      userRepository.save(user);
      log.info(FILE_SAVED_IN_FILE_SYSTEM + profileImage.getOriginalFilename());
    }
  }

  private String setProfileImageUrl(String username) {
    return ServletUriComponentsBuilder.fromCurrentContextPath()
        .path(
        USER_IMAGE_PATH
            + username
            + FORWARD_SLASH
            + username
            + DOT
            + JPG_EXTENSION)
        .toUriString();
  }

  private Role getRoleEnumName(String role) {
    return Role.valueOf(role.toUpperCase());
  }

  private String getTemporaryProfileImageUrl(String username) {
    return ServletUriComponentsBuilder.fromCurrentContextPath().path(DEFAULT_USER_IMAGE_PATH + username).toUriString();
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

  private void validateLoginAttempt(User user) {
    if (user.isNotLocked()) {
      if (loginAttemptService.hasExceededMaxAttempts(user.getUsername())) {
        user.setNotLocked(false);
      } else {
        user.setNotLocked(true);
      }
    } else {
      loginAttemptService.evictUserFromLoginAttemptCache(user.getUsername());
    }
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
}

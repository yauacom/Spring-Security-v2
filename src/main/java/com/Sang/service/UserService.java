package com.Sang.service;

import com.Sang.domain.User;
import com.Sang.exception.domain.EmailExistException;
import com.Sang.exception.domain.EmailNotFoundException;
import com.Sang.exception.domain.UserNotFoundException;
import com.Sang.exception.domain.UsernameExistException;
import java.io.IOException;
import java.util.List;
import org.springframework.web.multipart.MultipartFile;

public interface UserService {

  User register(String firstName, String lastName, String username, String email)
      throws UserNotFoundException, EmailExistException, UsernameExistException;

  List<User> getUsers();

  User findUserByUsername(String username);

  User findUserByEmail(String email);

  User addNewUser(
      String firstName,
      String lastName,
      String username,
      String email,
      String role,
      boolean isActive,
      boolean isNonLocked,
      MultipartFile profileImage
  ) throws UserNotFoundException, EmailExistException, UsernameExistException, IOException;

  User updateUser(
      String currentUsername,
      String newFirstName,
      String newLastName,
      String newUsername,
      String newEmail,
      String role,
      boolean isActive,
      boolean isNonLocked,
      MultipartFile profileImage
  ) throws UserNotFoundException, EmailExistException, UsernameExistException, IOException;

  void deleteUser (long id);

  void resetPassword(String email) throws EmailNotFoundException;

  User updateProfileImage(String username, MultipartFile profileImage)
      throws UserNotFoundException, EmailExistException, UsernameExistException, IOException;
}

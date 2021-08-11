package com.Sang.service;

import com.Sang.domain.User;
import com.Sang.exception.domain.EmailExistException;
import com.Sang.exception.domain.UserNotFoundException;
import com.Sang.exception.domain.UsernameExistException;
import java.util.List;

public interface UserService {

  User register(String firstName, String lastName, String username, String email)
      throws UserNotFoundException, EmailExistException, UsernameExistException;

  List<User> getUsers();

  User findUserByUsername(String username);

  User findUserByEmail(String email);
}

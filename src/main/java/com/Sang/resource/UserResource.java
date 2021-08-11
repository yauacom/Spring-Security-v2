package com.Sang.resource;

import static org.springframework.http.HttpStatus.*;

import com.Sang.domain.User;
import com.Sang.exception.ExceptionHandling;
import com.Sang.exception.domain.EmailExistException;
import com.Sang.exception.domain.UserNotFoundException;
import com.Sang.exception.domain.UsernameExistException;
import com.Sang.service.UserService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = {"/", "/user"})
@AllArgsConstructor
public class UserResource extends ExceptionHandling {
  private UserService userService;

  @GetMapping()
  public String showUser() {
    return "Welcome!";
  }

  @PostMapping("/register")
  public ResponseEntity<User> register(@RequestBody User user)
      throws UserNotFoundException, EmailExistException, UsernameExistException {
    User newUser = userService.register(user.getFirstName(), user.getLastName(), user.getUsername(), user.getEmail());
    return new ResponseEntity<>(newUser, OK);
  }
}

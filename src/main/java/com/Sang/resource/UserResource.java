package com.Sang.resource;

import static com.Sang.constant.SecurityConstant.JWT_TOKEN_HEADER;
import static org.springframework.http.HttpStatus.*;

import com.Sang.domain.User;
import com.Sang.domain.UserPrincipal;
import com.Sang.exception.ExceptionHandling;
import com.Sang.exception.domain.EmailExistException;
import com.Sang.exception.domain.UserNotFoundException;
import com.Sang.exception.domain.UsernameExistException;
import com.Sang.service.UserService;
import com.Sang.utility.JWTTokenProvider;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
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
  private AuthenticationManager authenticationManager;
  private JWTTokenProvider jwtTokenProvider;

  @GetMapping()
  public String showUser() {
    return "Welcome!";
  }

  @PostMapping("/login")
  public ResponseEntity<User> login(@RequestBody User user) {
    authenticate(user.getUsername(), user.getPassword());
    User loginUser = userService.findUserByUsername(user.getUsername());
    UserPrincipal userPrincipal = new UserPrincipal(loginUser);
    HttpHeaders jwtHeader = getJwtHeader(userPrincipal);
    return new ResponseEntity<>(loginUser, jwtHeader, OK);
  }

  @PostMapping("/register")
  public ResponseEntity<User> register(@RequestBody User user)
      throws UserNotFoundException, EmailExistException, UsernameExistException {
    User newUser = userService.register(user.getFirstName(), user.getLastName(), user.getUsername(), user.getEmail());
    return new ResponseEntity<>(newUser, OK);
  }

  private HttpHeaders getJwtHeader(UserPrincipal user) {
    HttpHeaders headers = new HttpHeaders();
    headers.add(JWT_TOKEN_HEADER, jwtTokenProvider.generateJWTToken(user)) ;
    return headers;
  }

  private void authenticate(String username, String password) {
    authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, password));
  }
}

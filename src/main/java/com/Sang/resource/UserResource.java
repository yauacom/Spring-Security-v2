package com.Sang.resource;

import static com.Sang.constant.FileConstant.*;
import static com.Sang.constant.SecurityConstant.JWT_TOKEN_HEADER;
import static org.springframework.http.HttpStatus.*;
import static org.springframework.http.MediaType.IMAGE_JPEG_VALUE;

import com.Sang.domain.HttpResponse;
import com.Sang.domain.User;
import com.Sang.domain.UserPrincipal;
import com.Sang.exception.ExceptionHandling;
import com.Sang.exception.domain.EmailExistException;
import com.Sang.exception.domain.EmailNotFoundException;
import com.Sang.exception.domain.UserNotFoundException;
import com.Sang.exception.domain.UsernameExistException;
import com.Sang.service.UserService;
import com.Sang.utility.JWTTokenProvider;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping(path = {"/", "/user"})
@AllArgsConstructor
public class UserResource extends ExceptionHandling {

  public static final String EMAIL_SENT = "An email with a new password was sent to: ";
  public static final String USER_DELETED_SUCCESSFULLY = "User deleted successfully";
  private UserService userService;
  private AuthenticationManager authenticationManager;
  private JWTTokenProvider jwtTokenProvider;

  @PostMapping("/login")
  public ResponseEntity<User> login(@RequestBody User user) {
    authenticate(user.getUsername(), user.getPassword());
    User loginUser = userService.findUserByUsername(user.getUsername());
    UserPrincipal userPrincipal = new UserPrincipal(loginUser);
    HttpHeaders jwtHeader = getJwtHeader(userPrincipal);
    return new ResponseEntity<>(loginUser, jwtHeader, OK);
  }

  @GetMapping("/find/{username}")
  public ResponseEntity<User> getUser(@PathVariable("username") String username) {
    User user = userService.findUserByUsername(username);
    return new ResponseEntity<>(user, OK);
  }

  @GetMapping("/list")
  public ResponseEntity<List<User>> getAllUsers(@PathVariable("username") String username) {
    List<User> users = userService.getUsers();
    return new ResponseEntity<>(users ,OK);
  }

  @GetMapping("/resetPassword/{email}")
  public ResponseEntity<HttpResponse> resetPassword(@PathVariable("email") String email)
      throws EmailNotFoundException {
  userService.resetPassword(email);
  return response(OK, EMAIL_SENT + email);
  }

  @GetMapping(path = "/image/{username}/{fileName}", produces = {IMAGE_JPEG_VALUE})
  public byte[] getProfileImage(@PathVariable("username") String username, @PathVariable("filename") String filename)
      throws IOException {
     return Files.readAllBytes(Paths.get(USER_FOLDER + username + FORWARD_SLASH + filename));
  }

  @GetMapping(path = "/image/profile/{username}", produces = {IMAGE_JPEG_VALUE})
  public byte[] getTemporaryProfileImageUrl(@PathVariable("username") String username) throws IOException {
     URL url = new URL(TEMP_PROFILE_IMAGE_BASE_URL + username);
     ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
     try (InputStream inputSteam = url.openStream()) {
        int bytesRead;
        byte[] chunk = new byte[1024];
        while ((bytesRead = inputSteam.read(chunk)) > 0 ) {
          byteArrayOutputStream.write(chunk, 0, bytesRead);
       }
     }
     return byteArrayOutputStream.toByteArray();
  }

  @DeleteMapping("/delete/{id}")
  @PreAuthorize("hasAnyAuthority('user:delete')")
  public ResponseEntity<HttpResponse> deleteUser(@PathVariable("id") long id) {
    userService.deleteUser(id);
    return response(NO_CONTENT, USER_DELETED_SUCCESSFULLY);
  }

  @PostMapping("/register")
  public ResponseEntity<User> register(@RequestBody User user)
      throws UserNotFoundException, EmailExistException, UsernameExistException {
    User newUser = userService.register(user.getFirstName(), user.getLastName(), user.getUsername(), user.getEmail());
    return new ResponseEntity<>(newUser, OK);
  }

  @PostMapping("/add")
  public ResponseEntity<User> addNewUser(
      @RequestParam("firstName") String firstName,
      @RequestParam("lastName") String lastName,
      @RequestParam("username") String username,
      @RequestParam("email") String email,
      @RequestParam("role") String role,
      @RequestParam("isActive") String isActive,
      @RequestParam("isNonLocked") String isNonLocked,
      @RequestParam(value = "profileImage", required = false) MultipartFile profileImage)
      throws UserNotFoundException, EmailExistException, IOException, UsernameExistException {
    User newUser = userService.addNewUser(
        firstName,
        lastName,
        username,
        email,
        role,
        Boolean.parseBoolean(isActive),
        Boolean.parseBoolean(isNonLocked),
        profileImage);
    return new ResponseEntity<>(newUser,OK);
  }

  @PostMapping("/update")
  public ResponseEntity<User> updateUser(
      @RequestParam("currentUsername") String currentUsername,
      @RequestParam("firstName") String firstName,
      @RequestParam("lastName") String lastName,
      @RequestParam("username") String username,
      @RequestParam("email") String email,
      @RequestParam("role") String role,
      @RequestParam("isActive") String isActive,
      @RequestParam("isNonLocked") String isNonLocked,
      @RequestParam(value = "profileImage", required = false) MultipartFile profileImage)
      throws UserNotFoundException, EmailExistException, IOException, UsernameExistException {
    User updateUser = userService.updateUser(
        currentUsername,
        firstName,
        lastName,
        username,
        email,
        role,
        Boolean.parseBoolean(isActive),
        Boolean.parseBoolean(isNonLocked),
        profileImage);
    return new ResponseEntity<>(updateUser,OK);
  }

  @PostMapping("/updateProfileImage")
  public ResponseEntity<User> updateProfileImage(
      @RequestParam("username") String username,
      @RequestParam("profileImage") MultipartFile profileImage)
      throws UserNotFoundException, EmailExistException, IOException, UsernameExistException {
    User user = userService.updateProfileImage(username,profileImage);
    return new ResponseEntity<>(user,OK);
  }

  private HttpHeaders getJwtHeader(UserPrincipal user) {
    HttpHeaders headers = new HttpHeaders();
    headers.add(JWT_TOKEN_HEADER, jwtTokenProvider.generateJWTToken(user)) ;
    return headers;
  }

  private void authenticate(String username, String password) {
    authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, password));
  }

  private ResponseEntity<HttpResponse> response(HttpStatus httpStatus, String message) {
    return new ResponseEntity<>(
        new HttpResponse(httpStatus.value(), httpStatus, httpStatus.getReasonPhrase().toUpperCase(), message),
        httpStatus);
  }
}

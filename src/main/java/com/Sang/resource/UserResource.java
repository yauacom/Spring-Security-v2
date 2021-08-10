package com.Sang.resource;

import com.Sang.domain.User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/user")
public class UserResource {

  @GetMapping()
  public String showUser() {
    return "Welcome!";
  }
}

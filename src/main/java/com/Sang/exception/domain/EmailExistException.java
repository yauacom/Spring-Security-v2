package com.Sang.exception.domain;

public class EmailExistException extends Exception {

  public EmailExistException(String message) {
    super(message);
  }
}

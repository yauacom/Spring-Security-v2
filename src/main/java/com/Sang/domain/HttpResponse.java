package com.Sang.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.http.HttpStatus;

@AllArgsConstructor
@Getter
@Setter
public class HttpResponse {
  private int httpStatusCode;
  private HttpStatus httpStatus;
  private String reason;
  private String message;
}

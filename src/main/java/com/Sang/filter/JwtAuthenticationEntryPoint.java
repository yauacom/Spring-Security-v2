package com.Sang.filter;

import static com.Sang.constant.SecurityConstant.FORBIDDEN_MESSAGE;
import static org.springframework.http.HttpStatus.*;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import com.Sang.domain.HttpResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.OutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.Http403ForbiddenEntryPoint;
import org.springframework.stereotype.Component;

@Component
public class JwtAuthenticationEntryPoint extends Http403ForbiddenEntryPoint {

  @Override
  public void commence(HttpServletRequest request, HttpServletResponse response,
      AuthenticationException exception) throws IOException {
    HttpResponse httpResponse = new HttpResponse(
        FORBIDDEN.value(),
        FORBIDDEN,
        FORBIDDEN.getReasonPhrase().toUpperCase(),
        FORBIDDEN_MESSAGE);
    response.setContentType(APPLICATION_JSON_VALUE);
    response.setStatus(FORBIDDEN.value());
    OutputStream outputStream = response.getOutputStream();
    ObjectMapper objectMapper = new ObjectMapper();
    objectMapper.writeValue(outputStream, httpResponse);
    outputStream.flush();
  }

}

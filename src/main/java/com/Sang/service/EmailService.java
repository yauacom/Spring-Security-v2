package com.Sang.service;

import static com.Sang.constant.EmailConstant.*;

import java.util.Date;
import lombok.AllArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class EmailService {

  private JavaMailSender mailSender;

  public void sendNewPasswordEmail(String firstName, String password, String email) {
    SimpleMailMessage message = createEmail(firstName, password, email);
    mailSender.send(message);
  }

  private SimpleMailMessage createEmail(String firstName, String password, String email) {
    SimpleMailMessage message = new SimpleMailMessage();
    message.setFrom(FROM_EMAIL);
    message.setTo(email);
//    message.setCc(CC_EMAIL);
    message.setSubject(EMAIL_SUBJECT);
    message.setText("Hello " + firstName + ", \n \n Your new account password is: " + password
        + "\n \n The Support Team");
    message.setSentDate(new Date());
    return message;
  }
}

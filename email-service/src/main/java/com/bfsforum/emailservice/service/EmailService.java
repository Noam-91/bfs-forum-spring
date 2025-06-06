package com.bfsforum.emailservice.service;

import com.bfsforum.emailservice.dto.TokenInfo;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.MessagingException;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
@Service
public class EmailService {

  @Value("${app.activation.base-url}")
  String activationBaseUrl;

  @Value("${token.expiration.minutes}")
  long expirationMinutes;

  @Value("${app.email.from}")
  String fromEmail;

  final Map<String, TokenInfo> tokenStore = new ConcurrentHashMap<>();

  public void sendActivationEmail(String toEmail) throws MessagingException, IOException,
          jakarta.mail.MessagingException {
    String token = UUID.randomUUID().toString();
    tokenStore.put(token, new TokenInfo(toEmail, Instant.now()));

    String activationLink = activationBaseUrl + "/activate?token=" + token;
    String subject = "Activate your account";
    String body = "Click the link to activate (valid for " + expirationMinutes
            + " minutes):\n" + activationLink;

    var email = CreateEmail.createEmail(toEmail, fromEmail, subject, body);
    SendMessage.sendEmail(fromEmail, toEmail, email);
  }

  public boolean validateToken(String token) {
    TokenInfo info = tokenStore.get(token);
    if (info == null) return false;
    return Duration.between(info.getCreatedAt(), Instant.now()).toMinutes() < expirationMinutes;
  }

  public String consumeToken(String token) {
    TokenInfo info = tokenStore.remove(token);
    return (info != null) ? info.getEmail() : null;
  }
}

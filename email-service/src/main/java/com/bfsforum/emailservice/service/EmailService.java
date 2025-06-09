package com.bfsforum.emailservice.service;

import com.bfsforum.emailservice.dao.VerificationTokenRepository;
import com.bfsforum.emailservice.domain.VerificationToken;
import com.bfsforum.emailservice.exception.EmailProcessingException;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.UUID;

import static org.springframework.util.StringUtils.replace;

@Transactional
@Service
@Slf4j
public class EmailService {
  @Value("${app.activation.base-url}")
  private String activationBaseUrl;
  @Value("${token.expiration.minutes}")
  private long expirationMinutes;
  @Value("${app.email.from}")
  private String fromEmail;
  private final VerificationTokenRepository tokenRepository;

  public EmailService(VerificationTokenRepository tokenRepository) {
    this.tokenRepository = tokenRepository;
  }

  /**
   * Send activation email to user via Gmail API
   *
   * @param toEmail
   * @param userId
   * @throws EmailProcessingException
   */
  public void sendActivationEmail(String toEmail, String userId) throws EmailProcessingException {
    // Lookup verification, generate new if not exist
    VerificationToken vt = tokenRepository.findByUserId(userId).orElseGet(() -> {
      VerificationToken newVT = VerificationToken.builder()
          .token(UUID.randomUUID().toString())
          .userId(userId)
          .build();
      tokenRepository.save(newVT);
      return newVT;
    });

    String tokenStr = vt.getToken();
    String activationLink = activationBaseUrl + "/activate?token=" + tokenStr;
    String subject = "Activate your account";
    String body = "Click the link to activate (valid for " + expirationMinutes + " minutes):\n" + activationLink;

    // create and send email with EmailUtil
    try {

      MimeMessage email = EmailUtil.createEmail(toEmail, fromEmail, subject, body);
      EmailUtil.sendEmail(email);
    } catch (MessagingException e) {
      log.error("Failed to create or send email to {} due to exception", toEmail, e);

      throw new EmailProcessingException("Failed to create email or to send email");
    } catch (IOException | GeneralSecurityException e) {
      log.error("Failed to send activation email to {} due to exception", toEmail, e);

      throw new EmailProcessingException("Failed to send email");
    }
  }

  public VerificationToken confirmTokenExists(String token) {
    return tokenRepository.findByToken(token).orElse(null);
  }
}

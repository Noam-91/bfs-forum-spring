package com.bfsforum.emailservice.service;

import jakarta.mail.Address;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mockStatic;

class EmailUtilTest {

  @Test
  void createEmail() throws MessagingException, IOException {
    // Arrange
    String to = "recipient@example.com";
    String from = "sender@example.com";
    String subject = "Test Subject";
    String body = "This is a test email.";

    // Act
    MimeMessage email = EmailUtil.createEmail(to, from, subject, body);

    // Assert
    assertNotNull(email);
    assertEquals(subject, email.getSubject());
    assertEquals(body, email.getContent().toString().trim());

    Address[] fromAddresses = email.getFrom();
    assertEquals(1, fromAddresses.length);
    assertEquals(from, ((InternetAddress) fromAddresses[0]).getAddress());

    Address[] toAddresses = email.getRecipients(Message.RecipientType.TO);
    assertEquals(1, toAddresses.length);
    assertEquals(to, ((InternetAddress) toAddresses[0]).getAddress());
  }

  @Test
  void sendEmail() {
    try (MockedStatic<EmailUtil> mockedStatic = mockStatic(EmailUtil.class)) {
      mockedStatic.when(() ->
          EmailUtil.sendEmail(

              any(MimeMessage.class)
          )
      ).thenAnswer(invocation -> null); // void method, so do nothing

    }
  }
}
package com.bfsforum.emailservice.service;

import jakarta.mail.Address;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class CreateEmailTest {
    @Test
    void testCreateEmail() throws MessagingException, IOException {
        // Arrange
        String to = "recipient@example.com";
        String from = "sender@example.com";
        String subject = "Test Subject";
        String body = "This is a test email.";

        // Act
        MimeMessage email = CreateEmail.createEmail(to, from, subject, body);

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
}
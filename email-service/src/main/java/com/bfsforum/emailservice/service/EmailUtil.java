package com.bfsforum.emailservice.service;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.model.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.Session;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import org.apache.commons.codec.binary.Base64;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Properties;

public class EmailUtil {
  /**
   * Create a MimeMessage using the parameters provided.
   *
   * @param toEmailAddress   email address of the receiver
   * @param fromEmailAddress email address of the sender, the mailbox account
   * @param subject          subject of the email
   * @param bodyText         body text of the email
   * @return the MimeMessage to be used to send email
   * @throws MessagingException - if a wrongly formatted address is encountered.
   */
  public static MimeMessage createEmail(String toEmailAddress,
                                        String fromEmailAddress,
                                        String subject,
                                        String bodyText)
      throws MessagingException {
    Properties props = new Properties();
    Session session = Session.getDefaultInstance(props, null);

    MimeMessage email = new MimeMessage(session);

    email.setFrom(new InternetAddress(fromEmailAddress));
    email.addRecipient(jakarta.mail.Message.RecipientType.TO,
        new InternetAddress(toEmailAddress));
    email.setSubject(subject);
    email.setText(bodyText);
    return email;
  }

  public static void sendEmail(MimeMessage email) throws IOException, GeneralSecurityException, MessagingException {

    Credential credential = GmailAuthorize.getCredentials();

    Gmail service = new Gmail.Builder(new NetHttpTransport(),
        GsonFactory.getDefaultInstance(),
        credential)
        .setApplicationName("Your App Name")
        .build();

    ByteArrayOutputStream buffer = new ByteArrayOutputStream();
    email.writeTo(buffer);
    byte[] rawMessageBytes = buffer.toByteArray();
    String encodedEmail = Base64.encodeBase64URLSafeString(rawMessageBytes);

    Message message = new Message();
    message.setRaw(encodedEmail);

    try {
      message = service.users().messages().send("me", message).execute();
      System.out.println("Message id: " + message.getId());
    } catch (GoogleJsonResponseException e) {
      System.err.println("Unable to send message: " + e.getDetails());
      throw e;
    }
  }
}

package com.bfsforum.emailservice.service;

import com.google.api.client.googleapis.json.GoogleJsonError;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.GmailScopes;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;

import org.apache.commons.codec.binary.Base64;
import org.springframework.messaging.MessagingException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import com.google.api.services.gmail.model.Message;
import jakarta.mail.internet.MimeMessage;

public class SendMessage {

  public static void sendEmail(String fromEmailAddress,
                               String toEmailAddress,
                               MimeMessage email)
          throws MessagingException, IOException, jakarta.mail.MessagingException {

    GoogleCredentials credentials = GoogleCredentials.getApplicationDefault()
            .createScoped(GmailScopes.GMAIL_SEND);
    HttpRequestInitializer requestInitializer = new HttpCredentialsAdapter(credentials);

    Gmail service = new Gmail.Builder(new NetHttpTransport(),
            GsonFactory.getDefaultInstance(),
            requestInitializer)
            .setApplicationName("Gmail samples")
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
      GoogleJsonError error = e.getDetails();
      if (error.getCode() == 403) {
        System.err.println("Unable to send message: " + e.getDetails());
      } else {
        throw e;
      }
    }
  }
}

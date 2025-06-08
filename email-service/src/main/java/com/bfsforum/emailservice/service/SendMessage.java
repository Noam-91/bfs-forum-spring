package com.bfsforum.emailservice.service;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.model.Message;
import jakarta.mail.internet.MimeMessage;
import org.apache.commons.codec.binary.Base64;

import java.io.ByteArrayOutputStream;

public class SendMessage {

  public static void sendEmail(MimeMessage email)
          throws Exception {

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

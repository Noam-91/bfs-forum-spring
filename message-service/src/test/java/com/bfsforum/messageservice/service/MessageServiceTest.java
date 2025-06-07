package com.bfsforum.messageservice.service;

import com.bfsforum.messageservice.domain.Message;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MessageServiceTest {

  @Test
  void createMessage() {
    // Arrange
    Message message = Message.builder()
        .email("test@test.com")
        .subject("Account Issue")
        .content("I cannot login to my account")
        .build();

  }

  @Test
  void getAllMessages() {
  }

  @Test
  void solveMessage() {
  }
}
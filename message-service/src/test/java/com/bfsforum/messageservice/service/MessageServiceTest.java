package com.bfsforum.messageservice.service;

import com.bfsforum.messageservice.domain.Message;
import com.bfsforum.messageservice.domain.Status;
import com.bfsforum.messageservice.exception.BadRequestException;
import com.bfsforum.messageservice.repository.MessageDao;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MessageServiceTest {
  @Mock
  private MessageDao messageDao;

  @InjectMocks
  private MessageService messageService;

  @Test
  void createMessageWithSufficientInfo_ShouldReturnVoid() {
    // Arrange
    Message message = Message.builder()
        .email("test@test.com")
        .subject("Account Issue")
        .content("I cannot login to my account")
        .build();
    when(messageDao.save(Mockito.any(Message.class))).thenReturn(message);

    // Act
    messageService.createMessage(message);
    Mockito.verify(messageDao, Mockito.times(1)).save(message);
  }

  @Test
  void createMessageWithInsufficientInfo_ShouldThrowBadRequest() {
    // Arrange
    Message message = Message.builder()
        .content("I cannot login to my account")
        .build();
    when(messageDao.save(message))
        .thenThrow(new RuntimeException("Could not commit JPA transaction"));

    // Act
    assertThrows(RuntimeException.class, () -> {
          messageService.createMessage(message);
        });
    Mockito.verify(messageDao, Mockito.times(1)).save(message);
  }

  @Test
  void getAllMessages_ShouldReturnListOfSize1() {
    // Arrange
    int page = 0;
    int size = 10;
    String userRole = "ADMIN";

    Message singleMessage = Message.builder()
        .id("fd145g1dsf0gv2")
        .email("test@example.com")
        .subject("Test Subject")
        .content("Test Content")
        .build();
    List<Message> expectedMessages = List.of(singleMessage);
    Pageable pageable = Pageable.ofSize(size).withPage(page);
    Page<Message> messagePage = new PageImpl<>(expectedMessages, pageable, 1);
    when(messageDao.findAll(any(Pageable.class))).thenReturn(messagePage);

    // Act
    List<Message> messages =messageService.getAllMessages(page, size, userRole);

    // Assert
    assertEquals(1, messages.size());
    assertEquals(singleMessage, messages.get(0));
    verify(messageDao, times(1)).findAll(any(Pageable.class));
  }

  @Test
  void solveMessage_ShouldUpdateStatusToSOLVED() {
    // Arrange
    String id = "fd145g1dsf0gv2";
    String userId = "XXXXXXX";
    String userRole = "ADMIN";
    Message message = Message.builder()
        .id(id)
        .email("test@example.com")
        .subject("Test Subject")
        .content("Test Content")
        .status(Status.UNSOLVED)
        .build();
    when(messageDao.findById(id)).thenReturn(java.util.Optional.of(message));
    when(messageDao.save(message)).thenReturn(message);

    // Act
    messageService.solveMessage(id, userId, userRole);

    // Assert
    assertEquals(Status.SOLVED, message.getStatus());
    assertEquals(userId, message.getUpdatedBy());
    verify(messageDao, times(1)).findById(id);
    verify(messageDao, times(1)).save(message);
  }
}
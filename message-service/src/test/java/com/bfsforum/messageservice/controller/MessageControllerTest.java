package com.bfsforum.messageservice.controller;

import com.bfsforum.messageservice.domain.Message;
import com.bfsforum.messageservice.exception.BadRequestException;
import com.bfsforum.messageservice.exception.NotAuthorizedException;
import com.bfsforum.messageservice.service.MessageService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.Slice;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@WebMvcTest(
    controllers = MessageController.class,
    excludeAutoConfiguration = SecurityAutoConfiguration.class // EXCLUDE SPRING SECURITY
)
class MessageControllerTest {
  @MockitoBean
  private MessageService messageService;

  @Autowired
  private MockMvc mockMvc;
  @Autowired
  private ObjectMapper objectMapper;

  @Test
  void createMessageWithSufficientInfo_ShouldReturnOk() throws Exception {
    // Arrange
    doNothing().when(messageService).createMessage(any(Message.class));

    // Act & Assert
    mockMvc.perform(MockMvcRequestBuilders.post("/messages")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(new Message())))
        .andExpect(MockMvcResultMatchers.status().isOk())
        .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("Message has been sent to the admin team."));
    Mockito.verify(messageService, Mockito.times(1)).createMessage(any(Message.class));
  }

  @Test
  void getAllMessagesWithADMINRole_ShouldReturnListOfMessages() throws Exception {
    // Arrange
    List<Message> messages = List.of(new Message());
    when(messageService.getAllMessages(anyInt(), anyInt(), anyString())).thenReturn(messages);

    // Act & Assert
    mockMvc.perform(MockMvcRequestBuilders.get("/messages")
            .contentType(MediaType.APPLICATION_JSON)
            .param("page", "0")
            .param("size", "10")
            .header("X-User-Role", "ADMIN"))
        .andExpect(MockMvcResultMatchers.status().isOk())
        .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(MockMvcResultMatchers.jsonPath("$").isArray())
        .andExpect(MockMvcResultMatchers.jsonPath("$[0]").exists());

    Mockito.verify(messageService, Mockito.times(1)).getAllMessages(anyInt(), anyInt(), anyString());
  }

  @Test
  void getAllMessagesWithUSERRole_ShouldReturnForbidden() throws Exception {
    // Arrange
    List<Message> messages = List.of(new Message());
    when(messageService.getAllMessages(anyInt(), anyInt(), anyString()))
        .thenThrow(new NotAuthorizedException("User is not authorized to get all messages"));

    // Act & Assert
    mockMvc.perform(MockMvcRequestBuilders.get("/messages")
            .contentType(MediaType.APPLICATION_JSON)
            .param("page", "0")
            .param("size", "10")
            .header("X-User-Role", "USER"))
        .andExpect(MockMvcResultMatchers.status().isForbidden());

    Mockito.verify(messageService, Mockito.times(1)).getAllMessages(anyInt(), anyInt(), anyString());
  }

  @Test
  void solveMessageWithADMINRole_ShouldReturnOk() {
    // Arrange
    doNothing().when(messageService).solveMessage(anyString(), anyString(), anyString());

    // Act & Assert
    try {
      mockMvc.perform(MockMvcRequestBuilders.patch("/messages/1")
              .contentType(MediaType.APPLICATION_JSON)
              .header("X-User-Id", "1")
              .header("X-User-Role", "ADMIN"))
          .andExpect(MockMvcResultMatchers.status().isOk())
          .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
          .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("Message has been solved."));
    } catch (Exception e) {
      fail("Exception thrown: " + e.getMessage());
    }
  }

  @Test
  void solveMessageWithSOLVEDMessage_ShouldReturnBadRequest() throws Exception {
    // Arrange
    doThrow(new BadRequestException("Message has been solved"))
        .when(messageService).solveMessage(anyString(), anyString(), anyString());

    // Act & Assert
    mockMvc.perform(MockMvcRequestBuilders.patch("/messages/1")
            .contentType(MediaType.APPLICATION_JSON)
            .header("X-User-Id", "1")
            .header("X-User-Role", "USER"))
        .andExpect(MockMvcResultMatchers.status().isBadRequest())
        .andExpect(MockMvcResultMatchers.jsonPath("$.error").value("Message has been solved"));
    Mockito.verify(messageService, Mockito.times(1)).solveMessage(anyString(), anyString(), anyString());
  }
}
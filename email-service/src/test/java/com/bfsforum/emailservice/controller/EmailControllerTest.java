package com.bfsforum.emailservice.controller;

import com.bfsforum.emailservice.service.EmailService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import static org.mockito.Mockito.*;

@WebMvcTest(EmailController.class)
class EmailControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private EmailService emailService;

    EmailControllerTest(MockMvc mockMvc, EmailService emailService) {
        this.mockMvc = mockMvc;
        this.emailService = emailService;
    }

    @Test
    void sendActivationEmail() {
    }

    @Test
    void activateUser() {
    }
    @Test
    void testSendActivationEmail_success() throws Exception {
        String jsonRequest = "{ \"email\": \"test@example.com\" }";

        doNothing().when(emailService).sendActivationEmail("test@example.com");

        mockMvc.perform(MockMvcRequestBuilders.post("/email/send-activation")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().string("Activation email sent."));
    }

    @Test
    void testSendActivationEmail_failure() throws Exception {
        String jsonRequest = "{ \"email\": \"test@example.com\" }";

        doThrow(new RuntimeException("Email failed")).when(emailService).sendActivationEmail("test@example.com");

        mockMvc.perform(MockMvcRequestBuilders.post("/email/send-activation")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(MockMvcResultMatchers.status().isInternalServerError())
                .andExpect(MockMvcResultMatchers.content().string("Failed to send email."));
    }

    @Test
    void testActivateUser_validToken() throws Exception {
        String token = "valid-token";

        when(emailService.validateToken(token)).thenReturn(true);
        when(emailService.consumeToken(token)).thenReturn("test@example.com");

        mockMvc.perform(MockMvcRequestBuilders.get("/email/activate")
                        .param("token", token))
                .andExpect(MockMvcResultMatchers.status().isFound())
                .andExpect(MockMvcResultMatchers.redirectedUrl("http://yourfrontend.com/home"));
    }

    @Test
    void testActivateUser_invalidToken() throws Exception {
        String token = "invalid-token";

        when(emailService.validateToken(token)).thenReturn(false);

        mockMvc.perform(MockMvcRequestBuilders.get("/email/activate")
                        .param("token", token))
                .andExpect(MockMvcResultMatchers.status().isUnauthorized())
                .andExpect(MockMvcResultMatchers.content().string("Invalid or expired token."));
    }
}
package com.bfsforum.emailservice.controller;

import com.bfsforum.emailservice.service.EmailService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import static org.mockito.Mockito.*;
@SpringBootTest
@AutoConfigureMockMvc
@WebMvcTest(EmailController.class)
class EmailControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private EmailService emailService;




    @Test
    public void testSendActivationEmail_success() throws Exception {
        String email = "test@example.com";

        mockMvc.perform(MockMvcRequestBuilders.post("/email/send-activation")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"" + email + "\"}"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().string("Activation email sent."));
    }

    @Test
    public void testSendActivationEmail_alreadySent() throws Exception {
        String email = "test@example.com";

        doThrow(new IllegalStateException("Activation link already sent. Please wait up to 10 minutes."))
                .when(emailService).sendActivationEmail(email);

        mockMvc.perform(MockMvcRequestBuilders.post("/email/send-activation")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"" + email + "\"}"))
                .andExpect(MockMvcResultMatchers.status().isTooManyRequests())
                .andExpect(MockMvcResultMatchers.content().string("Activation link already sent. Please wait up to 10 minutes."));
    }

    @Test
    public void testActivateUser_validToken() throws Exception {
        String token = "valid-token";
        String email = "test@example.com";

        Mockito.when(emailService.validateToken(token)).thenReturn(true);
        Mockito.when(emailService.consumeToken(token)).thenReturn(email);

        mockMvc.perform(MockMvcRequestBuilders.get("/email/activate")
                        .param("token", token))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().string("Account activated for " + email));
    }

    @Test
    public void testActivateUser_invalidToken() throws Exception {
        String token = "expired-token";

        Mockito.when(emailService.validateToken(token)).thenReturn(false);

        mockMvc.perform(MockMvcRequestBuilders.get("/email/activate")
                        .param("token", token))
                .andExpect(MockMvcResultMatchers.status().isUnauthorized())
                .andExpect(MockMvcResultMatchers.content().string("Invalid or expired token."));
    }
}
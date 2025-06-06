package com.bfsforum.emailservice.service;

import com.bfsforum.emailservice.dto.TokenInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.messaging.MessagingException;

import java.io.IOException;
import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;

class EmailServiceTest {

    @Test
    void sendActivationEmail() {
    }

    @Test
    void validateToken() {
    }

    @Test
    void consumeToken() {
    }
    private EmailService emailService;

    @BeforeEach
    void setup() {
        emailService = new EmailService();

        // manually set @Value fields since no Spring context
        emailService.activationBaseUrl = "http://localhost:8080/email";
        emailService.expirationMinutes = 10;
        emailService.fromEmail = "from@example.com";
    }

    @Test
    void testSendActivationEmail_success() throws MessagingException, IOException, jakarta.mail.MessagingException {
        String toEmail = "user@example.com";

        try (
                MockedStatic<CreateEmail> mockCreateEmail = mockStatic(CreateEmail.class);
                MockedStatic<SendMessage> mockSendMessage = mockStatic(SendMessage.class)
        ) {
            var mockMimeMessage = mock(jakarta.mail.internet.MimeMessage.class);
            mockCreateEmail.when(() ->
                    CreateEmail.createEmail(eq(toEmail), eq("from@example.com"), anyString(), contains("http://localhost:8080/email/activate?token="))
            ).thenReturn(mockMimeMessage);

            mockSendMessage.when(() -> SendMessage.sendEmail(any(), any(), any()))
                    .thenAnswer(invocation -> null);


            emailService.sendActivationEmail(toEmail);

            assertTrue(emailService.tokenStore.values().stream()
                    .anyMatch(token -> token.getEmail().equals(toEmail)));
        }
    }

    @Test
    void testValidateToken_valid() {
        String token = UUID.randomUUID().toString();
        emailService.tokenStore.put(token, new TokenInfo("user@example.com", Instant.now()));

        assertTrue(emailService.validateToken(token));
    }

    @Test
    void testValidateToken_expired() {
        String token = UUID.randomUUID().toString();
        Instant expiredTime = Instant.now().minusSeconds(15 * 60); // 15 minutes ago
        emailService.tokenStore.put(token, new TokenInfo("user@example.com", expiredTime));

        assertFalse(emailService.validateToken(token));
    }

    @Test
    void testConsumeToken() {
        String token = UUID.randomUUID().toString();
        emailService.tokenStore.put(token, new TokenInfo("test@example.com", Instant.now()));

        String result = emailService.consumeToken(token);
        assertEquals("test@example.com", result);

        // should be removed now
        assertFalse(emailService.tokenStore.containsKey(token));
    }

    @Test
    void testConsumeToken_invalid() {
        String result = emailService.consumeToken("non-existent-token");
        assertNull(result);
    }
}
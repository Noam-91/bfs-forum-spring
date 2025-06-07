package com.bfsforum.emailservice.service;

import com.bfsforum.emailservice.dao.UserRepository;
import com.bfsforum.emailservice.dto.Role;
import com.bfsforum.emailservice.dto.TokenInfo;
import com.bfsforum.emailservice.dto.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class EmailServiceTest {
    @InjectMocks
    private EmailService emailService;

    @Mock
    private UserRepository userRepository;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        // Inject mock values for @Value fields
        ReflectionTestUtils.setField(emailService, "activationBaseUrl", "http://localhost:8080/email");
        ReflectionTestUtils.setField(emailService, "expirationMinutes", 10L);
        ReflectionTestUtils.setField(emailService, "fromEmail", "noreply@example.com");
    }

    @Test
    public void testSendActivationEmail_success() throws Exception {
        String toEmail = "user@example.com";

        // No existing token yet
        emailService.sendActivationEmail(toEmail);

        // Since SendMessage.sendEmail is static, you can’t mock it here without a framework like PowerMockito.
        // Just verify the token was created
        assertTrue(emailService.tokenStore.values().stream()
                .anyMatch(t -> t.getEmail().equals(toEmail)));
    }

    @Test
    public void testSendActivationEmail_duplicateWithin10Min_throwsException() {
        String toEmail = "user@example.com";
        emailService.tokenStore.put("existing-token", new TokenInfo(toEmail, Instant.now()));

        IllegalStateException ex = assertThrows(IllegalStateException.class, () ->
                emailService.sendActivationEmail(toEmail));

        assertEquals("Activation link already sent. Please wait up to 10 minutes.", ex.getMessage());
    }

    @Test
    public void testValidateToken_validToken_returnsTrue() {
        String token = "valid-token";
        emailService.tokenStore.put(token, new TokenInfo("user@example.com", Instant.now()));

        assertTrue(emailService.validateToken(token));
    }

    @Test
    public void testValidateToken_expiredToken_returnsFalse() {
        String token = "expired-token";
        Instant expiredTime = Instant.now().minusSeconds(11 * 60);
        emailService.tokenStore.put(token, new TokenInfo("user@example.com", expiredTime));

        assertFalse(emailService.validateToken(token));
    }

    @Test
    public void testConsumeToken_updatesUserAndReturnsEmail() {
        String token = "consume-token";
        String email = "user@example.com";
        User mockUser = new User();
        mockUser.setUsername(email);
        mockUser.setActive(false);
        mockUser.setRole(Role.UNVERIFIED);

        emailService.tokenStore.put(token, new TokenInfo(email, Instant.now()));
        when(userRepository.findByUsername(email)).thenReturn(Optional.of(mockUser));

        String result = emailService.consumeToken(token);

        assertEquals(email, result);
        assertTrue(mockUser.isActive());
        assertEquals(Role.USER, mockUser.getRole());
        verify(userRepository).save(mockUser);
    }

    @Test
    public void testConsumeToken_noUserFound_returnsEmailOnly() {
        String token = "consume-token";
        String email = "unknown@example.com";

        emailService.tokenStore.put(token, new TokenInfo(email, Instant.now()));
        when(userRepository.findByUsername(email)).thenReturn(Optional.empty());

        String result = emailService.consumeToken(token);

        assertEquals(email, result);
        verify(userRepository, never()).save(any());
    }

    @Test
    public void testConsumeToken_invalidToken_returnsNull() {
        String result = emailService.consumeToken("nonexistent-token");
        assertNull(result);
    }
}
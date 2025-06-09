package com.bfsforum.emailservice.service;

import com.bfsforum.emailservice.dao.VerificationTokenRepository;
import com.bfsforum.emailservice.domain.VerificationToken;
import com.bfsforum.emailservice.dto.UserRegisterRequest;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Value;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.*;

class EmailServiceTest {
    @Mock
    private VerificationTokenRepository vtRepository;

    private MockedStatic<EmailUtil> mockedEmailUtil;

    @InjectMocks
    private EmailService emailService;

    private String activationBaseUrl;
    private long expirationMinutes;
    private String testFromEmail = "test-from@test.com";


    private UserRegisterRequest userRegisterRequest;
    private VerificationToken vt;
    private MimeMessage mockMimeMessage;


    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockedEmailUtil = mockStatic(EmailUtil.class);

        activationBaseUrl = "localhost:8080";
        expirationMinutes = 10;
        testFromEmail = "test-from@test.com";

        String userId = UUID.randomUUID().toString();
        userRegisterRequest = UserRegisterRequest.builder()
            .email("test@example.com")
            .userId(userId)
            .build();
        vt = VerificationToken.builder()
            .token(UUID.randomUUID().toString())
            .userId(userId)
            .build();

        // Mock EmailUtil behaviors
        mockMimeMessage = mock(MimeMessage.class);
        mockedEmailUtil.when(() ->
                EmailUtil.createEmail(any(String.class), any(String.class), any(String.class), any(String.class)))
            .thenReturn(mockMimeMessage);
        mockedEmailUtil.when(() -> EmailUtil.sendEmail(any(MimeMessage.class)))
            .thenAnswer(invocation -> {
                MimeMessage messageSent = invocation.getArgument(0);
                return null;
            });
    }

    @Test
    void sendActivationEmail_ShouldNotThrowException_WhenTokenExists() {
        when(vtRepository.findByUserId(userRegisterRequest.getUserId())).thenReturn(Optional.of(vt));

        // When: sendActivationEmail is called
        String testToEmail = userRegisterRequest.getEmail();
        String testUserId = userRegisterRequest.getUserId();
        assertDoesNotThrow(() -> emailService.sendActivationEmail(testToEmail, testUserId));

        verify(vtRepository, times(1)).findByUserId(testUserId);

        // Verify that tokenRepository.save was NOT called (since token already exists)
        verify(vtRepository, never()).save(any(VerificationToken.class));

        // Verify EmailUtil.createEmail was called with expected arguments
        String expectedSubject = "Activate your account";
        String activationLink = activationBaseUrl + "/activate?token=" + vt.getToken();
        String expectedBody = "Click the link to activate (valid for " + expirationMinutes + " minutes):\n" + activationLink;
        mockedEmailUtil.verify(() ->
            EmailUtil.createEmail(eq(testToEmail), eq(testFromEmail), eq(expectedSubject), eq(expectedBody)), times(1));

        // Verify EmailUtil.sendEmail was called once with the created MimeMessage
        mockedEmailUtil.verify(() -> EmailUtil.sendEmail(eq(mockMimeMessage)), times(1));
    }


}
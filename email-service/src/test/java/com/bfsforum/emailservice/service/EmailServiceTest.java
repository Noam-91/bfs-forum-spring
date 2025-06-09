package com.bfsforum.emailservice.service;

import com.bfsforum.emailservice.dao.VerificationTokenRepository;
import com.bfsforum.emailservice.domain.VerificationToken;
import com.bfsforum.emailservice.dto.UserRegisterRequest;
import com.bfsforum.emailservice.exception.EmailProcessingException;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.*;
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


    @InjectMocks
    private EmailService emailService;


    @Value("${app.activation.base-url}")
    private String activationBaseUrl;

    @Value("${token.expiration.minutes}")
    private long expirationMinutes;

    @Value("${app.email.from}")
    private String testFromEmail;
    private MockedStatic<EmailUtil> mockedEmailUtil;

    private UserRegisterRequest userRegisterRequest;
    private VerificationToken vt;
    private MimeMessage mockMimeMessage;


    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockedEmailUtil = mockStatic(EmailUtil.class);

//        activationBaseUrl = "localhost:8080";
//        expirationMinutes = 10;
//        testFromEmail = "test-from@test.com";

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
                        EmailUtil.createEmail(any(String.class), any(String.class),
                                any(String.class), any(String.class)))
                .thenReturn(mockMimeMessage);
        mockedEmailUtil.when(() -> EmailUtil.sendEmail(any(MimeMessage.class)))
                .thenAnswer(invocation -> {
                    invocation.getArgument(0);
                    return null;
                });
    }

    @AfterEach
    void tearDown() {
        mockedEmailUtil.close();
    }

    @Test
    void sendActivationEmail_ShouldNotThrowException_WhenTokenExists() {
        when(vtRepository.findByUserId(userRegisterRequest.getUserId())).thenReturn(Optional.of(vt));

        assertDoesNotThrow(() ->
                emailService.sendActivationEmail(userRegisterRequest.getEmail(), userRegisterRequest.getUserId()));

        verify(vtRepository, times(1)).findByUserId(userRegisterRequest.getUserId());
        verify(vtRepository, never()).save(any());

        String expectedBody = "Click the link to activate (valid for " + expirationMinutes + " minutes):\n" +
                activationBaseUrl + "/activate?token=" + vt.getToken();
        mockedEmailUtil.verify(() -> EmailUtil.createEmail(
                eq(userRegisterRequest.getEmail()), eq(testFromEmail), eq("Activate your account"),
                eq(expectedBody)), times(1));
    }

    @Test
    void sendActivationEmail_ShouldCreateNewToken_WhenTokenDoesNotExist() {
        when(vtRepository.findByUserId(userRegisterRequest.getUserId())).thenReturn(Optional.empty());

        assertDoesNotThrow(() ->
                emailService.sendActivationEmail(userRegisterRequest.getEmail(), userRegisterRequest.getUserId()));

        verify(vtRepository).save(any(VerificationToken.class));
    }

    @Test
    void sendActivationEmail_ShouldThrowEmailProcessingException_OnMessagingException() {
        when(vtRepository.findByUserId(any())).thenReturn(Optional.of(vt));
        mockedEmailUtil.when(() -> EmailUtil.createEmail(any(), any(), any(), any()))
                .thenThrow(new jakarta.mail.MessagingException("Mock error"));

        Assertions.assertThrows(EmailProcessingException.class, () ->
                emailService.sendActivationEmail("test@example.com", "any-id"));
    }

    @Test
    void sendActivationEmail_ShouldThrowEmailProcessingException_OnSecurityException() {
        when(vtRepository.findByUserId(any())).thenReturn(Optional.of(vt));
        mockedEmailUtil.when(() -> EmailUtil.createEmail(any(), any(), any(), any())).thenReturn(mockMimeMessage);
        mockedEmailUtil.when(() -> EmailUtil.sendEmail(any()))
                .thenThrow(new java.security.GeneralSecurityException("Security error"));

        Assertions.assertThrows(EmailProcessingException.class, () ->
                emailService.sendActivationEmail("test@example.com", "any-id"));
    }

    @Test
    void sendActivationEmail_ShouldThrowEmailProcessingException_OnIOException() {
        when(vtRepository.findByUserId(any())).thenReturn(Optional.of(vt));
        mockedEmailUtil.when(() -> EmailUtil.createEmail(any(), any(), any(), any())).thenReturn(mockMimeMessage);
        mockedEmailUtil.when(() -> EmailUtil.sendEmail(any()))
                .thenThrow(new java.io.IOException("IO error"));

        Assertions.assertThrows(EmailProcessingException.class, () ->
                emailService.sendActivationEmail("test@example.com", "any-id"));
    }

    @Test
    void confirmTokenExists_ShouldReturnToken_WhenFound() {
        when(vtRepository.findByToken(vt.getToken())).thenReturn(Optional.of(vt));
        VerificationToken result = emailService.confirmTokenExists(vt.getToken());
        Assertions.assertEquals(vt, result);
    }

    @Test
    void confirmTokenExists_ShouldReturnNull_WhenNotFound() {
        when(vtRepository.findByToken("invalid")).thenReturn(Optional.empty());
        VerificationToken result = emailService.confirmTokenExists("invalid");
        Assertions.assertNull(result);
    }


}
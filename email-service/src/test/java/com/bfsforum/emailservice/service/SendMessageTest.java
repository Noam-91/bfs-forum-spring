package com.bfsforum.emailservice.service;

import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mockStatic;

class SendMessageTest {

    @Test
    void sendEmail() {
    }
    @Test
    void testSendEmail_staticMock() {
        try (MockedStatic<SendMessage> mockedStatic = mockStatic(SendMessage.class)) {
            mockedStatic.when(() ->
                    SendMessage.sendEmail(
                            anyString(),
                            anyString(),
                            any(MimeMessage.class)
                    )
            ).thenAnswer(invocation -> null); // void method, so do nothing

            // call code that calls SendMessage.sendEmail(...)
        }

    }
}
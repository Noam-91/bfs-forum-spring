package com.bfsforum.emailservice.service;

import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mockStatic;

class SendMessageTest {


    @Test
    void testSendEmail_staticMock() {
        try (MockedStatic<SendMessage> mockedStatic = mockStatic(SendMessage.class)) {
            mockedStatic.when(() ->
                    SendMessage.sendEmail(

                            any(MimeMessage.class)
                    )
            ).thenAnswer(invocation -> null); // void method, so do nothing

        }

    }
}
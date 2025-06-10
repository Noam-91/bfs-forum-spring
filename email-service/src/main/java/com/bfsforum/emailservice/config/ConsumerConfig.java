package com.bfsforum.emailservice.config;

import com.bfsforum.emailservice.dto.UserRegisterRequest;
import com.bfsforum.emailservice.domain.VerificationToken;
import com.bfsforum.emailservice.exception.EmailProcessingException;
import com.bfsforum.emailservice.service.EmailService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;

import java.util.function.Consumer;

@Configuration
@Slf4j
public class ConsumerConfig {
  private final EmailService emailService;
  private final StreamBridge streamBridge;

  public ConsumerConfig(EmailService emailService, StreamBridge streamBridge) {
    this.emailService = emailService;
    this.streamBridge = streamBridge;
  }

  @Value("${app.kafka.binding.verification-response}")
  private String verificationResponseBinding;

  /**
   * Consumes registration events from user-service and send activation email.
   * no reply
   */
  @Bean
  public Consumer<Message<UserRegisterRequest>> registerNotificationConsumer() {
    return message -> {
      try {
        log.info("测试测试：收到注册消息 Header: {}", message.getHeaders());
        UserRegisterRequest user = message.getPayload();
        log.info("📨 收到注册消息 Payload: {}", user);
        String email = user.getEmail();
        String userId = user.getUserId();


        emailService.sendActivationEmail(email, userId);
        log.info("Activation email sent to {}", email);
      } catch (EmailProcessingException e) {
        log.error("Failed to send activation email to {}", e.getMessage());
      }
    };
  }

  /**
   * Consumes token verification requests from user-service and return the existence of token.
   * reply VerificationToken
   */
  @Bean
  public Consumer<Message<String>> verificationRequestConsumer() {
    return message -> {
      String correlationId = (String) message.getHeaders().get(KafkaHeaders.CORRELATION_ID);
      String token = message.getPayload();
      VerificationToken existedToken = emailService.confirmTokenExists(token);
      log.info("Token {} is valid", token);
      Message<VerificationToken> replyMessage = MessageBuilder.withPayload(existedToken)
          .setHeader(KafkaHeaders.CORRELATION_ID, correlationId)
          .build();

      streamBridge.send("verificationResponseSupplier-out-0", replyMessage);
    };
  }
}

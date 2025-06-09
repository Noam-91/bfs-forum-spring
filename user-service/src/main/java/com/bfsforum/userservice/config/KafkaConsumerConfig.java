package com.bfsforum.userservice.config;

import com.bfsforum.userservice.dto.EmailVerificationReply;
import com.bfsforum.userservice.dto.UserInfoReply;
import com.bfsforum.userservice.entity.User;
import com.bfsforum.userservice.repository.UserRepository;
import com.bfsforum.userservice.service.RequestReplyManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;

import java.time.LocalDateTime;
import java.util.function.Consumer;


@Slf4j
@Configuration
public class KafkaConsumerConfig {

    private final RequestReplyManager<EmailVerificationReply> requestReplyManager;
    private final UserRepository userRepository;
    private final StreamBridge streamBridge;

    public KafkaConsumerConfig(RequestReplyManager<EmailVerificationReply> requestReplyManager,
                               UserRepository userRepository,
                               StreamBridge streamBridge) {
        this.requestReplyManager = requestReplyManager;
        this.userRepository = userRepository;
        this.streamBridge = streamBridge;
    }

    @Bean
    public Consumer<Message<EmailVerificationReply>> emailVerificationReplyConsumer() {
        return message -> {
            String correlationId = (String) message.getHeaders().get(KafkaHeaders.CORRELATION_ID);
            EmailVerificationReply reply = message.getPayload();

            log.info("Received token verification reply for correlationId {}: {}", correlationId, reply);
            if (reply == null || reply.getExpiresAt() == null || reply.getExpiresAt().isBefore(LocalDateTime.now())) {
                log.warn("Invalid or expired token received for correlationId {}. reply={}", correlationId, reply.getExpiresAt());

                requestReplyManager.completeFutureExceptionally(
                        correlationId, new RuntimeException("Token is invalid or expired"));
                return;
            }
            requestReplyManager.completeFuture(correlationId, reply);
        };
    }

    @Value("${bfs-forum.kafka.user-info-reply-binding-name}")
    private String userInfoReplyBinding;
    @Bean
    public Consumer<Message<String>> userInfoRequestConsumer() {
        return message -> {
            String userId = message.getPayload();
            String correlationId = message.getHeaders().get(KafkaHeaders.CORRELATION_ID, String.class);

            log.info("Received user info request for userId={}, correlationId={}", userId, correlationId);

            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found: " + userId));

            UserInfoReply reply = UserInfoReply.builder()
                    .userId(user.getId())
                    .username(user.getUsername())
                    .firstName(user.getProfile().getFirstName())
                    .lastName(user.getProfile().getLastName())
                    .build();

            Message<UserInfoReply> replyMessage = MessageBuilder.withPayload(reply)
                    .setHeader(KafkaHeaders.CORRELATION_ID, correlationId)
                    .build();

            streamBridge.send(userInfoReplyBinding, replyMessage);
        };
    }
}
package com.bfsforum.userservice.config;

import com.bfsforum.userservice.dto.EmailVerificationReply;
import com.bfsforum.userservice.dto.UserInfoReply;
import com.bfsforum.userservice.entity.User;
import com.bfsforum.userservice.repository.UserRepository;
import com.bfsforum.userservice.service.RequestReplyManager;
import com.bfsforum.userservice.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;

import java.time.Instant;
import java.util.List;
import java.util.function.Consumer;


@Slf4j
@Configuration
public class KafkaConsumerConfig {

    private final RequestReplyManager<EmailVerificationReply> requestReplyManager;
    private final UserService userService;
    private final StreamBridge streamBridge;

    public KafkaConsumerConfig(RequestReplyManager<EmailVerificationReply> requestReplyManager,
                               UserService userService,
                               StreamBridge streamBridge) {
        this.requestReplyManager = requestReplyManager;
        this.userService = userService;
        this.streamBridge = streamBridge;
    }

    @Bean
    public Consumer<Message<EmailVerificationReply>> emailVerificationReplyConsumer() {
        return message -> {
            String correlationId = (String) message.getHeaders().get(KafkaHeaders.CORRELATION_ID);
            EmailVerificationReply reply = message.getPayload();

            log.info("Received token verification reply for correlationId {}: {}", correlationId, reply);
            if (reply == null || reply.getExpiredAt() == null || reply.getExpiredAt().isBefore(Instant.now())) {
                log.warn("Invalid or expired token received for correlationId {}. reply={}", correlationId, reply.getExpiredAt());

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
    public Consumer<Message<List<String>>> userInfoRequestConsumer() {
        return message -> {
            List<String> userIds = message.getPayload();
            String correlationId = message.getHeaders().get(KafkaHeaders.CORRELATION_ID, String.class);

            log.info("Received user info request: userId={}, correlationId={}", userIds, correlationId);

            List<User> users = userService.getUsersByIds(userIds);

            List<UserInfoReply> replies = users.stream().map(user -> UserInfoReply.builder()
                    .userId(user.getId())
                    .firstName(user.getProfile().getFirstName())
                    .lastName(user.getProfile().getLastName())
                    .imgUrl(user.getProfile().getImgUrl())
                    .build()).toList();

            Message<List<UserInfoReply>> replyMessage = MessageBuilder.withPayload(replies)
                    .setHeader(KafkaHeaders.CORRELATION_ID, correlationId)
                    .build();

            boolean sent = streamBridge.send(userInfoReplyBinding, replyMessage);
            log.debug("Sent reply to topic '{}'? {}", userInfoReplyBinding, sent);
            log.debug("Reply payload: {}", replies);
        };
    }
}
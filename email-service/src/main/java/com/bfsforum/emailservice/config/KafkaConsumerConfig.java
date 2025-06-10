package com.bfsforum.emailservice.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;

@Configuration
@Slf4j
public class KafkaConsumerConfig {
//    private final EmailService_backup emailService;
//    private final StreamBridge streamBridge;
//
//    public KafkaConsumerConfig(EmailService_backup emailService, StreamBridge streamBridge) {
//        this.emailService = emailService;
//        this.streamBridge = streamBridge;
//    }
//
//    @Bean
//    public Consumer<Message<String>> EmailEventConsumer() {
//        return message -> {
//            try {
//                String json = message.getPayload();
//                ObjectMapper objectMapper = new ObjectMapper();
//                JsonNode root = objectMapper.readTree(json);
//
//                if (root.has("email") && root.has("userId")) {
//                    String email = root.get("email").asText();
//                    String userId = root.get("userId").asText();
//
//                    log.info("Received request to send email to {}", email);
//                    emailService.sendActivationEmail(email, userId);
//
//                } else if (root.has("token")) {
//                    String token = root.get("token").asText();
//                    log.info("Received request to resolve token {}", token);
//
//                    Optional<VerificationToken> info = emailService.resolveToken(token);
//                    emailService.consumeToken(token);
//                    if (info.isPresent()) {
//                        VerificationToken result = info.get();
//                        Map<String, Object> response = Map.of(
//                                "userId", result.getUserId().toString(),
//                                "expiredAt", result.getExpiredAt().toString(),
//                                "token", token
//                        );
//                        streamBridge.send("emailEventReply-out-0", response);
//                    } else {
//                        log.warn("Token {} is invalid or expired", token);
//                        streamBridge.send("emailEventReply-out-0", Map.of(
//                                "token", token,
//                                "error", "Token invalid or expired"
//                        ));
//                    }
//                } else {
//                    log.warn("Unknown message format: {}", json);
//                }
//
//            } catch (Exception e) {
//                log.error("Failed to process email event", e);
//            }
//        };
//    }

}


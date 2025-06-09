package com.bfsforum.historyservice.web;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.bfsforum.historyservice.domain.Post;
import com.bfsforum.historyservice.service.RequestReplyManager;

/**
 * Controller to simulate the enrichment flow by delegating to a middle controller.
 */
@Slf4j
@RestController
@RequestMapping("/mock/enrich")
public class MockEnrichController {


    private final StreamBridge streamBridge;
    private final String requestBinding;
    private final String responseBinding;

    public MockEnrichController(
            StreamBridge streamBridge,
            @Value("${bfs-forum.kafka.request-binding-name}") String requestBinding,
            @Value("${spring.cloud.stream.bindings.postEnrichRequestEventConsumer-in-0.destination}") String responseBinding
    ) {
        this.streamBridge = streamBridge;
        this.requestBinding = requestBinding;
        this.responseBinding = responseBinding + "-out-0";
    }

    /**
     * Sends a list of strings to the request binding and then immediately to the response binding,
     * mimicking a loop, and returns the payload.
     */
    @PostMapping("/loop")
    public ResponseEntity<List<String>> sendLoop(@RequestBody List<String> payload) {
        String correlationId = UUID.randomUUID().toString();
        Message<List<String>> message = MessageBuilder
                .withPayload(payload)
                .setHeader(KafkaHeaders.CORRELATION_ID, correlationId)
                .build();

        boolean sentReq = streamBridge.send(requestBinding, message);
        log.info("Sent request to '{}', success={}", requestBinding, sentReq);

        boolean sentResp = streamBridge.send(responseBinding, message);
        log.info("Sent response to '{}', success={}", responseBinding, sentResp);

        return ResponseEntity.ok(payload);
    }
}



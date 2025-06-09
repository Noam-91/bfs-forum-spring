package com.bfsforum.historyservice.controller;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import com.bfsforum.historyservice.domain.Post;
import org.slf4j.Logger;
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

import com.bfsforum.historyservice.service.RequestReplyManager;

/**
 * Controller to simulate a request-response loop purely with string payloads.
 */
@RestController
@RequestMapping("/mock/kafka")
public class MockKafkaLoopController {

    private static final Logger log = LoggerFactory.getLogger(MockKafkaLoopController.class);

    private final RequestReplyManager<List<Post>> requestReplyManager;
    private final StreamBridge streamBridge;
    private final String requestBinding;
    private final String responseBinding;

    public MockKafkaLoopController(
            RequestReplyManager<List<Post>> requestReplyManager,
            StreamBridge streamBridge,
            @Value("historyEventSender-out-0") String requestBinding,
            @Value("postEnrichRequestEventConsumer-in-0") String responseBinding
    ) {
        this.requestReplyManager = requestReplyManager;
        this.streamBridge = streamBridge;
        this.requestBinding = requestBinding;
        this.responseBinding = responseBinding;
    }

    /**
     * Sends a list of string IDs as a request, simulates sending the same list back,
     * waits for the future to complete using correlationId, and returns the list.
     */
    @PostMapping("/loop")
    public ResponseEntity<List<Post>> sendLoop(@RequestBody List<Post> posts) {
        // a) create future and register it with correlationId
        String correlationId = UUID.randomUUID().toString();
        CompletableFuture<List<Post>> future =
                requestReplyManager.createAndStoreFuture(correlationId);

        // b) send enrichment request
        Message<List<Post>> requestMsg = MessageBuilder
                .withPayload(posts)
                .setHeader(KafkaHeaders.CORRELATION_ID, correlationId)
                .build();
        boolean reqSent = streamBridge.send(requestBinding, requestMsg);
        log.info("Request sent to '{}': {}", requestBinding, reqSent);

        // c) simulate response by sending back the same list of Posts
        Message<List<Post>> responseMsg = MessageBuilder
                .withPayload(posts)
                .setHeader(KafkaHeaders.CORRELATION_ID, correlationId)
                .build();
        boolean respSent = streamBridge.send(responseBinding, responseMsg);
        log.info("Response sent to '{}': {}", responseBinding, respSent);

        // d) wait for the future to complete and return the Posts
        List<Post> result = requestReplyManager.awaitFuture(correlationId, future);
        return ResponseEntity.ok(result);
    }
}

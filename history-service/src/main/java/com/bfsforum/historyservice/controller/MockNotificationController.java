package com.bfsforum.historyservice.controller;

import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.messaging.Message;
import org.springframework.web.bind.annotation.*;

import com.bfsforum.historyservice.domain.Post;

@RestController
@RequestMapping("/mock")
public class MockNotificationController {

    private final Consumer<Message<Post>> postNotificationEventConsumer;

    public MockNotificationController(
            @Qualifier("postNotificationEventConsumer") Consumer<Message<Post>> postNotificationEventConsumer
    ) {
        this.postNotificationEventConsumer = postNotificationEventConsumer;
    }

    @PostMapping("/post-view")
    public Map<String,String> mockPostView(@RequestBody Post post) {
        // generate a fake correlationId
        String correlationId = UUID.randomUUID().toString();

        // build a Spring Message with the header
        Message<Post> msg = MessageBuilder
                .withPayload(post)
                .setHeader(KafkaHeaders.CORRELATION_ID, correlationId)
                .build();

        // invoke your consumer directly
        postNotificationEventConsumer.accept(msg);

        return Map.of(
                "message", "Mocked post-view notification",
                "correlationId", correlationId
        );
    }
}

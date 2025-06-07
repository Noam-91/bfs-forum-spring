package com.bfsforum.historyserivce.kafka.consumer;

import com.bfsforum.historyserivce.kafka.event.HistoryPostViewedEvent;
import com.bfsforum.historyserivce.service.HistoryService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.time.ZoneId;

@Component
public class HistoryEventConsumer {
    private final HistoryService historyService;

    public HistoryEventConsumer(HistoryService historyService) {
        this.historyService = historyService;
    }

    /**
     * Listens for PostViewedEvent messages and upserts the history record.
     * whenever post-service make a http request to create or get a post.
     */
    @KafkaListener(
            topics = "post-viewed-topic",
            containerFactory = "postViewedKafkaListenerContainerFactory"
    )
    public void onPostViewed(HistoryPostViewedEvent event) {
        historyService.recordView(
                event.getUserId(),
                event.getPostId(),
                event.getViewedAt()
        );
    }
}

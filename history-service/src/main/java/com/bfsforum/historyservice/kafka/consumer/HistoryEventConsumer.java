//package com.bfsforum.historyservice.kafka.consumer;
//
//import com.bfsforum.historyservice.kafka.event.HistoryPostViewedEvent;
//import com.bfsforum.historyservice.service.HistoryService;
//import org.springframework.kafka.annotation.KafkaListener;
//import org.springframework.stereotype.Component;
//
//@Component
//public class HistoryEventConsumer {
//    private final HistoryService historyService;
//
//    public HistoryEventConsumer(HistoryService historyService) {
//        this.historyService = historyService;
//    }
//
//    /**
//     * Listens for PostViewedEvent messages and upserts the history record.
//     * whenever post-service make a http request to create or get a post.
//     */
//    @KafkaListener(topics = "post-viewed-topic")
//    public void onPostViewed(HistoryPostViewedEvent event) {
//        System.out.println("Consumed PostViewedEvent: "+ event);
//        historyService.recordView(
//                event.getUserId(),
//                event.getPostId(),
//                event.getViewedAt()
//        );
//    }
//}

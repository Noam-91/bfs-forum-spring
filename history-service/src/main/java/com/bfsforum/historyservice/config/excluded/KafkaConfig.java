//package com.bfsforum.historyservice.config.excluded;
//
//import com.bfsforum.historyservice.kafka.event.PostsEnrichmentRequest;
//import com.bfsforum.historyservice.kafka.event.PostsEnrichmentResponse;
//import lombok.extern.slf4j.Slf4j;
//import org.apache.kafka.clients.admin.NewTopic;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
//import org.springframework.kafka.config.TopicBuilder;
//import org.springframework.kafka.core.ConsumerFactory;
//import org.springframework.kafka.core.ProducerFactory;
//import org.springframework.kafka.listener.ConsumerRecordRecoverer;
//import org.springframework.kafka.listener.ContainerProperties;
//import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;
//import org.springframework.kafka.listener.DefaultErrorHandler;
//import org.springframework.kafka.requestreply.ReplyingKafkaTemplate;
//import org.springframework.util.backoff.FixedBackOff;

//@Slf4j
//@Configuration
//public class KafkaConfig {
//
//
//    @Bean
//    public NewTopic enrichmentRequestTopic() {
//        return TopicBuilder.name("posts-enrichment-request")
//                .partitions(1)
//                .replicas(1)
//                .build();
//    }
//
//    @Bean
//    public NewTopic enrichmentReplyTopic() {
//        return TopicBuilder.name("posts-enrichment-response")
//                .partitions(1)
//                .replicas(1)
//                .build();
//    }
//
//    @Bean
//    public ReplyingKafkaTemplate<String,PostsEnrichmentRequest,PostsEnrichmentResponse>
//    replyingKafkaTemplate(ProducerFactory<String,PostsEnrichmentRequest> pf,
//                          ConcurrentMessageListenerContainer<String,PostsEnrichmentResponse> replies) {
//        return new ReplyingKafkaTemplate<>(pf, replies);
//    }
//
//    @Bean
//    public ConcurrentMessageListenerContainer<String,PostsEnrichmentResponse> repliesContainer(
//            ConsumerFactory<String,PostsEnrichmentResponse> cf) {
//        ContainerProperties props = new ContainerProperties("posts-enrichment-response");
//        return new ConcurrentMessageListenerContainer<>(cf, props);
//    }
//
//    //
//    @Bean
//    public DefaultErrorHandler errorHandler() {
//        // no retries, but skip via the recoverer
//        FixedBackOff backOff = new FixedBackOff(0L, 0L);
//
//        // ConsumerRecordRecoverer is a functional interface matching (rec, ex) -> void
//        ConsumerRecordRecoverer recoverer = (record, exception) -> {
//            log.warn("Skipping bad record {} with exception:", record, exception);
//        };
//
//        return new DefaultErrorHandler(recoverer, backOff);
//    }
//
//    @Bean
//    public ConcurrentKafkaListenerContainerFactory<?,?> kafkaListenerContainerFactory(
//            ConsumerFactory<Object,Object> cf,
//            DefaultErrorHandler errorHandler
//    ) {
//        var factory = new ConcurrentKafkaListenerContainerFactory<>();
//        factory.setConsumerFactory(cf);
//        factory.setCommonErrorHandler(errorHandler);
//        return factory;
//    }
//}

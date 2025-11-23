package com.early_express.order_service.domain.order.infrastructure.messaging.tracking.producer;

import com.early_express.order_service.domain.order.domain.messaging.tracking.TrackingEventPublisher;
import com.early_express.order_service.domain.order.domain.messaging.tracking.TrackingStartRequestedEventData;
import com.early_express.order_service.domain.order.domain.messaging.tracking.event.TrackingStartRequestedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

/**
 * Tracking Event Kafka Publisher
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaTrackingEventPublisher implements TrackingEventPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${spring.kafka.topic.tracking-start-requested}")
    private String trackingStartRequestedTopic;

    @Override
    public void publishTrackingStartRequested(TrackingStartRequestedEventData eventData) {
        log.info("TrackingStartRequested 이벤트 발행 - orderId: {}", eventData.getOrderId());

        // EventData → Event 변환
        TrackingStartRequestedEvent event = TrackingStartRequestedEvent.from(eventData);

        // Kafka 발행
        kafkaTemplate.send(trackingStartRequestedTopic, eventData.getOrderId(), event)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("TrackingStartRequested 이벤트 발행 실패 - orderId: {}, error: {}",
                                eventData.getOrderId(), ex.getMessage(), ex);
                    } else {
                        log.info("TrackingStartRequested 이벤트 발행 성공 - orderId: {}, eventId: {}",
                                eventData.getOrderId(), event.getEventId());
                    }
                });
    }
}
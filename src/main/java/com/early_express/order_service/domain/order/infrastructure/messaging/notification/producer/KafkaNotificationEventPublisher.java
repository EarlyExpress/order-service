package com.early_express.order_service.domain.order.infrastructure.messaging.notification.producer;

import com.early_express.order_service.domain.order.domain.messaging.notification.NotificationEventPublisher;
import com.early_express.order_service.domain.order.domain.messaging.notification.NotificationRequestedEventData;
import com.early_express.order_service.domain.order.domain.messaging.notification.event.NotificationRequestedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

/**
 * Notification Event Kafka Publisher
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaNotificationEventPublisher implements NotificationEventPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${spring.kafka.topic.notification-requested}")
    private String notificationRequestedTopic;

    @Override
    public void publishNotificationRequested(NotificationRequestedEventData eventData) {
        log.info("NotificationRequested 이벤트 발행 - orderId: {}", eventData.getOrderId());

        // EventData → Event 변환
        NotificationRequestedEvent event = NotificationRequestedEvent.from(eventData);

        // Kafka 발행
        kafkaTemplate.send(notificationRequestedTopic, eventData.getOrderId(), event)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("NotificationRequested 이벤트 발행 실패 - orderId: {}, error: {}",
                                eventData.getOrderId(), ex.getMessage(), ex);
                    } else {
                        log.info("NotificationRequested 이벤트 발행 성공 - orderId: {}, eventId: {}",
                                eventData.getOrderId(), event.getEventId());
                    }
                });
    }
}
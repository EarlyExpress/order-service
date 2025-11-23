package com.early_express.order_service.domain.order.infrastructure.messaging.order.producer;

import com.early_express.order_service.domain.order.domain.messaging.order.OrderEventPublisher;
import com.early_express.order_service.domain.order.domain.messaging.order.OrderPaymentVerifiedEventData;
import com.early_express.order_service.domain.order.domain.messaging.order.event.OrderPaymentVerifiedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

/**
 * Order Event Kafka Publisher
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaOrderEventPublisher implements OrderEventPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${spring.kafka.topic.order-payment-verified}")
    private String orderPaymentVerifiedTopic;

    @Override
    public void publishOrderPaymentVerified(OrderPaymentVerifiedEventData eventData) {
        log.info("OrderPaymentVerified 이벤트 발행 - orderId: {}", eventData.getOrderId());

        // EventData → Event 변환
        OrderPaymentVerifiedEvent event = OrderPaymentVerifiedEvent.from(eventData);

        // Kafka 발행
        kafkaTemplate.send(orderPaymentVerifiedTopic, eventData.getOrderId(), event)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("OrderPaymentVerified 이벤트 발행 실패 - orderId: {}, error: {}",
                                eventData.getOrderId(), ex.getMessage(), ex);
                    } else {
                        log.info("OrderPaymentVerified 이벤트 발행 성공 - orderId: {}, eventId: {}",
                                eventData.getOrderId(), event.getEventId());
                    }
                });
    }
}
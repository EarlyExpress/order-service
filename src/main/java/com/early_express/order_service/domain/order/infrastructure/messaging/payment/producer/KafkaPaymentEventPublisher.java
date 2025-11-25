package com.early_express.order_service.domain.order.infrastructure.messaging.payment.producer;

import com.early_express.order_service.domain.order.domain.messaging.payment.PaymentEventPublisher;
import com.early_express.order_service.domain.order.domain.messaging.payment.RefundRequestedEventData;
import com.early_express.order_service.domain.order.infrastructure.messaging.payment.event.RefundRequestedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

/**
 * Payment Event Publisher 구현체 (Kafka)
 * Order Service → Payment Service 이벤트 발행
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaPaymentEventPublisher implements PaymentEventPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${spring.kafka.topic.refund-requested:refund-requested}")
    private String refundRequestedTopic;

    /**
     * 환불 요청 이벤트 발행
     * Topic: refund-requested
     */
    @Override
    public void publishRefundRequested(RefundRequestedEventData eventData) {
        log.info("환불 요청 이벤트 발행 준비 - orderId: {}, paymentId: {}",
                eventData.getOrderId(), eventData.getPaymentId());

        // 도메인 이벤트 → Kafka 이벤트 변환
        RefundRequestedEvent event = RefundRequestedEvent.from(eventData);

        // Kafka 발행
        sendEvent(refundRequestedTopic, eventData.getOrderId(), event);

        log.info("환불 요청 이벤트 발행 완료 - topic: {}, orderId: {}, paymentId: {}",
                refundRequestedTopic, eventData.getOrderId(), eventData.getPaymentId());
    }

    /**
     * Kafka로 이벤트 발행 (공통 헬퍼 메서드)
     */
    private void sendEvent(String topic, String key, Object event) {
        CompletableFuture<SendResult<String, Object>> future =
                kafkaTemplate.send(topic, key, event);

        future.whenComplete((result, ex) -> {
            if (ex != null) {
                log.error("이벤트 발행 실패 - topic: {}, key: {}, eventType: {}, error: {}",
                        topic, key, event.getClass().getSimpleName(), ex.getMessage());
            } else {
                log.debug("이벤트 발행 성공 - topic: {}, partition: {}, offset: {}",
                        topic,
                        result.getRecordMetadata().partition(),
                        result.getRecordMetadata().offset());
            }
        });
    }
}
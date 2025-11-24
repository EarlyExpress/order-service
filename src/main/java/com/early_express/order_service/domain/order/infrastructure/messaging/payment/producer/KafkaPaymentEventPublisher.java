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

    @Value("${spring.kafka.topic.order-events:order-service-events}")
    private String orderEventsTopic;

    /**
     * 환불 요청 이벤트 발행
     * Topic: order-service-events
     *
     * @param eventData 환불 요청 이벤트 데이터
     */
    @Override
    public void publishRefundRequested(RefundRequestedEventData eventData) {
        log.info("환불 요청 이벤트 발행 준비 - orderId: {}, paymentId: {}",
                eventData.getOrderId(), eventData.getPaymentId());

        try {
            // 도메인 이벤트 → Kafka 이벤트 변환
            RefundRequestedEvent kafkaEvent = RefundRequestedEvent.from(eventData);

            // Kafka 발행 (비동기)
            CompletableFuture<SendResult<String, Object>> future = kafkaTemplate.send(
                    orderEventsTopic,
                    eventData.getOrderId(), // Key: orderId (파티셔닝)
                    kafkaEvent
            );

            // 발행 결과 처리
            future.whenComplete((result, ex) -> {
                if (ex != null) {
                    log.error("환불 요청 이벤트 발행 실패 - orderId: {}, paymentId: {}, error: {}",
                            eventData.getOrderId(),
                            eventData.getPaymentId(),
                            ex.getMessage(),
                            ex);
                } else {
                    log.info("환불 요청 이벤트 발행 완료 - orderId: {}, paymentId: {}, partition: {}, offset: {}",
                            eventData.getOrderId(),
                            eventData.getPaymentId(),
                            result.getRecordMetadata().partition(),
                            result.getRecordMetadata().offset());
                }
            });

        } catch (Exception e) {
            log.error("환불 요청 이벤트 발행 중 예외 발생 - orderId: {}, paymentId: {}, error: {}",
                    eventData.getOrderId(),
                    eventData.getPaymentId(),
                    e.getMessage(),
                    e);
            throw new RuntimeException("환불 요청 이벤트 발행 실패", e);
        }
    }
}
package com.early_express.order_service.domain.order.infrastructure.messaging.payment.consumer;

import com.early_express.order_service.domain.order.application.service.OrderCompensationService;
import com.early_express.order_service.domain.order.infrastructure.messaging.payment.event.PaymentRefundedEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 환불 완료 이벤트 Consumer
 * Payment Service에서 발행한 환불 완료 이벤트를 수신하여 재고 복원
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentRefundedEventConsumer {

    private final OrderCompensationService compensationService;
    private final ObjectMapper objectMapper;

    /**
     * 환불 완료 이벤트 수신
     * Topic: payment-events
     * Group: order-service-payment-group
     */
    @KafkaListener(
            topics = "${spring.kafka.topic.payment-events:payment-events}",
            groupId = "${spring.kafka.consumer.group-id:order-service-payment-group}",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void handlePaymentRefunded(
            @Payload Map<String, Object> eventMap,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset,
            Acknowledgment acknowledgment) {

        try {
            // Map에서 필요한 정보 추출
            String eventType = (String) eventMap.get("eventType");

            // PaymentRefundedEvent가 아니면 무시
            if (!"PaymentRefundedEvent".equals(eventType)) {
                log.debug("다른 이벤트 타입 무시 - eventType: {}", eventType);
                acknowledgment.acknowledge();
                return;
            }

            // 이벤트 파싱
            PaymentRefundedEvent event = objectMapper.convertValue(eventMap, PaymentRefundedEvent.class);

            log.info("환불 완료 이벤트 수신 - orderId: {}, paymentId: {}, fullRefund: {}, partition: {}, offset: {}",
                    event.getOrderId(),
                    event.getPaymentId(),
                    event.isFullRefund(),
                    partition,
                    offset);

            // 재고 복원 처리
            compensationService.handlePaymentRefunded(event);

            // 수동 커밋
            acknowledgment.acknowledge();

            log.info("환불 완료 처리 완료 - orderId: {}, paymentId: {}",
                    event.getOrderId(), event.getPaymentId());

        } catch (Exception e) {
            log.error("환불 완료 처리 실패 - eventMap: {}, error: {}",
                    eventMap, e.getMessage(), e);

            // 재시도를 위해 ACK 하지 않음
            throw e;
        }
    }
}

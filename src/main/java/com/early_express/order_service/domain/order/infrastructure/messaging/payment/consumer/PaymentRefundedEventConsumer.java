package com.early_express.order_service.domain.order.infrastructure.messaging.payment.consumer;

import com.early_express.order_service.domain.order.application.service.OrderCompensationService;
import com.early_express.order_service.domain.order.infrastructure.messaging.payment.event.PaymentRefundedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

/**
 * 환불 완료 이벤트 Consumer
 * Payment Service에서 발행한 환불 완료 이벤트를 수신하여 재고 복원
 * Topic: payment-refunded (토픽 분리 패턴)
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentRefundedEventConsumer {

    private final OrderCompensationService compensationService;

    /**
     * 환불 완료 이벤트 수신
     * Topic: payment-refunded
     */
    @KafkaListener(
            topics = "${spring.kafka.topic.payment-refunded:payment-refunded}",
            groupId = "${spring.kafka.consumer.group-id:order-service-group}",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void handlePaymentRefunded(
            @Payload PaymentRefundedEvent event,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset,
            Acknowledgment acknowledgment) {

        log.info("환불 완료 이벤트 수신 - orderId: {}, paymentId: {}, fullRefund: {}, partition: {}, offset: {}",
                event.getOrderId(),
                event.getPaymentId(),
                event.isFullRefund(),
                partition,
                offset);

        try {
            // 재고 복원 처리
            compensationService.handlePaymentRefunded(event);

            // 수동 커밋
            acknowledgment.acknowledge();

            log.info("환불 완료 처리 완료 - orderId: {}, paymentId: {}",
                    event.getOrderId(), event.getPaymentId());

        } catch (Exception e) {
            log.error("환불 완료 처리 실패 - orderId: {}, paymentId: {}, error: {}",
                    event.getOrderId(), event.getPaymentId(), e.getMessage(), e);

            // 재시도를 위해 ACK 하지 않음
            throw e;
        }
    }
}
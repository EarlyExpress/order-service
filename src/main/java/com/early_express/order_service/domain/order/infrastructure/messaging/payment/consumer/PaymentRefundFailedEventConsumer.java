package com.early_express.order_service.domain.order.infrastructure.messaging.payment.consumer;

import com.early_express.order_service.domain.order.application.service.OrderCompensationService;
import com.early_express.order_service.domain.order.infrastructure.messaging.payment.event.PaymentRefundFailedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

/**
 * 환불 실패 이벤트 Consumer
 * Payment Service에서 발행한 환불 실패 이벤트를 수신하여 처리
 * Topic: payment-refund-failed (토픽 분리 패턴)
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentRefundFailedEventConsumer {

    private final OrderCompensationService compensationService;

    /**
     * 환불 실패 이벤트 수신
     * Topic: payment-refund-failed
     */
    @KafkaListener(
            topics = "${spring.kafka.topic.payment-refund-failed:payment-refund-failed}",
            groupId = "${spring.kafka.consumer.group-id:order-service-group}",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void handlePaymentRefundFailed(
            @Payload PaymentRefundFailedEvent event,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset,
            Acknowledgment acknowledgment) {

        log.error("환불 실패 이벤트 수신 - orderId: {}, paymentId: {}, error: {}, partition: {}, offset: {}",
                event.getOrderId(),
                event.getPaymentId(),
                event.getErrorMessage(),
                partition,
                offset);

        try {
            // 환불 실패 처리
            compensationService.handlePaymentRefundFailed(event);

            // 수동 커밋
            acknowledgment.acknowledge();

            log.warn("환불 실패 처리 완료 - orderId: {}, paymentId: {}",
                    event.getOrderId(), event.getPaymentId());

        } catch (Exception e) {
            log.error("환불 실패 처리 중 오류 - orderId: {}, paymentId: {}, error: {}",
                    event.getOrderId(), event.getPaymentId(), e.getMessage(), e);

            // 재시도를 위해 ACK 하지 않음
            throw e;
        }
    }
}
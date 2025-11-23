package com.early_express.order_service.domain.order.infrastructure.messaging.order.consumer;

import com.early_express.order_service.domain.order.application.service.OrderSagaOrchestratorService;
import com.early_express.order_service.domain.order.domain.messaging.order.event.OrderPaymentVerifiedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

/**
 * Order Event Consumer
 * Order 관련 이벤트 수신 및 처리
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OrderEventConsumer {

    private final OrderSagaOrchestratorService sagaOrchestratorService;

    /**
     * 결제 검증 완료 이벤트 수신
     * Step 3~7 비동기 처리 시작
     */
    @KafkaListener(
            topics = "${spring.kafka.topic.order-payment-verified}",
            groupId = "${spring.kafka.consumer.group-id}",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void handleOrderPaymentVerified(
            OrderPaymentVerifiedEvent event,
            Acknowledgment ack) {

        log.info(">>> OrderPaymentVerified 이벤트 수신 - eventId: {}, orderId: {}, sagaId: {}",
                event.getEventId(),
                event.getOrderId(),
                event.getSagaId());

        try {
            // Step 3: 경로 계산 실행
            sagaOrchestratorService.executeRouteCalculation(event);

            // 수동 커밋
            ack.acknowledge();

            log.info(">>> OrderPaymentVerified 이벤트 처리 완료 - orderId: {}",
                    event.getOrderId());

        } catch (Exception e) {
            log.error("OrderPaymentVerified 이벤트 처리 실패 - orderId: {}, error: {}",
                    event.getOrderId(), e.getMessage(), e);

            // 재시도를 위해 ack하지 않음 (Kafka가 자동 재시도)
            throw e;
        }
    }
}
package com.early_express.order_service.domain.order.infrastructure.messaging.payment.event;

import com.early_express.order_service.domain.order.domain.messaging.payment.RefundRequestedEventData;
import com.early_express.order_service.global.infrastructure.event.base.BaseEvent;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

/**
 * 환불 요청 이벤트 (Kafka 전송용)
 * Order Service → Payment Service
 * Topic: refund-requested
 */
@Getter
@SuperBuilder
@NoArgsConstructor
public class RefundRequestedEvent extends BaseEvent {

    private String paymentId;
    private String orderId;
    private String refundReason;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime requestedAt;

    /**
     * 도메인 이벤트 데이터로부터 Kafka 이벤트 생성
     */
    public static RefundRequestedEvent from(RefundRequestedEventData data) {
        RefundRequestedEvent event = RefundRequestedEvent.builder()
                .paymentId(data.getPaymentId())
                .orderId(data.getOrderId())
                .refundReason(data.getRefundReason())
                .requestedAt(data.getRequestedAt())
                .build();

        event.initBaseEvent("REFUND_REQUESTED", "order-service");

        return event;
    }
}
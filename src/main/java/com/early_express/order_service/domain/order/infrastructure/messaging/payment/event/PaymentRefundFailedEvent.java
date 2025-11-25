package com.early_express.order_service.domain.order.infrastructure.messaging.payment.event;

import com.early_express.order_service.global.infrastructure.event.base.BaseEvent;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 환불 실패 이벤트 (Kafka 수신용)
 * Payment Service → Order Service
 * Topic: payment-refund-failed
 */
@Getter
@SuperBuilder
@NoArgsConstructor
public class PaymentRefundFailedEvent extends BaseEvent {

    private String paymentId;
    private String orderId;
    private BigDecimal requestedRefundAmount;
    private String errorMessage;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime failedAt;
}
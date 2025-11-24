package com.early_express.order_service.domain.order.infrastructure.messaging.payment.event;

import com.early_express.order_service.global.infrastructure.event.base.BaseEvent;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 환불 완료 이벤트 (Kafka 수신용)
 * Payment Service → Order Service
 * Topic: payment-events
 */
@Getter
@SuperBuilder
@NoArgsConstructor
public class PaymentRefundedEvent extends BaseEvent {

    private String paymentId;
    private String orderId;
    private BigDecimal refundAmount;
    private BigDecimal totalRefundedAmount;
    private String refundReason;
    private String pgRefundId;
    private boolean fullRefund;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime refundedAt;
}

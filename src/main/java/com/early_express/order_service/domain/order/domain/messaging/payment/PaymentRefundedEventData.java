package com.early_express.order_service.domain.order.domain.messaging.payment;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 환불 완료 이벤트 데이터 (도메인)
 * Payment Service → Order Service
 */
@Getter
@Builder
public class PaymentRefundedEventData {

    /**
     * Payment ID
     */
    private final String paymentId;

    /**
     * Order ID
     */
    private final String orderId;

    /**
     * 환불 금액
     */
    private final BigDecimal refundAmount;

    /**
     * 총 환불 누적 금액
     */
    private final BigDecimal totalRefundedAmount;

    /**
     * 환불 사유
     */
    private final String refundReason;

    /**
     * PG 환불 ID
     */
    private final String pgRefundId;

    /**
     * 전액 환불 여부
     */
    private final boolean fullRefund;

    /**
     * 환불 완료 시간
     */
    private final LocalDateTime refundedAt;

    /**
     * 환불 완료 이벤트 데이터 생성
     */
    public static PaymentRefundedEventData of(
            String paymentId,
            String orderId,
            BigDecimal refundAmount,
            BigDecimal totalRefundedAmount,
            String refundReason,
            String pgRefundId,
            boolean fullRefund,
            LocalDateTime refundedAt) {

        return PaymentRefundedEventData.builder()
                .paymentId(paymentId)
                .orderId(orderId)
                .refundAmount(refundAmount)
                .totalRefundedAmount(totalRefundedAmount)
                .refundReason(refundReason)
                .pgRefundId(pgRefundId)
                .fullRefund(fullRefund)
                .refundedAt(refundedAt)
                .build();
    }
}
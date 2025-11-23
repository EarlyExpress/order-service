package com.early_express.order_service.domain.order.domain.messaging.payment;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 환불 실패 이벤트 데이터 (도메인)
 * Payment Service → Order Service
 */
@Getter
@Builder
public class PaymentRefundFailedEventData {

    /**
     * Payment ID
     */
    private final String paymentId;

    /**
     * Order ID
     */
    private final String orderId;

    /**
     * 요청한 환불 금액
     */
    private final BigDecimal requestedRefundAmount;

    /**
     * 에러 메시지
     */
    private final String errorMessage;

    /**
     * 실패 시간
     */
    private final LocalDateTime failedAt;

    /**
     * 환불 실패 이벤트 데이터 생성
     */
    public static PaymentRefundFailedEventData of(
            String paymentId,
            String orderId,
            BigDecimal requestedRefundAmount,
            String errorMessage,
            LocalDateTime failedAt) {

        return PaymentRefundFailedEventData.builder()
                .paymentId(paymentId)
                .orderId(orderId)
                .requestedRefundAmount(requestedRefundAmount)
                .errorMessage(errorMessage)
                .failedAt(failedAt)
                .build();
    }
}
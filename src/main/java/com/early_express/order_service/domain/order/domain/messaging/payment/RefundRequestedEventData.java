package com.early_express.order_service.domain.order.domain.messaging.payment;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 환불 요청 이벤트 데이터 (도메인)
 * Order Service → Payment Service
 */
@Getter
@Builder
public class RefundRequestedEventData {

    /**
     * Payment ID
     */
    private final String paymentId;

    /**
     * Order ID
     */
    private final String orderId;

    /**
     * 환불 사유
     */
    private final String refundReason;

    /**
     * 요청 시간
     */
    private final LocalDateTime requestedAt;

    /**
     * 환불 요청 이벤트 데이터 생성
     */
    public static RefundRequestedEventData of(
            String paymentId,
            String orderId,
            String refundReason) {

        return RefundRequestedEventData.builder()
                .paymentId(paymentId)
                .orderId(orderId)
                .refundReason(refundReason)
                .requestedAt(LocalDateTime.now())
                .build();
    }
}
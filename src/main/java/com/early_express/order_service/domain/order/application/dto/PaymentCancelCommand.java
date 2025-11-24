package com.early_express.order_service.domain.order.application.dto;

import lombok.Builder;
import lombok.Getter;

/**
 * 결제 취소 Command DTO
 */
@Getter
@Builder
public class PaymentCancelCommand {

    /**
     * Payment ID
     */
    private String paymentId;

    /**
     * Order ID
     */
    private String orderId;

    /**
     * 취소 사유
     */
    private String cancelReason;

    /**
     * 결제 취소 Command 생성
     */
    public static PaymentCancelCommand of(
            String paymentId,
            String orderId,
            String cancelReason) {

        return PaymentCancelCommand.builder()
                .paymentId(paymentId)
                .orderId(orderId)
                .cancelReason(cancelReason)
                .build();
    }
}
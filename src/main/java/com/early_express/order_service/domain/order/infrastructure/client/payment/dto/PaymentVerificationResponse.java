package com.early_express.order_service.domain.order.infrastructure.client.payment.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 결제 검증 및 등록 응답 DTO
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentVerificationResponse {

    /**
     * Payment ID (생성된 결제 엔티티 ID)
     */
    private String paymentId;

    /**
     * 검증 상태 (VERIFIED, FAILED)
     */
    private String status;

    /**
     * PG 거래 ID
     */
    private String pgTransactionId;

    /**
     * 검증된 금액
     */
    private BigDecimal verifiedAmount;

    /**
     * PG 승인 시간
     */
    private LocalDateTime pgApprovedAt;

    /**
     * 메시지
     */
    private String message;
}

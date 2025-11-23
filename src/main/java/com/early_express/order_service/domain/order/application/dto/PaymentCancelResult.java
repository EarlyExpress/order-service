package com.early_express.order_service.domain.order.application.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 결제 취소 결과 DTO (Application Layer)
 *
 * Payment Service 응답 → Application DTO 변환
 */
@Getter
@Builder
public class PaymentCancelResult {

    /**
     * 취소 성공 여부
     */
    private Boolean success;

    /**
     * Payment ID
     */
    private String paymentId;

    /**
     * 취소 금액
     */
    private BigDecimal canceledAmount;

    /**
     * PG 환불 ID
     */
    private String pgRefundId;

    /**
     * 취소 완료 시간
     */
    private LocalDateTime canceledAt;

    /**
     * 메시지
     */
    private String message;

    /**
     * 성공 여부 확인
     */
    public boolean isSuccess() {
        return Boolean.TRUE.equals(success);
    }
}
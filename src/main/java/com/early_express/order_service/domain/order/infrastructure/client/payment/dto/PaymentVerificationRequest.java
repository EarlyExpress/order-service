package com.early_express.order_service.domain.order.infrastructure.client.payment.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

/**
 * 결제 검증 및 등록 요청 DTO
 */
@Getter
@Builder
public class PaymentVerificationRequest {

    /**
     * 주문 ID
     */
    private String orderId;

    /**
     * PG 제공자 (TOSS)
     */
    private String pgProvider;

    /**
     * PG 결제 ID
     */
    private String pgPaymentId;

    /**
     * PG 결제 키
     */
    private String pgPaymentKey;

    /**
     * 예상 결제 금액 (검증용)
     */
    private BigDecimal expectedAmount;

    /**
     * 지불자 업체 ID
     */
    private String payerCompanyId;

    /**
     * 지불자 이름
     */
    private String payerName;

    /**
     * 지불자 이메일
     */
    private String payerEmail;

    /**
     * 지불자 연락처
     */
    private String payerPhone;

    /**
     * 수취인 업체 ID
     */
    private String payeeCompanyId;

    /**
     * 수취인 이름
     */
    private String payeeName;

    public static PaymentVerificationRequest of(
            String orderId,
            String pgProvider,
            String pgPaymentId,
            String pgPaymentKey,
            BigDecimal expectedAmount,
            String payerCompanyId,
            String payerName,
            String payerEmail,
            String payerPhone,
            String payeeCompanyId,
            String payeeName) {

        return PaymentVerificationRequest.builder()
                .orderId(orderId)
                .pgProvider(pgProvider)
                .pgPaymentId(pgPaymentId)
                .pgPaymentKey(pgPaymentKey)
                .expectedAmount(expectedAmount)
                .payerCompanyId(payerCompanyId)
                .payerName(payerName)
                .payerEmail(payerEmail)
                .payerPhone(payerPhone)
                .payeeCompanyId(payeeCompanyId)
                .payeeName(payeeName)
                .build();
    }
}

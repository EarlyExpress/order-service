package com.early_express.order_service.domain.order.domain.model.vo;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.math.BigDecimal;

/**
 * 금액 정보 Value Object
 * 단가, 총액, 결제 ID 정보를 담음
 */
@Getter
@EqualsAndHashCode
public class AmountInfo {

    private final BigDecimal unitPrice; // 단가
    private final BigDecimal totalAmount; // 총액
    private final String paymentId; // Payment ID (결제 생성 후 저장, nullable)

    @Builder
    private AmountInfo(
            BigDecimal unitPrice,
            BigDecimal totalAmount,
            String paymentId) {

        validateAmount(unitPrice, "단가");
        validateAmount(totalAmount, "총액");
        validateTotalAmount(unitPrice, totalAmount);

        this.unitPrice = unitPrice;
        this.totalAmount = totalAmount;
        this.paymentId = paymentId;
    }

    /**
     * 단가와 수량으로 금액 정보 생성
     */
    public static AmountInfo of(BigDecimal unitPrice, int quantity) {
        BigDecimal totalAmount = unitPrice.multiply(BigDecimal.valueOf(quantity));

        return AmountInfo.builder()
                .unitPrice(unitPrice)
                .totalAmount(totalAmount)
                .build();
    }

    /**
     * Payment ID 설정
     */
    public AmountInfo withPaymentId(String paymentId) {
        return AmountInfo.builder()
                .unitPrice(this.unitPrice)
                .totalAmount(this.totalAmount)
                .paymentId(paymentId)
                .build();
    }

    /**
     * Payment ID가 설정되었는지 확인
     */
    public boolean hasPaymentId() {
        return paymentId != null && !paymentId.trim().isEmpty();
    }

    /**
     * 금액이 일치하는지 검증
     */
    public boolean matchesAmount(BigDecimal amount) {
        return totalAmount.compareTo(amount) == 0;
    }

    private void validateAmount(BigDecimal amount, String fieldName) {
        if (amount == null) {
            throw new IllegalArgumentException(fieldName + "는 null일 수 없습니다.");
        }

        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(fieldName + "는 0보다 커야 합니다.");
        }
    }

    private void validateTotalAmount(BigDecimal unitPrice, BigDecimal totalAmount) {
        if (totalAmount.compareTo(unitPrice) < 0) {
            throw new IllegalArgumentException("총액은 단가보다 작을 수 없습니다.");
        }
    }
}

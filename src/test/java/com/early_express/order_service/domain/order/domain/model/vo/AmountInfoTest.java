package com.early_express.order_service.domain.order.domain.model.vo;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.*;

@DisplayName("AmountInfo vo 테스트")
class AmountInfoTest {

    @Test
    @DisplayName("단가와 수량으로 AmountInfo를 생성할 수 있다")
    void createAmountInfo() {
        // given
        BigDecimal unitPrice = BigDecimal.valueOf(10000);
        int quantity = 3;

        // when
        AmountInfo amountInfo = AmountInfo.of(unitPrice, quantity);

        // then
        assertThat(amountInfo.getUnitPrice()).isEqualByComparingTo(unitPrice);
        assertThat(amountInfo.getTotalAmount()).isEqualByComparingTo(BigDecimal.valueOf(30000));
        assertThat(amountInfo.hasPaymentId()).isFalse();
    }

    @Test
    @DisplayName("Payment ID를 설정할 수 있다")
    void setPaymentId() {
        // given
        AmountInfo amountInfo = AmountInfo.of(BigDecimal.valueOf(10000), 2);
        String paymentId = "payment-123";

        // when
        AmountInfo updated = amountInfo.withPaymentId(paymentId);

        // then
        assertThat(updated.hasPaymentId()).isTrue();
        assertThat(updated.getPaymentId()).isEqualTo(paymentId);
    }

    @Test
    @DisplayName("금액이 일치하는지 확인할 수 있다")
    void matchesAmount() {
        // given
        AmountInfo amountInfo = AmountInfo.of(BigDecimal.valueOf(10000), 2);

        // when & then
        assertThat(amountInfo.matchesAmount(BigDecimal.valueOf(20000))).isTrue();
        assertThat(amountInfo.matchesAmount(BigDecimal.valueOf(30000))).isFalse();
    }

    @Test
    @DisplayName("단가가 null이면 예외가 발생한다")
    void validateUnitPriceNull() {
        // when & then
        assertThatThrownBy(() -> AmountInfo.of(null, 1))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("null");
    }

    @Test
    @DisplayName("단가가 0 이하면 예외가 발생한다")
    void validateUnitPriceZero() {
        // when & then
        assertThatThrownBy(() -> AmountInfo.of(BigDecimal.ZERO, 1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("단가는 0보다 커야 합니다");
    }

    @Test
    @DisplayName("단가가 음수면 예외가 발생한다")
    void validateUnitPriceNegative() {
        // when & then
        assertThatThrownBy(() -> AmountInfo.of(BigDecimal.valueOf(-1000), 1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("단가는 0보다 커야 합니다");
    }

    @Test
    @DisplayName("같은 값을 가진 AmountInfo는 동일하다")
    void equalAmountInfos() {
        // given
        AmountInfo amountInfo1 = AmountInfo.of(BigDecimal.valueOf(10000), 2);
        AmountInfo amountInfo2 = AmountInfo.of(BigDecimal.valueOf(10000), 2);

        // when & then
        assertThat(amountInfo1).isEqualTo(amountInfo2);
        assertThat(amountInfo1.hashCode()).isEqualTo(amountInfo2.hashCode());
    }
}
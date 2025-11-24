package com.early_express.order_service.domain.order.domain.model.vo;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

@DisplayName("OrderNumber vo 테스트")
class OrderNumberTest {

    @Test
    @DisplayName("일련번호로 새로운 OrderNumber를 생성할 수 있다")
    void generateOrderNumber() {
        // given
        int sequenceNumber = 1;

        // when
        OrderNumber orderNumber = OrderNumber.generate(sequenceNumber);

        // then
        assertThat(orderNumber).isNotNull();
        assertThat(orderNumber.getValue()).startsWith("ORD-");
        assertThat(orderNumber.getValue()).endsWith("-001");
        assertThat(orderNumber.getValue()).matches("ORD-\\d{8}-\\d{3}");
    }

    @Test
    @DisplayName("기존 주문번호로부터 OrderNumber를 생성할 수 있다")
    void createOrderNumberFrom() {
        // given
        String number = "ORD-20250115-001";

        // when
        OrderNumber orderNumber = OrderNumber.from(number);

        // then
        assertThat(orderNumber.getValue()).isEqualTo(number);
    }

    @Test
    @DisplayName("null 값으로 OrderNumber를 생성하면 예외가 발생한다")
    void validateOrderNumberNull() {
        // when & then
        assertThatThrownBy(() -> OrderNumber.from(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("주문 번호는 null이거나 빈 값일 수 없습니다");
    }

    @Test
    @DisplayName("잘못된 형식으로 OrderNumber를 생성하면 예외가 발생한다")
    void validateOrderNumberFormat() {
        // when & then
        assertThatThrownBy(() -> OrderNumber.from("INVALID-FORMAT"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("주문 번호는 'ORD-'로 시작해야 합니다");
    }

    @Test
    @DisplayName("ORD- 접두사가 없으면 예외가 발생한다")
    void validateOrderNumberPrefix() {
        // when & then
        assertThatThrownBy(() -> OrderNumber.from("ABC-20250115-001"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("주문 번호는 'ORD-'로 시작해야 합니다");
    }

    @Test
    @DisplayName("같은 값을 가진 OrderNumber는 동일하다")
    void equalOrderNumbers() {
        // given
        String number = "ORD-20250115-001";
        OrderNumber orderNumber1 = OrderNumber.from(number);
        OrderNumber orderNumber2 = OrderNumber.from(number);

        // when & then
        assertThat(orderNumber1).isEqualTo(orderNumber2);
        assertThat(orderNumber1.hashCode()).isEqualTo(orderNumber2.hashCode());
    }
}
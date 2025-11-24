package com.early_express.order_service.domain.order.domain.model.vo;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

@DisplayName("OrderId vo 테스트")
class OrderIdTest {

    @Test
    @DisplayName("새로운 OrderId를 생성할 수 있다")
    void createOrderId() {
        // when
        OrderId orderId = OrderId.create();

        // then
        assertThat(orderId).isNotNull();
        assertThat(orderId.getValue()).isNotEmpty();
    }

    @Test
    @DisplayName("기존 ID로부터 OrderId를 생성할 수 있다")
    void createOrderIdFrom() {
        // given
        String id = "test-order-id";

        // when
        OrderId orderId = OrderId.from(id);

        // then
        assertThat(orderId.getValue()).isEqualTo(id);
    }

    @Test
    @DisplayName("null 값으로 OrderId를 생성하면 예외가 발생한다")
    void validateOrderIdNull() {
        // when & then
        assertThatThrownBy(() -> OrderId.from(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("주문 ID는 null이거나 빈 값일 수 없습니다");
    }

    @Test
    @DisplayName("빈 문자열로 OrderId를 생성하면 예외가 발생한다")
    void validateOrderIdEmpty() {
        // when & then
        assertThatThrownBy(() -> OrderId.from(""))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("주문 ID는 null이거나 빈 값일 수 없습니다");
    }

    @Test
    @DisplayName("같은 값을 가진 OrderId는 동일하다")
    void equalOrderIds() {
        // given
        String id = "test-order-id";
        OrderId orderId1 = OrderId.from(id);
        OrderId orderId2 = OrderId.from(id);

        // when & then
        assertThat(orderId1).isEqualTo(orderId2);
        assertThat(orderId1.hashCode()).isEqualTo(orderId2.hashCode());
    }
}
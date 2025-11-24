package com.early_express.order_service.domain.order.domain.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("배송 예외 테스트")
class DeliveryExceptionTest {

    @Test
    @DisplayName("에러 코드로 DeliveryException을 생성할 수 있다")
    void createWithErrorCode() {
        // given
        OrderErrorCode errorCode = OrderErrorCode.DELIVERY_CREATION_FAILED;

        // when
        DeliveryException exception = new DeliveryException(errorCode);

        // then
        assertThat(exception.getErrorCode()).isEqualTo(errorCode);
        assertThat(exception.getMessage()).isEqualTo(errorCode.getMessage());
    }

    @Test
    @DisplayName("에러 코드와 메시지로 DeliveryException을 생성할 수 있다")
    void createWithErrorCodeAndMessage() {
        // given
        OrderErrorCode errorCode = OrderErrorCode.ROUTE_CALCULATION_FAILED;
        String message = "HUB-001에서 HUB-002로의 경로 계산 실패";

        // when
        DeliveryException exception = new DeliveryException(errorCode, message);

        // then
        assertThat(exception.getErrorCode()).isEqualTo(errorCode);
        assertThat(exception.getMessage()).isEqualTo(message);
    }

    @Test
    @DisplayName("DeliveryException은 OrderException을 상속한다")
    void extendsOrderException() {
        // given
        DeliveryException exception = new DeliveryException(OrderErrorCode.DELIVERY_NOT_FOUND);

        // when & then
        assertThat(exception).isInstanceOf(OrderException.class);
    }
}
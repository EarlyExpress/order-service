package com.early_express.order_service.domain.order.domain.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

@DisplayName("주문 예외 테스트")
class OrderExceptionTest {

    @Test
    @DisplayName("에러 코드로 OrderException을 생성할 수 있다")
    void createWithErrorCode() {
        // given
        OrderErrorCode errorCode = OrderErrorCode.ORDER_NOT_FOUND;

        // when
        OrderException exception = new OrderException(errorCode);

        // then
        assertThat(exception.getErrorCode()).isEqualTo(errorCode);
        assertThat(exception.getMessage()).isEqualTo(errorCode.getMessage());
    }

    @Test
    @DisplayName("에러 코드와 메시지로 OrderException을 생성할 수 있다")
    void createWithErrorCodeAndMessage() {
        // given
        OrderErrorCode errorCode = OrderErrorCode.ORDER_NOT_FOUND;
        String message = "주문 ID 123을 찾을 수 없습니다";

        // when
        OrderException exception = new OrderException(errorCode, message);

        // then
        assertThat(exception.getErrorCode()).isEqualTo(errorCode);
        assertThat(exception.getMessage()).isEqualTo(message);
    }

    @Test
    @DisplayName("에러 코드와 원인으로 OrderException을 생성할 수 있다")
    void createWithErrorCodeAndCause() {
        // given
        OrderErrorCode errorCode = OrderErrorCode.ORDER_CREATION_FAILED;
        Throwable cause = new RuntimeException("DB 연결 실패");

        // when
        OrderException exception = new OrderException(errorCode, cause);

        // then
        assertThat(exception.getErrorCode()).isEqualTo(errorCode);
        assertThat(exception.getCause()).isEqualTo(cause);
    }

    @Test
    @DisplayName("모든 파라미터로 OrderException을 생성할 수 있다")
    void createWithAllParameters() {
        // given
        OrderErrorCode errorCode = OrderErrorCode.ORDER_CREATION_FAILED;
        String message = "주문 생성 실패";
        Throwable cause = new RuntimeException("DB 연결 실패");

        // when
        OrderException exception = new OrderException(errorCode, message, cause);

        // then
        assertThat(exception.getErrorCode()).isEqualTo(errorCode);
        assertThat(exception.getMessage()).isEqualTo(message);
        assertThat(exception.getCause()).isEqualTo(cause);
    }
}



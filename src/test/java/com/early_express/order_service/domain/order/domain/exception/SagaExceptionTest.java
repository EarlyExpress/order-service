package com.early_express.order_service.domain.order.domain.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Saga 예외 테스트")
class SagaExceptionTest {

    @Test
    @DisplayName("에러 코드로 SagaException을 생성할 수 있다")
    void createWithErrorCode() {
        // given
        OrderErrorCode errorCode = OrderErrorCode.SAGA_EXECUTION_FAILED;

        // when
        SagaException exception = new SagaException(errorCode);

        // then
        assertThat(exception.getErrorCode()).isEqualTo(errorCode);
        assertThat(exception.getMessage()).isEqualTo(errorCode.getMessage());
    }

    @Test
    @DisplayName("에러 코드와 메시지로 SagaException을 생성할 수 있다")
    void createWithErrorCodeAndMessage() {
        // given
        OrderErrorCode errorCode = OrderErrorCode.SAGA_STEP_FAILED;
        String message = "재고 예약 Step 실패";

        // when
        SagaException exception = new SagaException(errorCode, message);

        // then
        assertThat(exception.getErrorCode()).isEqualTo(errorCode);
        assertThat(exception.getMessage()).isEqualTo(message);
    }

    @Test
    @DisplayName("SagaException은 OrderException을 상속한다")
    void extendOrderException() {
        // given
        SagaException exception = new SagaException(OrderErrorCode.SAGA_TIMEOUT);

        // when & then
        assertThat(exception).isInstanceOf(OrderException.class);
    }
}


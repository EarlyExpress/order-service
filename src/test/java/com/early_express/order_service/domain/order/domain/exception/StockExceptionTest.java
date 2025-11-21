package com.early_express.order_service.domain.order.domain.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("재고 예외 테스트")
class StockExceptionTest {

    @Test
    @DisplayName("에러 코드로 StockException을 생성할 수 있다")
    void createWithErrorCode() {
        // given
        OrderErrorCode errorCode = OrderErrorCode.INSUFFICIENT_STOCK;

        // when
        StockException exception = new StockException(errorCode);

        // then
        assertThat(exception.getErrorCode()).isEqualTo(errorCode);
        assertThat(exception.getMessage()).isEqualTo(errorCode.getMessage());
    }

    @Test
    @DisplayName("에러 코드와 메시지로 StockException을 생성할 수 있다")
    void createWithErrorCodeAndMessage() {
        // given
        OrderErrorCode errorCode = OrderErrorCode.STOCK_RESERVATION_FAILED;
        String message = "상품 PROD-001의 재고 예약 실패";

        // when
        StockException exception = new StockException(errorCode, message);

        // then
        assertThat(exception.getErrorCode()).isEqualTo(errorCode);
        assertThat(exception.getMessage()).isEqualTo(message);
    }

    @Test
    @DisplayName("StockException은 OrderException을 상속한다")
    void extendsOrderException() {
        // given
        StockException exception = new StockException(OrderErrorCode.INSUFFICIENT_STOCK);

        // when & then
        assertThat(exception).isInstanceOf(OrderException.class);
    }
}

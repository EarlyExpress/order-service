package com.early_express.order_service.domain.order.domain.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("에러 코드 테스트")
class OrderErrorCodeTest {

    @Test
    @DisplayName("주문 관련 에러 코드는 올바른 값을 가진다")
    void orderErrorCodes() {
        // when & then
        assertThat(OrderErrorCode.ORDER_NOT_FOUND.getCode()).isEqualTo("ORDER_001");
        assertThat(OrderErrorCode.ORDER_NOT_FOUND.getStatus()).isEqualTo(404);
        assertThat(OrderErrorCode.ORDER_NOT_FOUND.getMessage()).contains("찾을 수 없습니다");

        assertThat(OrderErrorCode.ORDER_CREATION_FAILED.getCode()).isEqualTo("ORDER_002");
        assertThat(OrderErrorCode.ORDER_CREATION_FAILED.getStatus()).isEqualTo(500);
    }

    @Test
    @DisplayName("Saga 관련 에러 코드는 올바른 값을 가진다")
    void sagaErrorCodes() {
        // when & then
        assertThat(OrderErrorCode.SAGA_NOT_FOUND.getCode()).isEqualTo("SAGA_101");
        assertThat(OrderErrorCode.SAGA_NOT_FOUND.getStatus()).isEqualTo(404);

        assertThat(OrderErrorCode.SAGA_EXECUTION_FAILED.getCode()).isEqualTo("SAGA_102");
        assertThat(OrderErrorCode.SAGA_EXECUTION_FAILED.getStatus()).isEqualTo(500);
    }

    @Test
    @DisplayName("재고 관련 에러 코드는 올바른 값을 가진다")
    void stockErrorCodes() {
        // when & then
        assertThat(OrderErrorCode.INSUFFICIENT_STOCK.getCode()).isEqualTo("STOCK_201");
        assertThat(OrderErrorCode.INSUFFICIENT_STOCK.getStatus()).isEqualTo(409);

        assertThat(OrderErrorCode.STOCK_RESERVATION_FAILED.getCode()).isEqualTo("STOCK_202");
        assertThat(OrderErrorCode.STOCK_RESERVATION_FAILED.getStatus()).isEqualTo(500);
    }

    @Test
    @DisplayName("배송 관련 에러 코드는 올바른 값을 가진다")
    void deliveryErrorCodes() {
        // when & then
        assertThat(OrderErrorCode.DELIVERY_CREATION_FAILED.getCode()).isEqualTo("DELIVERY_301");
        assertThat(OrderErrorCode.DELIVERY_CREATION_FAILED.getStatus()).isEqualTo(500);

        assertThat(OrderErrorCode.ROUTE_NOT_FOUND.getCode()).isEqualTo("DELIVERY_305");
        assertThat(OrderErrorCode.ROUTE_NOT_FOUND.getStatus()).isEqualTo(404);
    }

    @Test
    @DisplayName("외부 서비스 연동 에러 코드는 올바른 값을 가진다")
    void externalServiceErrorCodes() {
        // when & then
        assertThat(OrderErrorCode.INVENTORY_SERVICE_ERROR.getCode()).isEqualTo("EXTERNAL_401");
        assertThat(OrderErrorCode.INVENTORY_SERVICE_ERROR.getStatus()).isEqualTo(502);

        assertThat(OrderErrorCode.EXTERNAL_SERVICE_TIMEOUT.getCode()).isEqualTo("EXTERNAL_409");
        assertThat(OrderErrorCode.EXTERNAL_SERVICE_TIMEOUT.getStatus()).isEqualTo(504);
    }
}
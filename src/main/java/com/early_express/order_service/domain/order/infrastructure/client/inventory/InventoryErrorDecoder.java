package com.early_express.order_service.domain.order.infrastructure.client.inventory;

import com.early_express.order_service.domain.order.domain.exception.OrderErrorCode;
import com.early_express.order_service.domain.order.domain.exception.SagaException;
import feign.Response;
import feign.codec.ErrorDecoder;
import lombok.extern.slf4j.Slf4j;

/**
 * Inventory Client 에러 디코더
 * Inventory Service의 HTTP 에러를 도메인 예외로 변환
 */
@Slf4j
public class InventoryErrorDecoder implements ErrorDecoder {

    private final ErrorDecoder defaultErrorDecoder = new Default();

    @Override
    public Exception decode(String methodKey, Response response) {
        log.error("Inventory Service 호출 실패 - Method: {}, Status: {}",
                methodKey, response.status());

        return switch (response.status()) {
            case 400 -> new SagaException(
                    OrderErrorCode.SAGA_STEP_FAILED,
                    "재고 서비스 요청이 올바르지 않습니다."
            );
            case 404 -> new SagaException(
                    OrderErrorCode.SAGA_STEP_FAILED,
                    "상품 또는 재고를 찾을 수 없습니다."
            );
            case 409 -> new SagaException(
                    OrderErrorCode.SAGA_STEP_FAILED,
                    "재고가 부족합니다."
            );
            case 500 -> new SagaException(
                    OrderErrorCode.SAGA_STEP_FAILED,
                    "재고 서비스 내부 오류가 발생했습니다."
            );
            case 503 -> new SagaException(
                    OrderErrorCode.SAGA_STEP_FAILED,
                    "재고 서비스를 사용할 수 없습니다."
            );
            default -> defaultErrorDecoder.decode(methodKey, response);
        };
    }
}

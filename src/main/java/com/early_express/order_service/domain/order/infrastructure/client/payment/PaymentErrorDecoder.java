package com.early_express.order_service.domain.order.infrastructure.client.payment;

import com.early_express.order_service.domain.order.domain.exception.OrderErrorCode;
import com.early_express.order_service.domain.order.domain.exception.SagaException;
import feign.Response;
import feign.codec.ErrorDecoder;
import lombok.extern.slf4j.Slf4j;

/**
 * Payment Client 에러 디코더
 * Payment Service의 HTTP 에러를 도메인 예외로 변환
 */
@Slf4j
public class PaymentErrorDecoder implements ErrorDecoder {

    private final ErrorDecoder defaultErrorDecoder = new Default();

    @Override
    public Exception decode(String methodKey, Response response) {
        log.error("Payment Service 호출 실패 - Method: {}, Status: {}",
                methodKey, response.status());

        return switch (response.status()) {
            case 400 -> new SagaException(
                    OrderErrorCode.SAGA_STEP_FAILED,
                    "결제 요청이 올바르지 않습니다."
            );
            case 404 -> new SagaException(
                    OrderErrorCode.SAGA_STEP_FAILED,
                    "결제 정보를 찾을 수 없습니다."
            );
            case 409 -> new SagaException(
                    OrderErrorCode.SAGA_STEP_FAILED,
                    "결제 금액이 일치하지 않습니다."
            );
            case 422 -> new SagaException(
                    OrderErrorCode.SAGA_STEP_FAILED,
                    "PG사 결제 검증에 실패했습니다."
            );
            case 500 -> new SagaException(
                    OrderErrorCode.SAGA_STEP_FAILED,
                    "결제 서비스 내부 오류가 발생했습니다."
            );
            case 503 -> new SagaException(
                    OrderErrorCode.SAGA_STEP_FAILED,
                    "결제 서비스를 사용할 수 없습니다."
            );
            default -> defaultErrorDecoder.decode(methodKey, response);
        };
    }
}

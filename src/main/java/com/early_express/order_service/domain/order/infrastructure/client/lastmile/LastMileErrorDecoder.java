package com.early_express.order_service.domain.order.infrastructure.client.lastmile;

import com.early_express.order_service.domain.order.domain.exception.OrderErrorCode;
import com.early_express.order_service.domain.order.domain.exception.SagaException;
import feign.Response;
import feign.codec.ErrorDecoder;
import lombok.extern.slf4j.Slf4j;

/**
 * Last Mile Client 에러 디코더
 * Last Mile Service의 HTTP 에러를 도메인 예외로 변환
 */
@Slf4j
public class LastMileErrorDecoder implements ErrorDecoder {

    private final ErrorDecoder defaultErrorDecoder = new Default();

    @Override
    public Exception decode(String methodKey, Response response) {
        log.error("Last Mile Service 호출 실패 - Method: {}, Status: {}",
                methodKey, response.status());

        return switch (response.status()) {
            case 400 -> new SagaException(
                    OrderErrorCode.LAST_MILE_DELIVERY_CREATION_FAILED
            );
            case 404 -> new SagaException(
                    OrderErrorCode.LAST_MILE_DELIVERY_NOT_FOUND
            );
            case 409 -> new SagaException(
                    OrderErrorCode.LAST_MILE_DELIVERY_ALREADY_EXISTS
            );
            case 422 -> new SagaException(
                    OrderErrorCode.LAST_MILE_DRIVER_NOT_AVAILABLE
            );
            case 500, 503 -> new SagaException(
                    OrderErrorCode.LAST_MILE_SERVICE_ERROR
            );
            default -> defaultErrorDecoder.decode(methodKey, response);
        };
    }
}
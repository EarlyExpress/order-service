package com.early_express.order_service.domain.order.infrastructure.client.hubdelivery;

import com.early_express.order_service.domain.order.domain.exception.OrderErrorCode;
import com.early_express.order_service.domain.order.domain.exception.SagaException;
import feign.Response;
import feign.codec.ErrorDecoder;
import lombok.extern.slf4j.Slf4j;

/**
 * Hub Delivery Client 에러 디코더
 * Hub Delivery Service의 HTTP 에러를 도메인 예외로 변환
 */
@Slf4j
public class HubDeliveryErrorDecoder implements ErrorDecoder {

    private final ErrorDecoder defaultErrorDecoder = new Default();

    @Override
    public Exception decode(String methodKey, Response response) {
        log.error("Hub Delivery Service 호출 실패 - Method: {}, Status: {}",
                methodKey, response.status());

        return switch (response.status()) {
            case 400 -> new SagaException(
                    OrderErrorCode.HUB_DELIVERY_CREATION_FAILED
            );
            case 404 -> new SagaException(
                    OrderErrorCode.HUB_DELIVERY_NOT_FOUND
            );
            case 409 -> new SagaException(
                    OrderErrorCode.HUB_DELIVERY_ALREADY_EXISTS
            );
            case 422 -> new SagaException(
                    OrderErrorCode.HUB_DELIVERY_INVALID_ROUTE
            );
            case 500, 503 -> new SagaException(
                    OrderErrorCode.HUB_DELIVERY_SERVICE_ERROR
            );
            default -> defaultErrorDecoder.decode(methodKey, response);
        };
    }
}
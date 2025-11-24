package com.early_express.order_service.domain.order.domain.exception;

/**
 * Saga 실행 중 발생하는 예외
 */
public class SagaException extends OrderException {

    public SagaException(OrderErrorCode errorCode) {
        super(errorCode);
    }

    public SagaException(OrderErrorCode errorCode, String message) {
        super(errorCode, message);
    }

    public SagaException(OrderErrorCode errorCode, Throwable cause) {
        super(errorCode, cause);
    }

    public SagaException(OrderErrorCode errorCode, String message, Throwable cause) {
        super(errorCode, message, cause);
    }
}
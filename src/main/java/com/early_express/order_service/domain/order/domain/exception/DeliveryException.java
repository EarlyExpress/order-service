package com.early_express.order_service.domain.order.domain.exception;

/**
 * 배송 관련 예외
 */
public class DeliveryException extends OrderException {

    public DeliveryException(OrderErrorCode errorCode) {
        super(errorCode);
    }

    public DeliveryException(OrderErrorCode errorCode, String message) {
        super(errorCode, message);
    }

    public DeliveryException(OrderErrorCode errorCode, Throwable cause) {
        super(errorCode, cause);
    }

    public DeliveryException(OrderErrorCode errorCode, String message, Throwable cause) {
        super(errorCode, message, cause);
    }
}
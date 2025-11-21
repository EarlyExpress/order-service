package com.early_express.order_service.domain.order.domain.exception;

/**
 * 재고 관련 예외
 */
public class StockException extends OrderException {

    public StockException(OrderErrorCode errorCode) {
        super(errorCode);
    }

    public StockException(OrderErrorCode errorCode, String message) {
        super(errorCode, message);
    }

    public StockException(OrderErrorCode errorCode, Throwable cause) {
        super(errorCode, cause);
    }

    public StockException(OrderErrorCode errorCode, String message, Throwable cause) {
        super(errorCode, message, cause);
    }
}
package com.early_express.order_service.domain.order.domain.exception;

import com.early_express.order_service.global.presentation.exception.GlobalException;

/**
 * Order Service의 모든 예외의 기본 클래스
 */
public class OrderException extends GlobalException {

    public OrderException(OrderErrorCode errorCode) {
        super(errorCode);
    }

    public OrderException(OrderErrorCode errorCode, String message) {
        super(errorCode, message);
    }

    public OrderException(OrderErrorCode errorCode, Throwable cause) {
        super(errorCode, cause);
    }

    public OrderException(OrderErrorCode errorCode, String message, Throwable cause) {
        super(errorCode, message, cause);
    }
}
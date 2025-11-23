package com.early_express.order_service.domain.order.domain.messaging.order;

/**
 * Order Event Publisher Interface (도메인 레이어)
 */
public interface OrderEventPublisher {

    /**
     * Step 2 완료 후 발행
     * 비동기 Step 3~7 트리거
     */
    void publishOrderPaymentVerified(OrderPaymentVerifiedEventData eventData);
}
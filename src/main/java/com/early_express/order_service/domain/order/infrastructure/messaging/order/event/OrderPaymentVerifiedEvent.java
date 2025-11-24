package com.early_express.order_service.domain.order.domain.messaging.order.event;

import com.early_express.order_service.domain.order.domain.messaging.order.OrderPaymentVerifiedEventData;
import com.early_express.order_service.global.infrastructure.event.base.BaseEvent;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

/**
 * 주문 결제 검증 완료 이벤트 (Kafka 메시지)
 * Order Service 내부 이벤트
 * Topic: order-events
 */
@Getter
@SuperBuilder
@NoArgsConstructor
public class OrderPaymentVerifiedEvent extends BaseEvent {

    /**
     * 주문 ID
     */
    private String orderId;

    /**
     * Saga ID
     */
    private String sagaId;

    /**
     * 상품 위치 허브 ID
     */
    private String productHubId;

    /**
     * 배송 주소
     */
    private String deliveryAddress;

    /**
     * 배송 상세 주소
     */
    private String deliveryAddressDetail;

    /**
     * 이벤트 데이터 발행 시간
     */
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime publishedAt;

    /**
     * EventData로부터 Event 생성
     */
    public static OrderPaymentVerifiedEvent from(OrderPaymentVerifiedEventData data) {
        OrderPaymentVerifiedEvent event = OrderPaymentVerifiedEvent.builder()
                .orderId(data.getOrderId())
                .sagaId(data.getSagaId())
                .productHubId(data.getProductHubId())
                .deliveryAddress(data.getDeliveryAddress())
                .deliveryAddressDetail(data.getDeliveryAddressDetail())
                .publishedAt(data.getPublishedAt())
                .build();

        event.initBaseEvent("ORDER_PAYMENT_VERIFIED", "order-service");

        return event;
    }
}
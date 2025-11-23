package com.early_express.order_service.domain.order.domain.messaging.order;

import com.early_express.order_service.domain.order.domain.model.Order;
import com.early_express.order_service.domain.order.domain.model.OrderSaga;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 주문 결제 검증 완료 이벤트 데이터
 * Step 2 완료 후 발행
 */
@Getter
@Builder
public class OrderPaymentVerifiedEventData {

    /**
     * 주문 ID
     */
    private String orderId;

    /**
     * Saga ID
     */
    private String sagaId;

    /**
     * 상품 위치 허브 ID (출발 허브)
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
     * 이벤트 발행 시간
     */
    private LocalDateTime publishedAt;

    /**
     * Order와 Saga로부터 이벤트 데이터 생성
     */
    public static OrderPaymentVerifiedEventData from(Order order, OrderSaga saga) {
        return OrderPaymentVerifiedEventData.builder()
                .orderId(order.getIdValue())
                .sagaId(saga.getSagaIdValue())
                .productHubId(order.getProductInfo().getProductHubId())
                .deliveryAddress(order.getReceiverInfo().getDeliveryAddress())
                .deliveryAddressDetail(order.getReceiverInfo().getDeliveryAddressDetail())
                .publishedAt(LocalDateTime.now())
                .build();
    }
}
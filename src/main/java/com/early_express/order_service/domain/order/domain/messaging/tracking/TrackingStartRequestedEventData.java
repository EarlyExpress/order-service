package com.early_express.order_service.domain.order.domain.messaging.tracking;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 추적 시작 요청 이벤트 데이터 (도메인)
 * Order Service → Tracking Service
 */
@Getter
@Builder
public class TrackingStartRequestedEventData {

    /**
     * 주문 ID
     */
    private final String orderId;

    /**
     * 주문 번호
     */
    private final String orderNumber;

    /**
     * 허브 배송 ID (nullable)
     */
    private final String hubDeliveryId;

    /**
     * 업체 배송 ID
     */
    private final String lastMileDeliveryId;

    /**
     * 출발 허브 ID
     */
    private final String originHubId;

    /**
     * 도착 허브 ID
     */
    private final String destinationHubId;

    /**
     * 거치는 허브
     * */
    private final String routingHub;
    /**
     * 허브 배송 필요 여부
     */
    private final Boolean requiresHubDelivery;

    /**
     * 예상 배송 완료 시간
     */
    private final LocalDateTime estimatedDeliveryTime;

    /**
     * 요청 시간
     */
    private final LocalDateTime requestedAt;

    /**
     * 추적 시작 요청 이벤트 데이터 생성
     */
    public static TrackingStartRequestedEventData of(
            String orderId,
            String orderNumber,
            String hubDeliveryId,
            String lastMileDeliveryId,
            String originHubId,
            String destinationHubId,
            String routingHub,
            Boolean requiresHubDelivery,
            LocalDateTime estimatedDeliveryTime) {

        return TrackingStartRequestedEventData.builder()
                .orderId(orderId)
                .orderNumber(orderNumber)
                .hubDeliveryId(hubDeliveryId)
                .lastMileDeliveryId(lastMileDeliveryId)
                .originHubId(originHubId)
                .destinationHubId(destinationHubId)
                .routingHub(routingHub)
                .requiresHubDelivery(requiresHubDelivery)
                .estimatedDeliveryTime(estimatedDeliveryTime)
                .requestedAt(LocalDateTime.now())
                .build();
    }
}
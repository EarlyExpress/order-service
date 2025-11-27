package com.early_express.order_service.domain.order.infrastructure.client.lastmile.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 업체 배송 생성 요청 DTO
 * Order Service → Last Mile Delivery Service
 */
@Getter
@Builder
public class LastMileDeliveryCreateRequest {

    /**
     * 주문 ID (추적용)
     */
    private String orderId;

    /**
     * 도착 허브 ID (배송 출발 허브)
     * - 허브 배송이 완료된 후 업체 배송이 시작되는 허브
     */
    private String hubId;

    /**
     * 배송 주소
     */
    private String deliveryAddress;

    /**
     * 수령인 이름
     */
    private String recipientName;

    /**
     * 수령인 Slack ID
     */
    private String recipientSlackId;

    /**
     * 예상 도착 시간
     */
    private LocalDateTime expectedTime;

    /**
     * 헬퍼 메서드 - Order 도메인에서 생성
     */
    public static LastMileDeliveryCreateRequest of(
            String orderId,
            String hubId,
            String deliveryAddress,
            String recipientName,
            String recipientSlackId,
            LocalDateTime expectedTime) {

        return LastMileDeliveryCreateRequest.builder()
                .orderId(orderId)
                .hubId(hubId)
                .deliveryAddress(deliveryAddress)
                .recipientName(recipientName)
                .recipientSlackId(recipientSlackId)
                .expectedTime(expectedTime)
                .build();
    }
}
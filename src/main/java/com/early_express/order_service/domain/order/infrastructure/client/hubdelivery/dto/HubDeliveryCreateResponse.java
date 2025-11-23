package com.early_express.order_service.domain.order.infrastructure.client.hubdelivery.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 허브 배송 생성 응답 DTO
 * Hub Delivery Service → Order Service
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HubDeliveryCreateResponse {

    /**
     * 생성된 허브 배송 ID
     */
    private String hubDeliveryId;

    /**
     * 주문 ID (요청과 매칭용)
     */
    private String orderId;

    /**
     * 배송 상태
     * - CREATED: 생성됨
     * - WAITING: 출발 대기
     * - IN_TRANSIT: 이동 중
     */
    private String status;

    /**
     * 메시지 (성공/실패 사유 등)
     */
    private String message;

    /**
     * 생성 성공 여부 확인
     */
    public boolean isSuccess() {
        return hubDeliveryId != null && !hubDeliveryId.isBlank();
    }
}
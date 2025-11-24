package com.early_express.order_service.domain.order.infrastructure.client.lastmile.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 업체 배송 생성 응답 DTO
 * Last Mile Delivery Service → Order Service
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LastMileDeliveryCreateResponse {

    /**
     * 생성된 업체 배송 ID
     */
    private String lastMileDeliveryId;

    /**
     * 주문 ID (요청과 매칭용)
     */
    private String orderId;

    /**
     * 배정된 배송 담당자 ID (nullable)
     * - 즉시 배정되지 않을 수 있음
     */
    private String assignedDriverId;

    /**
     * 배정된 배송 담당자 이름 (nullable)
     */
    private String assignedDriverName;

    /**
     * 배송 상태
     * - CREATED: 생성됨
     * - ASSIGNED: 담당자 배정됨
     * - PENDING: 대기 중 (허브 배송 완료 대기)
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
        return lastMileDeliveryId != null && !lastMileDeliveryId.isBlank();
    }

    /**
     * 담당자 배정 여부 확인
     */
    public boolean hasAssignedDriver() {
        return assignedDriverId != null && !assignedDriverId.isBlank();
    }
}
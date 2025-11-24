package com.early_express.order_service.domain.order.infrastructure.client.inventory.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 재고 복원 응답 DTO
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InventoryRestoreResponse {

    /**
     * 복원 성공 여부
     */
    private Boolean success;

    /**
     * 예약 ID
     */
    private String reservationId;

    /**
     * 복원된 총 수량
     */
    private Integer totalRestoredQuantity;

    /**
     * 메시지
     */
    private String message;
}
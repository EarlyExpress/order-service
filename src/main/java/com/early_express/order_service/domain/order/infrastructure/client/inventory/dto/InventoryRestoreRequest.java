package com.early_express.order_service.domain.order.infrastructure.client.inventory.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

/**
 * 재고 복원 요청 DTO (Compensation)
 *
 * - 예약 시 받은 reservedItems 그대로 전달
 * - Inventory Service가 각 허브별로 복원
 */
@Getter
@Builder
public class InventoryRestoreRequest {

    /**
     * 예약 ID
     */
    @NotBlank(message = "예약 ID는 필수입니다.")
    private String reservationId;

    /**
     * 주문 ID (추적용)
     */
    @NotBlank(message = "주문 ID는 필수입니다.")
    private String orderId;

    /**
     * 복원할 상품 목록
     * - 예약 시 받은 reservedItems 그대로 사용
     */
    @NotEmpty(message = "복원할 상품 목록은 비어있을 수 없습니다.")
    @Valid
    private List<RestoreItem> items;

    /**
     * 복원 사유
     */
    @NotBlank(message = "복원 사유는 필수입니다.")
    private String reason;

    /**
     * 복원 상품 정보
     */
    @Getter
    @Builder
    public static class RestoreItem {

        /**
         * 상품 ID
         */
        @NotBlank(message = "상품 ID는 필수입니다.")
        private String productId;

        /**
         * 허브 ID (예약 시 받은 값)
         */
        @NotBlank(message = "허브 ID는 필수입니다.")
        private String hubId;

        /**
         * 복원할 수량
         */
        @NotNull(message = "수량은 필수입니다.")
        @Min(value = 1, message = "수량은 1 이상이어야 합니다.")
        private Integer quantity;
    }

    /**
     * InventoryReservationResponse에서 RestoreRequest 생성
     */
    public static InventoryRestoreRequest from(
            String reservationId,
            String orderId,
            List<InventoryReservationResponse.ReservedItem> reservedItems,
            String reason) {

        List<RestoreItem> restoreItems = reservedItems.stream()
                .filter(InventoryReservationResponse.ReservedItem::getSuccess)
                .map(item -> RestoreItem.builder()
                        .productId(item.getProductId())
                        .hubId(item.getHubId())
                        .quantity(item.getQuantity())
                        .build())
                .toList();

        return InventoryRestoreRequest.builder()
                .reservationId(reservationId)
                .orderId(orderId)
                .items(restoreItems)
                .reason(reason)
                .build();
    }
}

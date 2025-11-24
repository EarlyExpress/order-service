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
 * 재고 예약 요청 DTO
 *
 * - Order Service는 허브를 지정하지 않음
 * - Inventory Service가 재고가 있는 허브를 자동으로 찾아 예약
 * - 여러 상품 지원 (items 배열)
 */
@Getter
@Builder
public class InventoryReservationRequest {

    /**
     * 주문 ID (추적용)
     */
    @NotBlank(message = "주문 ID는 필수입니다.")
    private String orderId;

    /**
     * 예약할 상품 목록
     */
    @NotEmpty(message = "예약할 상품 목록은 비어있을 수 없습니다.")
    @Valid
    private List<ReservationItem> items;

    /**
     * 예약 상품 정보
     */
    @Getter
    @Builder
    public static class ReservationItem {

        /**
         * 상품 ID
         */
        @NotBlank(message = "상품 ID는 필수입니다.")
        private String productId;

        /**
         * 예약 수량
         */
        @NotNull(message = "수량은 필수입니다.")
        @Min(value = 1, message = "수량은 1 이상이어야 합니다.")
        private Integer quantity;
    }

    /**
     * 단일 상품 주문용 헬퍼 메서드
     */
    public static InventoryReservationRequest of(
            String orderId,
            String productId,
            Integer quantity) {

        return InventoryReservationRequest.builder()
                .orderId(orderId)
                .items(List.of(
                        ReservationItem.builder()
                                .productId(productId)
                                .quantity(quantity)
                                .build()
                ))
                .build();
    }

    /**
     * 여러 상품 주문용 헬퍼 메서드
     */
    public static InventoryReservationRequest ofMultiple(
            String orderId,
            List<ReservationItem> items) {

        return InventoryReservationRequest.builder()
                .orderId(orderId)
                .items(items)
                .build();
    }
}
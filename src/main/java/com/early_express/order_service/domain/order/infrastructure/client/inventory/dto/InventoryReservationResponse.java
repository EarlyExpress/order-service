package com.early_express.order_service.domain.order.infrastructure.client.inventory.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 재고 예약 응답 DTO
 *
 * - Inventory Service가 자동으로 허브를 선택하여 예약
 * - 한 상품이 여러 허브에 분산될 수 있음
 * - 예: 마른 오징어 50박스 → 경기북부 30박스 + 대전 20박스
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InventoryReservationResponse {

    /**
     * 예약 ID (UUID)
     */
    private String reservationId;

    /**
     * 주문 ID
     */
    private String orderId;

    /**
     * 전체 성공 여부
     * - 모든 상품의 모든 수량이 예약되었으면 true
     */
    private Boolean allSuccess;

    /**
     * 예약된 상품 목록
     * - 한 상품이 여러 허브에서 예약될 수 있음
     */
    private List<ReservedItem> reservedItems;

    /**
     * 예약된 상품 상세 정보
     */
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ReservedItem {

        /**
         * 상품 ID
         */
        private String productId;

        /**
         * 예약된 허브 ID
         * - Inventory Service가 자동으로 선택
         */
        private String hubId;

        /**
         * 예약된 수량
         */
        private Integer quantity;

        /**
         * 예약 성공 여부
         */
        private Boolean success;

        /**
         * 실패 시 에러 메시지
         */
        private String errorMessage;
    }

    /**
     * 전체 예약 성공 여부 확인
     */
    public boolean isAllReserved() {
        return Boolean.TRUE.equals(allSuccess);
    }

    /**
     * 특정 상품의 예약 정보 조회
     */
    public List<ReservedItem> getReservedItemsByProductId(String productId) {
        return reservedItems.stream()
                .filter(item -> item.getProductId().equals(productId))
                .toList();
    }

    /**
     * 특정 상품의 총 예약 수량 계산
     */
    public int getTotalReservedQuantity(String productId) {
        return reservedItems.stream()
                .filter(item -> item.getProductId().equals(productId))
                .filter(ReservedItem::getSuccess)
                .mapToInt(ReservedItem::getQuantity)
                .sum();
    }

    /**
     * 허브 ID 목록 추출 (중복 제거)
     */
    public List<String> getDistinctHubIds() {
        return reservedItems.stream()
                .map(ReservedItem::getHubId)
                .distinct()
//                .toList();    //json변환을 위해 컬렉션 추가
                .collect(Collectors.toList());
    }
}
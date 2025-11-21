package com.early_express.order_service.domain.order.domain.model.vo;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

/**
 * 상품 정보 Value Object
 * 상품 ID, 위치 허브, 수량 정보를 담음
 */
@Getter
@EqualsAndHashCode
public class ProductInfo {

    private final String productId;
    private final String productHubId; // 재고 예약 후 결정됨
    private final Integer quantity;

    @Builder
    private ProductInfo(
            String productId,
            String productHubId,
            Integer quantity) {

        validateNotNull(productId, "상품 ID");
        validateQuantity(quantity);

        this.productId = productId;
        this.productHubId = productHubId; // nullable (재고 예약 전)
        this.quantity = quantity;
    }

    public static ProductInfo of(String productId, Integer quantity) {
        return ProductInfo.builder()
                .productId(productId)
                .quantity(quantity)
                .build();
    }

    /**
     * 재고 예약 후 상품 위치 허브 정보 업데이트
     */
    public ProductInfo withProductHubId(String productHubId) {
        return ProductInfo.builder()
                .productId(this.productId)
                .productHubId(productHubId)
                .quantity(this.quantity)
                .build();
    }

    /**
     * 상품이 위치한 허브가 설정되었는지 확인
     */
    public boolean hasProductHubId() {
        return productHubId != null && !productHubId.trim().isEmpty();
    }

    private void validateNotNull(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(fieldName + "는 null이거나 빈 값일 수 없습니다.");
        }
    }

    private void validateQuantity(Integer quantity) {
        if (quantity == null || quantity <= 0) {
            throw new IllegalArgumentException("수량은 0보다 커야 합니다.");
        }
    }
}

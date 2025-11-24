package com.early_express.order_service.domain.order.domain.model.vo;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

@DisplayName("ProductInfo vo 테스트")
class ProductInfoTest {

    @Test
    @DisplayName("상품 정보를 생성할 수 있다")
    void createProductInfo() {
        // given
        String productId = "PROD-001";
        Integer quantity = 5;

        // when
        ProductInfo productInfo = ProductInfo.of(productId, quantity);

        // then
        assertThat(productInfo.getProductId()).isEqualTo(productId);
        assertThat(productInfo.getQuantity()).isEqualTo(quantity);
        assertThat(productInfo.hasProductHubId()).isFalse();
    }

    @Test
    @DisplayName("상품 위치 허브 정보를 설정할 수 있다")
    void setProductHubId() {
        // given
        ProductInfo productInfo = ProductInfo.of("PROD-001", 5);
        String productHubId = "HUB-001";

        // when
        ProductInfo updated = productInfo.withProductHubId(productHubId);

        // then
        assertThat(updated.hasProductHubId()).isTrue();
        assertThat(updated.getProductHubId()).isEqualTo(productHubId);
    }

    @Test
    @DisplayName("상품 ID가 null이면 예외가 발생한다")
    void validateProductIdNull() {
        // when & then
        assertThatThrownBy(() -> ProductInfo.of(null, 5))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("상품 ID는 null이거나 빈 값일 수 없습니다");
    }

    @Test
    @DisplayName("상품 ID가 빈 문자열이면 예외가 발생한다")
    void validateProductIdEmpty() {
        // when & then
        assertThatThrownBy(() -> ProductInfo.of("", 5))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("상품 ID는 null이거나 빈 값일 수 없습니다");
    }

    @Test
    @DisplayName("수량이 null이면 예외가 발생한다")
    void validateQuantityNull() {
        // when & then
        assertThatThrownBy(() -> ProductInfo.of("PROD-001", null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("수량은 0보다 커야 합니다");
    }

    @Test
    @DisplayName("수량이 0 이하면 예외가 발생한다")
    void validateQuantityZero() {
        // when & then
        assertThatThrownBy(() -> ProductInfo.of("PROD-001", 0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("수량은 0보다 커야 합니다");
    }

    @Test
    @DisplayName("수량이 음수면 예외가 발생한다")
    void validateQuantityNegative() {
        // when & then
        assertThatThrownBy(() -> ProductInfo.of("PROD-001", -5))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("수량은 0보다 커야 합니다");
    }

    @Test
    @DisplayName("같은 값을 가진 ProductInfo는 동일하다")
    void equalProductInfos() {
        // given
        ProductInfo productInfo1 = ProductInfo.of("PROD-001", 5);
        ProductInfo productInfo2 = ProductInfo.of("PROD-001", 5);

        // when & then
        assertThat(productInfo1).isEqualTo(productInfo2);
        assertThat(productInfo1.hashCode()).isEqualTo(productInfo2.hashCode());
    }
}
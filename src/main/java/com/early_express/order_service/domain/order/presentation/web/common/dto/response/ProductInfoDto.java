package com.early_express.order_service.domain.order.presentation.web.common.dto.response;

import com.early_express.order_service.domain.order.domain.model.vo.ProductInfo;
import lombok.Builder;
import lombok.Getter;

/**
 * 상품 정보 DTO
 */
@Getter
@Builder
public class ProductInfoDto {

    private String productId;
    private String productHubId;
    private Integer quantity;

    public static ProductInfoDto from(ProductInfo productInfo) {
        return ProductInfoDto.builder()
                .productId(productInfo.getProductId())
                .productHubId(productInfo.getProductHubId())
                .quantity(productInfo.getQuantity())
                .build();
    }
}
package com.early_express.order_service.domain.order.presentation.web.common.dto.response;

import com.early_express.order_service.domain.order.domain.model.vo.DeliveryInfo;
import lombok.Builder;
import lombok.Getter;

/**
 * 배송 정보 DTO
 */
@Getter
@Builder
public class DeliveryInfoDto {

    private Boolean requiresHubDelivery;
    private String hubDeliveryId;
    private String lastMileDeliveryId;

    public static DeliveryInfoDto from(DeliveryInfo deliveryInfo) {
        return DeliveryInfoDto.builder()
                .requiresHubDelivery(deliveryInfo.getRequiresHubDelivery())
                .hubDeliveryId(deliveryInfo.getHubDeliveryId())
                .lastMileDeliveryId(deliveryInfo.getLastMileDeliveryId())
                .build();
    }
}
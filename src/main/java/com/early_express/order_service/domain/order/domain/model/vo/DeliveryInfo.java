package com.early_express.order_service.domain.order.domain.model.vo;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

/**
 * 배송 정보 Value Object
 * 허브 배송 필요 여부 및 배송 ID 정보를 담음
 */
@Getter
@EqualsAndHashCode
public class DeliveryInfo {

    private final Boolean requiresHubDelivery; // 허브 배송 필요 여부
    private final String hubDeliveryId; // 허브 배송 ID (nullable)
    private final String lastMileDeliveryId; // 업체 배송 ID

    @Builder
    private DeliveryInfo(
            Boolean requiresHubDelivery,
            String hubDeliveryId,
            String lastMileDeliveryId) {

        this.requiresHubDelivery = requiresHubDelivery != null ? requiresHubDelivery : false;
        this.hubDeliveryId = hubDeliveryId;
        this.lastMileDeliveryId = lastMileDeliveryId;
    }

    /**
     * 초기 배송 정보 생성 (배송 ID 없음)
     */
    public static DeliveryInfo initial() {
        return DeliveryInfo.builder()
                .requiresHubDelivery(false)
                .build();
    }

    /**
     * 허브 배송 필요 여부 설정
     */
    public DeliveryInfo withRequiresHubDelivery(boolean requiresHubDelivery) {
        return DeliveryInfo.builder()
                .requiresHubDelivery(requiresHubDelivery)
                .hubDeliveryId(this.hubDeliveryId)
                .lastMileDeliveryId(this.lastMileDeliveryId)
                .build();
    }

    /**
     * 허브 배송 ID 설정
     */
    public DeliveryInfo withHubDeliveryId(String hubDeliveryId) {
        return DeliveryInfo.builder()
                .requiresHubDelivery(this.requiresHubDelivery)
                .hubDeliveryId(hubDeliveryId)
                .lastMileDeliveryId(this.lastMileDeliveryId)
                .build();
    }

    /**
     * 업체 배송 ID 설정
     */
    public DeliveryInfo withLastMileDeliveryId(String lastMileDeliveryId) {
        return DeliveryInfo.builder()
                .requiresHubDelivery(this.requiresHubDelivery)
                .hubDeliveryId(this.hubDeliveryId)
                .lastMileDeliveryId(lastMileDeliveryId)
                .build();
    }

    /**
     * 허브 배송이 필요한지 확인
     */
    public boolean needsHubDelivery() {
        return Boolean.TRUE.equals(requiresHubDelivery);
    }

    /**
     * 허브 배송 ID가 설정되었는지 확인
     */
    public boolean hasHubDeliveryId() {
        return hubDeliveryId != null && !hubDeliveryId.trim().isEmpty();
    }

    /**
     * 업체 배송 ID가 설정되었는지 확인
     */
    public boolean hasLastMileDeliveryId() {
        return lastMileDeliveryId != null && !lastMileDeliveryId.trim().isEmpty();
    }
}
package com.early_express.order_service.domain.order.infrastructure.client.hub.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Getter;

/**
 * 허브 경로 계산 요청 DTO
 */
@Getter
@Builder
public class HubRouteCalculationRequest {

    /**
     * 주문 ID (추적용)
     */
    @NotBlank(message = "주문 ID는 필수입니다.")
    private String orderId;

    /**
     * 출발 허브 ID (상품 위치 허브)
     */
    @NotBlank(message = "출발 허브 ID는 필수입니다.")
    private String originHubId;

    /**
     * 도착지 주소
     */
    @NotBlank(message = "도착지 주소는 필수입니다.")
    private String destinationAddress;

    /**
     * 도착지 상세 주소
     */
    private String destinationAddressDetail;

    /**
     * 헬퍼 메서드
     */
    public static HubRouteCalculationRequest of(
            String orderId,
            String originHubId,
            String destinationAddress,
            String destinationAddressDetail) {

        return HubRouteCalculationRequest.builder()
                .orderId(orderId)
                .originHubId(originHubId)
                .destinationAddress(destinationAddress)
                .destinationAddressDetail(destinationAddressDetail)
                .build();
    }
}
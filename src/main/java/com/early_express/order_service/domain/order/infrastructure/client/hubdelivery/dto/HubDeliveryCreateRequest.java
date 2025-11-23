package com.early_express.order_service.domain.order.infrastructure.client.hubdelivery.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

/**
 * 허브 배송 생성 요청 DTO
 * Order Service → Hub Delivery Service
 */
@Getter
@Builder
public class HubDeliveryCreateRequest {

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
     * 도착 허브 ID (최종 목적지 허브)
     */
    @NotBlank(message = "도착 허브 ID는 필수입니다.")
    private String destinationHubId;

    /**
     * 경유 허브 목록 (순서대로)
     * [출발 허브, 경유1, 경유2, ..., 도착 허브]
     */
    @NotEmpty(message = "경유 허브 목록은 필수입니다.")
    private List<String> routeHubs;

    /**
     * 경로 상세 정보 JSON
     * - 각 구간별 거리, 예상 시간 등
     */
    private String routeInfoJson;

    /**
     * 발송 시한 (AI 계산 결과)
     * ISO 8601 형식: "2025-12-10T09:00:00"
     */
    private String departureDeadline;

    /**
     * 예상 도착 시간 (AI 계산 결과)
     * ISO 8601 형식: "2025-12-12T15:00:00"
     */
    private String estimatedArrivalTime;

    /**
     * 헬퍼 메서드 - Order 도메인에서 생성
     */
    public static HubDeliveryCreateRequest of(
            String orderId,
            String originHubId,
            String destinationHubId,
            List<String> routeHubs,
            String routeInfoJson,
            String departureDeadline,
            String estimatedArrivalTime) {

        return HubDeliveryCreateRequest.builder()
                .orderId(orderId)
                .originHubId(originHubId)
                .destinationHubId(destinationHubId)
                .routeHubs(routeHubs)
                .routeInfoJson(routeInfoJson)
                .departureDeadline(departureDeadline)
                .estimatedArrivalTime(estimatedArrivalTime)
                .build();
    }
}
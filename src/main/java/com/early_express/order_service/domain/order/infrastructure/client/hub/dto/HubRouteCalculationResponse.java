package com.early_express.order_service.domain.order.infrastructure.client.hub.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 허브 경로 계산 응답 DTO
 *
 * Hub Service는 경로와 거리만 제공
 * 시간 계산은 AI Service가 담당
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HubRouteCalculationResponse {

    /**
     * 주문 ID
     */
    private String orderId;

    /**
     * 출발 허브 ID
     */
    private String originHubId;

    /**
     * 도착 허브 ID
     * - Hub Service가 주소 기반으로 결정한 도착 허브
     */
    private String destinationHubId;

    /**
     * 거쳐야 할 허브 목록 (순서대로)
     * - [출발 허브, 경유 허브1, 경유 허브2, ..., 도착 허브]
     * - 예: ["HUB-001", "HUB-002", "HUB-003"]
     */
    private List<String> routeHubs;

    /**
     * 허브 배송 필요 여부
     * - routeHubs.size() >= 2 이면 true
     * - 출발 허브 != 도착 허브
     */
    private Boolean requiresHubDelivery;

    /**
     * 총 예상 거리 (km)
     * - 허브 간 이동 거리 합계
     */
    private Double estimatedDistance;

    /**
     * 경로 상세 정보 JSON
     * - 각 구간별 거리, 허브 정보 등
     * - AI Service가 시간 계산 시 참고
     */
    private String routeInfoJson;

    /**
     * 허브 배송 필요 여부 확인
     */
    public boolean needsHubDelivery() {
        return Boolean.TRUE.equals(requiresHubDelivery);
    }

    /**
     * 경유해야 할 허브 개수
     */
    public int getHubCount() {
        return routeHubs != null ? routeHubs.size() : 0;
    }
}
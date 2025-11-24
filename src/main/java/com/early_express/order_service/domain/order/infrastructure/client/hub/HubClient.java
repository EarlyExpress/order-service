package com.early_express.order_service.domain.order.infrastructure.client.hub;

import com.early_express.order_service.domain.order.infrastructure.client.hub.dto.HubRouteCalculationRequest;
import com.early_express.order_service.domain.order.infrastructure.client.hub.dto.HubRouteCalculationResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * Hub Service Feign Client
 * 허브 경로 계산 서비스와의 동기 통신
 */
@FeignClient(
        name = "hub-service",
        url = "${client.hub-service.url}",
        configuration = HubClientConfig.class
)
public interface HubClient {

    /**
     * 허브 경로 계산 (Saga Step 3)
     * - 출발 허브에서 도착 허브까지의 최적 경로 계산
     * - 거쳐야 할 허브 목록 반환
     * - 허브 배송 필요 여부 판단
     *
     * @param request 경로 계산 요청
     * @return 경로 계산 결과 (거쳐야 할 허브 목록, 거리, 시간)
     */
    @PostMapping("/v1/hub/internal/route/calculate")
    HubRouteCalculationResponse calculateRoute(
            @RequestBody HubRouteCalculationRequest request
    );
}
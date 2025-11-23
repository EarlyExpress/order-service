package com.early_express.order_service.domain.order.infrastructure.client.lastmile;

import com.early_express.order_service.domain.order.infrastructure.client.lastmile.dto.LastMileDeliveryCreateRequest;
import com.early_express.order_service.domain.order.infrastructure.client.lastmile.dto.LastMileDeliveryCreateResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * Last Mile Delivery Service Feign Client
 * 업체 배송 서비스와의 동기 통신
 */
@FeignClient(
        name = "last-mile-service",
        url = "${client.last-mile-service.url}",
        configuration = LastMileClientConfig.class
)
public interface LastMileClient {

    /**
     * 업체 배송 생성 (Saga Step 5)
     * - 도착 허브에서 수령 업체까지의 배송 생성
     * - 배송 담당자 자동 배정 (순번 기반)
     *
     * @param request 업체 배송 생성 요청
     * @return 생성된 업체 배송 정보
     */
    @PostMapping("/v1/last-mile/internal/deliveries")
    LastMileDeliveryCreateResponse createDelivery(
            @RequestBody LastMileDeliveryCreateRequest request
    );

    /**
     * 업체 배송 취소 (보상 트랜잭션)
     * - Saga 실패 시 생성된 배송 취소
     *
     * @param lastMileDeliveryId 취소할 배송 ID
     * @return 취소 결과
     */
    @PostMapping("/v1/last-mile/internal/deliveries/{lastMileDeliveryId}/cancel")
    LastMileDeliveryCreateResponse cancelDelivery(
            @PathVariable("lastMileDeliveryId") String lastMileDeliveryId
    );
}
package com.early_express.order_service.domain.order.infrastructure.client.hubdelivery;

import com.early_express.order_service.domain.order.infrastructure.client.hubdelivery.dto.HubDeliveryCreateRequest;
import com.early_express.order_service.domain.order.infrastructure.client.hubdelivery.dto.HubDeliveryCreateResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * Hub Delivery Service Feign Client
 * 허브 간 배송 서비스와의 동기 통신
 */
@FeignClient(
        name = "hub-delivery-service",
//        url = "${client.hub-delivery-service.url}",
        configuration = HubDeliveryClientConfig.class
)
public interface HubDeliveryClient {

    /**
     * 허브 배송 생성 (Saga Step 4)
     * - 출발 허브에서 도착 허브까지의 배송 생성
     * - 경유 허브별 구간 배송 자동 생성
     *
     * @param request 허브 배송 생성 요청
     * @return 생성된 허브 배송 정보
     */
    @PostMapping("/v1/hub-delivery/internal/deliveries")
    HubDeliveryCreateResponse createDelivery(
            @RequestBody HubDeliveryCreateRequest request
    );

    /**
     * 허브 배송 취소 (보상 트랜잭션)
     * - Saga 실패 시 생성된 배송 취소
     *
     * @param hubDeliveryId 취소할 허브 배송 ID
     * @return 취소 결과
     */
    @PostMapping("/v1/hub-delivery/internal/deliveries/{hubDeliveryId}/cancel")
    HubDeliveryCreateResponse cancelDelivery(
            @PathVariable("hubDeliveryId") String hubDeliveryId
    );
}
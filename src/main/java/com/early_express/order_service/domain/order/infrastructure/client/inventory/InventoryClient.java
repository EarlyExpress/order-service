package com.early_express.order_service.domain.order.infrastructure.client.inventory;

import com.early_express.order_service.domain.order.infrastructure.client.inventory.dto.InventoryReservationRequest;
import com.early_express.order_service.domain.order.infrastructure.client.inventory.dto.InventoryReservationResponse;
import com.early_express.order_service.domain.order.infrastructure.client.inventory.dto.InventoryRestoreRequest;
import com.early_express.order_service.domain.order.infrastructure.client.inventory.dto.InventoryRestoreResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * Inventory Service Feign Client
 * 재고 서비스와의 동기 통신
 */
@FeignClient(
        name = "inventory-service",
        url = "${client.inventory-service.url}",
        configuration = InventoryClientConfig.class
)
public interface InventoryClient {

    /**
     * 재고 예약 (Saga Step 1)
     * - 즉시 재고 확인 및 예약
     * - productHubId 획득 (상품 위치 허브)
     *
     * @param request 재고 예약 요청
     * @return 예약 정보 (reservationId, productHubId, reservedQuantity)
     */
    @PostMapping("/v1/inventory/internal/all/reserve")
    InventoryReservationResponse reserveStock(
            @RequestBody InventoryReservationRequest request
    );

    /**
     * 재고 복원 (Compensation)
     * - 재고 예약 취소
     * - 예약된 수량 복원
     *
     * @param request 재고 복원 요청
     * @return 복원 결과
     */
    @PostMapping("/v1/inventory/internal/all/restore")
    InventoryRestoreResponse restoreStock(
            @RequestBody InventoryRestoreRequest request
    );
}
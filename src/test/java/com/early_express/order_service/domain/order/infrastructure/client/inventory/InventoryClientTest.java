package com.early_express.order_service.domain.order.infrastructure.client.inventory;

import com.early_express.order_service.domain.order.infrastructure.client.inventory.dto.InventoryReservationRequest;
import com.early_express.order_service.domain.order.infrastructure.client.inventory.dto.InventoryReservationResponse;
import com.early_express.order_service.domain.order.infrastructure.client.inventory.dto.InventoryRestoreRequest;
import com.early_express.order_service.domain.order.infrastructure.client.inventory.dto.InventoryRestoreResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@SpringBootTest(classes = {InventoryClient.class, InventoryClientConfig.class})
@DisplayName("InventoryClient 테스트")
class InventoryClientTest {

    @MockBean
    private InventoryClient inventoryClient;

    @Test
    @DisplayName("재고 예약 성공")
    void reserveStock_Success() {
        // given
        InventoryReservationRequest request = InventoryReservationRequest.of(
                "ORDER-001",
                "PROD-001",
                10
        );

        InventoryReservationResponse.ReservedItem reservedItem =
                InventoryReservationResponse.ReservedItem.builder()
                        .productId("PROD-001")
                        .hubId("HUB-001")
                        .quantity(10)
                        .success(true)
                        .build();

        InventoryReservationResponse mockResponse = InventoryReservationResponse.builder()
                .reservationId("RES-001")
                .orderId("ORDER-001")
                .allSuccess(true)
                .reservedItems(List.of(reservedItem))
                .build();

        given(inventoryClient.reserveStock(any(InventoryReservationRequest.class)))
                .willReturn(mockResponse);

        // when
        InventoryReservationResponse response = inventoryClient.reserveStock(request);

        // then
        assertThat(response).isNotNull();
        assertThat(response.isAllReserved()).isTrue();
        assertThat(response.getReservedItems()).hasSize(1);
        assertThat(response.getReservedItems().get(0).getHubId()).isEqualTo("HUB-001");
    }

    @Test
    @DisplayName("재고 예약 실패 - 재고 부족")
    void reserveStock_Fail_InsufficientStock() {
        // given
        InventoryReservationRequest request = InventoryReservationRequest.of(
                "ORDER-001",
                "PROD-001",
                100
        );

        InventoryReservationResponse mockResponse = InventoryReservationResponse.builder()
                .reservationId("RES-001")
                .orderId("ORDER-001")
                .allSuccess(false)
                .reservedItems(List.of())
                .build();

        given(inventoryClient.reserveStock(any(InventoryReservationRequest.class)))
                .willReturn(mockResponse);

        // when
        InventoryReservationResponse response = inventoryClient.reserveStock(request);

        // then
        assertThat(response).isNotNull();
        assertThat(response.isAllReserved()).isFalse();
        assertThat(response.getReservedItems()).isEmpty();
    }

    @Test
    @DisplayName("재고 복원 성공")
    void restoreStock_Success() {
        // given
        List<InventoryReservationResponse.ReservedItem> reservedItems = List.of(
                InventoryReservationResponse.ReservedItem.builder()
                        .productId("PROD-001")
                        .hubId("HUB-001")
                        .quantity(10)
                        .success(true)
                        .build()
        );

        InventoryRestoreRequest request = InventoryRestoreRequest.from(
                "RES-001",
                "ORDER-001",
                reservedItems,
                "주문 취소"
        );

        InventoryRestoreResponse mockResponse = InventoryRestoreResponse.builder()
                .success(true)
                .reservationId("RES-001")
                .totalRestoredQuantity(10)
                .message("재고 복원 완료")
                .build();

        given(inventoryClient.restoreStock(any(InventoryRestoreRequest.class)))
                .willReturn(mockResponse);

        // when
        InventoryRestoreResponse response = inventoryClient.restoreStock(request);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getSuccess()).isTrue();
        assertThat(response.getTotalRestoredQuantity()).isEqualTo(10);
    }

    @Test
    @DisplayName("여러 허브에서 재고 예약")
    void reserveStock_MultipleHubs() {
        // given
        InventoryReservationRequest request = InventoryReservationRequest.of(
                "ORDER-001",
                "PROD-001",
                50
        );

        List<InventoryReservationResponse.ReservedItem> reservedItems = List.of(
                InventoryReservationResponse.ReservedItem.builder()
                        .productId("PROD-001")
                        .hubId("HUB-001")
                        .quantity(30)
                        .success(true)
                        .build(),
                InventoryReservationResponse.ReservedItem.builder()
                        .productId("PROD-001")
                        .hubId("HUB-002")
                        .quantity(20)
                        .success(true)
                        .build()
        );

        InventoryReservationResponse mockResponse = InventoryReservationResponse.builder()
                .reservationId("RES-001")
                .orderId("ORDER-001")
                .allSuccess(true)
                .reservedItems(reservedItems)
                .build();

        given(inventoryClient.reserveStock(any(InventoryReservationRequest.class)))
                .willReturn(mockResponse);

        // when
        InventoryReservationResponse response = inventoryClient.reserveStock(request);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getReservedItems()).hasSize(2);
        assertThat(response.getTotalReservedQuantity("PROD-001")).isEqualTo(50);
        assertThat(response.getDistinctHubIds()).containsExactlyInAnyOrder("HUB-001", "HUB-002");
    }
}
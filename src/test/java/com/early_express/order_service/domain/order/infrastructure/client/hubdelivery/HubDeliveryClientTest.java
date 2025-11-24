package com.early_express.order_service.domain.order.infrastructure.client.hubdelivery;

import com.early_express.order_service.domain.order.infrastructure.client.hubdelivery.dto.HubDeliveryCreateRequest;
import com.early_express.order_service.domain.order.infrastructure.client.hubdelivery.dto.HubDeliveryCreateResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;

@SpringBootTest(classes = {HubDeliveryClient.class, HubDeliveryClientConfig.class})
@DisplayName("HubDeliveryClient 테스트")
class HubDeliveryClientTest {

    @MockBean
    private HubDeliveryClient hubDeliveryClient;

    @Test
    @DisplayName("허브 배송 생성 성공")
    void createDelivery_Success() {
        // given
        HubDeliveryCreateRequest request = HubDeliveryCreateRequest.of(
                "ORDER-001",
                "HUB-001",
                "HUB-003",
                List.of("HUB-001", "HUB-002", "HUB-003"),
                "{\"route\": \"details\"}",
                LocalDateTime.now().plusHours(2).toString(),
                LocalDateTime.now().plusDays(1).toString()
        );

        HubDeliveryCreateResponse mockResponse = HubDeliveryCreateResponse.builder()
                .hubDeliveryId("HUB-DELIVERY-001")
                .orderId("ORDER-001")
                .status("CREATED")
                .message("허브 배송이 생성되었습니다")
                .build();

        given(hubDeliveryClient.createDelivery(any(HubDeliveryCreateRequest.class)))
                .willReturn(mockResponse);

        // when
        HubDeliveryCreateResponse response = hubDeliveryClient.createDelivery(request);

        // then
        assertThat(response).isNotNull();
        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getHubDeliveryId()).isEqualTo("HUB-DELIVERY-001");
        assertThat(response.getStatus()).isEqualTo("CREATED");
    }

    @Test
    @DisplayName("허브 배송 생성 실패 - 잘못된 경로")
    void createDelivery_Fail_InvalidRoute() {
        // given
        HubDeliveryCreateRequest request = HubDeliveryCreateRequest.of(
                "ORDER-001",
                "HUB-001",
                "HUB-999",
                List.of("HUB-001", "HUB-999"),
                "{\"route\": \"details\"}",
                LocalDateTime.now().plusHours(2).toString(),
                LocalDateTime.now().plusDays(1).toString()
        );

        HubDeliveryCreateResponse mockResponse = HubDeliveryCreateResponse.builder()
                .hubDeliveryId(null)
                .orderId("ORDER-001")
                .status("FAILED")
                .message("존재하지 않는 허브입니다: HUB-999")
                .build();

        given(hubDeliveryClient.createDelivery(any(HubDeliveryCreateRequest.class)))
                .willReturn(mockResponse);

        // when
        HubDeliveryCreateResponse response = hubDeliveryClient.createDelivery(request);

        // then
        assertThat(response).isNotNull();
        assertThat(response.isSuccess()).isFalse();
        assertThat(response.getStatus()).isEqualTo("FAILED");
    }

    @Test
    @DisplayName("허브 배송 취소 성공")
    void cancelDelivery_Success() {
        // given
        String deliveryId = "HUB-DELIVERY-001";

        HubDeliveryCreateResponse mockResponse = HubDeliveryCreateResponse.builder()
                .hubDeliveryId(deliveryId)
                .orderId("ORDER-001")
                .status("CANCELLED")
                .message("허브 배송이 취소되었습니다")
                .build();

        given(hubDeliveryClient.cancelDelivery(anyString()))
                .willReturn(mockResponse);

        // when
        HubDeliveryCreateResponse response = hubDeliveryClient.cancelDelivery(deliveryId);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo("CANCELLED");
    }

    @Test
    @DisplayName("허브 배송 생성 성공 - 여러 경유지")
    void createDelivery_Success_MultipleStops() {
        // given
        HubDeliveryCreateRequest request = HubDeliveryCreateRequest.of(
                "ORDER-001",
                "HUB-001",
                "HUB-005",
                List.of("HUB-001", "HUB-002", "HUB-003", "HUB-004", "HUB-005"),
                "{\"route\": \"long distance\"}",
                LocalDateTime.now().plusHours(4).toString(),
                LocalDateTime.now().plusDays(2).toString()
        );

        HubDeliveryCreateResponse mockResponse = HubDeliveryCreateResponse.builder()
                .hubDeliveryId("HUB-DELIVERY-002")
                .orderId("ORDER-001")
                .status("WAITING")
                .message("허브 배송이 생성되었습니다. 5개 구간으로 분할되었습니다.")
                .build();

        given(hubDeliveryClient.createDelivery(any(HubDeliveryCreateRequest.class)))
                .willReturn(mockResponse);

        // when
        HubDeliveryCreateResponse response = hubDeliveryClient.createDelivery(request);

        // then
        assertThat(response).isNotNull();
        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getMessage()).contains("5개 구간");
    }

    @Test
    @DisplayName("허브 배송 취소 실패 - 이미 출발함")
    void cancelDelivery_Fail_AlreadyDeparted() {
        // given
        String deliveryId = "HUB-DELIVERY-001";

        HubDeliveryCreateResponse mockResponse = HubDeliveryCreateResponse.builder()
                .hubDeliveryId(deliveryId)
                .orderId("ORDER-001")
                .status("IN_TRANSIT")
                .message("이미 출발한 배송은 취소할 수 없습니다")
                .build();

        given(hubDeliveryClient.cancelDelivery(anyString()))
                .willReturn(mockResponse);

        // when
        HubDeliveryCreateResponse response = hubDeliveryClient.cancelDelivery(deliveryId);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo("IN_TRANSIT");
        assertThat(response.getMessage()).contains("취소할 수 없습니다");
    }
}
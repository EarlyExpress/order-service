package com.early_express.order_service.domain.order.infrastructure.client.hub;

import com.early_express.order_service.domain.order.infrastructure.client.hub.dto.HubRouteCalculationRequest;
import com.early_express.order_service.domain.order.infrastructure.client.hub.dto.HubRouteCalculationResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@SpringBootTest(classes = {HubClient.class, HubClientConfig.class})
@DisplayName("HubClient 테스트")
class HubClientTest {

    @MockBean
    private HubClient hubClient;

    @Test
    @DisplayName("경로 계산 성공 - 허브 배송 필요")
    void calculateRoute_Success_RequiresHubDelivery() {
        // given
        HubRouteCalculationRequest request = HubRouteCalculationRequest.of(
                "ORDER-001",
                "HUB-001",
                "서울시 강남구 테헤란로 123",
                "2층 202호"
        );

        HubRouteCalculationResponse mockResponse = HubRouteCalculationResponse.builder()
                .orderId("ORDER-001")
                .originHubId("HUB-001")
                .destinationHubId("HUB-003")
                .routeHubs(List.of("HUB-001", "HUB-002", "HUB-003"))
                .requiresHubDelivery(true)
                .estimatedDistance(150.5)
                .routeInfoJson("{\"sections\": [{\"from\": \"HUB-001\", \"to\": \"HUB-002\", \"distance\": 80.2}]}")
                .build();

        given(hubClient.calculateRoute(any(HubRouteCalculationRequest.class)))
                .willReturn(mockResponse);

        // when
        HubRouteCalculationResponse response = hubClient.calculateRoute(request);

        // then
        assertThat(response).isNotNull();
        assertThat(response.needsHubDelivery()).isTrue();
        assertThat(response.getRouteHubs()).hasSize(3);
        assertThat(response.getHubCount()).isEqualTo(3);
        assertThat(response.getEstimatedDistance()).isEqualTo(150.5);
    }

    @Test
    @DisplayName("경로 계산 성공 - 허브 배송 불필요 (동일 허브)")
    void calculateRoute_Success_SameHub() {
        // given
        HubRouteCalculationRequest request = HubRouteCalculationRequest.of(
                "ORDER-001",
                "HUB-001",
                "서울시 강북구 노원로 456",
                "1층 101호"
        );

        HubRouteCalculationResponse mockResponse = HubRouteCalculationResponse.builder()
                .orderId("ORDER-001")
                .originHubId("HUB-001")
                .destinationHubId("HUB-001")
                .routeHubs(List.of("HUB-001"))
                .requiresHubDelivery(false)
                .estimatedDistance(0.0)
                .routeInfoJson("{\"sections\": []}")
                .build();

        given(hubClient.calculateRoute(any(HubRouteCalculationRequest.class)))
                .willReturn(mockResponse);

        // when
        HubRouteCalculationResponse response = hubClient.calculateRoute(request);

        // then
        assertThat(response).isNotNull();
        assertThat(response.needsHubDelivery()).isFalse();
        assertThat(response.getOriginHubId()).isEqualTo(response.getDestinationHubId());
        assertThat(response.getHubCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("경로 계산 성공 - 여러 허브 경유")
    void calculateRoute_Success_MultipleHubs() {
        // given
        HubRouteCalculationRequest request = HubRouteCalculationRequest.of(
                "ORDER-001",
                "HUB-001",
                "부산시 해운대구 센텀로 789",
                null
        );

        HubRouteCalculationResponse mockResponse = HubRouteCalculationResponse.builder()
                .orderId("ORDER-001")
                .originHubId("HUB-001")
                .destinationHubId("HUB-005")
                .routeHubs(List.of("HUB-001", "HUB-002", "HUB-003", "HUB-004", "HUB-005"))
                .requiresHubDelivery(true)
                .estimatedDistance(400.0)
                .routeInfoJson("{\"sections\": []}")
                .build();

        given(hubClient.calculateRoute(any(HubRouteCalculationRequest.class)))
                .willReturn(mockResponse);

        // when
        HubRouteCalculationResponse response = hubClient.calculateRoute(request);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getHubCount()).isEqualTo(5);
        assertThat(response.getRouteHubs())
                .containsExactly("HUB-001", "HUB-002", "HUB-003", "HUB-004", "HUB-005");
    }
}
package com.early_express.order_service.domain.order.infrastructure.client.lastmile;

import com.early_express.order_service.domain.order.infrastructure.client.lastmile.dto.LastMileDeliveryCreateRequest;
import com.early_express.order_service.domain.order.infrastructure.client.lastmile.dto.LastMileDeliveryCreateResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;

@SpringBootTest(classes = {LastMileClient.class, LastMileClientConfig.class})
@DisplayName("LastMileClient 테스트")
class LastMileClientTest {

    @MockBean
    private LastMileClient lastMileClient;

    @Test
    @DisplayName("업체 배송 생성 성공 - 담당자 즉시 배정")
    void createDelivery_Success_WithDriver() {
        // given
        LastMileDeliveryCreateRequest request = LastMileDeliveryCreateRequest.of(
                "ORDER-001",
                "HUB-002",
                "COMP-002",
                "서울시 강남구 테헤란로 123",
                "2층 202호",
                "홍길동",
                "010-1234-5678",
                "2025-01-22T15:00:00",
                "조심히 배송 부탁드립니다"
        );

        LastMileDeliveryCreateResponse mockResponse = LastMileDeliveryCreateResponse.builder()
                .lastMileDeliveryId("DELIVERY-001")
                .orderId("ORDER-001")
                .assignedDriverId("DRIVER-001")
                .assignedDriverName("김기사")
                .status("ASSIGNED")
                .message("배송 생성 및 담당자 배정 완료")
                .build();

        given(lastMileClient.createDelivery(any(LastMileDeliveryCreateRequest.class)))
                .willReturn(mockResponse);

        // when
        LastMileDeliveryCreateResponse response = lastMileClient.createDelivery(request);

        // then
        assertThat(response).isNotNull();
        assertThat(response.isSuccess()).isTrue();
        assertThat(response.hasAssignedDriver()).isTrue();
        assertThat(response.getLastMileDeliveryId()).isEqualTo("DELIVERY-001");
        assertThat(response.getAssignedDriverName()).isEqualTo("김기사");
    }

    @Test
    @DisplayName("업체 배송 생성 성공 - 담당자 미배정")
    void createDelivery_Success_WithoutDriver() {
        // given
        LastMileDeliveryCreateRequest request = LastMileDeliveryCreateRequest.of(
                "ORDER-001",
                "HUB-002",
                "COMP-002",
                "서울시 강남구 테헤란로 123",
                "2층 202호",
                "홍길동",
                "010-1234-5678",
                "2025-01-22T15:00:00",
                "조심히 배송 부탁드립니다"
        );

        LastMileDeliveryCreateResponse mockResponse = LastMileDeliveryCreateResponse.builder()
                .lastMileDeliveryId("DELIVERY-001")
                .orderId("ORDER-001")
                .assignedDriverId(null)
                .assignedDriverName(null)
                .status("PENDING")
                .message("배송 생성 완료, 담당자 배정 대기 중")
                .build();

        given(lastMileClient.createDelivery(any(LastMileDeliveryCreateRequest.class)))
                .willReturn(mockResponse);

        // when
        LastMileDeliveryCreateResponse response = lastMileClient.createDelivery(request);

        // then
        assertThat(response).isNotNull();
        assertThat(response.isSuccess()).isTrue();
        assertThat(response.hasAssignedDriver()).isFalse();
        assertThat(response.getStatus()).isEqualTo("PENDING");
    }

    @Test
    @DisplayName("업체 배송 취소 성공")
    void cancelDelivery_Success() {
        // given
        String deliveryId = "DELIVERY-001";

        LastMileDeliveryCreateResponse mockResponse = LastMileDeliveryCreateResponse.builder()
                .lastMileDeliveryId(deliveryId)
                .status("CANCELLED")
                .message("배송이 취소되었습니다")
                .build();

        given(lastMileClient.cancelDelivery(anyString()))
                .willReturn(mockResponse);

        // when
        LastMileDeliveryCreateResponse response = lastMileClient.cancelDelivery(deliveryId);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo("CANCELLED");
    }

    @Test
    @DisplayName("업체 배송 생성 실패")
    void createDelivery_Fail() {
        // given
        LastMileDeliveryCreateRequest request = LastMileDeliveryCreateRequest.of(
                "ORDER-001",
                "HUB-002",
                "COMP-002",
                "서울시 강남구 테헤란로 123",
                "2층 202호",
                "홍길동",
                "010-1234-5678",
                "2025-01-22T15:00:00",
                null
        );

        LastMileDeliveryCreateResponse mockResponse = LastMileDeliveryCreateResponse.builder()
                .lastMileDeliveryId(null)
                .orderId("ORDER-001")
                .status("FAILED")
                .message("배송 생성 실패: 담당자를 찾을 수 없습니다")
                .build();

        given(lastMileClient.createDelivery(any(LastMileDeliveryCreateRequest.class)))
                .willReturn(mockResponse);

        // when
        LastMileDeliveryCreateResponse response = lastMileClient.createDelivery(request);

        // then
        assertThat(response).isNotNull();
        assertThat(response.isSuccess()).isFalse();
        assertThat(response.getStatus()).isEqualTo("FAILED");
    }
}
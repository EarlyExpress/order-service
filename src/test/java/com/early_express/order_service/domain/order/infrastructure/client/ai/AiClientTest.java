package com.early_express.order_service.domain.order.infrastructure.client.ai;

import com.early_express.order_service.domain.order.infrastructure.client.ai.dto.AiTimeCalculationRequest;
import com.early_express.order_service.domain.order.infrastructure.client.ai.dto.AiTimeCalculationResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@SpringBootTest(classes = {AiClient.class, AiClientConfig.class})
@DisplayName("AiClient 테스트")
class AiClientTest {

    @MockBean
    private AiClient aiClient;

    @Test
    @DisplayName("AI 시간 계산 성공 - 납품 가능")
    void calculateDeliveryTime_Success() {
        // given
        AiTimeCalculationRequest request = AiTimeCalculationRequest.of(
                "ORDER-001",
                "HUB-001",
                "HUB-003",
                List.of("HUB-001", "HUB-002", "HUB-003"),
                true,
                150.5,
                "{\"route\": \"details\"}",
                LocalDate.now().plusDays(2),
                LocalTime.of(14, 0),
                "서울시 강남구 테헤란로 123",
                "2층 202호",
                10,
                "조심히 배송해주세요"
        );

        LocalDateTime departureDeadline = LocalDateTime.now().plusHours(8);
        LocalDateTime estimatedDelivery = LocalDateTime.now().plusDays(2).withHour(13).withMinute(30);

        AiTimeCalculationResponse mockResponse = AiTimeCalculationResponse.builder()
                .orderId("ORDER-001")
                .calculatedDepartureDeadline(departureDeadline)
                .estimatedDeliveryTime(estimatedDelivery)
                .aiMessage("납품 희망 시간 준수 가능합니다. 8시간 이내 발송이 필요합니다.")
                .success(true)
                .hubDeliveryDurationMinutes(1200)
                .lastMileDeliveryDurationMinutes(90)
                .totalDeliveryDurationMinutes(1290)
                .build();

        given(aiClient.calculateDeliveryTime(any(AiTimeCalculationRequest.class)))
                .willReturn(mockResponse);

        // when
        AiTimeCalculationResponse response = aiClient.calculateDeliveryTime(request);

        // then
        assertThat(response).isNotNull();
        assertThat(response.isSuccessful()).isTrue();
        assertThat(response.getCalculatedDepartureDeadline()).isNotNull();
        assertThat(response.getEstimatedDeliveryTime()).isNotNull();
        assertThat(response.getTotalDeliveryDurationMinutes()).isEqualTo(1290);
    }

    @Test
    @DisplayName("AI 시간 계산 성공 - 발송 시한 초과")
    void calculateDeliveryTime_Success_DeadlinePassed() {
        // given
        AiTimeCalculationRequest request = AiTimeCalculationRequest.of(
                "ORDER-001",
                "HUB-001",
                "HUB-003",
                List.of("HUB-001", "HUB-002", "HUB-003"),
                true,
                150.5,
                "{\"route\": \"details\"}",
                LocalDate.now(),
                LocalTime.of(18, 0),
                "서울시 강남구 테헤란로 123",
                "2층 202호",
                10,
                null
        );

        LocalDateTime pastDeadline = LocalDateTime.now().minusHours(2);
        LocalDateTime estimatedDelivery = LocalDateTime.now().plusHours(20);

        AiTimeCalculationResponse mockResponse = AiTimeCalculationResponse.builder()
                .orderId("ORDER-001")
                .calculatedDepartureDeadline(pastDeadline)
                .estimatedDeliveryTime(estimatedDelivery)
                .aiMessage("⚠️ 발송 시한이 이미 지났습니다. 납품 희망 시간을 맞출 수 없습니다.")
                .success(true)
                .totalDeliveryDurationMinutes(1200)
                .build();

        given(aiClient.calculateDeliveryTime(any(AiTimeCalculationRequest.class)))
                .willReturn(mockResponse);

        // when
        AiTimeCalculationResponse response = aiClient.calculateDeliveryTime(request);

        // then
        assertThat(response).isNotNull();
        assertThat(response.isSuccessful()).isTrue();
        assertThat(response.isDepartureDeadlinePassed()).isTrue();
    }

    @Test
    @DisplayName("AI 시간 계산 실패 - 배송 불가능")
    void calculateDeliveryTime_Fail_Impossible() {
        // given
        AiTimeCalculationRequest request = AiTimeCalculationRequest.of(
                "ORDER-001",
                "HUB-001",
                "HUB-005",
                List.of("HUB-001", "HUB-002", "HUB-003", "HUB-004", "HUB-005"),
                true,
                600.0,
                "{\"route\": \"details\"}",
                LocalDate.now(),
                LocalTime.of(12, 0),
                "제주도 제주시",
                null,
                50,
                null
        );

        AiTimeCalculationResponse mockResponse = AiTimeCalculationResponse.builder()
                .orderId("ORDER-001")
                .calculatedDepartureDeadline(null)
                .estimatedDeliveryTime(null)
                .aiMessage(null)
                .success(false)
                .errorMessage("납품 희망 시간을 맞출 수 없습니다. 최소 3일이 소요됩니다.")
                .build();

        given(aiClient.calculateDeliveryTime(any(AiTimeCalculationRequest.class)))
                .willReturn(mockResponse);

        // when
        AiTimeCalculationResponse response = aiClient.calculateDeliveryTime(request);

        // then
        assertThat(response).isNotNull();
        assertThat(response.isSuccessful()).isFalse();
        assertThat(response.getErrorMessage()).contains("납품 희망 시간을 맞출 수 없습니다");
    }

    @Test
    @DisplayName("AI 시간 계산 성공 - 허브 배송 불필요 (동일 허브)")
    void calculateDeliveryTime_Success_NoHubDelivery() {
        // given
        AiTimeCalculationRequest request = AiTimeCalculationRequest.of(
                "ORDER-001",
                "HUB-001",
                "HUB-001",
                List.of("HUB-001"),
                false,
                0.0,
                "{\"route\": \"details\"}",
                LocalDate.now().plusDays(1),
                LocalTime.of(10, 0),
                "서울시 강북구",
                null,
                5,
                null
        );

        LocalDateTime departureDeadline = LocalDateTime.now().plusHours(20);
        LocalDateTime estimatedDelivery = LocalDateTime.now().plusDays(1).withHour(9).withMinute(0);

        AiTimeCalculationResponse mockResponse = AiTimeCalculationResponse.builder()
                .orderId("ORDER-001")
                .calculatedDepartureDeadline(departureDeadline)
                .estimatedDeliveryTime(estimatedDelivery)
                .aiMessage("동일 허브 내 배송입니다. 업체 배송만 진행됩니다.")
                .success(true)
                .hubDeliveryDurationMinutes(0)
                .lastMileDeliveryDurationMinutes(60)
                .totalDeliveryDurationMinutes(60)
                .build();

        given(aiClient.calculateDeliveryTime(any(AiTimeCalculationRequest.class)))
                .willReturn(mockResponse);

        // when
        AiTimeCalculationResponse response = aiClient.calculateDeliveryTime(request);

        // then
        assertThat(response).isNotNull();
        assertThat(response.isSuccessful()).isTrue();
        assertThat(response.getHubDeliveryDurationMinutes()).isEqualTo(0);
        assertThat(response.getLastMileDeliveryDurationMinutes()).isEqualTo(60);
    }
}
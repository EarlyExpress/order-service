package com.early_express.order_service.domain.order.domain.model.vo;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.*;

@DisplayName("AiCalculationResult vo 테스트")
class AiCalculationResultTest {

    @Test
    @DisplayName("빈 AI 계산 결과를 생성할 수 있다")
    void createEmptyAiCalculationResult() {
        // when
        AiCalculationResult result = AiCalculationResult.empty();

        // then
        assertThat(result.hasRouteInfo()).isFalse();
        assertThat(result.isCalculated()).isFalse();
        assertThat(result.requiresHubDelivery()).isFalse();
    }

    @Test
    @DisplayName("경로 정보만 저장할 수 있다")
    void createWithRouteOnly() {
        // given
        String routeInfoJson = "{\"hubs\":[\"HUB-001\",\"HUB-002\"]}";

        // when
        AiCalculationResult result = AiCalculationResult.withRouteOnly(routeInfoJson);

        // then
        assertThat(result.hasRouteInfo()).isTrue();
        assertThat(result.getRouteInfo()).isEqualTo(routeInfoJson);
        assertThat(result.isCalculated()).isFalse();
    }

    @Test
    @DisplayName("완전한 AI 계산 결과를 생성할 수 있다")
    void createCompleteResult() {
        // given
        LocalDateTime departureDeadline = LocalDateTime.now().plusHours(2);
        LocalDateTime estimatedDeliveryTime = LocalDateTime.now().plusHours(24);
        String routeInfoJson = "{\"hubs\":[\"HUB-001\",\"HUB-002\"]}";

        // when
        AiCalculationResult result = AiCalculationResult.of(
                departureDeadline,
                estimatedDeliveryTime,
                routeInfoJson
        );

        // then
        assertThat(result.isCalculated()).isTrue();
        assertThat(result.getCalculatedDepartureDeadline()).isEqualTo(departureDeadline);
        assertThat(result.getEstimatedDeliveryTime()).isEqualTo(estimatedDeliveryTime);
        assertThat(result.hasRouteInfo()).isTrue();
    }

    @Test
    @DisplayName("AI 계산 결과를 추가할 수 있다")
    void addAiCalculation() {
        // given
        AiCalculationResult result = AiCalculationResult.withRouteOnly("{\"hubs\":[\"HUB-001\"]}");
        LocalDateTime departureDeadline = LocalDateTime.now().plusHours(2);
        LocalDateTime estimatedDeliveryTime = LocalDateTime.now().plusHours(24);
        String aiMessage = "계산 완료";

        // when
        AiCalculationResult updated = result.withAiCalculation(
                departureDeadline,
                estimatedDeliveryTime,
                aiMessage
        );

        // then
        assertThat(updated.isCalculated()).isTrue();
        assertThat(updated.getAiMessage()).isEqualTo(aiMessage);
    }

    @Test
    @DisplayName("허브가 2개 이상이면 허브 배송이 필요하다")
    void requiresHubDeliveryWithMultipleHubs() {
        // given
        String routeInfoJson = "{\"hubs\":[\"HUB-001\",\"HUB-002\"]}";
        AiCalculationResult result = AiCalculationResult.withRouteOnly(routeInfoJson);

        // when & then
        assertThat(result.requiresHubDelivery()).isTrue();
    }

    @Test
    @DisplayName("허브가 1개면 허브 배송이 불필요하다")
    void noHubDeliveryWithSingleHub() {
        // given
        String routeInfoJson = "{\"hubs\":[\"HUB-001\"]}";
        AiCalculationResult result = AiCalculationResult.withRouteOnly(routeInfoJson);

        // when & then
        assertThat(result.requiresHubDelivery()).isFalse();
    }

    @Test
    @DisplayName("경로 정보가 없으면 허브 배송이 불필요하다")
    void noHubDeliveryWithoutRoute() {
        // given
        AiCalculationResult result = AiCalculationResult.empty();

        // when & then
        assertThat(result.requiresHubDelivery()).isFalse();
    }

    @Test
    @DisplayName("잘못된 JSON 형식이면 허브 배송 불필요로 처리된다")
    void handleInvalidJson() {
        // given
        String invalidJson = "invalid json";
        AiCalculationResult result = AiCalculationResult.withRouteOnly(invalidJson);

        // when & then
        assertThat(result.requiresHubDelivery()).isFalse();
    }

    @Test
    @DisplayName("발송 시한이 지났는지 확인할 수 있다")
    void checkDepartureDeadlinePassed() {
        // given
        LocalDateTime pastDeadline = LocalDateTime.now().minusHours(1);
        AiCalculationResult result = AiCalculationResult.of(
                pastDeadline,
                LocalDateTime.now().plusHours(24),
                "{}"
        );

        // when & then
        assertThat(result.isDepartureDeadlinePassed()).isTrue();
    }

    @Test
    @DisplayName("발송 시한이 아직 남아있는지 확인할 수 있다")
    void checkDepartureDeadlineNotPassed() {
        // given
        LocalDateTime futureDeadline = LocalDateTime.now().plusHours(2);
        AiCalculationResult result = AiCalculationResult.of(
                futureDeadline,
                LocalDateTime.now().plusHours(24),
                "{}"
        );

        // when & then
        assertThat(result.isDepartureDeadlinePassed()).isFalse();
    }

    @Test
    @DisplayName("발송 시한이 없으면 지나지 않은 것으로 처리된다")
    void departureDeadlineNotPassedWhenNull() {
        // given
        AiCalculationResult result = AiCalculationResult.empty();

        // when & then
        assertThat(result.isDepartureDeadlinePassed()).isFalse();
    }

    @Test
    @DisplayName("같은 값을 가진 AiCalculationResult는 동일하다")
    void equalAiCalculationResults() {
        // given
        String routeInfo = "{\"hubs\":[\"HUB-001\"]}";
        AiCalculationResult result1 = AiCalculationResult.withRouteOnly(routeInfo);
        AiCalculationResult result2 = AiCalculationResult.withRouteOnly(routeInfo);

        // when & then
        assertThat(result1).isEqualTo(result2);
        assertThat(result1.hashCode()).isEqualTo(result2.hashCode());
    }
}
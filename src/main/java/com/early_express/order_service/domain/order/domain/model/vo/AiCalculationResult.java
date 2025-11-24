package com.early_express.order_service.domain.order.domain.model.vo;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;

/**
 * AI 계산 결과 Value Object
 * Hub Service의 경로 정보 + AI의 시간 계산 결과
 */
@Slf4j
@Getter
@EqualsAndHashCode
public class AiCalculationResult {

    private final String routeInfoJson; // Hub Service에서 계산한 경로 JSON
    private final LocalDateTime calculatedDepartureDeadline; // AI가 계산한 발송 시한
    private final LocalDateTime estimatedDeliveryTime; // AI가 계산한 예상 도착 시간
    private final String aiMessage; // AI의 판단 근거 메시지

    @Builder
    private AiCalculationResult(
            String routeInfoJson,
            LocalDateTime calculatedDepartureDeadline,
            LocalDateTime estimatedDeliveryTime,
            String aiMessage) {

        this.routeInfoJson = routeInfoJson;
        this.calculatedDepartureDeadline = calculatedDepartureDeadline;
        this.estimatedDeliveryTime = estimatedDeliveryTime;
        this.aiMessage = aiMessage;
    }

    /**
     * 초기 상태 (계산 전)
     */
    public static AiCalculationResult empty() {
        return AiCalculationResult.builder().build();
    }

    /**
     * 경로 정보만 저장 (Hub Service 응답)
     */
    public static AiCalculationResult withRouteOnly(String routeInfoJson) {
        return AiCalculationResult.builder()
                .routeInfoJson(routeInfoJson)
                .build();
    }

    /**
     * 완전한 AI 계산 결과 생성 (모든 필드)
     * OrderEntity에서 도메인 모델로 변환 시 사용
     */
    public static AiCalculationResult of(
            LocalDateTime calculatedDepartureDeadline,
            LocalDateTime estimatedDeliveryTime,
            String routeInfoJson) {

        return AiCalculationResult.builder()
                .routeInfoJson(routeInfoJson)
                .calculatedDepartureDeadline(calculatedDepartureDeadline)
                .estimatedDeliveryTime(estimatedDeliveryTime)
                .build();
    }

    /**
     * AI 계산 결과 추가 (AI Service 응답)
     */
    public AiCalculationResult withAiCalculation(
            LocalDateTime departureDeadline,
            LocalDateTime estimatedDeliveryTime,
            String aiMessage) {

        return AiCalculationResult.builder()
                .routeInfoJson(this.routeInfoJson)
                .calculatedDepartureDeadline(departureDeadline)
                .estimatedDeliveryTime(estimatedDeliveryTime)
                .aiMessage(aiMessage)
                .build();
    }

    /**
     * 경로 정보에서 허브 배송 필요 여부 판단
     * JSON 파싱하여 거쳐야 할 허브가 2개 이상인지 확인
     */
    public boolean requiresHubDelivery() {
        if (routeInfoJson == null || routeInfoJson.trim().isEmpty()) {
            return false;
        }

        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(routeInfoJson);
            JsonNode hubs = root.get("hubs");

            if (hubs == null || !hubs.isArray()) {
                return false;
            }

            // 허브가 2개 이상이면 허브 배송 필요
            // 예: ["HUB-001", "HUB-002"] -> 출발 허브에서 도착 허브로 이동 필요
            return hubs.size() >= 2;

        } catch (Exception e) {
            log.warn("경로 정보 파싱 실패, 허브 배송 불필요로 처리 - routeInfo: {}",
                    routeInfoJson, e);
            return false;
        }
    }

    /**
     * 경로 정보가 있는지 확인
     */
    public boolean hasRouteInfo() {
        return routeInfoJson != null && !routeInfoJson.trim().isEmpty();
    }

    /**
     * AI 계산이 완료되었는지 확인
     */
    public boolean isCalculated() {
        return calculatedDepartureDeadline != null && estimatedDeliveryTime != null;
    }

    /**
     * 발송 시한이 지났는지 확인
     */
    public boolean isDepartureDeadlinePassed() {
        if (calculatedDepartureDeadline == null) {
            return false;
        }
        return LocalDateTime.now().isAfter(calculatedDepartureDeadline);
    }

    /**
     * 경로 정보 반환
     * OrderEntity에서 사용
     */
    public String getRouteInfo() {
        return this.routeInfoJson;
    }
}
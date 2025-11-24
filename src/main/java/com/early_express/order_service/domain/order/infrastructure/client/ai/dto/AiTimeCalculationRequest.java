package com.early_express.order_service.domain.order.infrastructure.client.ai.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
 * AI 시간 계산 요청 DTO
 * Hub Service의 경로 계산 결과를 기반으로 발송 시한 및 배송 시간 계산
 */
@Getter
@Builder
public class AiTimeCalculationRequest {

    /**
     * 주문 ID (추적용)
     */
    @NotBlank(message = "주문 ID는 필수입니다.")
    private String orderId;

    /**
     * 출발 허브 ID
     */
    @NotBlank(message = "출발 허브 ID는 필수입니다.")
    private String originHubId;

    /**
     * 도착 허브 ID
     */
    @NotBlank(message = "도착 허브 ID는 필수입니다.")
    private String destinationHubId;

    /**
     * 거쳐야 할 허브 목록 (순서대로)
     */
    @NotNull(message = "경로 허브 목록은 필수입니다.")
    private List<String> routeHubs;

    /**
     * 허브 배송 필요 여부
     */
    @NotNull(message = "허브 배송 필요 여부는 필수입니다.")
    private Boolean requiresHubDelivery;

    /**
     * 총 예상 거리 (km)
     */
    @NotNull(message = "예상 거리는 필수입니다.")
    private Double estimatedDistance;

    /**
     * 경로 상세 정보 JSON
     */
    private String routeInfoJson;

    /**
     * 납품 희망 일자
     */
    @NotNull(message = "납품 희망 일자는 필수입니다.")
    private LocalDate requestedDeliveryDate;

    /**
     * 납품 희망 시간
     */
    @NotNull(message = "납품 희망 시간은 필수입니다.")
    private LocalTime requestedDeliveryTime;

    /**
     * 배송 주소
     */
    @NotBlank(message = "배송 주소는 필수입니다.")
    private String deliveryAddress;

    /**
     * 배송 상세 주소
     */
    private String deliveryAddressDetail;

    /**
     * 상품 수량
     */
    @NotNull(message = "상품 수량은 필수입니다.")
    private Integer quantity;

    /**
     * 특별 요청사항
     */
    private String specialInstructions;

    /**
     * 헬퍼 메서드 - Hub 응답 + 주문 정보로 생성
     */
    public static AiTimeCalculationRequest of(
            String orderId,
            String originHubId,
            String destinationHubId,
            List<String> routeHubs,
            Boolean requiresHubDelivery,
            Double estimatedDistance,
            String routeInfoJson,
            LocalDate requestedDeliveryDate,
            LocalTime requestedDeliveryTime,
            String deliveryAddress,
            String deliveryAddressDetail,
            Integer quantity,
            String specialInstructions) {

        return AiTimeCalculationRequest.builder()
                .orderId(orderId)
                .originHubId(originHubId)
                .destinationHubId(destinationHubId)
                .routeHubs(routeHubs)
                .requiresHubDelivery(requiresHubDelivery)
                .estimatedDistance(estimatedDistance)
                .routeInfoJson(routeInfoJson)
                .requestedDeliveryDate(requestedDeliveryDate)
                .requestedDeliveryTime(requestedDeliveryTime)
                .deliveryAddress(deliveryAddress)
                .deliveryAddressDetail(deliveryAddressDetail)
                .quantity(quantity)
                .specialInstructions(specialInstructions)
                .build();
    }
}
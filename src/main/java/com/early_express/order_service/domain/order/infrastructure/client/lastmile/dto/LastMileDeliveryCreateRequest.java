package com.early_express.order_service.domain.order.infrastructure.client.lastmile.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Getter;

/**
 * 업체 배송 생성 요청 DTO
 * Order Service → Last Mile Delivery Service
 */
@Getter
@Builder
public class LastMileDeliveryCreateRequest {

    /**
     * 주문 ID (추적용)
     */
    @NotBlank(message = "주문 ID는 필수입니다.")
    private String orderId;

    /**
     * 도착 허브 ID (배송 출발 허브)
     * - 허브 배송이 완료된 후 업체 배송이 시작되는 허브
     */
    @NotBlank(message = "도착 허브 ID는 필수입니다.")
    private String destinationHubId;

    /**
     * 수령 업체 ID
     */
    @NotBlank(message = "수령 업체 ID는 필수입니다.")
    private String receiverCompanyId;

    /**
     * 받는 곳 주소
     */
    @NotBlank(message = "받는 곳 주소는 필수입니다.")
    private String receiverAddress;

    /**
     * 받는 곳 상세 주소
     */
    private String receiverAddressDetail;

    /**
     * 받는 사람 이름
     */
    @NotBlank(message = "받는 사람 이름은 필수입니다.")
    private String receiverName;

    /**
     * 받는 사람 전화번호
     */
    @NotBlank(message = "받는 사람 전화번호는 필수입니다.")
    private String receiverPhone;

    /**
     * 예상 도착 시간 (AI 계산 결과)
     * ISO 8601 형식: "2025-12-12T15:00:00"
     */
    private String expectedArrivalTime;

    /**
     * 특이사항/요청사항
     */
    private String specialInstructions;

    /**
     * 헬퍼 메서드 - Order 도메인에서 생성
     */
    public static LastMileDeliveryCreateRequest of(
            String orderId,
            String destinationHubId,
            String receiverCompanyId,
            String receiverAddress,
            String receiverAddressDetail,
            String receiverName,
            String receiverPhone,
            String expectedArrivalTime,
            String specialInstructions) {

        return LastMileDeliveryCreateRequest.builder()
                .orderId(orderId)
                .destinationHubId(destinationHubId)
                .receiverCompanyId(receiverCompanyId)
                .receiverAddress(receiverAddress)
                .receiverAddressDetail(receiverAddressDetail)
                .receiverName(receiverName)
                .receiverPhone(receiverPhone)
                .expectedArrivalTime(expectedArrivalTime)
                .specialInstructions(specialInstructions)
                .build();
    }
}
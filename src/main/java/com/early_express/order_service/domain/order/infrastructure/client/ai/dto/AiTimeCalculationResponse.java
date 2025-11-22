package com.early_express.order_service.domain.order.infrastructure.client.ai.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * AI 시간 계산 응답 DTO
 * AI가 계산한 발송 시한 및 예상 배송 시간
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiTimeCalculationResponse {

    /**
     * 주문 ID
     */
    private String orderId;

    /**
     * AI가 계산한 발송 시한
     * - 이 시간까지 발송해야 납품 희망 시간 준수 가능
     */
    private LocalDateTime calculatedDepartureDeadline;

    /**
     * AI가 계산한 예상 배송 완료 시간
     * - 발송 시한에 출발했을 때의 예상 도착 시간
     */
    private LocalDateTime estimatedDeliveryTime;

    /**
     * AI의 판단 근거 메시지
     * - 계산 로직 설명, 고려 사항 등
     */
    private String aiMessage;

    /**
     * 계산 성공 여부
     */
    private Boolean success;

    /**
     * 에러 메시지 (실패 시)
     */
    private String errorMessage;

    /**
     * 허브 배송 예상 소요 시간 (분)
     * - 선택적 정보
     */
    private Integer hubDeliveryDurationMinutes;

    /**
     * 업체 배송 예상 소요 시간 (분)
     * - 선택적 정보
     */
    private Integer lastMileDeliveryDurationMinutes;

    /**
     * 총 예상 배송 시간 (분)
     */
    private Integer totalDeliveryDurationMinutes;

    /**
     * 계산 성공 확인
     */
    public boolean isSuccessful() {
        return Boolean.TRUE.equals(success)
                && calculatedDepartureDeadline != null
                && estimatedDeliveryTime != null;
    }

    /**
     * 발송 시한 초과 여부 확인
     */
    public boolean isDepartureDeadlinePassed() {
        if (calculatedDepartureDeadline == null) {
            return false;
        }
        return LocalDateTime.now().isAfter(calculatedDepartureDeadline);
    }
}

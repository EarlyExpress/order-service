package com.early_express.order_service.domain.order.presentation.web.common.dto.response;

import com.early_express.order_service.domain.order.domain.model.vo.AiCalculationResult;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * AI 계산 결과 DTO
 */
@Getter
@Builder
public class AiCalculationResultDto {

    private String routeInfoJson;
    private LocalDateTime calculatedDepartureDeadline;
    private LocalDateTime estimatedDeliveryTime;
    private String aiMessage;

    public static AiCalculationResultDto from(AiCalculationResult aiResult) {
        return AiCalculationResultDto.builder()
                .routeInfoJson(aiResult.getRouteInfoJson())
                .calculatedDepartureDeadline(aiResult.getCalculatedDepartureDeadline())
                .estimatedDeliveryTime(aiResult.getEstimatedDeliveryTime())
                .aiMessage(aiResult.getAiMessage())
                .build();
    }
}
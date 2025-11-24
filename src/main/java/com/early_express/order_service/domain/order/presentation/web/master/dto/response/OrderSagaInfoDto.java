package com.early_express.order_service.domain.order.presentation.web.master.dto.response;

import com.early_express.order_service.domain.order.domain.model.OrderSaga;
import com.early_express.order_service.domain.order.domain.model.SagaStatus;
import com.early_express.order_service.domain.order.domain.model.SagaStep;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Saga 정보 DTO (Master 전용)
 * 시스템 관리자가 Saga 진행 상태를 모니터링하기 위한 정보
 */
@Getter
@Builder
public class OrderSagaInfoDto {

    private String sagaId;
    private String orderId;
    private SagaStatus status;
    private String statusDescription;
    private SagaStep currentStep;
    private String currentStepDescription;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
    private String failureReason;
    private List<SagaStepHistoryDto> stepHistory;

    /**
     * Domain → DTO 변환
     */
    public static OrderSagaInfoDto from(OrderSaga saga) {
        return OrderSagaInfoDto.builder()
                .sagaId(saga.getSagaIdValue())
                .orderId(saga.getOrderIdValue())
                .status(saga.getStatus())
                .statusDescription(saga.getStatus().getDescription())
                .currentStep(saga.getCurrentStep())
                .currentStepDescription(saga.getCurrentStep() != null ?
                        saga.getCurrentStep().getDescription() : null)
                .startedAt(saga.getStartedAt())
                .completedAt(saga.getCompletedAt())
                .failureReason(saga.getFailureReason())
                .stepHistory(saga.getStepHistory().stream()
                        .map(SagaStepHistoryDto::from)
                        .toList())
                .build();
    }

    /**
     * Saga Step History DTO
     */
    @Getter
    @Builder
    public static class SagaStepHistoryDto {
        private SagaStep step;
        private String stepDescription;
        private String status;
        private String statusDescription;
        private String errorMessage;
        private LocalDateTime startedAt;
        private LocalDateTime completedAt;
        private Integer retryCount;

        public static SagaStepHistoryDto from(
                com.early_express.order_service.domain.order.domain.model.SagaStepHistory history) {
            return SagaStepHistoryDto.builder()
                    .step(history.getStep())
                    .stepDescription(history.getStep().getDescription())
                    .status(history.getStatus().name())
                    .statusDescription(history.getStatus().getDescription())
                    .errorMessage(history.getErrorMessage())
                    .startedAt(history.getStartedAt())
                    .completedAt(history.getCompletedAt())
                    .retryCount(history.getRetryCount())
                    .build();
        }
    }
}
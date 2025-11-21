package com.early_express.order_service.domain.order.domain.model;

import com.early_express.order_service.domain.order.domain.exception.OrderErrorCode;
import com.early_express.order_service.domain.order.domain.exception.SagaException;
import com.early_express.order_service.domain.order.domain.model.vo.CompensationData;
import com.early_express.order_service.domain.order.domain.model.vo.OrderId;
import com.early_express.order_service.domain.order.domain.model.vo.SagaId;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * OrderSaga Aggregate Root
 * Saga 패턴의 오케스트레이션을 관리하는 핵심 도메인 모델
 */
@Getter
public class OrderSaga {

    private final SagaId sagaId;
    private final OrderId orderId;

    private SagaStatus status;
    private SagaStep currentStep;

    private CompensationData compensationData;

    private final LocalDateTime startedAt;
    private LocalDateTime completedAt;
    private String failureReason;

    private final List<SagaStepHistory> stepHistory;

    @Builder
    private OrderSaga(
            SagaId sagaId,
            OrderId orderId,
            SagaStatus status,
            SagaStep currentStep,
            CompensationData compensationData,
            LocalDateTime startedAt,
            LocalDateTime completedAt,
            String failureReason,
            List<SagaStepHistory> stepHistory) {

        this.sagaId = sagaId;
        this.orderId = orderId;
        this.status = status;
        this.currentStep = currentStep;
        this.compensationData = compensationData;
        this.startedAt = startedAt;
        this.completedAt = completedAt;
        this.failureReason = failureReason;
        this.stepHistory = stepHistory != null ? new ArrayList<>(stepHistory) : new ArrayList<>();
    }

    /**
     * 새로운 Saga 생성 (팩토리 메서드)
     */
    public static OrderSaga create(OrderId orderId) {
        return OrderSaga.builder()
                .sagaId(SagaId.create())
                .orderId(orderId)
                .status(SagaStatus.PENDING)
                .compensationData(CompensationData.empty())
                .startedAt(LocalDateTime.now())
                .stepHistory(new ArrayList<>())
                .build();
    }

    // ===== Saga 상태 관리 메서드 =====

    /**
     * Saga 시작
     */
    public void start() {
        validateStatus(SagaStatus.PENDING, "Saga 시작");

        this.status = SagaStatus.IN_PROGRESS;
        this.currentStep = SagaStep.STOCK_RESERVE;
    }

    /**
     * Step 시작
     */
    public void startStep(SagaStep step) {
        validateNotFinalState("Step 시작");

        this.currentStep = step;

        SagaStepHistory history = SagaStepHistory.create(this.sagaId, step);
        this.stepHistory.add(history);
    }

    /**
     * Step 성공 처리
     */
    public void completeStep(SagaStep step, Object stepData) {
        validateCurrentStep(step, "Step 완료");

        // Step History 업데이트
        SagaStepHistory history = findStepHistory(step);
        history.complete(stepData);

        // 보상 데이터 저장 (필요한 경우)
        if (step.isNeedsCompensation()) {
            this.compensationData = this.compensationData.addStepData(
                    step.name(),
                    stepData
            );
        }

        // 마지막 Step이면 Saga 완료
        if (step.isLastStep()) {
            this.complete();
        } else {
            this.currentStep = step.getNextStep();
        }
    }

    /**
     * Step History 추가 (Step 완료 없이 히스토리만 기록)
     * AI 계산 완료 시 사용 - Step은 이미 완료되었지만 추가 정보를 기록할 때
     */
    public void addStepHistory(SagaStep step, Object stepData) {
        // 기존 히스토리가 있는지 확인
        boolean historyExists = this.stepHistory.stream()
                .anyMatch(history -> history.getStep() == step);

        if (!historyExists) {
            // 히스토리가 없으면 새로 생성
            SagaStepHistory history = SagaStepHistory.create(this.sagaId, step);
            history.complete(stepData);
            this.stepHistory.add(history);
        } else {
            // 히스토리가 있으면 마지막 것에 데이터만 추가
            SagaStepHistory history = findStepHistory(step);
            if (history.getStatus() != StepStatus.SUCCESS) {
                history.complete(stepData);
            }
        }

        // 보상 데이터 저장 (필요한 경우)
        if (step.isNeedsCompensation()) {
            this.compensationData = this.compensationData.addStepData(
                    step.name(),
                    stepData
            );
        }
    }

    /**
     * Step 실패 처리
     */
    public void failStep(SagaStep step, String errorMessage) {
        validateCurrentStep(step, "Step 실패");

        // Step History 업데이트
        SagaStepHistory history = findStepHistory(step);
        history.fail(errorMessage);

        // Best Effort Step은 실패해도 계속 진행
        if (step.isBestEffort()) {
            if (step.isLastStep()) {
                this.complete();
            } else {
                this.currentStep = step.getNextStep();
            }
        } else {
            // 필수 Step 실패 시 보상 트랜잭션 시작
            this.startCompensation(errorMessage);
        }
    }

    /**
     * 보상 트랜잭션 시작
     */
    public void startCompensation(String failureReason) {
        this.status = SagaStatus.COMPENSATING;
        this.failureReason = failureReason;
    }

    /**
     * 보상 Step 실행
     */
    public void executeCompensation(SagaStep originalStep, SagaStep compensationStep) {
        if (this.status != SagaStatus.COMPENSATING) {
            throw new SagaException(
                    OrderErrorCode.SAGA_STATE_MISMATCH,
                    "보상 실행은 COMPENSATING 상태에서만 가능합니다."
            );
        }

        SagaStepHistory history = SagaStepHistory.create(this.sagaId, compensationStep);
        this.stepHistory.add(history);
    }

    /**
     * 보상 Step 완료
     */
    public void completeCompensation(SagaStep compensationStep) {
        SagaStepHistory history = findStepHistory(compensationStep);
        history.complete(null);
    }

    /**
     * 보상 Step 실패
     */
    public void failCompensation(SagaStep compensationStep, String errorMessage) {
        SagaStepHistory history = findStepHistory(compensationStep);
        history.fail(errorMessage);

        this.status = SagaStatus.COMPENSATION_FAILED;
        this.completedAt = LocalDateTime.now();
    }

    /**
     * 모든 보상 완료
     */
    public void completeAllCompensations() {
        this.status = SagaStatus.COMPENSATED;
        this.completedAt = LocalDateTime.now();
    }

    /**
     * Saga 완료
     */
    public void complete() {
        validateStatus(SagaStatus.IN_PROGRESS, "Saga 완료");

        this.status = SagaStatus.COMPLETED;
        this.completedAt = LocalDateTime.now();
    }

    /**
     * Saga 실패
     */
    public void fail(String failureReason) {
        this.status = SagaStatus.FAILED;
        this.failureReason = failureReason;
        this.completedAt = LocalDateTime.now();
    }

    // ===== 조회 메서드 =====

    /**
     * 완료된 Step 목록 조회 (보상이 필요한 것만)
     */
    public List<SagaStep> getCompletedStepsNeedingCompensation() {
        return this.stepHistory.stream()
                .filter(history -> history.getStatus() == StepStatus.SUCCESS)
                .map(SagaStepHistory::getStep)
                .filter(SagaStep::isNeedsCompensation)
                .toList();
    }

    /**
     * 특정 Step의 보상 데이터 조회
     */
    public Object getCompensationDataForStep(SagaStep step) {
        return this.compensationData.getStepData(step.name());
    }

    /**
     * Step History 조회
     */
    private SagaStepHistory findStepHistory(SagaStep step) {
        return this.stepHistory.stream()
                .filter(history -> history.getStep() == step)
                .reduce((first, second) -> second) // 가장 최근 것
                .orElseThrow(() -> new SagaException(
                        OrderErrorCode.SAGA_STEP_FAILED,
                        "Step History를 찾을 수 없습니다: " + step.getDescription()
                ));
    }

    /**
     * Saga가 진행 중인지 확인
     */
    public boolean isInProgress() {
        return this.status == SagaStatus.IN_PROGRESS;
    }

    /**
     * Saga가 완료되었는지 확인
     */
    public boolean isCompleted() {
        return this.status == SagaStatus.COMPLETED;
    }

    /**
     * 보상 중인지 확인
     */
    public boolean isCompensating() {
        return this.status == SagaStatus.COMPENSATING;
    }

    // ===== 검증 메서드 =====

    /**
     * 상태 검증
     */
    private void validateStatus(SagaStatus expectedStatus, String operation) {
        if (this.status != expectedStatus) {
            throw new SagaException(
                    OrderErrorCode.SAGA_STATE_MISMATCH,
                    String.format("%s는 %s 상태에서만 가능합니다. 현재 상태: %s",
                            operation,
                            expectedStatus.getDescription(),
                            this.status.getDescription())
            );
        }
    }

    /**
     * 현재 Step 검증
     */
    private void validateCurrentStep(SagaStep step, String operation) {
        if (this.currentStep != step) {
            throw new SagaException(
                    OrderErrorCode.SAGA_STEP_FAILED,
                    String.format("%s는 현재 Step(%s)에서만 가능합니다. 요청 Step: %s",
                            operation,
                            this.currentStep != null ? this.currentStep.getDescription() : "없음",
                            step.getDescription())
            );
        }
    }

    /**
     * 최종 상태 아닌지 검증
     */
    private void validateNotFinalState(String operation) {
        if (this.status.isFinalState()) {
            throw new SagaException(
                    OrderErrorCode.SAGA_ALREADY_COMPLETED,
                    String.format("%s는 최종 상태(%s)에서 불가능합니다.",
                            operation,
                            this.status.getDescription())
            );
        }
    }

    /**
     * Saga ID 문자열 반환
     */
    public String getSagaIdValue() {
        return this.sagaId.getValue();
    }

    /**
     * Order ID 문자열 반환
     */
    public String getOrderIdValue() {
        return this.orderId.getValue();
    }

    /**
     * Step History 복사본 반환 (불변성 보장)
     */
    public List<SagaStepHistory> getStepHistory() {
        return new ArrayList<>(this.stepHistory);
    }
}
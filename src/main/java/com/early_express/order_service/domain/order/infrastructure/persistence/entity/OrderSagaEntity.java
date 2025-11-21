package com.early_express.order_service.domain.order.infrastructure.persistence.entity;

import com.early_express.order_service.domain.order.domain.model.OrderSaga;
import com.early_express.order_service.domain.order.domain.model.SagaStatus;
import com.early_express.order_service.domain.order.domain.model.SagaStep;
import com.early_express.order_service.domain.order.domain.model.vo.CompensationData;
import com.early_express.order_service.domain.order.domain.model.vo.OrderId;
import com.early_express.order_service.domain.order.domain.model.vo.SagaId;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * OrderSaga JPA Entity
 */
@Entity
@Table(name = "p_order_sagas")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OrderSagaEntity {

    @Id
    @Column(name = "saga_id", length = 36)
    private String sagaId;

    @Column(name = "order_id", nullable = false, unique = true, length = 36)
    private String orderId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private SagaStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "current_step", length = 50)
    private SagaStep currentStep;

    @Column(name = "compensation_data", columnDefinition = "TEXT")
    private String compensationData; // JSON

    @Column(name = "started_at", nullable = false)
    private LocalDateTime startedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "failure_reason", columnDefinition = "TEXT")
    private String failureReason;

    @OneToMany(mappedBy = "saga", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SagaStepHistoryEntity> stepHistory = new ArrayList<>();

    @Builder
    private OrderSagaEntity(
            String sagaId,
            String orderId,
            SagaStatus status,
            SagaStep currentStep,
            String compensationData,
            LocalDateTime startedAt,
            LocalDateTime completedAt,
            String failureReason,
            List<SagaStepHistoryEntity> stepHistory) {

        this.sagaId = sagaId;
        this.orderId = orderId;
        this.status = status;
        this.currentStep = currentStep;
        this.compensationData = compensationData;
        this.startedAt = startedAt;
        this.completedAt = completedAt;
        this.failureReason = failureReason;
        if (stepHistory != null) {
            this.stepHistory = stepHistory;
        }
    }

    /**
     * 도메인 모델로부터 엔티티 생성
     */
    public static OrderSagaEntity fromDomain(OrderSaga saga) {
        OrderSagaEntity entity = OrderSagaEntity.builder()
                .sagaId(saga.getSagaIdValue())
                .orderId(saga.getOrderIdValue())
                .status(saga.getStatus())
                .currentStep(saga.getCurrentStep())
                .compensationData(saga.getCompensationData().toJson())
                .startedAt(saga.getStartedAt())
                .completedAt(saga.getCompletedAt())
                .failureReason(saga.getFailureReason())
                .build();

        // Step History 변환
        saga.getStepHistory().forEach(history -> {
            SagaStepHistoryEntity historyEntity = SagaStepHistoryEntity.fromDomain(history);
            entity.addStepHistory(historyEntity);
        });

        return entity;
    }

    /**
     * 엔티티를 도메인 모델로 변환
     */
    public OrderSaga toDomain() {
        return OrderSaga.builder()
                .sagaId(SagaId.from(this.sagaId))
                .orderId(OrderId.from(this.orderId))
                .status(this.status)
                .currentStep(this.currentStep)
                .compensationData(CompensationData.fromJson(
                        this.compensationData != null ? this.compensationData : "{}"
                ))
                .startedAt(this.startedAt)
                .completedAt(this.completedAt)
                .failureReason(this.failureReason)
                .stepHistory(this.stepHistory.stream()
                        .map(SagaStepHistoryEntity::toDomain)
                        .toList())
                .build();
    }

    /**
     * 도메인 모델로 엔티티 업데이트
     */
    public void updateFromDomain(OrderSaga saga) {
        this.status = saga.getStatus();
        this.currentStep = saga.getCurrentStep();
        this.compensationData = saga.getCompensationData().toJson();
        this.completedAt = saga.getCompletedAt();
        this.failureReason = saga.getFailureReason();

        // Step History 동기화
        this.stepHistory.clear();
        saga.getStepHistory().forEach(history -> {
            SagaStepHistoryEntity historyEntity = SagaStepHistoryEntity.fromDomain(history);
            this.addStepHistory(historyEntity);
        });
    }

    /**
     * Step History 추가
     */
    public void addStepHistory(SagaStepHistoryEntity history) {
        this.stepHistory.add(history);
        history.setSaga(this);
    }
}

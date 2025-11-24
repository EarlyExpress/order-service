package com.early_express.order_service.domain.order.infrastructure.persistence.entity;

import com.early_express.order_service.domain.order.domain.model.SagaStep;
import com.early_express.order_service.domain.order.domain.model.SagaStepHistory;
import com.early_express.order_service.domain.order.domain.model.StepStatus;
import com.early_express.order_service.domain.order.domain.model.vo.SagaId;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * SagaStepHistory JPA Entity
 */
@Entity
@Table(name = "p_saga_step_histories")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SagaStepHistoryEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "saga_id", nullable = false)
    private OrderSagaEntity saga;

    @Column(name = "saga_id_value", nullable = false, length = 36)
    private String sagaIdValue;

    @Enumerated(EnumType.STRING)
    @Column(name = "step", nullable = false, length = 50)
    private SagaStep step;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private StepStatus status;

    @Column(name = "request", columnDefinition = "TEXT")
    private String request;

    @Column(name = "response", columnDefinition = "TEXT")
    private String response;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @Column(name = "started_at", nullable = false)
    private LocalDateTime startedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "retry_count", nullable = false)
    private Integer retryCount;

    @Builder
    private SagaStepHistoryEntity(
            Long id,
            String sagaIdValue,
            SagaStep step,
            StepStatus status,
            String request,
            String response,
            String errorMessage,
            LocalDateTime startedAt,
            LocalDateTime completedAt,
            Integer retryCount) {

        this.id = id;
        this.sagaIdValue = sagaIdValue;
        this.step = step;
        this.status = status;
        this.request = request;
        this.response = response;
        this.errorMessage = errorMessage;
        this.startedAt = startedAt;
        this.completedAt = completedAt;
        this.retryCount = retryCount != null ? retryCount : 0;
    }

    /**
     * 도메인 모델로부터 엔티티 생성
     */
    public static SagaStepHistoryEntity fromDomain(SagaStepHistory history) {
        return SagaStepHistoryEntity.builder()
                .id(history.getId())
                .sagaIdValue(history.getSagaIdValue())
                .step(history.getStep())
                .status(history.getStatus())
                .request(history.getRequest())
                .response(history.getResponse())
                .errorMessage(history.getErrorMessage())
                .startedAt(history.getStartedAt())
                .completedAt(history.getCompletedAt())
                .retryCount(history.getRetryCount())
                .build();
    }

    /**
     * 엔티티를 도메인 모델로 변환
     */
    public SagaStepHistory toDomain() {
        SagaStepHistory history = SagaStepHistory.builder()
                .id(this.id)
                .sagaId(SagaId.from(this.sagaIdValue))
                .step(this.step)
                .status(this.status)
                .request(this.request)
                .response(this.response)
                .errorMessage(this.errorMessage)
                .startedAt(this.startedAt)
                .completedAt(this.completedAt)
                .retryCount(this.retryCount)
                .build();

        // JPA ID 설정
        if (this.id != null) {
            history.setId(this.id);
        }

        return history;
    }

    /**
     * Saga 연관관계 설정
     */
    public void setSaga(OrderSagaEntity saga) {
        this.saga = saga;
    }

    /**
     * Saga ID 설정 (fromDomain 이후 사용)
     */
    public void setSagaIdValue(String sagaIdValue) {
        this.sagaIdValue = sagaIdValue;
    }
}

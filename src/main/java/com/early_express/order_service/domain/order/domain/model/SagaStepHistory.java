package com.early_express.order_service.domain.order.domain.model;

import com.early_express.order_service.domain.order.domain.model.vo.SagaId;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * SagaStepHistory Entity
 * Saga Step의 실행 이력을 기록
 */
@Getter
public class SagaStepHistory {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    private Long id; // JPA 자동 생성용
    private final SagaId sagaId;
    private final SagaStep step;
    private StepStatus status;
    private String request; // JSON
    private String response; // JSON
    private String errorMessage;
    private final LocalDateTime startedAt;
    private LocalDateTime completedAt;
    private int retryCount;

    @Builder
    private SagaStepHistory(
            Long id,
            SagaId sagaId,
            SagaStep step,
            StepStatus status,
            String request,
            String response,
            String errorMessage,
            LocalDateTime startedAt,
            LocalDateTime completedAt,
            int retryCount) {

        this.id = id;
        this.sagaId = sagaId;
        this.step = step;
        this.status = status;
        this.request = request;
        this.response = response;
        this.errorMessage = errorMessage;
        this.startedAt = startedAt;
        this.completedAt = completedAt;
        this.retryCount = retryCount;
    }

    /**
     * 새로운 Step History 생성 (팩토리 메서드)
     */
    public static SagaStepHistory create(SagaId sagaId, SagaStep step) {
        return SagaStepHistory.builder()
                .sagaId(sagaId)
                .step(step)
                .status(StepStatus.PENDING)
                .startedAt(LocalDateTime.now())
                .retryCount(0)
                .build();
    }

    /**
     * Step 실행 시작
     */
    public void start(Object requestData) {
        this.status = StepStatus.IN_PROGRESS;
        this.request = toJson(requestData);
    }

    /**
     * Step 실행 완료
     */
    public void complete(Object responseData) {
        this.status = StepStatus.SUCCESS;
        this.response = toJson(responseData);
        this.completedAt = LocalDateTime.now();
    }

    /**
     * Step 실행 실패
     */
    public void fail(String errorMessage) {
        this.status = StepStatus.FAILED;
        this.errorMessage = errorMessage;
        this.completedAt = LocalDateTime.now();
    }

    /**
     * Step 보상 완료
     */
    public void compensated() {
        this.status = StepStatus.COMPENSATED;
    }

    /**
     * 재시도 증가
     */
    public void incrementRetryCount() {
        this.retryCount++;
    }

    /**
     * 객체를 JSON 문자열로 변환
     */
    private String toJson(Object data) {
        if (data == null) {
            return null;
        }

        try {
            return objectMapper.writeValueAsString(data);
        } catch (JsonProcessingException e) {
            return data.toString();
        }
    }

    /**
     * Step이 성공했는지 확인
     */
    public boolean isSuccessful() {
        return this.status == StepStatus.SUCCESS;
    }

    /**
     * Step이 실패했는지 확인
     */
    public boolean isFailed() {
        return this.status == StepStatus.FAILED;
    }

    /**
     * 최대 재시도 횟수 초과 여부 확인
     */
    public boolean hasExceededMaxRetries(int maxRetries) {
        return this.retryCount >= maxRetries;
    }

    /**
     * Saga ID 문자열 반환
     */
    public String getSagaIdValue() {
        return this.sagaId.getValue();
    }

    /**
     * JPA용 ID setter
     */
    public void setId(Long id) {
        this.id = id;
    }
}

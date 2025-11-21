package com.early_express.order_service.domain.order.domain.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Saga 상태 Enum
 */
@Getter
@RequiredArgsConstructor
public enum SagaStatus {

    /**
     * 시작 대기
     */
    PENDING("시작 대기"),

    /**
     * 진행 중
     */
    IN_PROGRESS("진행 중"),

    /**
     * 완료 (모든 Step 성공)
     */
    COMPLETED("완료"),

    /**
     * 보상 트랜잭션 실행 중
     */
    COMPENSATING("보상 실행 중"),

    /**
     * 보상 완료 (롤백됨)
     */
    COMPENSATED("보상 완료"),

    /**
     * 보상 실패 (수동 개입 필요)
     */
    COMPENSATION_FAILED("보상 실패"),

    /**
     * 실패
     */
    FAILED("실패");

    private final String description;

    /**
     * 최종 상태인지 확인
     */
    public boolean isFinalState() {
        return this == COMPLETED
                || this == COMPENSATED
                || this == COMPENSATION_FAILED
                || this == FAILED;
    }

    /**
     * 성공적으로 완료되었는지 확인
     */
    public boolean isSuccessful() {
        return this == COMPLETED;
    }

    /**
     * 보상 중인지 확인
     */
    public boolean isCompensating() {
        return this == COMPENSATING;
    }
}

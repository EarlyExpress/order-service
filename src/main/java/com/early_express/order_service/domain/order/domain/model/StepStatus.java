package com.early_express.order_service.domain.order.domain.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Saga Step 실행 상태 Enum
 */
@Getter
@RequiredArgsConstructor
public enum StepStatus {

    /**
     * 실행 대기
     */
    PENDING("실행 대기"),

    /**
     * 실행 중
     */
    IN_PROGRESS("실행 중"),

    /**
     * 성공
     */
    SUCCESS("성공"),

    /**
     * 실패
     */
    FAILED("실패"),

    /**
     * 보상 완료
     */
    COMPENSATED("보상 완료");

    private final String description;

    /**
     * 최종 상태인지 확인
     */
    public boolean isFinalState() {
        return this == SUCCESS || this == FAILED || this == COMPENSATED;
    }

    /**
     * 성공했는지 확인
     */
    public boolean isSuccessful() {
        return this == SUCCESS;
    }

    /**
     * 실패했는지 확인
     */
    public boolean isFailed() {
        return this == FAILED;
    }
}

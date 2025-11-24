package com.early_express.order_service.domain.order.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

@DisplayName("SagaStatus enum 테스트")
class SagaStatusTest {

    @Test
    @DisplayName("PENDING 상태는 최종 상태가 아니다")
    void pendingIsNotFinalState() {
        // when & then
        assertThat(SagaStatus.PENDING.isFinalState()).isFalse();
    }

    @Test
    @DisplayName("IN_PROGRESS 상태는 최종 상태가 아니다")
    void inProgressIsNotFinalState() {
        // when & then
        assertThat(SagaStatus.IN_PROGRESS.isFinalState()).isFalse();
    }

    @Test
    @DisplayName("COMPLETED는 최종 상태이다")
    void completedIsFinalState() {
        // when & then
        assertThat(SagaStatus.COMPLETED.isFinalState()).isTrue();
    }

    @Test
    @DisplayName("COMPENSATED는 최종 상태이다")
    void compensatedIsFinalState() {
        // when & then
        assertThat(SagaStatus.COMPENSATED.isFinalState()).isTrue();
    }

    @Test
    @DisplayName("FAILED는 최종 상태이다")
    void failedIsFinalState() {
        // when & then
        assertThat(SagaStatus.FAILED.isFinalState()).isTrue();
    }

    @Test
    @DisplayName("COMPENSATION_FAILED는 최종 상태이다")
    void compensationFailedIsFinalState() {
        // when & then
        assertThat(SagaStatus.COMPENSATION_FAILED.isFinalState()).isTrue();
    }

    @Test
    @DisplayName("COMPLETED는 성공 상태이다")
    void completedIsSuccessful() {
        // when & then
        assertThat(SagaStatus.COMPLETED.isSuccessful()).isTrue();
    }

    @Test
    @DisplayName("COMPENSATED는 성공 상태가 아니다")
    void compensatedIsNotSuccessful() {
        // when & then
        assertThat(SagaStatus.COMPENSATED.isSuccessful()).isFalse();
    }

    @Test
    @DisplayName("COMPENSATING 상태를 확인할 수 있다")
    void checkCompensatingStatus() {
        // when & then
        assertThat(SagaStatus.COMPENSATING.isCompensating()).isTrue();
        assertThat(SagaStatus.IN_PROGRESS.isCompensating()).isFalse();
    }

    @Test
    @DisplayName("COMPENSATING은 최종 상태가 아니다")
    void compensatingIsNotFinalState() {
        // when & then
        assertThat(SagaStatus.COMPENSATING.isFinalState()).isFalse();
    }

    @Test
    @DisplayName("모든 상태는 설명을 가지고 있다")
    void allStatusesHaveDescription() {
        // when & then
        for (SagaStatus status : SagaStatus.values()) {
            assertThat(status.getDescription()).isNotEmpty();
        }
    }
}
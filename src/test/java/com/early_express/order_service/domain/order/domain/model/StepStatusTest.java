package com.early_express.order_service.domain.order.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

@DisplayName("StepStatus enum 테스트")
class StepStatusTest {

    @Test
    @DisplayName("PENDING은 최종 상태가 아니다")
    void pendingIsNotFinalState() {
        // when & then
        assertThat(StepStatus.PENDING.isFinalState()).isFalse();
    }

    @Test
    @DisplayName("IN_PROGRESS는 최종 상태가 아니다")
    void inProgressIsNotFinalState() {
        // when & then
        assertThat(StepStatus.IN_PROGRESS.isFinalState()).isFalse();
    }

    @Test
    @DisplayName("SUCCESS는 최종 상태이다")
    void successIsFinalState() {
        // when & then
        assertThat(StepStatus.SUCCESS.isFinalState()).isTrue();
    }

    @Test
    @DisplayName("FAILED는 최종 상태이다")
    void failedIsFinalState() {
        // when & then
        assertThat(StepStatus.FAILED.isFinalState()).isTrue();
    }

    @Test
    @DisplayName("COMPENSATED는 최종 상태이다")
    void compensatedIsFinalState() {
        // when & then
        assertThat(StepStatus.COMPENSATED.isFinalState()).isTrue();
    }

    @Test
    @DisplayName("SUCCESS는 성공 상태이다")
    void successIsSuccessful() {
        // when & then
        assertThat(StepStatus.SUCCESS.isSuccessful()).isTrue();
    }

    @Test
    @DisplayName("FAILED는 성공 상태가 아니다")
    void failedIsNotSuccessful() {
        // when & then
        assertThat(StepStatus.FAILED.isSuccessful()).isFalse();
    }

    @Test
    @DisplayName("COMPENSATED는 성공 상태가 아니다")
    void compensatedIsNotSuccessful() {
        // when & then
        assertThat(StepStatus.COMPENSATED.isSuccessful()).isFalse();
    }

    @Test
    @DisplayName("FAILED는 실패 상태이다")
    void failedIsFailed() {
        // when & then
        assertThat(StepStatus.FAILED.isFailed()).isTrue();
    }

    @Test
    @DisplayName("SUCCESS는 실패 상태가 아니다")
    void successIsNotFailed() {
        // when & then
        assertThat(StepStatus.SUCCESS.isFailed()).isFalse();
    }

    @Test
    @DisplayName("모든 상태는 설명을 가지고 있다")
    void allStatusesHaveDescription() {
        // when & then
        for (StepStatus status : StepStatus.values()) {
            assertThat(status.getDescription()).isNotEmpty();
        }
    }
}
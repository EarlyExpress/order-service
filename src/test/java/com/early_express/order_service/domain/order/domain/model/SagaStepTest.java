package com.early_express.order_service.domain.order.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

@DisplayName("SagaStep enum 테스트")
class SagaStepTest {

    @Test
    @DisplayName("Forward Step을 확인할 수 있다")
    void checkForwardStep() {
        // when & then
        assertThat(SagaStep.STOCK_RESERVE.isForwardStep()).isTrue();
        assertThat(SagaStep.PAYMENT_VERIFY.isForwardStep()).isTrue();
        assertThat(SagaStep.ROUTE_CALCULATE.isForwardStep()).isTrue();
    }

    @Test
    @DisplayName("Compensation Step을 확인할 수 있다")
    void checkCompensationStep() {
        // when & then
        assertThat(SagaStep.STOCK_RESTORE.isCompensationStep()).isTrue();
        assertThat(SagaStep.PAYMENT_CANCEL.isCompensationStep()).isTrue();
        assertThat(SagaStep.HUB_DELIVERY_CANCEL.isCompensationStep()).isTrue();
    }

    @Test
    @DisplayName("Forward Step은 Compensation Step이 아니다")
    void forwardStepIsNotCompensationStep() {
        // when & then
        assertThat(SagaStep.STOCK_RESERVE.isCompensationStep()).isFalse();
    }

    @Test
    @DisplayName("대응하는 보상 Step을 조회할 수 있다")
    void getCompensationStep() {
        // when & then
        assertThat(SagaStep.STOCK_RESERVE.getCompensationStep())
                .isEqualTo(SagaStep.STOCK_RESTORE);
        assertThat(SagaStep.PAYMENT_VERIFY.getCompensationStep())
                .isEqualTo(SagaStep.PAYMENT_CANCEL);
        assertThat(SagaStep.HUB_DELIVERY_CREATE.getCompensationStep())
                .isEqualTo(SagaStep.HUB_DELIVERY_CANCEL);
    }

    @Test
    @DisplayName("보상 Step이 없는 경우 예외가 발생한다")
    void getCompensationStepThrowsException() {
        // when & then
        assertThatThrownBy(() -> SagaStep.ROUTE_CALCULATE.getCompensationStep())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("보상 Step이 없는 Step입니다");
    }

    @Test
    @DisplayName("다음 Step을 조회할 수 있다")
    void getNextStep() {
        // when & then
        assertThat(SagaStep.STOCK_RESERVE.getNextStep())
                .isEqualTo(SagaStep.PAYMENT_VERIFY);
        assertThat(SagaStep.PAYMENT_VERIFY.getNextStep())
                .isEqualTo(SagaStep.ROUTE_CALCULATE);
        assertThat(SagaStep.ROUTE_CALCULATE.getNextStep())
                .isEqualTo(SagaStep.HUB_DELIVERY_CREATE);
    }

    @Test
    @DisplayName("마지막 Step인지 확인할 수 있다")
    void checkLastStep() {
        // when & then
        assertThat(SagaStep.TRACKING_START.isLastStep()).isTrue();
        assertThat(SagaStep.STOCK_RESERVE.isLastStep()).isFalse();
    }

    @Test
    @DisplayName("마지막 Step에서 다음 Step을 조회하면 예외가 발생한다")
    void getNextStepFromLastStepThrowsException() {
        // when & then
        assertThatThrownBy(() -> SagaStep.TRACKING_START.getNextStep())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("다음 Step이 없습니다");
    }

    @Test
    @DisplayName("보상이 필요한 Step을 확인할 수 있다")
    void checkNeedsCompensation() {
        // when & then
        assertThat(SagaStep.STOCK_RESERVE.isNeedsCompensation()).isTrue();
        assertThat(SagaStep.PAYMENT_VERIFY.isNeedsCompensation()).isTrue();
        assertThat(SagaStep.ROUTE_CALCULATE.isNeedsCompensation()).isFalse();
    }

    @Test
    @DisplayName("필수 Step을 확인할 수 있다")
    void checkMandatory() {
        // when & then
        assertThat(SagaStep.STOCK_RESERVE.isMandatory()).isTrue();
        assertThat(SagaStep.PAYMENT_VERIFY.isMandatory()).isTrue();
        assertThat(SagaStep.NOTIFICATION_SEND.isMandatory()).isFalse();
    }

    @Test
    @DisplayName("Best Effort Step을 확인할 수 있다")
    void checkBestEffort() {
        // when & then
        assertThat(SagaStep.NOTIFICATION_SEND.isBestEffort()).isTrue();
        assertThat(SagaStep.TRACKING_START.isBestEffort()).isTrue();
        assertThat(SagaStep.STOCK_RESERVE.isBestEffort()).isFalse();
    }

    @Test
    @DisplayName("모든 Step은 설명을 가지고 있다")
    void allStepsHaveDescription() {
        // when & then
        for (SagaStep step : SagaStep.values()) {
            assertThat(step.getDescription()).isNotEmpty();
        }
    }

    @Test
    @DisplayName("Step 순서가 올바른지 확인할 수 있다")
    void checkStepSequence() {
        // when
        SagaStep current = SagaStep.STOCK_RESERVE;

        // then
        assertThat(current.getNextStep()).isEqualTo(SagaStep.PAYMENT_VERIFY);
        assertThat(current.getNextStep().getNextStep()).isEqualTo(SagaStep.ROUTE_CALCULATE);
        assertThat(current.getNextStep().getNextStep().getNextStep())
                .isEqualTo(SagaStep.HUB_DELIVERY_CREATE);
    }
}
package com.early_express.order_service.domain.order.domain.model;

import com.early_express.order_service.domain.order.domain.model.vo.OrderId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

@DisplayName("OrderSaga Domain 테스트")
class OrderSagaTest {

    @Test
    @DisplayName("새로운 Saga를 생성할 수 있다")
    void createOrderSaga() {
        // given
        OrderId orderId = OrderId.create();

        // when
        OrderSaga saga = OrderSaga.create(orderId);

        // then
        assertThat(saga).isNotNull();
        assertThat(saga.getStatus()).isEqualTo(SagaStatus.PENDING);
        assertThat(saga.getOrderIdValue()).isEqualTo(orderId.getValue());
        assertThat(saga.getStepHistory()).isEmpty();
    }

    @Test
    @DisplayName("Saga를 시작할 수 있다")
    void startSaga() {
        // given
        OrderSaga saga = OrderSaga.create(OrderId.create());

        // when
        saga.start();

        // then
        assertThat(saga.getStatus()).isEqualTo(SagaStatus.IN_PROGRESS);
        assertThat(saga.getCurrentStep()).isEqualTo(SagaStep.STOCK_RESERVE);
        assertThat(saga.isInProgress()).isTrue();
    }

    @Test
    @DisplayName("Step을 시작할 수 있다")
    void startStep() {
        // given
        OrderSaga saga = OrderSaga.create(OrderId.create());
        saga.start();

        // when
        saga.startStep(SagaStep.STOCK_RESERVE);

        // then
        assertThat(saga.getStepHistory()).hasSize(1);
        assertThat(saga.getStepHistory().get(0).getStep()).isEqualTo(SagaStep.STOCK_RESERVE);
    }

    @Test
    @DisplayName("Step을 완료할 수 있다")
    void completeStep() {
        // given
        OrderSaga saga = OrderSaga.create(OrderId.create());
        saga.start();
        saga.startStep(SagaStep.STOCK_RESERVE);
        Object stepData = "stockReserveData";

        // when
        saga.completeStep(SagaStep.STOCK_RESERVE, stepData);

        // then
        assertThat(saga.getCurrentStep()).isEqualTo(SagaStep.PAYMENT_VERIFY);
        assertThat(saga.getCompensationDataForStep(SagaStep.STOCK_RESERVE)).isEqualTo(stepData);
    }

    @Test
    @DisplayName("마지막 Step을 완료하면 Saga가 완료된다")
    void completeSagaWithLastStep() {
        // given
        OrderSaga saga = OrderSaga.create(OrderId.create());
        saga.start();

        // 모든 Step 실행
        for (SagaStep step : new SagaStep[]{
                SagaStep.STOCK_RESERVE,
                SagaStep.PAYMENT_VERIFY,
                SagaStep.ROUTE_CALCULATE,
                SagaStep.HUB_DELIVERY_CREATE,
                SagaStep.LAST_MILE_DELIVERY_CREATE,
                SagaStep.NOTIFICATION_SEND,
                SagaStep.TRACKING_START
        }) {
            saga.startStep(step);
            saga.completeStep(step, "data");
        }

        // then
        assertThat(saga.getStatus()).isEqualTo(SagaStatus.COMPLETED);
        assertThat(saga.isCompleted()).isTrue();
    }

    @Test
    @DisplayName("Step이 실패하면 보상 트랜잭션이 시작된다")
    void failStepStartsCompensation() {
        // given
        OrderSaga saga = OrderSaga.create(OrderId.create());
        saga.start();
        saga.startStep(SagaStep.PAYMENT_VERIFY);

        // when
        saga.failStep(SagaStep.PAYMENT_VERIFY, "결제 검증 실패");

        // then
        assertThat(saga.getStatus()).isEqualTo(SagaStatus.COMPENSATING);
        assertThat(saga.isCompensating()).isTrue();
        assertThat(saga.getFailureReason()).contains("결제 검증 실패");
    }

    @Test
    @DisplayName("Best Effort Step은 실패해도 계속 진행된다")
    void bestEffortStepContinuesOnFailure() {
        // given
        OrderSaga saga = OrderSaga.create(OrderId.create());
        saga.start();

        // 마지막 Step까지 진행
        for (SagaStep step : new SagaStep[]{
                SagaStep.STOCK_RESERVE,
                SagaStep.PAYMENT_VERIFY,
                SagaStep.ROUTE_CALCULATE,
                SagaStep.HUB_DELIVERY_CREATE,
                SagaStep.LAST_MILE_DELIVERY_CREATE
        }) {
            saga.startStep(step);
            saga.completeStep(step, "data");
        }

        saga.startStep(SagaStep.NOTIFICATION_SEND);

        // when
        saga.failStep(SagaStep.NOTIFICATION_SEND, "알림 실패");

        // then
        assertThat(saga.getCurrentStep()).isEqualTo(SagaStep.TRACKING_START); // 다음 Step으로 진행
        assertThat(saga.getStatus()).isEqualTo(SagaStatus.IN_PROGRESS); // 여전히 진행 중
    }

    @Test
    @DisplayName("보상 Step을 실행할 수 있다")
    void executeCompensation() {
        // given
        OrderSaga saga = OrderSaga.create(OrderId.create());
        saga.start();
        saga.startStep(SagaStep.STOCK_RESERVE);
        saga.completeStep(SagaStep.STOCK_RESERVE, "data");
        saga.startCompensation("테스트 실패");

        // when
        saga.executeCompensation(SagaStep.STOCK_RESERVE, SagaStep.STOCK_RESTORE);

        // then
        assertThat(saga.getStepHistory()).hasSize(2);
    }

    @Test
    @DisplayName("모든 보상을 완료하면 Saga가 보상 완료 상태가 된다")
    void completeAllCompensations() {
        // given
        OrderSaga saga = OrderSaga.create(OrderId.create());
        saga.start();
        saga.startCompensation("테스트 실패");

        // when
        saga.completeAllCompensations();

        // then
        assertThat(saga.getStatus()).isEqualTo(SagaStatus.COMPENSATED);
    }

    @Test
    @DisplayName("보상 Step이 실패하면 보상 실패 상태가 된다")
    void compensationFailed() {
        // given
        OrderSaga saga = OrderSaga.create(OrderId.create());
        saga.start();
        saga.startCompensation("테스트 실패");
        saga.executeCompensation(SagaStep.STOCK_RESERVE, SagaStep.STOCK_RESTORE);

        // when
        saga.failCompensation(SagaStep.STOCK_RESTORE, "보상 실패");

        // then
        assertThat(saga.getStatus()).isEqualTo(SagaStatus.COMPENSATION_FAILED);
    }

    @Test
    @DisplayName("완료된 Step 중 보상이 필요한 것들을 조회할 수 있다")
    void getCompletedStepsNeedingCompensation() {
        // given
        OrderSaga saga = OrderSaga.create(OrderId.create());
        saga.start();

        saga.startStep(SagaStep.STOCK_RESERVE);
        saga.completeStep(SagaStep.STOCK_RESERVE, "data1");

        saga.startStep(SagaStep.PAYMENT_VERIFY);
        saga.completeStep(SagaStep.PAYMENT_VERIFY, "data2");

        // when
        var completedSteps = saga.getCompletedStepsNeedingCompensation();

        // then
        assertThat(completedSteps).contains(SagaStep.STOCK_RESERVE, SagaStep.PAYMENT_VERIFY);
    }

    @Test
    @DisplayName("Step History를 추가할 수 있다")
    void addStepHistory() {
        // given
        OrderSaga saga = OrderSaga.create(OrderId.create());
        saga.start();

        // when
        saga.addStepHistory(SagaStep.ROUTE_CALCULATE, "routeData");

        // then
        assertThat(saga.getStepHistory()).isNotEmpty();
        assertThat(saga.getCompensationDataForStep(SagaStep.ROUTE_CALCULATE)).isNull(); // ROUTE_CALCULATE는 보상 불필요
    }

    @Test
    @DisplayName("잘못된 상태에서 Step을 완료하면 예외가 발생한다")
    void completeStepInWrongState() {
        // given
        OrderSaga saga = OrderSaga.create(OrderId.create());
        saga.start();
        saga.startStep(SagaStep.STOCK_RESERVE);

        // when & then
        assertThatThrownBy(() -> saga.completeStep(SagaStep.PAYMENT_VERIFY, "data"))
                .hasMessageContaining("현재 Step")
                .hasMessageContaining("에서만 가능합니다");
    }

    @Test
    @DisplayName("PENDING 상태가 아닌 경우 시작하면 예외가 발생한다")
    void startSagaInWrongState() {
        // given
        OrderSaga saga = OrderSaga.create(OrderId.create());
        saga.start();

        // when & then
        assertThatThrownBy(() -> saga.start())
                .isInstanceOf(Exception.class)
                .hasMessageContaining("가능합니다");
    }
}
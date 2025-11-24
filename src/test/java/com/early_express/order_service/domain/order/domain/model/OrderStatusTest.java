package com.early_express.order_service.domain.order.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

@DisplayName("OrderStatus enum 테스트")
class OrderStatusTest {

    @Test
    @DisplayName("주문 생성 상태는 취소 가능하다")
    void pendingOrderIsCancellable() {
        // when & then
        assertThat(OrderStatus.PENDING.isCancellable()).isTrue();
    }

    @Test
    @DisplayName("배송 중 상태는 취소 불가능하다")
    void inDeliveryOrderIsNotCancellable() {
        // when & then
        assertThat(OrderStatus.IN_DELIVERY.isCancellable()).isFalse();
        assertThat(OrderStatus.HUB_IN_TRANSIT.isCancellable()).isFalse();
    }

    @Test
    @DisplayName("허브 대기 상태까지는 취소 가능하다")
    void hubWaitingOrderIsCancellable() {
        // when & then
        assertThat(OrderStatus.HUB_WAITING.isCancellable()).isTrue();
    }

    @Test
    @DisplayName("Saga 진행 중인 상태를 확인할 수 있다")
    void checkSagaInProgress() {
        // when & then
        assertThat(OrderStatus.STOCK_CHECKING.isSagaInProgress()).isTrue();
        assertThat(OrderStatus.PAYMENT_VERIFYING.isSagaInProgress()).isTrue();
        assertThat(OrderStatus.ROUTE_CALCULATING.isSagaInProgress()).isTrue();
    }

    @Test
    @DisplayName("확정된 주문은 Saga 진행 상태가 아니다")
    void confirmedOrderIsNotSagaInProgress() {
        // when & then
        assertThat(OrderStatus.CONFIRMED.isSagaInProgress()).isFalse();
    }

    @Test
    @DisplayName("배송 진행 중인 상태를 확인할 수 있다")
    void checkInDelivery() {
        // when & then
        assertThat(OrderStatus.HUB_WAITING.isInDelivery()).isTrue();
        assertThat(OrderStatus.HUB_IN_TRANSIT.isInDelivery()).isTrue();
        assertThat(OrderStatus.IN_DELIVERY.isInDelivery()).isTrue();
    }

    @Test
    @DisplayName("주문 확정은 배송 진행 상태가 아니다")
    void confirmedOrderIsNotInDelivery() {
        // when & then
        assertThat(OrderStatus.CONFIRMED.isInDelivery()).isFalse();
    }

    @Test
    @DisplayName("최종 상태를 확인할 수 있다")
    void checkFinalState() {
        // when & then
        assertThat(OrderStatus.COMPLETED.isFinalState()).isTrue();
        assertThat(OrderStatus.CANCELLED.isFinalState()).isTrue();
        assertThat(OrderStatus.FAILED.isFinalState()).isTrue();
        assertThat(OrderStatus.COMPENSATED.isFinalState()).isTrue();
    }

    @Test
    @DisplayName("진행 중인 상태는 최종 상태가 아니다")
    void inProgressOrderIsNotFinalState() {
        // when & then
        assertThat(OrderStatus.PENDING.isFinalState()).isFalse();
        assertThat(OrderStatus.IN_DELIVERY.isFinalState()).isFalse();
        assertThat(OrderStatus.CONFIRMED.isFinalState()).isFalse();
    }

    @Test
    @DisplayName("모든 상태는 설명을 가지고 있다")
    void allStatusesHaveDescription() {
        // when & then
        for (OrderStatus status : OrderStatus.values()) {
            assertThat(status.getDescription()).isNotEmpty();
        }
    }
}
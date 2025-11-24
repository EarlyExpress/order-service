package com.early_express.order_service.domain.order.infrastructure.persistence.entity;

import com.early_express.order_service.domain.order.domain.model.*;
import com.early_express.order_service.domain.order.domain.model.vo.OrderId;
import com.early_express.order_service.domain.order.domain.model.vo.SagaId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

@DisplayName("OrderSagaEntity 테스트")
class OrderSagaEntityTest {

    @Nested
    @DisplayName("fromDomain 메서드는")
    class FromDomainTest {

        @Test
        @DisplayName("Saga 도메인 모델의 모든 필드를 엔티티로 정확히 변환한다")
        void shouldConvertAllFieldsFromDomainToEntity() {
            // given
            OrderSaga saga = OrderSaga.create(OrderId.create());
            saga.start();

            // when
            OrderSagaEntity entity = OrderSagaEntity.fromDomain(saga);

            // then
            assertThat(entity).isNotNull();
            // sagaId는 엔티티 생성 시 새로 생성되므로 null이 아님을 확인
            assertThat(entity.getSagaId()).isNotNull();
            assertThat(entity.getOrderId()).isEqualTo(saga.getOrderIdValue());
            assertThat(entity.getStatus()).isEqualTo(saga.getStatus());
            assertThat(entity.getCurrentStep()).isEqualTo(saga.getCurrentStep());
            assertThat(entity.getStartedAt()).isEqualTo(saga.getStartedAt());
            assertThat(entity.getCompletedAt()).isNull();
            assertThat(entity.getFailureReason()).isNull();
        }

        @Test
        @DisplayName("Step History를 함께 변환하고 sagaIdValue를 설정한다")
        void shouldConvertStepHistoryWithSagaIdValue() {
            // given
            OrderSaga saga = createSagaWithSteps();

            // when
            OrderSagaEntity entity = OrderSagaEntity.fromDomain(saga);

            // then
            assertThat(entity.getStepHistory()).hasSize(2);
            assertThat(entity.getStepHistory())
                    .extracting(SagaStepHistoryEntity::getStep)
                    .containsExactly(SagaStep.STOCK_RESERVE, SagaStep.PAYMENT_VERIFY);

            // 모든 Step History의 sagaIdValue가 설정되었는지 확인
            assertThat(entity.getStepHistory())
                    .allMatch(history -> history.getSagaIdValue().equals(entity.getSagaId()));
        }

        @Test
        @DisplayName("보상 데이터를 JSON으로 변환한다")
        void shouldConvertCompensationDataToJson() {
            // given
            OrderSaga saga = OrderSaga.create(OrderId.create());
            saga.start();
            saga.startStep(SagaStep.STOCK_RESERVE);
            saga.completeStep(SagaStep.STOCK_RESERVE, "stock-reservation-data");

            // when
            OrderSagaEntity entity = OrderSagaEntity.fromDomain(saga);

            // then
            assertThat(entity.getCompensationData()).isNotNull();
            assertThat(entity.getCompensationData()).contains("STOCK_RESERVE");
        }

        @Test
        @DisplayName("완료된 Saga를 정확히 변환한다")
        void shouldConvertCompletedSaga() {
            // given
            OrderSaga saga = createCompletedSaga();

            // when
            OrderSagaEntity entity = OrderSagaEntity.fromDomain(saga);

            // then
            assertThat(entity.getStatus()).isEqualTo(SagaStatus.COMPLETED);
            assertThat(entity.getCompletedAt()).isNotNull();
            assertThat(entity.getCurrentStep()).isEqualTo(SagaStep.TRACKING_START);
        }

        @Test
        @DisplayName("실패한 Saga를 정확히 변환한다")
        void shouldConvertFailedSaga() {
            // given
            OrderSaga saga = OrderSaga.create(OrderId.create());
            saga.start();
            saga.startStep(SagaStep.STOCK_RESERVE);
            saga.failStep(SagaStep.STOCK_RESERVE, "재고 부족");
            saga.startCompensation("재고 부족");

            // when
            OrderSagaEntity entity = OrderSagaEntity.fromDomain(saga);

            // then
            assertThat(entity.getStatus()).isEqualTo(SagaStatus.COMPENSATING);
            assertThat(entity.getFailureReason()).isEqualTo("재고 부족");
        }
    }

    @Nested
    @DisplayName("toDomain 메서드는")
    class ToDomainTest {

        @Test
        @DisplayName("엔티티의 모든 필드를 도메인 모델로 정확히 변환한다")
        void shouldConvertAllFieldsFromEntityToDomain() {
            // given
            OrderSagaEntity entity = createTestSagaEntity();

            // when
            OrderSaga saga = entity.toDomain();

            // then
            assertThat(saga).isNotNull();
            assertThat(saga.getSagaIdValue()).isEqualTo(entity.getSagaId());
            assertThat(saga.getOrderIdValue()).isEqualTo(entity.getOrderId());
            assertThat(saga.getStatus()).isEqualTo(entity.getStatus());
            assertThat(saga.getCurrentStep()).isEqualTo(entity.getCurrentStep());
            assertThat(saga.getStartedAt()).isEqualTo(entity.getStartedAt());
        }

        @Test
        @DisplayName("Step History를 함께 복원한다")
        void shouldRestoreStepHistory() {
            // given
            OrderSagaEntity entity = createSagaEntityWithHistory();

            // when
            OrderSaga saga = entity.toDomain();

            // then
            assertThat(saga.getStepHistory()).hasSize(2);
            assertThat(saga.getStepHistory())
                    .extracting(SagaStepHistory::getStep)
                    .containsExactly(SagaStep.STOCK_RESERVE, SagaStep.PAYMENT_VERIFY);
        }

        @Test
        @DisplayName("보상 데이터를 올바르게 복원한다")
        void shouldRestoreCompensationData() {
            // given
            OrderSagaEntity entity = createSagaEntityWithCompensationData();

            // when
            OrderSaga saga = entity.toDomain();

            // then
            assertThat(saga.getCompensationData()).isNotNull();
            assertThat(saga.getCompensationData().hasStepData("STOCK_RESERVE")).isTrue();
        }

        @Test
        @DisplayName("빈 보상 데이터도 안전하게 처리한다")
        void shouldHandleEmptyCompensationData() {
            // given
            OrderSagaEntity entity = OrderSagaEntity.builder()
                    .sagaId("saga-1")
                    .orderId("order-1")
                    .status(SagaStatus.PENDING)
                    .compensationData(null)
                    .startedAt(LocalDateTime.now())
                    .build();

            // when
            OrderSaga saga = entity.toDomain();

            // then
            assertThat(saga.getCompensationData()).isNotNull();
            assertThat(saga.getCompensationData().isEmpty()).isTrue();
        }
    }

    @Nested
    @DisplayName("updateFromDomain 메서드는")
    class UpdateFromDomainTest {

        @Test
        @DisplayName("Saga 상태가 업데이트된다")
        void shouldUpdateSagaStatus() {
            // given
            OrderSagaEntity entity = createTestSagaEntity();
            OrderSaga saga = entity.toDomain();

            saga.start();
            saga.startStep(SagaStep.STOCK_RESERVE);
            saga.completeStep(SagaStep.STOCK_RESERVE, "stock-data");

            // when
            entity.updateFromDomain(saga);

            // then
            assertThat(entity.getStatus()).isEqualTo(SagaStatus.IN_PROGRESS);
            assertThat(entity.getCurrentStep()).isEqualTo(SagaStep.PAYMENT_VERIFY);
        }

        @Test
        @DisplayName("Step History가 동기화되고 sagaIdValue가 설정된다")
        void shouldSynchronizeStepHistoryWithSagaIdValue() {
            // given
            OrderSagaEntity entity = createTestSagaEntity();
            OrderSaga saga = entity.toDomain();

            saga.start();
            saga.startStep(SagaStep.STOCK_RESERVE);
            saga.completeStep(SagaStep.STOCK_RESERVE, "stock-data");
            saga.startStep(SagaStep.PAYMENT_VERIFY);
            saga.completeStep(SagaStep.PAYMENT_VERIFY, "payment-data");

            // when
            entity.updateFromDomain(saga);

            // then
            assertThat(entity.getStepHistory()).hasSize(2);
            List<SagaStep> steps = entity.getStepHistory().stream()
                    .map(SagaStepHistoryEntity::getStep)
                    .toList();
            assertThat(steps).containsExactly(SagaStep.STOCK_RESERVE, SagaStep.PAYMENT_VERIFY);

            // 모든 Step History의 sagaIdValue가 entity의 sagaId와 일치하는지 확인
            assertThat(entity.getStepHistory())
                    .allMatch(history -> history.getSagaIdValue().equals(entity.getSagaId()));
        }

        @Test
        @DisplayName("보상 데이터가 업데이트된다")
        void shouldUpdateCompensationData() {
            // given
            OrderSagaEntity entity = createTestSagaEntity();
            OrderSaga saga = entity.toDomain();

            saga.start();
            saga.startStep(SagaStep.STOCK_RESERVE);
            saga.completeStep(SagaStep.STOCK_RESERVE, "stock-data");
            saga.startStep(SagaStep.PAYMENT_VERIFY);
            saga.completeStep(SagaStep.PAYMENT_VERIFY, "payment-data");

            // when
            entity.updateFromDomain(saga);

            // then
            assertThat(entity.getCompensationData()).contains("STOCK_RESERVE");
            assertThat(entity.getCompensationData()).contains("PAYMENT_VERIFY");
        }

        @Test
        @DisplayName("Saga 완료 시 완료 시간이 설정된다")
        void shouldSetCompletedAtWhenSagaCompletes() {
            // given
            OrderSagaEntity entity = createTestSagaEntity();
            OrderSaga saga = createCompletedSaga();

            // when
            entity.updateFromDomain(saga);

            // then
            assertThat(entity.getStatus()).isEqualTo(SagaStatus.COMPLETED);
            assertThat(entity.getCompletedAt()).isNotNull();
        }

        @Test
        @DisplayName("Saga 실패 시 실패 사유가 설정된다")
        void shouldSetFailureReasonWhenSagaFails() {
            // given
            OrderSagaEntity entity = createTestSagaEntity();
            OrderSaga saga = entity.toDomain();

            saga.start();
            saga.startStep(SagaStep.STOCK_RESERVE);
            saga.failStep(SagaStep.STOCK_RESERVE, "재고 부족");
            saga.startCompensation("재고 부족");

            // when
            entity.updateFromDomain(saga);

            // then
            assertThat(entity.getStatus()).isEqualTo(SagaStatus.COMPENSATING);
            assertThat(entity.getFailureReason()).isEqualTo("재고 부족");
        }

        @Test
        @DisplayName("이전 Step History를 제거하고 새로운 History로 교체한다")
        void shouldReplaceOldHistoryWithNew() {
            // given
            OrderSagaEntity entity = createSagaEntityWithHistory();
            assertThat(entity.getStepHistory()).hasSize(2);

            OrderSaga saga = entity.toDomain();
            saga.startStep(SagaStep.ROUTE_CALCULATE);
            saga.completeStep(SagaStep.ROUTE_CALCULATE, "route-data");

            // when
            entity.updateFromDomain(saga);

            // then
            assertThat(entity.getStepHistory()).hasSize(3);
            assertThat(entity.getStepHistory())
                    .extracting(SagaStepHistoryEntity::getStep)
                    .containsExactly(
                            SagaStep.STOCK_RESERVE,
                            SagaStep.PAYMENT_VERIFY,
                            SagaStep.ROUTE_CALCULATE
                    );
        }
    }

    @Nested
    @DisplayName("addStepHistory 메서드는")
    class AddStepHistoryTest {

        @Test
        @DisplayName("Step History를 추가하고 양방향 관계를 설정한다")
        void shouldAddStepHistoryAndSetBidirectionalRelationship() {
            // given
            OrderSagaEntity sagaEntity = createTestSagaEntity();
            SagaStepHistoryEntity historyEntity = SagaStepHistoryEntity.builder()
                    .sagaIdValue(sagaEntity.getSagaId())
                    .step(SagaStep.STOCK_RESERVE)
                    .status(StepStatus.SUCCESS)
                    .startedAt(LocalDateTime.now())
                    .build();

            // when
            sagaEntity.addStepHistory(historyEntity);

            // then
            assertThat(sagaEntity.getStepHistory()).contains(historyEntity);
            assertThat(historyEntity.getSaga()).isEqualTo(sagaEntity);
        }

        @Test
        @DisplayName("여러 개의 Step History를 순서대로 추가할 수 있다")
        void shouldAddMultipleStepHistoriesInOrder() {
            // given
            OrderSagaEntity sagaEntity = createTestSagaEntity();

            SagaStepHistoryEntity history1 = createStepHistoryEntity(
                    sagaEntity.getSagaId(), SagaStep.STOCK_RESERVE);
            SagaStepHistoryEntity history2 = createStepHistoryEntity(
                    sagaEntity.getSagaId(), SagaStep.PAYMENT_VERIFY);
            SagaStepHistoryEntity history3 = createStepHistoryEntity(
                    sagaEntity.getSagaId(), SagaStep.ROUTE_CALCULATE);

            // when
            sagaEntity.addStepHistory(history1);
            sagaEntity.addStepHistory(history2);
            sagaEntity.addStepHistory(history3);

            // then
            assertThat(sagaEntity.getStepHistory()).hasSize(3);
            assertThat(sagaEntity.getStepHistory())
                    .extracting(SagaStepHistoryEntity::getStep)
                    .containsExactly(
                            SagaStep.STOCK_RESERVE,
                            SagaStep.PAYMENT_VERIFY,
                            SagaStep.ROUTE_CALCULATE
                    );
        }
    }

    @Nested
    @DisplayName("도메인-엔티티 양방향 변환은")
    class BidirectionalConversionTest {

        @Test
        @DisplayName("도메인 -> 엔티티 -> 도메인 변환 시 데이터가 보존된다")
        void shouldPreserveDataInRoundTripConversion() {
            // given
            OrderSaga originalSaga = createCompletedSaga();

            // when
            OrderSagaEntity entity = OrderSagaEntity.fromDomain(originalSaga);
            OrderSaga convertedSaga = entity.toDomain();

            // then
            // sagaId는 엔티티 생성 시 새로 만들어지므로, 엔티티의 sagaId와 비교
            assertThat(convertedSaga.getSagaIdValue()).isEqualTo(entity.getSagaId());
            assertThat(convertedSaga.getOrderIdValue()).isEqualTo(originalSaga.getOrderIdValue());
            assertThat(convertedSaga.getStatus()).isEqualTo(originalSaga.getStatus());
            assertThat(convertedSaga.getStepHistory()).hasSameSizeAs(originalSaga.getStepHistory());
        }

        @Test
        @DisplayName("여러 번의 업데이트 후에도 데이터 무결성이 유지된다")
        void shouldMaintainDataIntegrityAfterMultipleUpdates() {
            // given
            OrderSaga saga = OrderSaga.create(OrderId.create());
            OrderSagaEntity entity = OrderSagaEntity.fromDomain(saga);

            // when - 여러 단계의 업데이트
            saga = entity.toDomain();
            saga.start();
            entity.updateFromDomain(saga);

            saga = entity.toDomain();
            saga.startStep(SagaStep.STOCK_RESERVE);
            saga.completeStep(SagaStep.STOCK_RESERVE, "stock-data");
            entity.updateFromDomain(saga);

            saga = entity.toDomain();
            saga.startStep(SagaStep.PAYMENT_VERIFY);
            saga.completeStep(SagaStep.PAYMENT_VERIFY, "payment-data");
            entity.updateFromDomain(saga);

            // then
            OrderSaga finalSaga = entity.toDomain();
            assertThat(finalSaga.getStatus()).isEqualTo(SagaStatus.IN_PROGRESS);
            assertThat(finalSaga.getCurrentStep()).isEqualTo(SagaStep.ROUTE_CALCULATE);
            assertThat(finalSaga.getStepHistory()).hasSize(2);
        }

        @Test
        @DisplayName("보상 트랜잭션 실행 시에도 데이터가 정확히 변환된다")
        void shouldConvertCompensationDataCorrectly() {
            // given
            OrderSaga saga = OrderSaga.create(OrderId.create());
            saga.start();
            saga.startStep(SagaStep.STOCK_RESERVE);
            saga.completeStep(SagaStep.STOCK_RESERVE, "stock-data");
            saga.startStep(SagaStep.PAYMENT_VERIFY);
            saga.failStep(SagaStep.PAYMENT_VERIFY, "결제 실패");
            saga.startCompensation("결제 실패");

            // when
            OrderSagaEntity entity = OrderSagaEntity.fromDomain(saga);
            OrderSaga convertedSaga = entity.toDomain();

            // then
            assertThat(convertedSaga.getStatus()).isEqualTo(SagaStatus.COMPENSATING);
            assertThat(convertedSaga.getFailureReason()).isEqualTo("결제 실패");
            assertThat(convertedSaga.getCompensationData().hasStepData("STOCK_RESERVE")).isTrue();
        }
    }

    // ===== 테스트 데이터 생성 헬퍼 메서드 =====

    private OrderSaga createSagaWithSteps() {
        OrderSaga saga = OrderSaga.create(OrderId.create());
        saga.start();
        saga.startStep(SagaStep.STOCK_RESERVE);
        saga.completeStep(SagaStep.STOCK_RESERVE, "stock-reservation-data");
        saga.startStep(SagaStep.PAYMENT_VERIFY);
        saga.completeStep(SagaStep.PAYMENT_VERIFY, "payment-data");
        return saga;
    }

    private OrderSaga createCompletedSaga() {
        OrderSaga saga = OrderSaga.create(OrderId.create());
        saga.start();

        // Step 1: 재고 예약
        saga.startStep(SagaStep.STOCK_RESERVE);
        saga.completeStep(SagaStep.STOCK_RESERVE, "stock-data");

        // Step 2: 결제 검증
        saga.startStep(SagaStep.PAYMENT_VERIFY);
        saga.completeStep(SagaStep.PAYMENT_VERIFY, "payment-data");

        // Step 3: 경로 계산
        saga.startStep(SagaStep.ROUTE_CALCULATE);
        saga.completeStep(SagaStep.ROUTE_CALCULATE, "route-data");

        // Step 4: 허브 배송 생성
        saga.startStep(SagaStep.HUB_DELIVERY_CREATE);
        saga.completeStep(SagaStep.HUB_DELIVERY_CREATE, "hub-delivery-data");

        // Step 5: 업체 배송 생성
        saga.startStep(SagaStep.LAST_MILE_DELIVERY_CREATE);
        saga.completeStep(SagaStep.LAST_MILE_DELIVERY_CREATE, "last-mile-data");

        // Step 6: 알림 발송
        saga.startStep(SagaStep.NOTIFICATION_SEND);
        saga.completeStep(SagaStep.NOTIFICATION_SEND, "notification-data");

        // Step 7: 추적 시작 (마지막 Step - Saga 자동 완료)
        saga.startStep(SagaStep.TRACKING_START);
        saga.completeStep(SagaStep.TRACKING_START, "tracking-data");

        return saga;
    }

    private OrderSagaEntity createTestSagaEntity() {
        return OrderSagaEntity.builder()
                .sagaId("test-saga-id")
                .orderId("test-order-id")
                .status(SagaStatus.PENDING)
                .currentStep(null)
                .compensationData("{}")
                .startedAt(LocalDateTime.now())
                .completedAt(null)
                .failureReason(null)
                .build();
    }

    private OrderSagaEntity createSagaEntityWithHistory() {
        OrderSagaEntity entity = createTestSagaEntity();

        SagaStepHistoryEntity history1 = createStepHistoryEntity(
                entity.getSagaId(), SagaStep.STOCK_RESERVE);
        SagaStepHistoryEntity history2 = createStepHistoryEntity(
                entity.getSagaId(), SagaStep.PAYMENT_VERIFY);

        entity.addStepHistory(history1);
        entity.addStepHistory(history2);

        return entity;
    }

    private OrderSagaEntity createSagaEntityWithCompensationData() {
        OrderSaga saga = createSagaWithSteps();
        return OrderSagaEntity.fromDomain(saga);
    }

    private SagaStepHistoryEntity createStepHistoryEntity(String sagaId, SagaStep step) {
        return SagaStepHistoryEntity.builder()
                .sagaIdValue(sagaId)
                .step(step)
                .status(StepStatus.SUCCESS)
                .startedAt(LocalDateTime.now())
                .completedAt(LocalDateTime.now())
                .retryCount(0)
                .build();
    }
}
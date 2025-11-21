package com.early_express.order_service.domain.order.infrastructure.persistence.entity;

import com.early_express.order_service.domain.order.domain.model.SagaStep;
import com.early_express.order_service.domain.order.domain.model.SagaStepHistory;
import com.early_express.order_service.domain.order.domain.model.StepStatus;
import com.early_express.order_service.domain.order.domain.model.vo.SagaId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.*;

@DisplayName("SagaStepHistoryEntity 테스트")
class SagaStepHistoryEntityTest {

    @Nested
    @DisplayName("fromDomain 메서드는")
    class FromDomainTest {

        @Test
        @DisplayName("도메인 모델의 모든 필드를 엔티티로 정확히 변환한다")
        void shouldConvertAllFieldsFromDomainToEntity() {
            // given
            SagaId sagaId = SagaId.create();
            SagaStepHistory history = SagaStepHistory.create(sagaId, SagaStep.STOCK_RESERVE);
            history.start("request-data");
            history.complete("response-data");

            // when
            SagaStepHistoryEntity entity = SagaStepHistoryEntity.fromDomain(history);

            // then
            assertThat(entity).isNotNull();
            assertThat(entity.getSagaIdValue()).isEqualTo(history.getSagaIdValue());
            assertThat(entity.getStep()).isEqualTo(history.getStep());
            assertThat(entity.getStatus()).isEqualTo(history.getStatus());
            assertThat(entity.getRequest()).isEqualTo(history.getRequest());
            assertThat(entity.getResponse()).isEqualTo(history.getResponse());
            assertThat(entity.getStartedAt()).isEqualTo(history.getStartedAt());
            assertThat(entity.getCompletedAt()).isEqualTo(history.getCompletedAt());
            assertThat(entity.getRetryCount()).isEqualTo(history.getRetryCount());
        }

        @Test
        @DisplayName("실패한 Step의 에러 메시지를 변환한다")
        void shouldConvertErrorMessage() {
            // given
            SagaId sagaId = SagaId.create();
            SagaStepHistory history = SagaStepHistory.create(sagaId, SagaStep.STOCK_RESERVE);
            history.start("request-data");
            history.fail("재고 부족");

            // when
            SagaStepHistoryEntity entity = SagaStepHistoryEntity.fromDomain(history);

            // then
            assertThat(entity.getStatus()).isEqualTo(StepStatus.FAILED);
            assertThat(entity.getErrorMessage()).isEqualTo("재고 부족");
            assertThat(entity.getCompletedAt()).isNotNull();
        }

        @Test
        @DisplayName("재시도 횟수를 정확히 변환한다")
        void shouldConvertRetryCount() {
            // given
            SagaId sagaId = SagaId.create();
            SagaStepHistory history = SagaStepHistory.create(sagaId, SagaStep.PAYMENT_VERIFY);
            history.incrementRetryCount();
            history.incrementRetryCount();
            history.incrementRetryCount();

            // when
            SagaStepHistoryEntity entity = SagaStepHistoryEntity.fromDomain(history);

            // then
            assertThat(entity.getRetryCount()).isEqualTo(3);
        }

        @Test
        @DisplayName("JPA ID가 설정되어 있으면 유지한다")
        void shouldPreserveJpaId() {
            // given
            SagaId sagaId = SagaId.create();
            SagaStepHistory history = SagaStepHistory.create(sagaId, SagaStep.STOCK_RESERVE);
            history.setId(123L);

            // when
            SagaStepHistoryEntity entity = SagaStepHistoryEntity.fromDomain(history);

            // then
            assertThat(entity.getId()).isEqualTo(123L);
        }
    }

    @Nested
    @DisplayName("toDomain 메서드는")
    class ToDomainTest {

        @Test
        @DisplayName("엔티티의 모든 필드를 도메인 모델로 정확히 변환한다")
        void shouldConvertAllFieldsFromEntityToDomain() {
            // given
            SagaStepHistoryEntity entity = createTestStepHistoryEntity();

            // when
            SagaStepHistory history = entity.toDomain();

            // then
            assertThat(history).isNotNull();
            assertThat(history.getSagaIdValue()).isEqualTo(entity.getSagaIdValue());
            assertThat(history.getStep()).isEqualTo(entity.getStep());
            assertThat(history.getStatus()).isEqualTo(entity.getStatus());
            assertThat(history.getRequest()).isEqualTo(entity.getRequest());
            assertThat(history.getResponse()).isEqualTo(entity.getResponse());
            assertThat(history.getStartedAt()).isEqualTo(entity.getStartedAt());
            assertThat(history.getCompletedAt()).isEqualTo(entity.getCompletedAt());
            assertThat(history.getRetryCount()).isEqualTo(entity.getRetryCount());
        }

        @Test
        @DisplayName("실패 정보를 올바르게 복원한다")
        void shouldRestoreFailureInfo() {
            // given
            SagaStepHistoryEntity entity = SagaStepHistoryEntity.builder()
                    .sagaIdValue("saga-123")
                    .step(SagaStep.PAYMENT_VERIFY)
                    .status(StepStatus.FAILED)
                    .errorMessage("결제 검증 실패")
                    .startedAt(LocalDateTime.now().minusMinutes(5))
                    .completedAt(LocalDateTime.now())
                    .retryCount(2)
                    .build();

            // when
            SagaStepHistory history = entity.toDomain();

            // then
            assertThat(history.getStatus()).isEqualTo(StepStatus.FAILED);
            assertThat(history.getErrorMessage()).isEqualTo("결제 검증 실패");
            assertThat(history.isFailed()).isTrue();
        }

        @Test
        @DisplayName("JPA ID를 도메인 모델에 설정한다")
        void shouldSetJpaIdInDomain() {
            // given
            SagaStepHistoryEntity entity = SagaStepHistoryEntity.builder()
                    .id(456L)
                    .sagaIdValue("saga-123")
                    .step(SagaStep.STOCK_RESERVE)
                    .status(StepStatus.SUCCESS)
                    .startedAt(LocalDateTime.now())
                    .completedAt(LocalDateTime.now())
                    .retryCount(0)
                    .build();

            // when
            SagaStepHistory history = entity.toDomain();

            // then
            assertThat(history.getId()).isEqualTo(456L);
        }

        @Test
        @DisplayName("null 값들을 안전하게 처리한다")
        void shouldHandleNullValuesSafely() {
            // given
            SagaStepHistoryEntity entity = SagaStepHistoryEntity.builder()
                    .sagaIdValue("saga-123")
                    .step(SagaStep.STOCK_RESERVE)
                    .status(StepStatus.PENDING)
                    .request(null)
                    .response(null)
                    .errorMessage(null)
                    .startedAt(LocalDateTime.now())
                    .completedAt(null)
                    .retryCount(0)
                    .build();

            // when
            SagaStepHistory history = entity.toDomain();

            // then
            assertThat(history).isNotNull();
            assertThat(history.getRequest()).isNull();
            assertThat(history.getResponse()).isNull();
            assertThat(history.getErrorMessage()).isNull();
            assertThat(history.getCompletedAt()).isNull();
        }
    }

    @Nested
    @DisplayName("setSaga 메서드는")
    class SetSagaTest {

        @Test
        @DisplayName("OrderSagaEntity와의 양방향 관계를 설정한다")
        void shouldSetBidirectionalRelationship() {
            // given
            SagaStepHistoryEntity historyEntity = createTestStepHistoryEntity();
            OrderSagaEntity sagaEntity = OrderSagaEntity.builder()
                    .sagaId(historyEntity.getSagaIdValue())
                    .orderId("order-123")
                    .status(com.early_express.order_service.domain.order.domain.model.SagaStatus.IN_PROGRESS)
                    .startedAt(LocalDateTime.now())
                    .compensationData("{}")
                    .build();

            // when
            historyEntity.setSaga(sagaEntity);

            // then
            assertThat(historyEntity.getSaga()).isEqualTo(sagaEntity);
        }

        @Test
        @DisplayName("여러 번 호출해도 마지막 설정이 유지된다")
        void shouldKeepLastSagaWhenCalledMultipleTimes() {
            // given
            SagaStepHistoryEntity historyEntity = createTestStepHistoryEntity();
            OrderSagaEntity sagaEntity1 = createTestSagaEntity("saga-1");
            OrderSagaEntity sagaEntity2 = createTestSagaEntity("saga-2");

            // when
            historyEntity.setSaga(sagaEntity1);
            historyEntity.setSaga(sagaEntity2);

            // then
            assertThat(historyEntity.getSaga()).isEqualTo(sagaEntity2);
        }
    }

    @Nested
    @DisplayName("도메인-엔티티 양방향 변환은")
    class BidirectionalConversionTest {

        @Test
        @DisplayName("도메인 -> 엔티티 -> 도메인 변환 시 데이터가 보존된다")
        void shouldPreserveDataInRoundTripConversion() {
            // given
            SagaId sagaId = SagaId.create();
            SagaStepHistory originalHistory = SagaStepHistory.create(sagaId, SagaStep.STOCK_RESERVE);
            originalHistory.start("request-data");
            originalHistory.complete("response-data");

            // when
            SagaStepHistoryEntity entity = SagaStepHistoryEntity.fromDomain(originalHistory);
            SagaStepHistory convertedHistory = entity.toDomain();

            // then
            assertThat(convertedHistory.getSagaIdValue()).isEqualTo(originalHistory.getSagaIdValue());
            assertThat(convertedHistory.getStep()).isEqualTo(originalHistory.getStep());
            assertThat(convertedHistory.getStatus()).isEqualTo(originalHistory.getStatus());
            assertThat(convertedHistory.getRequest()).isEqualTo(originalHistory.getRequest());
            assertThat(convertedHistory.getResponse()).isEqualTo(originalHistory.getResponse());
        }

        @Test
        @DisplayName("복잡한 객체의 JSON 직렬화/역직렬화가 정상 동작한다")
        void shouldHandleComplexObjectSerialization() {
            // given
            SagaId sagaId = SagaId.create();
            SagaStepHistory history = SagaStepHistory.create(sagaId, SagaStep.STOCK_RESERVE);

            // 복잡한 객체를 request로 설정
            StockReservationData requestData = new StockReservationData(
                    "product-123", "hub-456", 10);
            history.start(requestData);

            StockReservationResponse responseData = new StockReservationResponse(
                    "reservation-789", true);
            history.complete(responseData);

            // when
            SagaStepHistoryEntity entity = SagaStepHistoryEntity.fromDomain(history);
            SagaStepHistory convertedHistory = entity.toDomain();

            // then
            assertThat(convertedHistory.getRequest()).isNotNull();
            assertThat(convertedHistory.getResponse()).isNotNull();
            assertThat(convertedHistory.getRequest()).contains("product-123");
            assertThat(convertedHistory.getResponse()).contains("reservation-789");
        }

        @Test
        @DisplayName("재시도 시나리오에서도 데이터가 정확히 보존된다")
        void shouldPreserveDataInRetryScenario() {
            // given
            SagaId sagaId = SagaId.create();
            SagaStepHistory history = SagaStepHistory.create(sagaId, SagaStep.PAYMENT_VERIFY);
            history.incrementRetryCount();
            history.incrementRetryCount();
            history.start("payment-request");
            history.fail("네트워크 오류");

            // when
            SagaStepHistoryEntity entity = SagaStepHistoryEntity.fromDomain(history);
            SagaStepHistory convertedHistory = entity.toDomain();

            // then
            assertThat(convertedHistory.getRetryCount()).isEqualTo(2);
            assertThat(convertedHistory.getStatus()).isEqualTo(StepStatus.FAILED);
            assertThat(convertedHistory.getErrorMessage()).isEqualTo("네트워크 오류");
        }

        @Test
        @DisplayName("모든 Step 타입에 대해 변환이 정상 동작한다")
        void shouldWorkForAllStepTypes() {
            // given
            SagaId sagaId = SagaId.create();
            SagaStep[] allSteps = SagaStep.values();

            for (SagaStep step : allSteps) {
                // when
                SagaStepHistory history = SagaStepHistory.create(sagaId, step);
                SagaStepHistoryEntity entity = SagaStepHistoryEntity.fromDomain(history);
                SagaStepHistory converted = entity.toDomain();

                // then
                assertThat(converted.getStep()).isEqualTo(step);
            }
        }
    }

    @Nested
    @DisplayName("엔티티 빌더는")
    class BuilderTest {

        @Test
        @DisplayName("모든 필수 필드가 설정되면 정상 생성된다")
        void shouldBuildWithRequiredFields() {
            // when
            SagaStepHistoryEntity entity = SagaStepHistoryEntity.builder()
                    .sagaIdValue("saga-123")
                    .step(SagaStep.STOCK_RESERVE)
                    .status(StepStatus.PENDING)
                    .startedAt(LocalDateTime.now())
                    .build();

            // then
            assertThat(entity).isNotNull();
            assertThat(entity.getSagaIdValue()).isEqualTo("saga-123");
            assertThat(entity.getStep()).isEqualTo(SagaStep.STOCK_RESERVE);
            assertThat(entity.getStatus()).isEqualTo(StepStatus.PENDING);
        }

        @Test
        @DisplayName("retryCount가 null이면 0으로 초기화된다")
        void shouldInitializeRetryCountToZeroWhenNull() {
            // when
            SagaStepHistoryEntity entity = SagaStepHistoryEntity.builder()
                    .sagaIdValue("saga-123")
                    .step(SagaStep.STOCK_RESERVE)
                    .status(StepStatus.PENDING)
                    .startedAt(LocalDateTime.now())
                    .retryCount(null)
                    .build();

            // then
            assertThat(entity.getRetryCount()).isEqualTo(0);
        }

        @Test
        @DisplayName("선택적 필드들이 null이어도 생성된다")
        void shouldBuildWithOptionalFieldsNull() {
            // when
            SagaStepHistoryEntity entity = SagaStepHistoryEntity.builder()
                    .sagaIdValue("saga-123")
                    .step(SagaStep.STOCK_RESERVE)
                    .status(StepStatus.PENDING)
                    .startedAt(LocalDateTime.now())
                    .request(null)
                    .response(null)
                    .errorMessage(null)
                    .completedAt(null)
                    .build();

            // then
            assertThat(entity).isNotNull();
            assertThat(entity.getRequest()).isNull();
            assertThat(entity.getResponse()).isNull();
            assertThat(entity.getErrorMessage()).isNull();
            assertThat(entity.getCompletedAt()).isNull();
        }
    }

    // ===== 테스트 데이터 생성 헬퍼 메서드 =====

    private SagaStepHistoryEntity createTestStepHistoryEntity() {
        return SagaStepHistoryEntity.builder()
                .sagaIdValue("test-saga-id")
                .step(SagaStep.STOCK_RESERVE)
                .status(StepStatus.SUCCESS)
                .request("{\"productId\":\"product-123\",\"quantity\":10}")
                .response("{\"reservationId\":\"reservation-456\"}")
                .startedAt(LocalDateTime.now().minusMinutes(5))
                .completedAt(LocalDateTime.now())
                .retryCount(0)
                .build();
    }

    private OrderSagaEntity createTestSagaEntity(String sagaId) {
        return OrderSagaEntity.builder()
                .sagaId(sagaId)
                .orderId("order-123")
                .status(com.early_express.order_service.domain.order.domain.model.SagaStatus.IN_PROGRESS)
                .startedAt(LocalDateTime.now())
                .compensationData("{}")
                .build();
    }

    // 테스트용 데이터 클래스
    private record StockReservationData(String productId, String hubId, int quantity) {}
    private record StockReservationResponse(String reservationId, boolean success) {}
}
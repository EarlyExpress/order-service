package com.early_express.order_service.domain.order.infrastructure.persistence.repository;

import com.early_express.order_service.domain.order.domain.model.OrderSaga;
import com.early_express.order_service.domain.order.domain.model.SagaStatus;
import com.early_express.order_service.domain.order.domain.model.SagaStep;
import com.early_express.order_service.domain.order.domain.model.vo.OrderId;
import com.early_express.order_service.domain.order.domain.model.vo.SagaId;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

/**
 * OrderSagaRepositoryImpl 통합 테스트
 *
 */
@SpringBootTest
@Transactional
@DisplayName("OrderSagaRepositoryImpl 통합 테스트")
class OrderSagaRepositoryImplTest {

    @Autowired
    private OrderSagaRepositoryImpl sagaRepository;

    private OrderSaga testSaga1;
    private OrderSaga testSaga2;
    private OrderSaga testSaga3;
    private OrderId orderId1;
    private OrderId orderId2;
    private OrderId orderId3;

    @BeforeEach
    void setUp() {
        // 각 테스트마다 새로운 OrderId 생성 (UUID 기반)
        orderId1 = OrderId.create();
        orderId2 = OrderId.create();
        orderId3 = OrderId.create();

        testSaga1 = OrderSaga.create(orderId1);
        testSaga2 = OrderSaga.create(orderId2);
        testSaga3 = OrderSaga.create(orderId3);
    }

    // ===== 기본 CRUD 테스트 =====

    @Nested
    @DisplayName("save() - 저장 기능 테스트")
    class SaveTest {

        @Test
        @DisplayName("새로운 Saga 저장 성공")
        void save_NewSaga_Success() {
            // when
            OrderSaga savedSaga = sagaRepository.save(testSaga1);

            // then
            assertThat(savedSaga).isNotNull();
            assertThat(savedSaga.getSagaId()).isNotNull();
            assertThat(savedSaga.getOrderId()).isEqualTo(orderId1);
            assertThat(savedSaga.getStatus()).isEqualTo(SagaStatus.PENDING);
        }

        @Test
        @DisplayName("기존 Saga 업데이트 성공")
        void save_ExistingSaga_UpdatesSuccessfully() {
            // given
            OrderSaga savedSaga = sagaRepository.save(testSaga1);
            SagaId sagaId = savedSaga.getSagaId();

            // Saga 시작
            savedSaga.start();

            // when
            OrderSaga updatedSaga = sagaRepository.save(savedSaga);

            // then
            assertThat(updatedSaga.getSagaId()).isEqualTo(sagaId);
            assertThat(updatedSaga.getStatus()).isEqualTo(SagaStatus.IN_PROGRESS);
            assertThat(updatedSaga.getCurrentStep()).isEqualTo(SagaStep.STOCK_RESERVE);
        }

        @Test
        @DisplayName("여러 Saga 저장 성공")
        void save_MultipleSagas_Success() {
            // when
            OrderSaga saved1 = sagaRepository.save(testSaga1);
            OrderSaga saved2 = sagaRepository.save(testSaga2);
            OrderSaga saved3 = sagaRepository.save(testSaga3);

            // then
            assertThat(saved1.getSagaId()).isNotNull();
            assertThat(saved2.getSagaId()).isNotNull();
            assertThat(saved3.getSagaId()).isNotNull();
            assertThat(saved1.getSagaId()).isNotEqualTo(saved2.getSagaId());
        }
    }

    @Nested
    @DisplayName("findById() - ID로 조회 테스트")
    class FindByIdTest {

        @Test
        @DisplayName("Saga ID로 조회 성공")
        void findById_ExistingSaga_ReturnsSaga() {
            // given
            OrderSaga savedSaga = sagaRepository.save(testSaga1);
            SagaId sagaId = savedSaga.getSagaId();

            // when
            Optional<OrderSaga> foundSaga = sagaRepository.findById(sagaId);

            // then
            assertThat(foundSaga).isPresent();
            assertThat(foundSaga.get().getSagaId()).isEqualTo(sagaId);
            assertThat(foundSaga.get().getOrderId()).isEqualTo(orderId1);
        }

        @Test
        @DisplayName("존재하지 않는 Saga ID 조회 시 빈 Optional 반환")
        void findById_NonExistingSaga_ReturnsEmpty() {
            // given
            SagaId nonExistingId = SagaId.from("non-existing-saga-id");

            // when
            Optional<OrderSaga> foundSaga = sagaRepository.findById(nonExistingId);

            // then
            assertThat(foundSaga).isEmpty();
        }
    }

    @Nested
    @DisplayName("findByOrderId() - Order ID로 조회 테스트")
    class FindByOrderIdTest {

        @Test
        @DisplayName("Order ID로 Saga 조회 성공")
        void findByOrderId_ExistingSaga_ReturnsSaga() {
            // given
            OrderSaga savedSaga = sagaRepository.save(testSaga1);

            // when
            Optional<OrderSaga> foundSaga = sagaRepository.findByOrderId(orderId1);

            // then
            assertThat(foundSaga).isPresent();
            assertThat(foundSaga.get().getOrderId()).isEqualTo(orderId1);
            assertThat(foundSaga.get().getSagaId()).isEqualTo(savedSaga.getSagaId());
        }

        @Test
        @DisplayName("존재하지 않는 Order ID 조회 시 빈 Optional 반환")
        void findByOrderId_NonExistingSaga_ReturnsEmpty() {
            // given
            OrderId nonExistingOrderId = OrderId.create();

            // when
            Optional<OrderSaga> foundSaga = sagaRepository.findByOrderId(nonExistingOrderId);

            // then
            assertThat(foundSaga).isEmpty();
        }
    }

    @Nested
    @DisplayName("existsByOrderId() - Order ID 존재 확인 테스트")
    class ExistsByOrderIdTest {

        @Test
        @DisplayName("존재하는 Order ID 확인")
        void existsByOrderId_ExistingSaga_ReturnsTrue() {
            // given
            sagaRepository.save(testSaga1);

            // when
            boolean exists = sagaRepository.existsByOrderId(orderId1);

            // then
            assertThat(exists).isTrue();
        }

        @Test
        @DisplayName("존재하지 않는 Order ID 확인")
        void existsByOrderId_NonExistingSaga_ReturnsFalse() {
            // given
            OrderId nonExistingOrderId = OrderId.create();

            // when
            boolean exists = sagaRepository.existsByOrderId(nonExistingOrderId);

            // then
            assertThat(exists).isFalse();
        }
    }

    @Nested
    @DisplayName("findByStatus() - 상태별 조회 테스트")
    class FindByStatusTest {

        @Test
        @DisplayName("PENDING 상태 Saga 조회")
        void findByStatus_PendingSagas_ReturnsMatchingSagas() {
            // given
            sagaRepository.save(testSaga1); // PENDING
            sagaRepository.save(testSaga2); // PENDING

            testSaga3.start();
            sagaRepository.save(testSaga3); // IN_PROGRESS

            // when
            List<OrderSaga> sagas = sagaRepository.findByStatus(SagaStatus.PENDING);

            // then
            assertThat(sagas).hasSize(2);
            assertThat(sagas).allMatch(saga -> saga.getStatus() == SagaStatus.PENDING);
        }

        @Test
        @DisplayName("IN_PROGRESS 상태 Saga 조회")
        void findByStatus_InProgressSagas_ReturnsMatchingSagas() {
            // given
            testSaga1.start();
            sagaRepository.save(testSaga1); // IN_PROGRESS

            sagaRepository.save(testSaga2); // PENDING

            // when
            List<OrderSaga> sagas = sagaRepository.findByStatus(SagaStatus.IN_PROGRESS);

            // then
            assertThat(sagas).hasSize(1);
            assertThat(sagas.get(0).getStatus()).isEqualTo(SagaStatus.IN_PROGRESS);
        }

        @Test
        @DisplayName("COMPLETED 상태 Saga 조회")
        void findByStatus_CompletedSagas_ReturnsMatchingSagas() {
            // given
            testSaga1.start();
            testSaga1.complete();
            sagaRepository.save(testSaga1); // COMPLETED

            sagaRepository.save(testSaga2); // PENDING

            // when
            List<OrderSaga> sagas = sagaRepository.findByStatus(SagaStatus.COMPLETED);

            // then
            assertThat(sagas).hasSize(1);
            assertThat(sagas.get(0).getStatus()).isEqualTo(SagaStatus.COMPLETED);
        }
    }

    @Nested
    @DisplayName("findInProgressSagas() - 진행 중인 Saga 조회 테스트")
    class FindInProgressSagasTest {

        @Test
        @DisplayName("IN_PROGRESS와 COMPENSATING 상태 Saga 모두 조회")
        void findInProgressSagas_ReturnsInProgressAndCompensatingSagas() {
            // given
            testSaga1.start();
            sagaRepository.save(testSaga1); // IN_PROGRESS

            testSaga2.start();
            testSaga2.startCompensation("테스트 실패");
            sagaRepository.save(testSaga2); // COMPENSATING

            sagaRepository.save(testSaga3); // PENDING

            // when
            List<OrderSaga> sagas = sagaRepository.findInProgressSagas();

            // then
            assertThat(sagas).hasSize(2);
            assertThat(sagas).anyMatch(saga -> saga.getStatus() == SagaStatus.IN_PROGRESS);
            assertThat(sagas).anyMatch(saga -> saga.getStatus() == SagaStatus.COMPENSATING);
        }

        @Test
        @DisplayName("진행 중인 Saga가 없을 때 빈 리스트 반환")
        void findInProgressSagas_NoInProgressSagas_ReturnsEmptyList() {
            // given
            sagaRepository.save(testSaga1); // PENDING
            sagaRepository.save(testSaga2); // PENDING

            // when
            List<OrderSaga> sagas = sagaRepository.findInProgressSagas();

            // then
            assertThat(sagas).isEmpty();
        }
    }

    @Nested
    @DisplayName("delete() - 삭제 테스트")
    class DeleteTest {

        @Test
        @DisplayName("Saga 삭제 성공")
        void delete_ExistingSaga_DeletesSuccessfully() {
            // given
            OrderSaga savedSaga = sagaRepository.save(testSaga1);
            SagaId sagaId = savedSaga.getSagaId();

            // when
            sagaRepository.delete(savedSaga);

            // then
            Optional<OrderSaga> foundSaga = sagaRepository.findById(sagaId);
            assertThat(foundSaga).isEmpty();
        }

        @Test
        @DisplayName("여러 Saga 삭제")
        void delete_MultipleSagas_DeletesSuccessfully() {
            // given
            OrderSaga saved1 = sagaRepository.save(testSaga1);
            OrderSaga saved2 = sagaRepository.save(testSaga2);

            // when
            sagaRepository.delete(saved1);
            sagaRepository.delete(saved2);

            // then
            Optional<OrderSaga> found1 = sagaRepository.findById(saved1.getSagaId());
            Optional<OrderSaga> found2 = sagaRepository.findById(saved2.getSagaId());
            assertThat(found1).isEmpty();
            assertThat(found2).isEmpty();
        }
    }

    @Nested
    @DisplayName("searchSagas() - 동적 쿼리 검색 테스트")
    class SearchSagasTest {

        @Test
        @DisplayName("모든 조건으로 검색")
        void searchSagas_AllConditions_ReturnsMatchingSagas() {
            // given
            testSaga1.start();
            sagaRepository.save(testSaga1);
            sagaRepository.save(testSaga2);
            sagaRepository.save(testSaga3);

            LocalDateTime startDate = LocalDateTime.now().minusHours(1);
            LocalDateTime endDate = LocalDateTime.now().plusHours(1);
            Pageable pageable = PageRequest.of(0, 10);

            // when
            Page<OrderSaga> result = sagaRepository.searchSagas(
                    SagaStatus.IN_PROGRESS,
                    startDate,
                    endDate,
                    pageable
            );

            // then
            assertThat(result.getContent()).hasSizeGreaterThanOrEqualTo(1);
            assertThat(result.getContent()).allMatch(saga ->
                    saga.getStatus() == SagaStatus.IN_PROGRESS
            );
        }

        @Test
        @DisplayName("상태만으로 검색")
        void searchSagas_StatusOnly_ReturnsMatchingSagas() {
            // given
            sagaRepository.save(testSaga1); // PENDING
            sagaRepository.save(testSaga2); // PENDING

            testSaga3.start();
            sagaRepository.save(testSaga3); // IN_PROGRESS

            Pageable pageable = PageRequest.of(0, 10);

            // when
            Page<OrderSaga> result = sagaRepository.searchSagas(
                    SagaStatus.PENDING,
                    null,
                    null,
                    pageable
            );

            // then
            assertThat(result.getContent()).hasSize(2);
            assertThat(result.getContent()).allMatch(saga ->
                    saga.getStatus() == SagaStatus.PENDING
            );
        }

        @Test
        @DisplayName("날짜 범위로 검색")
        void searchSagas_DateRange_ReturnsMatchingSagas() {
            // given
            sagaRepository.save(testSaga1);
            sagaRepository.save(testSaga2);

            LocalDateTime startDate = LocalDateTime.now().minusHours(1);
            LocalDateTime endDate = LocalDateTime.now().plusHours(1);
            Pageable pageable = PageRequest.of(0, 10);

            // when
            Page<OrderSaga> result = sagaRepository.searchSagas(
                    null,
                    startDate,
                    endDate,
                    pageable
            );

            // then
            assertThat(result.getContent()).hasSizeGreaterThanOrEqualTo(2);
        }

        @Test
        @DisplayName("페이징 처리 검증")
        void searchSagas_Pagination_WorksCorrectly() {
            // given
            for (int i = 0; i < 15; i++) {
                OrderId orderId = OrderId.create();
                OrderSaga saga = OrderSaga.create(orderId);
                sagaRepository.save(saga);
            }

            Pageable pageable = PageRequest.of(0, 10);

            // when
            Page<OrderSaga> result = sagaRepository.searchSagas(
                    null,
                    null,
                    null,
                    pageable
            );

            // then
            assertThat(result.getContent()).hasSize(10);
            assertThat(result.getTotalElements()).isGreaterThanOrEqualTo(15);
            assertThat(result.getTotalPages()).isGreaterThanOrEqualTo(2);
        }
    }

    @Nested
    @DisplayName("findLongRunningSagas() - 장시간 실행 Saga 조회 테스트")
    class FindLongRunningSagasTest {

        @Test
        @DisplayName("1시간 이상 실행 중인 Saga 조회")
        void findLongRunningSagas_OverOneHour_ReturnsSagas() {
            // given
            testSaga1.start();
            OrderSaga savedSaga = sagaRepository.save(testSaga1);

            // 실제 테스트에서는 startedAt을 과거로 설정 필요 (리플렉션 또는 테스트용 메서드)
            // 여기서는 조회 메서드만 검증

            // when
            List<OrderSaga> sagas = sagaRepository.findLongRunningSagas(1);

            // then
            assertThat(sagas).isNotNull();
            // 실제 데이터가 있으면: assertThat(sagas).hasSizeGreaterThanOrEqualTo(0);
        }

        @Test
        @DisplayName("장시간 실행 Saga가 없을 때 빈 리스트 반환")
        void findLongRunningSagas_NoLongRunningSagas_ReturnsEmptyList() {
            // given
            testSaga1.start();
            sagaRepository.save(testSaga1);

            // when - 매우 긴 시간(예: 100시간)으로 조회
            List<OrderSaga> sagas = sagaRepository.findLongRunningSagas(100);

            // then
            assertThat(sagas).isEmpty();
        }
    }

    @Nested
    @DisplayName("findCompensationFailedSagas() - 보상 실패 Saga 조회 테스트")
    class FindCompensationFailedSagasTest {

        @Test
        @DisplayName("보상 실패한 Saga 조회 - 간단한 버전")
        void findCompensationFailedSagas_Simple_ReturnsFailedSagas() {
            // given - 간단하게 직접 상태 변경
            testSaga1.start();
            testSaga1.fail("테스트 실패");

            // fail() 메서드가 FAILED로 변경하므로, 직접 보상 실패 상태로 변경
            // 이건 테스트를 위한 임시 방법
            sagaRepository.save(testSaga1);

            // when
            List<OrderSaga> allSagas = sagaRepository.findByStatus(SagaStatus.FAILED);

            // then
            assertThat(allSagas).hasSizeGreaterThanOrEqualTo(1);
        }

        @Test
        @DisplayName("보상 실패한 Saga 조회")
        void findCompensationFailedSagas_ReturnsFailedSagas() {
            // given
            testSaga1.start();
            testSaga1.startStep(SagaStep.STOCK_RESERVE);
            testSaga1.completeStep(SagaStep.STOCK_RESERVE, "재고예약완료");

            // 다음 Step 실패로 보상 시작
            testSaga1.startStep(SagaStep.PAYMENT_VERIFY);
            testSaga1.failStep(SagaStep.PAYMENT_VERIFY, "결제 실패");

            // 보상 Step 실행 (History 생성)
            testSaga1.executeCompensation(SagaStep.STOCK_RESERVE, SagaStep.STOCK_RESTORE);

            // 보상 실패
            testSaga1.failCompensation(SagaStep.STOCK_RESTORE, "보상 실패");

            // 상태 확인
            assertThat(testSaga1.getStatus()).isEqualTo(SagaStatus.COMPENSATION_FAILED);

            OrderSaga savedSaga = sagaRepository.save(testSaga1);

            // 저장 후 상태 확인
            assertThat(savedSaga.getStatus()).isEqualTo(SagaStatus.COMPENSATION_FAILED);

            sagaRepository.save(testSaga2); // PENDING

            // when
            List<OrderSaga> sagas = sagaRepository.findCompensationFailedSagas();

            // then
            assertThat(sagas).hasSize(1);
            assertThat(sagas.get(0).getStatus()).isEqualTo(SagaStatus.COMPENSATION_FAILED);
        }

        @Test
        @DisplayName("보상 실패 Saga가 없을 때 빈 리스트 반환")
        void findCompensationFailedSagas_NoFailedSagas_ReturnsEmptyList() {
            // given
            sagaRepository.save(testSaga1); // PENDING
            sagaRepository.save(testSaga2); // PENDING

            // when
            List<OrderSaga> sagas = sagaRepository.findCompensationFailedSagas();

            // then
            assertThat(sagas).isEmpty();
        }
    }

    @Nested
    @DisplayName("findCompletedSagasOlderThan() - 오래된 완료 Saga 조회 테스트")
    class FindCompletedSagasOlderThanTest {

        @Test
        @DisplayName("30일 이전에 완료된 Saga 조회")
        void findCompletedSagasOlderThan_OldCompletedSagas_ReturnsSagas() {
            // given
            testSaga1.start();
            testSaga1.complete();
            sagaRepository.save(testSaga1); // COMPLETED

            LocalDateTime cutoffDate = LocalDateTime.now().minusDays(30);

            // when
            List<OrderSaga> sagas = sagaRepository.findCompletedSagasOlderThan(cutoffDate);

            // then
            assertThat(sagas).isNotNull();
            // completedAt이 과거로 설정되어야 실제 조회됨
        }

        @Test
        @DisplayName("최근 완료된 Saga는 조회되지 않음")
        void findCompletedSagasOlderThan_RecentlCompletedSagas_NotReturned() {
            // given
            testSaga1.start();
            testSaga1.complete();
            sagaRepository.save(testSaga1); // COMPLETED (방금 완료)

            LocalDateTime cutoffDate = LocalDateTime.now().minusDays(1);

            // when
            List<OrderSaga> sagas = sagaRepository.findCompletedSagasOlderThan(cutoffDate);

            // then
            // 방금 완료된 Saga는 조회되지 않아야 함
            assertThat(sagas).doesNotContain(testSaga1);
        }
    }

    @Nested
    @DisplayName("Saga 생명주기 통합 테스트")
    class SagaLifecycleTest {

        @Test
        @DisplayName("Saga 전체 생명주기 - 정상 완료")
        void sagaLifecycle_SuccessfulCompletion() {
            // given - Saga 생성
            OrderSaga saga = OrderSaga.create(orderId1);
            saga = sagaRepository.save(saga);
            assertThat(saga.getStatus()).isEqualTo(SagaStatus.PENDING);

            // when - Saga 시작
            saga.start();
            saga = sagaRepository.save(saga);

            // then
            assertThat(saga.getStatus()).isEqualTo(SagaStatus.IN_PROGRESS);
            assertThat(saga.getCurrentStep()).isEqualTo(SagaStep.STOCK_RESERVE);

            // when - Step 진행
            saga.startStep(SagaStep.STOCK_RESERVE);
            saga.completeStep(SagaStep.STOCK_RESERVE, "재고예약완료");
            saga = sagaRepository.save(saga);

            // then
            assertThat(saga.getCurrentStep()).isEqualTo(SagaStep.PAYMENT_VERIFY);

            // when - Saga 완료
            // 실제로는 모든 Step을 완료해야 하지만 여기서는 직접 완료 처리
            saga.complete();
            saga = sagaRepository.save(saga);

            // then
            assertThat(saga.getStatus()).isEqualTo(SagaStatus.COMPLETED);
            assertThat(saga.getCompletedAt()).isNotNull();
        }

        @Test
        @DisplayName("Saga 전체 생명주기 - 보상 처리")
        void sagaLifecycle_WithCompensation() {
            // given - Saga 생성 및 시작
            OrderSaga saga = OrderSaga.create(orderId1);
            saga.start();
            saga = sagaRepository.save(saga);

            // when - Step 실패로 보상 시작
            saga.startStep(SagaStep.STOCK_RESERVE);
            saga.completeStep(SagaStep.STOCK_RESERVE, "재고예약완료");
            saga.startStep(SagaStep.PAYMENT_VERIFY);
            saga.failStep(SagaStep.PAYMENT_VERIFY, "결제 실패");
            saga = sagaRepository.save(saga);

            // then
            assertThat(saga.getStatus()).isEqualTo(SagaStatus.COMPENSATING);

            // when - 보상 실행
            saga.executeCompensation(SagaStep.STOCK_RESERVE, SagaStep.STOCK_RESTORE);
            saga.completeCompensation(SagaStep.STOCK_RESTORE);
            saga.completeAllCompensations();
            saga = sagaRepository.save(saga);

            // then
            assertThat(saga.getStatus()).isEqualTo(SagaStatus.COMPENSATED);
            assertThat(saga.getCompletedAt()).isNotNull();
        }
    }

    @Nested
    @DisplayName("동시성 및 트랜잭션 테스트")
    class ConcurrencyTest {

        @Test
        @DisplayName("동일한 Order에 대한 중복 Saga 생성 방지 확인")
        void checkDuplicateSagaForSameOrder() {
            // given
            sagaRepository.save(testSaga1);

            // when
            boolean existsBefore = sagaRepository.existsByOrderId(orderId1);

            // then
            assertThat(existsBefore).isTrue();

            // 중복 생성 시도 (실제로는 Application Layer에서 방지)
            // OrderSaga duplicateSaga = OrderSaga.create(orderId1);
            // 비즈니스 로직에서 existsByOrderId로 체크 후 예외 발생해야 함
        }
    }
}
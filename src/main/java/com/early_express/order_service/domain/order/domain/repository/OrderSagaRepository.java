package com.early_express.order_service.domain.order.domain.repository;

import com.early_express.order_service.domain.order.domain.model.OrderSaga;
import com.early_express.order_service.domain.order.domain.model.SagaStatus;
import com.early_express.order_service.domain.order.domain.model.vo.OrderId;
import com.early_express.order_service.domain.order.domain.model.vo.SagaId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * OrderSaga Domain Repository Interface
 */
public interface OrderSagaRepository {

    /**
     * Saga 저장
     * - ID가 있으면 업데이트 (Dirty Checking)
     * - ID가 없으면 새로 생성
     */
    OrderSaga save(OrderSaga saga);

    /**
     * Saga ID로 조회
     */
    Optional<OrderSaga> findById(SagaId sagaId);

    /**
     * Order ID로 Saga 조회
     */
    Optional<OrderSaga> findByOrderId(OrderId orderId);

    /**
     * Order ID로 Saga 존재 여부 확인
     */
    boolean existsByOrderId(OrderId orderId);

    /**
     * 상태별 Saga 목록 조회
     */
    List<OrderSaga> findByStatus(SagaStatus status);

    /**
     * 진행 중인 Saga 목록 조회
     * (IN_PROGRESS, COMPENSATING 상태)
     */
    List<OrderSaga> findInProgressSagas();

    /**
     * Saga 삭제
     */
    void delete(OrderSaga saga);

    // ===== QueryDSL 동적 쿼리 메서드 =====

    /**
     * Saga 검색 (동적 쿼리)
     *
     * @param status Saga 상태
     * @param startDate 시작일
     * @param endDate 종료일
     * @param pageable 페이징 정보
     * @return Saga 목록 (페이징)
     */
    Page<OrderSaga> searchSagas(
            SagaStatus status,
            LocalDateTime startDate,
            LocalDateTime endDate,
            Pageable pageable);

    /**
     * 장시간 진행 중인 Saga 조회 (타임아웃 의심)
     *
     * @param hours 몇 시간 이상 진행 중
     * @return Saga 목록
     */
    List<OrderSaga> findLongRunningSagas(int hours);

    /**
     * 보상 실패한 Saga 조회 (수동 개입 필요)
     *
     * @return Saga 목록
     */
    List<OrderSaga> findCompensationFailedSagas();

    /**
     * 완료된 Saga 조회 (특정 기간)
     * 정리(Cleanup)용
     *
     * @param date 기준 날짜 (이전 것들 조회)
     * @return Saga 목록
     */
    List<OrderSaga> findCompletedSagasOlderThan(LocalDateTime date);

    public void deleteAll();
}
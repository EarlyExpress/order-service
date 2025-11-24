package com.early_express.order_service.domain.order.application.service;

import com.early_express.order_service.domain.order.domain.exception.OrderErrorCode;
import com.early_express.order_service.domain.order.domain.exception.SagaException;
import com.early_express.order_service.domain.order.domain.model.OrderSaga;
import com.early_express.order_service.domain.order.domain.model.SagaStatus;
import com.early_express.order_service.domain.order.domain.model.vo.OrderId;
import com.early_express.order_service.domain.order.domain.model.vo.SagaId;
import com.early_express.order_service.domain.order.domain.repository.OrderSagaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Order Saga Query Service
 * Saga 조회를 담당
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderSagaQueryService {

    private final OrderSagaRepository sagaRepository;

    /**
     * Saga ID로 조회
     *
     * @param sagaId Saga ID
     * @return Saga
     */
    public OrderSaga getSagaById(String sagaId) {
        log.debug("Saga 조회 - sagaId: {}", sagaId);

        return sagaRepository.findById(SagaId.from(sagaId))
                .orElseThrow(() -> new SagaException(
                        OrderErrorCode.SAGA_NOT_FOUND,
                        "Saga를 찾을 수 없습니다: " + sagaId
                ));
    }

    /**
     * Order ID로 Saga 조회
     *
     * @param orderId 주문 ID
     * @return Saga
     */
    public OrderSaga getSagaByOrderId(String orderId) {
        log.debug("Order ID로 Saga 조회 - orderId: {}", orderId);

        return sagaRepository.findByOrderId(OrderId.from(orderId))
                .orElseThrow(() -> new SagaException(
                        OrderErrorCode.SAGA_NOT_FOUND,
                        "해당 주문의 Saga를 찾을 수 없습니다: " + orderId
                ));
    }

    /**
     * 상태별 Saga 목록 조회
     *
     * @param status Saga 상태
     * @return Saga 목록
     */
    public List<OrderSaga> getSagasByStatus(SagaStatus status) {
        log.debug("상태별 Saga 조회 - status: {}", status);

        return sagaRepository.findByStatus(status);
    }

    /**
     * 진행 중인 Saga 목록 조회
     * (IN_PROGRESS, COMPENSATING 상태)
     *
     * @return Saga 목록
     */
    public List<OrderSaga> getInProgressSagas() {
        log.debug("진행 중인 Saga 조회");

        return sagaRepository.findInProgressSagas();
    }

    /**
     * Saga 검색 (동적 쿼리)
     *
     * @param query 검색 조건
     * @param pageable 페이징 정보
     * @return Saga 목록 (페이징)
     */
    public Page<OrderSaga> searchSagas(SagaSearchQuery query, Pageable pageable) {
        log.debug("Saga 검색 - status: {}, startDate: {}, endDate: {}",
                query.status(), query.startDate(), query.endDate());

        return sagaRepository.searchSagas(
                query.status(),
                query.startDate(),
                query.endDate(),
                pageable
        );
    }

    /**
     * 장시간 진행 중인 Saga 조회 (타임아웃 의심)
     * - 모니터링용
     *
     * @param hours 몇 시간 이상 진행 중
     * @return Saga 목록
     */
    public List<OrderSaga> getLongRunningSagas(int hours) {
        log.debug("장시간 진행 중인 Saga 조회 - hours: {}", hours);

        return sagaRepository.findLongRunningSagas(hours);
    }

    /**
     * 보상 실패한 Saga 조회 (수동 개입 필요)
     * - 모니터링 및 알림용
     *
     * @return Saga 목록
     */
    public List<OrderSaga> getCompensationFailedSagas() {
        log.debug("보상 실패 Saga 조회");

        return sagaRepository.findCompensationFailedSagas();
    }

    /**
     * 완료된 Saga 조회 (특정 기간)
     * - 정리(Cleanup)용
     *
     * @param date 기준 날짜 (이전 것들 조회)
     * @return Saga 목록
     */
    public List<OrderSaga> getCompletedSagasOlderThan(LocalDateTime date) {
        log.debug("완료된 Saga 조회 - date: {}", date);

        return sagaRepository.findCompletedSagasOlderThan(date);
    }

    /**
     * Saga 존재 여부 확인
     *
     * @param sagaId Saga ID
     * @return 존재 여부
     */
    public boolean existsSaga(String sagaId) {
        return sagaRepository.findById(SagaId.from(sagaId)).isPresent();
    }

    /**
     * Order에 대한 Saga 존재 여부 확인
     *
     * @param orderId 주문 ID
     * @return 존재 여부
     */
    public boolean existsSagaForOrder(String orderId) {
        return sagaRepository.existsByOrderId(OrderId.from(orderId));
    }

    /**
     * Saga 검색 Query DTO
     */
    public record SagaSearchQuery(
            SagaStatus status,
            LocalDateTime startDate,
            LocalDateTime endDate
    ) {
    }
}
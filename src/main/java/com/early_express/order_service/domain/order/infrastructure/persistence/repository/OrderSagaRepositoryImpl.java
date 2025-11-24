package com.early_express.order_service.domain.order.infrastructure.persistence.repository;

import com.early_express.order_service.domain.order.domain.model.OrderSaga;
import com.early_express.order_service.domain.order.domain.model.SagaStatus;
import com.early_express.order_service.domain.order.domain.model.vo.OrderId;
import com.early_express.order_service.domain.order.domain.model.vo.SagaId;
import com.early_express.order_service.domain.order.domain.repository.OrderSagaRepository;
import com.early_express.order_service.domain.order.infrastructure.persistence.entity.OrderSagaEntity;
import com.early_express.order_service.domain.order.infrastructure.persistence.entity.QOrderSagaEntity;
import com.early_express.order_service.domain.order.infrastructure.persistence.jpa.OrderSagaJpaRepository;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * OrderSaga Repository 구현체
 */
@Repository
@RequiredArgsConstructor
public class OrderSagaRepositoryImpl implements OrderSagaRepository {

    private final OrderSagaJpaRepository sagaJpaRepository;
    private final JPAQueryFactory queryFactory;
    private final QOrderSagaEntity qSaga = QOrderSagaEntity.orderSagaEntity;

    @Override
    @Transactional
    public OrderSaga save(OrderSaga saga) {
        OrderSagaEntity entity;

        // ID가 있으면 기존 엔티티 조회 후 업데이트 (Dirty Checking)
        if (saga.getSagaId() != null) {
            entity = sagaJpaRepository.findById(saga.getSagaIdValue())
                    .orElseThrow(() -> new IllegalArgumentException("Saga not found: " + saga.getSagaIdValue()));
            entity.updateFromDomain(saga);
        } else {
            // ID가 없으면 새로 생성
            entity = OrderSagaEntity.fromDomain(saga);
        }

        OrderSagaEntity savedEntity = sagaJpaRepository.save(entity);
        return savedEntity.toDomain();
    }

    @Override
    public void deleteAll() {
        sagaJpaRepository.deleteAll();
    }

    @Override
    public Optional<OrderSaga> findById(SagaId sagaId) {
        return sagaJpaRepository.findById(sagaId.getValue())
                .map(OrderSagaEntity::toDomain);
    }

    @Override
    @Transactional
    public Optional<OrderSaga> findByOrderId(OrderId orderId) {
        return sagaJpaRepository.findByOrderId(orderId.getValue())
                .map(OrderSagaEntity::toDomain);
    }

    @Override
    public boolean existsByOrderId(OrderId orderId) {
        return sagaJpaRepository.existsByOrderId(orderId.getValue());
    }

    @Override
    public List<OrderSaga> findByStatus(SagaStatus status) {
        return sagaJpaRepository.findByStatus(status).stream()
                .map(OrderSagaEntity::toDomain)
                .toList();
    }

    @Override
    public List<OrderSaga> findInProgressSagas() {
        List<SagaStatus> inProgressStatuses = Arrays.asList(
                SagaStatus.IN_PROGRESS,
                SagaStatus.COMPENSATING
        );

        return sagaJpaRepository.findByStatusIn(inProgressStatuses).stream()
                .map(OrderSagaEntity::toDomain)
                .toList();
    }

    @Override
    public void delete(OrderSaga saga) {
        sagaJpaRepository.deleteById(saga.getSagaIdValue());
    }

    // ===== QueryDSL 동적 쿼리 메서드 =====

    /**
     * Saga 검색 (동적 쿼리)
     */
    public Page<OrderSaga> searchSagas(
            SagaStatus status,
            LocalDateTime startDate,
            LocalDateTime endDate,
            Pageable pageable) {

        List<OrderSagaEntity> content = queryFactory
                .selectFrom(qSaga)
                .where(
                        statusEq(status),
                        startedAtBetween(startDate, endDate)
                )
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .orderBy(qSaga.startedAt.desc())
                .fetch();

        long total = queryFactory
                .selectFrom(qSaga)
                .where(
                        statusEq(status),
                        startedAtBetween(startDate, endDate)
                )
                .fetchCount();

        List<OrderSaga> sagas = content.stream()
                .map(OrderSagaEntity::toDomain)
                .toList();

        return new PageImpl<>(sagas, pageable, total);
    }

    /**
     * 장시간 진행 중인 Saga 조회 (타임아웃 의심)
     */
    public List<OrderSaga> findLongRunningSagas(int hours) {
        LocalDateTime threshold = LocalDateTime.now().minusHours(hours);

        List<OrderSagaEntity> entities = queryFactory
                .selectFrom(qSaga)
                .where(
                        qSaga.status.in(SagaStatus.IN_PROGRESS, SagaStatus.COMPENSATING),
                        qSaga.startedAt.before(threshold)
                )
                .orderBy(qSaga.startedAt.asc())
                .fetch();

        return entities.stream()
                .map(OrderSagaEntity::toDomain)
                .toList();
    }

    /**
     * 보상 실패한 Saga 조회 (수동 개입 필요)
     */
    public List<OrderSaga> findCompensationFailedSagas() {
        List<OrderSagaEntity> entities = queryFactory
                .selectFrom(qSaga)
                .where(qSaga.status.eq(SagaStatus.COMPENSATION_FAILED))
                .orderBy(qSaga.startedAt.desc())
                .fetch();

        return entities.stream()
                .map(OrderSagaEntity::toDomain)
                .toList();
    }

    /**
     * 완료된 Saga 조회 (특정 기간)
     * 정리(Cleanup)용
     */
    public List<OrderSaga> findCompletedSagasOlderThan(LocalDateTime date) {
        List<OrderSagaEntity> entities = queryFactory
                .selectFrom(qSaga)
                .where(
                        qSaga.status.in(SagaStatus.COMPLETED, SagaStatus.COMPENSATED),
                        qSaga.completedAt.before(date)
                )
                .fetch();

        return entities.stream()
                .map(OrderSagaEntity::toDomain)
                .toList();
    }

    // ===== QueryDSL 조건 메서드 =====

    private BooleanExpression statusEq(SagaStatus status) {
        return status != null ? qSaga.status.eq(status) : null;
    }

    private BooleanExpression startedAtBetween(LocalDateTime startDate, LocalDateTime endDate) {
        if (startDate != null && endDate != null) {
            return qSaga.startedAt.between(startDate, endDate);
        } else if (startDate != null) {
            return qSaga.startedAt.goe(startDate);
        } else if (endDate != null) {
            return qSaga.startedAt.loe(endDate);
        }
        return null;
    }


}

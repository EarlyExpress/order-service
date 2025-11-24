package com.early_express.order_service.domain.order.infrastructure.persistence.repository;

import com.early_express.order_service.domain.order.domain.exception.OrderErrorCode;
import com.early_express.order_service.domain.order.domain.exception.OrderException;
import com.early_express.order_service.domain.order.domain.model.Order;
import com.early_express.order_service.domain.order.domain.model.OrderStatus;
import com.early_express.order_service.domain.order.domain.model.vo.OrderId;
import com.early_express.order_service.domain.order.domain.model.vo.OrderNumber;
import com.early_express.order_service.domain.order.domain.repository.OrderRepository;
import com.early_express.order_service.domain.order.infrastructure.persistence.entity.OrderEntity;
import com.early_express.order_service.domain.order.infrastructure.persistence.entity.QOrderEntity;
import com.early_express.order_service.domain.order.infrastructure.persistence.jpa.OrderJpaRepository;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Order Repository 구현체
 * QueryDSL을 사용한 동적 쿼리 지원
 */
@Repository
@RequiredArgsConstructor
public class OrderRepositoryImpl implements OrderRepository {

    private final OrderJpaRepository orderJpaRepository;
    private final JPAQueryFactory queryFactory;
    private final QOrderEntity qOrder = QOrderEntity.orderEntity;

//    @Override
//    public Order save(Order order) {
//        OrderEntity entity;
//
//        if (order.getId() != null) {
//            entity = orderJpaRepository.findById(order.getIdValue())
//                    .orElseThrow(() -> new IllegalArgumentException("Order not found: " + order.getIdValue()));
//            entity.updateFromDomain(order);
//        } else {
//            entity = OrderEntity.fromDomain(order);
//        }
//
//        OrderEntity savedEntity = orderJpaRepository.save(entity);
//
//        return savedEntity.toDomain();
//    }
    @Override
    @Transactional
    public Order save(Order order) {
        OrderEntity entity;

        if (order.getId() != null) {
            // 기존 주문 업데이트
            entity = orderJpaRepository.findById(order.getIdValue())
                    .orElseThrow(() -> new OrderException(
                            OrderErrorCode.ORDER_NOT_FOUND,
                            "주문을 찾을 수 없습니다: " + order.getIdValue()
                    ));

            // 영속 상태 엔티티 업데이트 (변경 감지)
            entity.updateFromDomain(order);

            // save() 호출은 선택적 (변경 감지로 자동 업데이트됨)
            // 하지만 명시적으로 호출하는 것이 코드 의도를 명확히 함
        } else {
            // 신규 주문 생성
            entity = OrderEntity.fromDomain(order);
            entity = orderJpaRepository.save(entity);
        }

        return entity.toDomain();
    }

    @Override
    public void deleteAll() {
        orderJpaRepository.deleteAll();
    }

    @Override
    public Optional<Order> findById(OrderId orderId) {
        return orderJpaRepository.findById(orderId.getValue())
                .map(OrderEntity::toDomain);
    }

    @Override
    public Optional<Order> findByOrderNumber(OrderNumber orderNumber) {
        return orderJpaRepository.findByOrderNumber(orderNumber.getValue())
                .map(OrderEntity::toDomain);
    }

    @Override
    public boolean existsByOrderNumber(OrderNumber orderNumber) {
        return orderJpaRepository.existsByOrderNumber(orderNumber.getValue());
    }

    @Override
    public List<Order> findByCompanyId(String companyId) {
        return orderJpaRepository.findByCompanyId(companyId).stream()
                .map(OrderEntity::toDomain)
                .toList();
    }

    @Override
    public List<Order> findByStatus(OrderStatus status) {
        return orderJpaRepository.findByStatusAndIsDeletedFalse(status).stream()
                .map(OrderEntity::toDomain)
                .toList();
    }

    @Override
    public Optional<Order> findByLastMileDeliveryId(String lastMileDeliveryId) {
        return orderJpaRepository.findByLastMileDeliveryIdAndIsDeletedFalse(lastMileDeliveryId)
                .map(OrderEntity::toDomain);
    }

    @Override
    public Optional<Order> findByPaymentId(String paymentId) {
        return orderJpaRepository.findByPaymentIdAndIsDeletedFalse(paymentId)
                .map(OrderEntity::toDomain);
    }

    @Override
    public void delete(Order order, String deletedBy) {
        OrderEntity entity = orderJpaRepository.findById(order.getIdValue())
                .orElseThrow(() -> new IllegalArgumentException("Order not found"));
        entity.delete(deletedBy);
        orderJpaRepository.save(entity);
    }

    // ===== QueryDSL 동적 쿼리 메서드 =====

    /**
     * 주문 검색 (동적 쿼리)
     *
     * @param companyId 업체 ID (공급/수령 업체)
     * @param status 주문 상태
     * @param startDate 시작일
     * @param endDate 종료일
     * @param pageable 페이징 정보
     * @return 주문 목록 (페이징)
     */
    public Page<Order> searchOrders(
            String companyId,
            OrderStatus status,
            LocalDateTime startDate,
            LocalDateTime endDate,
            Pageable pageable) {

        List<OrderEntity> content = queryFactory
                .selectFrom(qOrder)
                .where(
                        companyIdEq(companyId),
                        statusEq(status),
                        createdAtBetween(startDate, endDate),
                        qOrder.isDeleted.isFalse()
                )
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .orderBy(qOrder.createdAt.desc())
                .fetch();

        long total = queryFactory
                .selectFrom(qOrder)
                .where(
                        companyIdEq(companyId),
                        statusEq(status),
                        createdAtBetween(startDate, endDate),
                        qOrder.isDeleted.isFalse()
                )
                .fetchCount();

        List<Order> orders = content.stream()
                .map(OrderEntity::toDomain)
                .toList();

        return new PageImpl<>(orders, pageable, total);
    }

    /**
     * 발송 시한 임박 주문 조회
     *
     * @param hours 몇 시간 내
     * @return 주문 목록
     */
    public List<Order> findOrdersWithUpcomingDeadline(int hours) {
        LocalDateTime threshold = LocalDateTime.now().plusHours(hours);

        List<OrderEntity> entities = queryFactory
                .selectFrom(qOrder)
                .where(
                        qOrder.status.eq(OrderStatus.CONFIRMED),
                        qOrder.calculatedDepartureDeadline.before(threshold),
                        qOrder.calculatedDepartureDeadline.after(LocalDateTime.now()),
                        qOrder.isDeleted.isFalse()
                )
                .orderBy(qOrder.calculatedDepartureDeadline.asc())
                .fetch();

        return entities.stream()
                .map(OrderEntity::toDomain)
                .toList();
    }

    /**
     * 허브별 주문 목록 조회 (페이징)
     * - supplierHubId, receiverHubId, destinationHubId 중 하나라도 일치하면 조회
     *
     * @param hubId 허브 ID
     * @param pageable 페이징 정보
     * @return 주문 목록 (페이징)
     */
    @Override
    public Page<Order> findByHubId(String hubId, Pageable pageable) {
        List<OrderEntity> content = queryFactory
                .selectFrom(qOrder)
                .where(
                        hubIdEq(hubId),
                        qOrder.isDeleted.isFalse()
                )
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .orderBy(qOrder.createdAt.desc())
                .fetch();

        long total = queryFactory
                .selectFrom(qOrder)
                .where(
                        hubIdEq(hubId),
                        qOrder.isDeleted.isFalse()
                )
                .fetchCount();

        List<Order> orders = content.stream()
                .map(OrderEntity::toDomain)
                .toList();

        return new PageImpl<>(orders, pageable, total);
    }

    /**
     * 허브별 주문 검색 (동적 쿼리 + 페이징)
     * - 상태, 날짜 필터링 포함
     *
     * @param hubId 허브 ID
     * @param status 주문 상태 (nullable)
     * @param startDate 시작일 (nullable)
     * @param endDate 종료일 (nullable)
     * @param pageable 페이징 정보
     * @return 주문 목록 (페이징)
     */
    @Override
    public Page<Order> searchOrdersByHubId(
            String hubId,
            OrderStatus status,
            LocalDateTime startDate,
            LocalDateTime endDate,
            Pageable pageable) {

        List<OrderEntity> content = queryFactory
                .selectFrom(qOrder)
                .where(
                        hubIdEq(hubId),
                        statusEq(status),
                        createdAtBetween(startDate, endDate),
                        qOrder.isDeleted.isFalse()
                )
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .orderBy(qOrder.createdAt.desc())
                .fetch();

        long total = queryFactory
                .selectFrom(qOrder)
                .where(
                        hubIdEq(hubId),
                        statusEq(status),
                        createdAtBetween(startDate, endDate),
                        qOrder.isDeleted.isFalse()
                )
                .fetchCount();

        List<Order> orders = content.stream()
                .map(OrderEntity::toDomain)
                .toList();

        return new PageImpl<>(orders, pageable, total);
    }

    /**
     * 발송 시한 초과 주문 조회
     *
     * @return 주문 목록
     */
    public List<Order> findOverdueOrders() {
        List<OrderEntity> entities = queryFactory
                .selectFrom(qOrder)
                .where(
                        qOrder.status.eq(OrderStatus.CONFIRMED),
                        qOrder.calculatedDepartureDeadline.before(LocalDateTime.now()),
                        qOrder.actualDepartureTime.isNull(),
                        qOrder.isDeleted.isFalse()
                )
                .orderBy(qOrder.calculatedDepartureDeadline.asc())
                .fetch();

        return entities.stream()
                .map(OrderEntity::toDomain)
                .toList();
    }

    /**
     * 관리자용: 삭제된 주문 포함 조회
     *
     * @param orderId 주문 ID
     * @return 주문 (삭제된 것 포함)
     */
    public Optional<Order> findByIdIncludingDeleted(OrderId orderId) {
        return orderJpaRepository.findById(orderId.getValue())
                .map(OrderEntity::toDomain);
    }

    /**
     * 관리자용: 전체 주문 검색 (삭제된 것 포함)
     */
    public Page<Order> searchAllOrdersIncludingDeleted(
            String companyId,
            OrderStatus status,
            Boolean isDeleted,
            LocalDateTime startDate,
            LocalDateTime endDate,
            Pageable pageable) {

        List<OrderEntity> content = queryFactory
                .selectFrom(qOrder)
                .where(
                        companyIdEq(companyId),
                        statusEq(status),
                        isDeletedEq(isDeleted),
                        createdAtBetween(startDate, endDate)
                )
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .orderBy(qOrder.createdAt.desc())
                .fetch();

        long total = queryFactory
                .selectFrom(qOrder)
                .where(
                        companyIdEq(companyId),
                        statusEq(status),
                        isDeletedEq(isDeleted),
                        createdAtBetween(startDate, endDate)
                )
                .fetchCount();

        List<Order> orders = content.stream()
                .map(OrderEntity::toDomain)
                .toList();

        return new PageImpl<>(orders, pageable, total);
    }

    // ===== QueryDSL 조건 메서드 =====

    /**
     * 허브 ID 조건
     * - supplierHubId, receiverHubId, destinationHubId 중 하나라도 일치
     */
    private BooleanExpression hubIdEq(String hubId) {
        if (hubId == null) {
            return null;
        }
        return qOrder.supplierHubId.eq(hubId)
                .or(qOrder.receiverHubId.eq(hubId))
                .or(qOrder.destinationHubId.eq(hubId));
    }


    private BooleanExpression companyIdEq(String companyId) {
        if (companyId == null) {
            return null;
        }
        return qOrder.supplierCompanyId.eq(companyId)
                .or(qOrder.receiverCompanyId.eq(companyId));
    }

    private BooleanExpression statusEq(OrderStatus status) {
        return status != null ? qOrder.status.eq(status) : null;
    }

    private BooleanExpression isDeletedEq(Boolean isDeleted) {
        return isDeleted != null ? qOrder.isDeleted.eq(isDeleted) : null;
    }

    private BooleanExpression createdAtBetween(LocalDateTime startDate, LocalDateTime endDate) {
        if (startDate != null && endDate != null) {
            return qOrder.createdAt.between(startDate, endDate);
        } else if (startDate != null) {
            return qOrder.createdAt.goe(startDate);
        } else if (endDate != null) {
            return qOrder.createdAt.loe(endDate);
        }
        return null;
    }
}

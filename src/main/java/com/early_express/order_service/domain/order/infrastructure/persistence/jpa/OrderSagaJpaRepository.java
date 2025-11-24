package com.early_express.order_service.domain.order.infrastructure.persistence.jpa;

import com.early_express.order_service.domain.order.domain.model.SagaStatus;
import com.early_express.order_service.domain.order.infrastructure.persistence.entity.OrderSagaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * OrderSaga JPA Repository
 */
public interface OrderSagaJpaRepository extends JpaRepository<OrderSagaEntity, String> {

    /**
     * Order ID로 Saga 조회
     */
    Optional<OrderSagaEntity> findByOrderId(String orderId);

    /**
     * Order ID로 Saga 존재 여부 확인
     */
    boolean existsByOrderId(String orderId);

    /**
     * 상태별 Saga 목록 조회
     */
    List<OrderSagaEntity> findByStatus(SagaStatus status);

    /**
     * 진행 중인 Saga 목록 조회
     */
    List<OrderSagaEntity> findByStatusIn(List<SagaStatus> statuses);
}

package com.early_express.order_service.domain.order.infrastructure.persistence.jpa;

import com.early_express.order_service.domain.order.domain.model.OrderStatus;
import com.early_express.order_service.domain.order.infrastructure.persistence.entity.OrderEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/**
 * Order JPA Repository
 */
public interface OrderJpaRepository extends JpaRepository<OrderEntity, String> {

    /**
     * 주문 번호로 조회
     */
    Optional<OrderEntity> findByOrderNumber(String orderNumber);

    /**
     * 주문 번호로 존재 여부 확인
     */
    boolean existsByOrderNumber(String orderNumber);

    /**
     * 업체별 주문 목록 조회
     */
    @Query("SELECT o FROM OrderEntity o " +
            "WHERE o.supplierCompanyId = :companyId OR o.receiverCompanyId = :companyId " +
            "AND o.isDeleted = false " +
            "ORDER BY o.createdAt DESC")
    List<OrderEntity> findByCompanyId(@Param("companyId") String companyId);

    /**
     * 상태별 주문 목록 조회
     */
    List<OrderEntity> findByStatusAndIsDeletedFalse(OrderStatus status);

    /**
     * 배송 ID로 주문 조회
     */
    Optional<OrderEntity> findByLastMileDeliveryIdAndIsDeletedFalse(String lastMileDeliveryId);

    /**
     * 결제 ID로 주문 조회
     */
    Optional<OrderEntity> findByPaymentIdAndIsDeletedFalse(String paymentId);
}

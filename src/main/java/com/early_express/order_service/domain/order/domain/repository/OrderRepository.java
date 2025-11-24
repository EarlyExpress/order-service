package com.early_express.order_service.domain.order.domain.repository;

import com.early_express.order_service.domain.order.domain.model.Order;
import com.early_express.order_service.domain.order.domain.model.OrderStatus;
import com.early_express.order_service.domain.order.domain.model.vo.OrderId;
import com.early_express.order_service.domain.order.domain.model.vo.OrderNumber;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Order Domain Repository Interface
 *
 * 도메인 레이어의 Repository 인터페이스
 * Infrastructure 레이어에서 구현
 */
public interface OrderRepository {

    /**
     * 주문 저장
     * - ID가 있으면 업데이트 (Dirty Checking)
     * - ID가 없으면 새로 생성
     */
    Order save(Order order);

    /**
     * 주문 ID로 조회
     */
    Optional<Order> findById(OrderId orderId);

    /**
     * 주문 번호로 조회
     */
    Optional<Order> findByOrderNumber(OrderNumber orderNumber);

    /**
     * 주문 번호 존재 여부 확인
     */
    boolean existsByOrderNumber(OrderNumber orderNumber);

    /**
     * 업체별 주문 목록 조회
     */
    List<Order> findByCompanyId(String companyId);

    /**
     * 상태별 주문 목록 조회
     */
    List<Order> findByStatus(OrderStatus status);

    /**
     * 배송 ID로 주문 조회
     */
    Optional<Order> findByLastMileDeliveryId(String lastMileDeliveryId);

    /**
     * 결제 ID로 주문 조회
     */
    Optional<Order> findByPaymentId(String paymentId);

    /**
     * 주문 삭제 (Soft Delete)
     */
    void delete(Order order, String deletedBy);

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
    Page<Order> searchOrders(
            String companyId,
            OrderStatus status,
            LocalDateTime startDate,
            LocalDateTime endDate,
            Pageable pageable);

    /**
     * 허브별 주문 목록 조회 (페이징)
     * - 공급 허브, 수령 허브, 도착 허브 중 하나라도 해당되면 조회
     *
     * @param hubId 허브 ID
     * @param pageable 페이징 정보
     * @return 주문 목록 (페이징)
     */
    Page<Order> findByHubId(String hubId, Pageable pageable);

    /**
     * 허브별 주문 검색 (동적 쿼리 + 페이징)
     * - 상태 필터링 포함
     *
     * @param hubId 허브 ID
     * @param status 주문 상태 (nullable)
     * @param startDate 시작일 (nullable)
     * @param endDate 종료일 (nullable)
     * @param pageable 페이징 정보
     * @return 주문 목록 (페이징)
     */
    Page<Order> searchOrdersByHubId(
            String hubId,
            OrderStatus status,
            LocalDateTime startDate,
            LocalDateTime endDate,
            Pageable pageable);

    /**
     * 발송 시한 임박 주문 조회
     *
     * @param hours 몇 시간 내
     * @return 주문 목록
     */
    List<Order> findOrdersWithUpcomingDeadline(int hours);

    /**
     * 발송 시한 초과 주문 조회
     *
     * @return 주문 목록
     */
    List<Order> findOverdueOrders();

    /**
     * 관리자용: 삭제된 주문 포함 조회
     *
     * @param orderId 주문 ID
     * @return 주문 (삭제된 것 포함)
     */
    Optional<Order> findByIdIncludingDeleted(OrderId orderId);

    /**
     * 관리자용: 전체 주문 검색 (삭제된 것 포함)
     *
     * @param companyId 업체 ID
     * @param status 주문 상태
     * @param isDeleted 삭제 여부
     * @param startDate 시작일
     * @param endDate 종료일
     * @param pageable 페이징 정보
     * @return 주문 목록 (페이징)
     */
    Page<Order> searchAllOrdersIncludingDeleted(
            String companyId,
            OrderStatus status,
            Boolean isDeleted,
            LocalDateTime startDate,
            LocalDateTime endDate,
            Pageable pageable);

    public void deleteAll();
}
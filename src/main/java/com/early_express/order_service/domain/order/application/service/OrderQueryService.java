package com.early_express.order_service.domain.order.application.service;

import com.early_express.order_service.domain.order.domain.exception.OrderErrorCode;
import com.early_express.order_service.domain.order.domain.exception.OrderException;
import com.early_express.order_service.domain.order.domain.model.Order;
import com.early_express.order_service.domain.order.domain.model.OrderStatus;
import com.early_express.order_service.domain.order.domain.model.vo.OrderId;
import com.early_express.order_service.domain.order.domain.model.vo.OrderNumber;
import com.early_express.order_service.domain.order.domain.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Order Query Service
 * 주문 조회를 담당
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderQueryService {

    private final OrderRepository orderRepository;

    /**
     * 주문 ID로 조회
     *
     * @param orderId 주문 ID
     * @return 주문
     */
    public Order getOrderById(String orderId) {
        log.debug("주문 조회 - orderId: {}", orderId);

        return orderRepository.findById(OrderId.from(orderId))
                .orElseThrow(() -> new OrderException(
                        OrderErrorCode.ORDER_NOT_FOUND,
                        "주문을 찾을 수 없습니다: " + orderId
                ));
    }

    /**
     * 주문 번호로 조회
     *
     * @param orderNumber 주문 번호
     * @return 주문
     */
    public Order getOrderByOrderNumber(String orderNumber) {
        log.debug("주문 조회 - orderNumber: {}", orderNumber);

        return orderRepository.findByOrderNumber(OrderNumber.from(orderNumber))
                .orElseThrow(() -> new OrderException(
                        OrderErrorCode.ORDER_NOT_FOUND,
                        "주문을 찾을 수 없습니다: " + orderNumber
                ));
    }

    /**
     * 업체별 주문 목록 조회
     *
     * @param companyId 업체 ID (공급 또는 수령)
     * @return 주문 목록
     */
    public List<Order> getOrdersByCompanyId(String companyId) {
        log.debug("업체별 주문 조회 - companyId: {}", companyId);

        return orderRepository.findByCompanyId(companyId);
    }

    /**
     * 상태별 주문 목록 조회
     *
     * @param status 주문 상태
     * @return 주문 목록
     */
    public List<Order> getOrdersByStatus(OrderStatus status) {
        log.debug("상태별 주문 조회 - status: {}", status);

        return orderRepository.findByStatus(status);
    }

    /**
     * 배송 ID로 주문 조회
     *
     * @param lastMileDeliveryId 업체 배송 ID
     * @return 주문
     */
    public Order getOrderByLastMileDeliveryId(String lastMileDeliveryId) {
        log.debug("배송 ID로 주문 조회 - deliveryId: {}", lastMileDeliveryId);

        return orderRepository.findByLastMileDeliveryId(lastMileDeliveryId)
                .orElseThrow(() -> new OrderException(
                        OrderErrorCode.ORDER_NOT_FOUND,
                        "해당 배송 ID의 주문을 찾을 수 없습니다: " + lastMileDeliveryId
                ));
    }

    /**
     * 결제 ID로 주문 조회
     *
     * @param paymentId 결제 ID
     * @return 주문
     */
    public Order getOrderByPaymentId(String paymentId) {
        log.debug("결제 ID로 주문 조회 - paymentId: {}", paymentId);

        return orderRepository.findByPaymentId(paymentId)
                .orElseThrow(() -> new OrderException(
                        OrderErrorCode.ORDER_NOT_FOUND,
                        "해당 결제 ID의 주문을 찾을 수 없습니다: " + paymentId
                ));
    }

    /**
     * 허브별 주문 목록 조회 (페이징)
     * - 공급 허브, 수령 허브, 도착 허브 중 하나라도 해당되면 조회
     *
     * @param hubId 허브 ID
     * @param pageable 페이징 정보
     * @return 주문 목록 (페이징)
     */
    public Page<Order> getOrdersByHubId(String hubId, Pageable pageable) {
        log.debug("허브별 주문 조회 - hubId: {}", hubId);

        return orderRepository.findByHubId(hubId, pageable);
    }

    /**
     * 허브별 주문 검색 (동적 쿼리)
     * - 상태, 날짜 필터링 포함
     *
     * @param query 검색 조건
     * @param pageable 페이징 정보
     * @return 주문 목록 (페이징)
     */
    public Page<Order> searchOrdersByHubId(HubOrderSearchQuery query, Pageable pageable) {
        log.debug("허브별 주문 검색 - hubId: {}, status: {}, startDate: {}, endDate: {}",
                query.hubId(), query.status(), query.startDate(), query.endDate());

        return orderRepository.searchOrdersByHubId(
                query.hubId(),
                query.status(),
                query.startDate(),
                query.endDate(),
                pageable
        );
    }

    /**
     * 주문 검색 (동적 쿼리)
     *
     * @param query 검색 조건
     * @param pageable 페이징 정보
     * @return 주문 목록 (페이징)
     */
    public Page<Order> searchOrders(OrderSearchQuery query, Pageable pageable) {
        log.debug("주문 검색 - companyId: {}, status: {}, startDate: {}, endDate: {}",
                query.companyId(), query.status(), query.startDate(), query.endDate());

        return orderRepository.searchOrders(
                query.companyId(),
                query.status(),
                query.startDate(),
                query.endDate(),
                pageable
        );
    }

    /**
     * 발송 시한 임박 주문 조회
     * - 모니터링 및 알림용
     *
     * @param hours 몇 시간 내
     * @return 주문 목록
     */
    public List<Order> getOrdersWithUpcomingDeadline(int hours) {
        log.debug("발송 시한 임박 주문 조회 - hours: {}", hours);

        return orderRepository.findOrdersWithUpcomingDeadline(hours);
    }

    /**
     * 발송 시한 초과 주문 조회
     * - 모니터링 및 알림용
     *
     * @return 주문 목록
     */
    public List<Order> getOverdueOrders() {
        log.debug("발송 시한 초과 주문 조회");

        return orderRepository.findOverdueOrders();
    }

    /**
     * 주문 존재 여부 확인
     *
     * @param orderId 주문 ID
     * @return 존재 여부
     */
    public boolean existsOrder(String orderId) {
        return orderRepository.findById(OrderId.from(orderId)).isPresent();
    }

    /**
     * 주문 번호 존재 여부 확인
     *
     * @param orderNumber 주문 번호
     * @return 존재 여부
     */
    public boolean existsOrderNumber(String orderNumber) {
        return orderRepository.existsByOrderNumber(OrderNumber.from(orderNumber));
    }

    /**
     * 관리자용: 삭제된 주문 포함 조회
     *
     * @param orderId 주문 ID
     * @return 주문 (삭제된 것 포함)
     */
    public Order getOrderByIdIncludingDeleted(String orderId) {
        log.debug("주문 조회 (삭제 포함) - orderId: {}", orderId);

        return orderRepository.findByIdIncludingDeleted(OrderId.from(orderId))
                .orElseThrow(() -> new OrderException(
                        OrderErrorCode.ORDER_NOT_FOUND,
                        "주문을 찾을 수 없습니다: " + orderId
                ));
    }

    /**
     * 관리자용: 전체 주문 검색 (삭제된 것 포함)
     *
     * @param query 검색 조건
     * @param pageable 페이징 정보
     * @return 주문 목록 (페이징)
     */
    public Page<Order> searchAllOrdersIncludingDeleted(
            OrderSearchQueryWithDeleted query,
            Pageable pageable) {

        log.debug("주문 검색 (삭제 포함) - companyId: {}, status: {}, isDeleted: {}",
                query.companyId(), query.status(), query.isDeleted());

        return orderRepository.searchAllOrdersIncludingDeleted(
                query.companyId(),
                query.status(),
                query.isDeleted(),
                query.startDate(),
                query.endDate(),
                pageable
        );
    }

    /**
     * 주문 검색 Query DTO
     */
    public record OrderSearchQuery(
            String companyId,
            OrderStatus status,
            LocalDateTime startDate,
            LocalDateTime endDate
    ) {
    }

    /**
     * 허브별 주문 검색 Query DTO
     */
    public record HubOrderSearchQuery(
            String hubId,
            OrderStatus status,
            LocalDateTime startDate,
            LocalDateTime endDate
    ) {
    }

    /**
     * 주문 검색 Query DTO (삭제 포함)
     */
    public record OrderSearchQueryWithDeleted(
            String companyId,
            OrderStatus status,
            Boolean isDeleted,
            LocalDateTime startDate,
            LocalDateTime endDate
    ) {
    }
}
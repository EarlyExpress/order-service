package com.early_express.order_service.domain.order.application.service;

import com.early_express.order_service.domain.order.application.dto.OrderCreateCommand;
import com.early_express.order_service.domain.order.domain.exception.OrderErrorCode;
import com.early_express.order_service.domain.order.domain.exception.OrderException;
import com.early_express.order_service.domain.order.domain.model.Order;
import com.early_express.order_service.domain.order.domain.model.vo.*;
import com.early_express.order_service.domain.order.domain.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Order Command Service
 * 주문 생성 및 상태 변경을 담당
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class OrderCommandService {

    private final OrderRepository orderRepository;
    private final OrderSagaOrchestratorService sagaOrchestratorService;
    private final OrderNumberGeneratorService orderNumberGeneratorService;

    /**
     * 주문 생성
     * - Order 도메인 생성
     * - DB 저장
     * - Saga 시작 (동기: Step 1, 2 / 비동기: Step 3~)
     *
     * 주의: Saga 실패 시에도 Order는 반환됨 (상태: FAILED 또는 COMPENSATED)
     *
     * @return 생성된 주문 (성공 또는 실패)
     */
    public Order createOrder(OrderCreateCommand command) {
        log.info("주문 생성 시작 - companyId: {}, productId: {}",
                command.getSupplierCompanyId(), command.getProductId());

        // 1. 주문 번호 생성 (OrderNumberGeneratorService 사용)
        OrderNumber orderNumber = orderNumberGeneratorService.generateOrderNumber();

        // 2. Value Objects 생성
        CompanyInfo companyInfo = CompanyInfo.of(
                command.getSupplierCompanyId(),
                command.getSupplierHubId(),
                command.getReceiverCompanyId(),
                command.getReceiverHubId()
        );

        ProductInfo productInfo = ProductInfo.of(
                command.getProductId(),
                command.getQuantity()
        );

        ReceiverInfo receiverInfo = ReceiverInfo.of(
                command.getReceiverName(),
                command.getReceiverPhone(),
                command.getReceiverEmail(),
                command.getDeliveryAddress(),
                command.getDeliveryAddressDetail(),
                command.getDeliveryPostalCode(),
                command.getDeliveryNote()
        );

        RequestInfo requestInfo = RequestInfo.of(
                command.getRequestedDeliveryDate(),
                command.getRequestedDeliveryTime(),
                command.getSpecialInstructions()
        );

        PgPaymentInfo pgPaymentInfo = PgPaymentInfo.of(
                command.getPgProvider(),
                command.getPgPaymentId()
        );

        // 3. Order Aggregate 생성
        Order order = Order.create(
                orderNumber,
                companyInfo,
                productInfo,
                receiverInfo,
                requestInfo,
                command.getUnitPrice(),
                pgPaymentInfo,
                command.getCreatedBy()
        );

        // 4. 주문 저장 (PENDING 상태)
        Order savedOrder = orderRepository.save(order);

        log.info("주문 생성 완료 - orderId: {}, orderNumber: {}",
                savedOrder.getIdValue(), savedOrder.getOrderNumberValue());

        // 5. Saga 시작 (동기: Step 1, 2 → 비동기: Step 3~)
        // 실패 시 내부에서 보상 트랜잭션 자동 실행
        try {
            sagaOrchestratorService.startOrderSaga(savedOrder);
            log.info("Saga 시작 완료 - orderId: {}", savedOrder.getIdValue());

            // 성공 시 최신 Order 반환
            return orderRepository.findById(savedOrder.getId())
                    .orElse(savedOrder);

        } catch (Exception e) {
            log.error("Saga 실행 실패 - orderId: {}, error: {}",
                    savedOrder.getIdValue(), e.getMessage(), e);

            // 보상 트랜잭션은 이미 handleSagaFailure()에서 처리됨
            // Order 상태 재조회하여 최신 상태 반환 (FAILED 또는 COMPENSATED)
            Order failedOrder = orderRepository.findById(savedOrder.getId())
                    .orElse(savedOrder);

            log.warn("주문 생성 실패 처리 완료 - orderId: {}, status: {}",
                    failedOrder.getIdValue(), failedOrder.getStatus().getDescription());

            // 실패한 Order 반환 (API에서 상태 확인 가능)
            return failedOrder;
        }
    }

    /**
     * 주문 취소
     * - 취소 가능 상태 검증 (도메인)
     * - 상태 변경 및 저장 (Dirty Checking)
     *
     * @param orderId 주문 ID
     * @param cancelReason 취소 사유
     */
    public void cancelOrder(String orderId, String cancelReason) {
        log.info("주문 취소 시작 - orderId: {}, reason: {}", orderId, cancelReason);

        // 1. 주문 조회
        Order order = findOrderById(orderId);

        // 2. 취소 처리 (도메인 검증 포함)
        order.cancel(cancelReason);

        // 3. 저장 (Dirty Checking)
        orderRepository.save(order);

        log.info("주문 취소 완료 - orderId: {}, status: {}",
                orderId, order.getStatus().getDescription());

        // TODO: 취소 이벤트 발행 (보상 트랜잭션 트리거)
        // publishOrderCancelledEvent(order);
    }

    /**
     * 주문 실패 처리
     * - Saga 실패 시 호출
     *
     * @param orderId 주문 ID
     */
    public void failOrder(String orderId) {
        log.info("주문 실패 처리 - orderId: {}", orderId);

        Order order = findOrderById(orderId);
        order.fail();
        orderRepository.save(order);

        log.info("주문 실패 완료 - orderId: {}, status: FAILED", orderId);
    }

    /**
     * 주문 보상 완료 처리
     * - 보상 트랜잭션 완료 시 호출
     *
     * @param orderId 주문 ID
     */
    public void compensateOrder(String orderId) {
        log.info("주문 보상 완료 처리 - orderId: {}", orderId);

        Order order = findOrderById(orderId);
        order.compensate();
        orderRepository.save(order);

        log.info("주문 보상 완료 - orderId: {}, status: COMPENSATED", orderId);
    }

    /**
     * 배송 시작 (허브 배송)
     * - 외부 이벤트 수신 시 호출
     *
     * @param orderId 주문 ID
     * @param actualDepartureTime 실제 발송 시간
     */
    public void startHubDelivery(String orderId, LocalDateTime actualDepartureTime) {
        log.info("허브 배송 시작 - orderId: {}", orderId);

        Order order = findOrderById(orderId);
        order.startHubDelivery(actualDepartureTime);
        orderRepository.save(order);

        log.info("허브 배송 시작 완료 - orderId: {}, status: HUB_IN_TRANSIT", orderId);
    }

    /**
     * 허브 도착
     *
     * @param orderId 주문 ID
     * @param hubArrivalTime 허브 도착 시간
     */
    public void arriveAtHub(String orderId, LocalDateTime hubArrivalTime) {
        log.info("허브 도착 - orderId: {}", orderId);

        Order order = findOrderById(orderId);
        order.arriveAtHub(hubArrivalTime);
        orderRepository.save(order);

        log.info("허브 도착 완료 - orderId: {}, status: HUB_ARRIVED", orderId);
    }

    /**
     * 최종 배송 시작
     *
     * @param orderId 주문 ID
     * @param finalDeliveryStartTime 최종 배송 시작 시간
     */
    public void startFinalDelivery(String orderId, LocalDateTime finalDeliveryStartTime) {
        log.info("최종 배송 시작 - orderId: {}", orderId);

        Order order = findOrderById(orderId);
        order.startFinalDelivery(finalDeliveryStartTime);
        orderRepository.save(order);

        log.info("최종 배송 시작 완료 - orderId: {}, status: IN_DELIVERY", orderId);
    }

    /**
     * 배송 완료
     *
     * @param orderId 주문 ID
     * @param actualDeliveryTime 실제 배송 완료 시간
     * @param signature 서명 (Base64)
     * @param actualReceiverName 실제 수령자
     */
    public void completeDelivery(
            String orderId,
            LocalDateTime actualDeliveryTime,
            String signature,
            String actualReceiverName) {

        log.info("배송 완료 - orderId: {}", orderId);

        Order order = findOrderById(orderId);
        order.completeDelivery(actualDeliveryTime, signature, actualReceiverName);
        orderRepository.save(order);

        log.info("배송 완료 - orderId: {}, status: COMPLETED", orderId);
    }

    /**
     * 주문 조회 (내부용)
     */
    private Order findOrderById(String orderId) {
        return orderRepository.findById(OrderId.from(orderId))
                .orElseThrow(() -> new OrderException(
                        OrderErrorCode.ORDER_NOT_FOUND,
                        "주문을 찾을 수 없습니다: " + orderId
                ));
    }
}
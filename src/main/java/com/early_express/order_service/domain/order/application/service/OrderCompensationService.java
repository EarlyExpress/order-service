package com.early_express.order_service.domain.order.application.service;

import com.early_express.order_service.domain.order.application.dto.PaymentCancelCommand;
import com.early_express.order_service.domain.order.application.dto.PaymentCancelResult;
import com.early_express.order_service.domain.order.domain.exception.OrderErrorCode;
import com.early_express.order_service.domain.order.domain.exception.SagaException;
import com.early_express.order_service.domain.order.domain.messaging.payment.PaymentEventPublisher;
import com.early_express.order_service.domain.order.domain.messaging.payment.RefundRequestedEventData;
import com.early_express.order_service.domain.order.domain.model.Order;
import com.early_express.order_service.domain.order.domain.model.OrderSaga;
import com.early_express.order_service.domain.order.domain.model.SagaStep;
import com.early_express.order_service.domain.order.domain.model.vo.OrderId;
import com.early_express.order_service.domain.order.domain.repository.OrderRepository;
import com.early_express.order_service.domain.order.domain.repository.OrderSagaRepository;
import com.early_express.order_service.domain.order.infrastructure.client.hubdelivery.HubDeliveryClient;
import com.early_express.order_service.domain.order.infrastructure.client.inventory.InventoryClient;
import com.early_express.order_service.domain.order.infrastructure.client.inventory.dto.InventoryReservationResponse;
import com.early_express.order_service.domain.order.infrastructure.client.inventory.dto.InventoryRestoreRequest;
import com.early_express.order_service.domain.order.infrastructure.client.inventory.dto.InventoryRestoreResponse;
import com.early_express.order_service.domain.order.infrastructure.client.lastmile.LastMileClient;
import com.early_express.order_service.domain.order.infrastructure.client.payment.dto.PaymentVerificationResponse;
import com.early_express.order_service.domain.order.infrastructure.messaging.payment.event.PaymentRefundFailedEvent;
import com.early_express.order_service.domain.order.infrastructure.messaging.payment.event.PaymentRefundedEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Order Compensation Service
 * 보상 트랜잭션 및 이벤트 기반 보상 처리
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OrderCompensationService {

    private final OrderRepository orderRepository;
    private final OrderSagaRepository sagaRepository;
    private final InventoryClient inventoryClient;
    private final HubDeliveryClient hubDeliveryClient;
    private final LastMileClient lastMileClient;
    private final PaymentEventPublisher paymentEventPublisher;
    private final ObjectMapper objectMapper;

    /**
     * 재고 부족으로 인한 보상 시작
     * - Step 1 실패 시 호출
     * - 결제 환불 이벤트 발행
     *
     * @param orderId 주문 ID
     * @param failureReason 실패 사유
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void startCompensationForStockFailure(String orderId, String failureReason) {
        log.warn("!!! 재고 부족으로 보상 시작 - orderId: {}, reason: {}",
                orderId, failureReason);

        // 1. Order 및 Saga 조회
        Order order = findOrderById(orderId);
        OrderSaga saga = findSagaByOrderId(orderId);

        // 2. Saga 보상 시작
        saga.startCompensation(failureReason);
        sagaRepository.save(saga);

        // 3. 결제 환불 이벤트 발행 (Payment Service가 환불 처리)
        if (order.getAmountInfo().hasPaymentId()) {
            publishRefundRequestedEvent(order, failureReason);
        }

        // 4. Order 상태를 FAILED로 변경
        order.fail();
        orderRepository.save(order);

        log.warn("!!! 재고 부족 보상 시작 완료 - orderId: {}, 결제 환불 이벤트 발행됨", orderId);
    }

    /**
     * 결제 검증 실패로 인한 보상 시작
     * - Step 2 실패 시 호출
     * - 재고만 복원 (결제는 이미 실패)
     *
     * @param orderId 주문 ID
     * @param failureReason 실패 사유
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void startCompensationForPaymentFailure(String orderId, String failureReason) {
        log.warn("!!! 결제 검증 실패로 보상 시작 - orderId: {}, reason: {}",
                orderId, failureReason);

        // 1. Order 및 Saga 조회
        Order order = findOrderById(orderId);
        OrderSaga saga = findSagaByOrderId(orderId);

        // 2. Saga 보상 시작
        saga.startCompensation(failureReason);
        sagaRepository.save(saga);

        // 3. STOCK_RESTORE 히스토리 생성 (중요!)
        saga.executeCompensation(SagaStep.STOCK_RESERVE, SagaStep.STOCK_RESTORE);
        saga = sagaRepository.save(saga);

        try {
            // 4. 재고 복원 (동기)
            compensateStock(order, saga);

            // 5. 보상 완료
            saga.completeCompensation(SagaStep.STOCK_RESTORE);
            saga.completeAllCompensations();
            saga = sagaRepository.save(saga);

            order.compensate();
            orderRepository.save(order);

            log.warn("!!! 결제 검증 실패 보상 완료 - orderId: {}", orderId);

        } catch (Exception e) {
            log.error("재고 복원 실패 - orderId: {}", orderId, e);

            saga.failCompensation(SagaStep.STOCK_RESTORE, e.getMessage());
            sagaRepository.save(saga);

            order.fail();
            orderRepository.save(order);

            throw e;
        }
    }

    /**
     * 환불 완료 이벤트 처리 (Payment Service → Order Service)
     * - PAYMENT_CANCEL Step 완료 처리
     * - 재고 복원 실행
     * - 보상 완료 처리
     *
     * @param event 환불 완료 이벤트
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handlePaymentRefunded(PaymentRefundedEvent event) {
        log.info(">>> 환불 완료 이벤트 처리 시작 - orderId: {}, paymentId: {}",
                event.getOrderId(), event.getPaymentId());

        try {
            // 1. Order 및 Saga 조회
            Order order = findOrderById(event.getOrderId());
            OrderSaga saga = findSagaByOrderId(event.getOrderId());

            // 2. PAYMENT_CANCEL Step 완료 처리
            saga.completeCompensation(SagaStep.PAYMENT_CANCEL);
            sagaRepository.save(saga);

            log.info(">>> PAYMENT_CANCEL Step 완료 처리됨 - orderId: {}", event.getOrderId());

            // 3. 재고 복원
            compensateStock(order, saga);

            // 4. 모든 보상 완료
            saga.completeAllCompensations();
            sagaRepository.save(saga);

            order.compensate();
            orderRepository.save(order);

            log.info(">>> 환불 완료 처리 완료 - orderId: {}, 재고 복원됨", event.getOrderId());

        } catch (Exception e) {
            log.error("환불 완료 처리 실패 - orderId: {}, error: {}",
                    event.getOrderId(), e.getMessage(), e);
            throw e;
        }
    }

    /**
     * 환불 실패 이벤트 처리 (Payment Service → Order Service)
     * - 보상 실패 상태로 변경
     * - 수동 개입 필요
     *
     * @param event 환불 실패 이벤트
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handlePaymentRefundFailed(PaymentRefundFailedEvent event) {
        log.error(">>> 환불 실패 이벤트 처리 - orderId: {}, paymentId: {}, error: {}",
                event.getOrderId(), event.getPaymentId(), event.getErrorMessage());

        try {
            // 1. Order 및 Saga 조회
            Order order = findOrderById(event.getOrderId());
            OrderSaga saga = findSagaByOrderId(event.getOrderId());

            // 2. 보상 실패 처리
            saga.failCompensation(
                    SagaStep.PAYMENT_CANCEL,
                    event.getErrorMessage()
            );
            sagaRepository.save(saga);

            order.fail();
            orderRepository.save(order);

            log.error(">>> 환불 실패 처리 완료 - orderId: {}, 수동 개입 필요", event.getOrderId());

            // TODO: 관리자 알림 발송

        } catch (Exception e) {
            log.error("환불 실패 처리 중 오류 - orderId: {}, error: {}",
                    event.getOrderId(), e.getMessage(), e);
            throw e;
        }
    }

    /**
     * 재고 복원 (Compensation)
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    protected void compensateStock(Order order, OrderSaga saga) {
        log.info(">>> 재고 복원 시작 - orderId: {}", order.getIdValue());

        try {
            // 1. 보상 데이터 조회
            Object stepData = saga.getCompensationDataForStep(SagaStep.STOCK_RESERVE);

            if (stepData == null) {
                log.warn("재고 예약 데이터가 없음 - orderId: {}, 재고 복원 건너뜀", order.getIdValue());
                return;
            }

//            InventoryReservationResponse reserveResponse = (InventoryReservationResponse) stepData;
            // LinkedHashMap을 InventoryReservationResponse로 변환
            InventoryReservationResponse reserveResponse;
            if (stepData instanceof InventoryReservationResponse) {
                reserveResponse = (InventoryReservationResponse) stepData;
            } else {
                // LinkedHashMap → InventoryReservationResponse 변환
                reserveResponse = objectMapper.convertValue(stepData, InventoryReservationResponse.class);
            }

            // 2. 재고 복원 요청
            InventoryRestoreRequest request = InventoryRestoreRequest.from(
                    reserveResponse.getReservationId(),
                    order.getIdValue(),
                    reserveResponse.getReservedItems(),
                    "주문 생성 실패로 인한 재고 복원"
            );

            // 3. Inventory Service 호출 (동기)
            InventoryRestoreResponse response = inventoryClient.restoreStock(request);

            if (!Boolean.TRUE.equals(response.getSuccess())) {
                throw new SagaException(
                        OrderErrorCode.SAGA_COMPENSATION_FAILED,
                        "재고 복원에 실패했습니다: " + response.getMessage()
                );
            }

            // 4. Saga Step 완료
//            saga.completeCompensation(SagaStep.STOCK_RESTORE);
//            sagaRepository.save(saga);

            log.info(">>> 재고 복원 완료 - orderId: {}, restoredQuantity: {}",
                    order.getIdValue(), response.getTotalRestoredQuantity());

        } catch (Exception e) {
            log.error("재고 복원 실패 - orderId: {}, error: {}",
                    order.getIdValue(), e.getMessage(), e);

            // 보상 실패 기록
            saga.failCompensation(SagaStep.STOCK_RESTORE, e.getMessage());
            sagaRepository.save(saga);

            throw e;
        }
    }

    /**
     * 경로 계산 실패 시 보상 트랜잭션
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void startCompensationForRouteFailure(String orderId, String failureReason) {
        log.info("경로 계산 실패 보상 시작 - orderId: {}", orderId);

        Order order = findOrderById(orderId);
        OrderSaga saga = findSagaByOrderId(orderId);

        saga.startCompensation(failureReason);
        saga = sagaRepository.save(saga);

        var completedSteps = saga.getCompletedStepsNeedingCompensation();
        log.info("보상 대상 Step 목록: {}", completedSteps);

        for (SagaStep step : completedSteps.reversed()) {
            saga = executeCompensationStepInternal(order, saga, step);
        }

        saga.completeAllCompensations();
        sagaRepository.save(saga);

        order.compensate();
        orderRepository.save(order);

        log.info("보상 트랜잭션 완료 - orderId: {}", orderId);
    }

    /**
     * 개별 보상 Step 실행 (내부용)
     */
    private OrderSaga executeCompensationStepInternal(Order order, OrderSaga saga, SagaStep originalStep) {
        SagaStep compensationStep = originalStep.getCompensationStep();

        log.info(">>> 보상 Step 실행 - originalStep: {}, compensationStep: {}",
                originalStep.getDescription(), compensationStep.getDescription());

        saga.executeCompensation(originalStep, compensationStep);
        saga = sagaRepository.save(saga);

        try {
            switch (compensationStep) {
                case PAYMENT_CANCEL -> {
                    // 결제 취소 이벤트 발행
                    compensatePayment(order, saga);
                    log.info(">>> 결제 취소 이벤트 발행 완료, PaymentRefundedEvent 대기 중");
                    // 이벤트 기반이므로 완료 처리는 Consumer에서
                    return saga;
                }
                case STOCK_RESTORE -> {
                    // 재고 복원 (동기)
                    compensateStockInternal(order, saga);
                }
                case HUB_DELIVERY_CANCEL -> {
                    // 허브 배송 취소 (동기)
                    compensateHubDelivery(order);
                }
                case LAST_MILE_DELIVERY_CANCEL -> {
                    // 업체 배송 취소 (동기)
                    compensateLastMileDelivery(order);
                }
                default -> throw new IllegalStateException(
                        "지원하지 않는 보상 Step: " + compensationStep
                );
            }

            // 완료 처리
            saga.completeCompensation(compensationStep);
            saga = sagaRepository.save(saga);

            log.info(">>> 보상 Step 완료 - compensationStep: {}", compensationStep.getDescription());

        } catch (Exception e) {
            log.error("보상 Step 실패 - step: {}, error: {}",
                    compensationStep.getDescription(), e.getMessage(), e);

            saga.failCompensation(compensationStep, e.getMessage());
            saga = sagaRepository.save(saga);

            order.fail();
            orderRepository.save(order);

            throw e;
        }

        return saga;
    }

    /**
     * 결제 취소 (이벤트 발행)
     */
    private void compensatePayment(Order order, OrderSaga saga) {
        log.info(">>> 결제 취소 이벤트 발행 시작 - orderId: {}", order.getIdValue());

        Object stepData = saga.getCompensationDataForStep(SagaStep.PAYMENT_VERIFY);

        PaymentVerificationResponse verifyResponse;
        if (stepData instanceof PaymentVerificationResponse) {
            verifyResponse = (PaymentVerificationResponse) stepData;
        } else {
            verifyResponse = objectMapper.convertValue(stepData, PaymentVerificationResponse.class);
        }

        RefundRequestedEventData eventData = RefundRequestedEventData.of(
                verifyResponse.getPaymentId(),
                order.getIdValue(),
                "주문 생성 실패로 인한 자동 취소"
        );

        paymentEventPublisher.publishRefundRequested(eventData);

        log.info(">>> 결제 취소 이벤트 발행 완료 - orderId: {}, paymentId: {}",
                order.getIdValue(), verifyResponse.getPaymentId());
    }

    /**
     * 재고 복원 (내부용 - saga 저장 없음)
     */
    private void compensateStockInternal(Order order, OrderSaga saga) {
        log.info(">>> 재고 복원 시작 - orderId: {}", order.getIdValue());

        Object stepData = saga.getCompensationDataForStep(SagaStep.STOCK_RESERVE);

        if (stepData == null) {
            log.warn("재고 예약 데이터가 없음 - orderId: {}, 재고 복원 건너뜀", order.getIdValue());
            return;
        }

        InventoryReservationResponse reserveResponse;
        if (stepData instanceof InventoryReservationResponse) {
            reserveResponse = (InventoryReservationResponse) stepData;
        } else {
            reserveResponse = objectMapper.convertValue(stepData, InventoryReservationResponse.class);
        }

        InventoryRestoreRequest request = InventoryRestoreRequest.from(
                reserveResponse.getReservationId(),
                order.getIdValue(),
                reserveResponse.getReservedItems(),
                "주문 생성 실패로 인한 재고 복원"
        );

        InventoryRestoreResponse response = inventoryClient.restoreStock(request);

        if (!Boolean.TRUE.equals(response.getSuccess())) {
            throw new SagaException(
                    OrderErrorCode.SAGA_COMPENSATION_FAILED,
                    "재고 복원에 실패했습니다: " + response.getMessage()
            );
        }

        log.info(">>> 재고 복원 완료 - orderId: {}, restoredQuantity: {}",
                order.getIdValue(), response.getTotalRestoredQuantity());
    }

    /**
     * 허브 배송 취소
     */
    private void compensateHubDelivery(Order order) {
        log.info(">>> 허브 배송 취소 시작 - orderId: {}", order.getIdValue());

        String hubDeliveryId = order.getDeliveryInfo().getHubDeliveryId();

        if (hubDeliveryId == null || hubDeliveryId.isBlank()) {
            log.info(">>> 허브 배송 ID 없음, 취소 스킵 - orderId: {}", order.getIdValue());
            return;
        }

         hubDeliveryClient.cancelDelivery(hubDeliveryId);

        log.info(">>> 허브 배송 취소 완료 - orderId: {}, hubDeliveryId: {}",
                order.getIdValue(), hubDeliveryId);
    }

    /**
     * 업체 배송 취소
     */
    private void compensateLastMileDelivery(Order order) {
        log.info(">>> 업체 배송 취소 시작 - orderId: {}", order.getIdValue());

        String lastMileDeliveryId = order.getDeliveryInfo().getLastMileDeliveryId();

        if (lastMileDeliveryId == null || lastMileDeliveryId.isBlank()) {
            log.info(">>> 업체 배송 ID 없음, 취소 스킵 - orderId: {}", order.getIdValue());
            return;
        }

         lastMileClient.cancelDelivery(lastMileDeliveryId);

        log.info(">>> 업체 배송 취소 완료 - orderId: {}, lastMileDeliveryId: {}",
                order.getIdValue(), lastMileDeliveryId);
    }

    /**
     * 환불 요청 이벤트 발행
     */
    private void publishRefundRequestedEvent(Order order, String refundReason) {
        log.info("환불 요청 이벤트 발행 - orderId: {}, paymentId: {}",
                order.getIdValue(), order.getAmountInfo().getPaymentId());

        RefundRequestedEventData eventData = RefundRequestedEventData.of(
                order.getAmountInfo().getPaymentId(),
                order.getIdValue(),
                refundReason
        );

        paymentEventPublisher.publishRefundRequested(eventData);
    }

    /**
     * 주문 조회 (내부용)
     */
    private Order findOrderById(String orderId) {
        return orderRepository.findById(OrderId.from(orderId))
                .orElseThrow(() -> new SagaException(
                        OrderErrorCode.ORDER_NOT_FOUND,
                        "주문을 찾을 수 없습니다: " + orderId
                ));
    }

    /**
     * Saga 조회 (내부용)
     */
    private OrderSaga findSagaByOrderId(String orderId) {
        return sagaRepository.findByOrderId(OrderId.from(orderId))
                .orElseThrow(() -> new SagaException(
                        OrderErrorCode.SAGA_NOT_FOUND,
                        "Saga를 찾을 수 없습니다: " + orderId
                ));
    }
}
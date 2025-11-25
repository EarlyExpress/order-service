package com.early_express.order_service.domain.order.application.service;

import com.early_express.order_service.domain.order.domain.exception.OrderErrorCode;
import com.early_express.order_service.domain.order.domain.exception.SagaException;
import com.early_express.order_service.domain.order.domain.messaging.notification.NotificationEventPublisher;
import com.early_express.order_service.domain.order.domain.messaging.notification.NotificationRequestedEventData;
import com.early_express.order_service.domain.order.domain.messaging.order.OrderEventPublisher;
import com.early_express.order_service.domain.order.domain.messaging.order.OrderPaymentVerifiedEventData;
import com.early_express.order_service.domain.order.domain.messaging.order.event.OrderPaymentVerifiedEvent;
import com.early_express.order_service.domain.order.domain.messaging.payment.PaymentEventPublisher;
import com.early_express.order_service.domain.order.domain.messaging.payment.RefundRequestedEventData;
import com.early_express.order_service.domain.order.domain.messaging.tracking.TrackingEventPublisher;
import com.early_express.order_service.domain.order.domain.messaging.tracking.TrackingStartRequestedEventData;
import com.early_express.order_service.domain.order.domain.model.Order;
import com.early_express.order_service.domain.order.domain.model.OrderSaga;
import com.early_express.order_service.domain.order.domain.model.SagaStep;
import com.early_express.order_service.domain.order.domain.model.vo.AiCalculationResult;
import com.early_express.order_service.domain.order.domain.model.vo.DeliveryInfo;
import com.early_express.order_service.domain.order.domain.model.vo.OrderId;
import com.early_express.order_service.domain.order.domain.repository.OrderRepository;
import com.early_express.order_service.domain.order.domain.repository.OrderSagaRepository;
import com.early_express.order_service.domain.order.infrastructure.client.ai.AiClient;
import com.early_express.order_service.domain.order.infrastructure.client.ai.dto.AiTimeCalculationRequest;
import com.early_express.order_service.domain.order.infrastructure.client.ai.dto.AiTimeCalculationResponse;
import com.early_express.order_service.domain.order.infrastructure.client.hub.HubClient;
import com.early_express.order_service.domain.order.infrastructure.client.hub.dto.HubRouteCalculationRequest;
import com.early_express.order_service.domain.order.infrastructure.client.hub.dto.HubRouteCalculationResponse;
import com.early_express.order_service.domain.order.infrastructure.client.hubdelivery.HubDeliveryClient;
import com.early_express.order_service.domain.order.infrastructure.client.hubdelivery.dto.HubDeliveryCreateRequest;
import com.early_express.order_service.domain.order.infrastructure.client.hubdelivery.dto.HubDeliveryCreateResponse;
import com.early_express.order_service.domain.order.infrastructure.client.inventory.InventoryClient;
import com.early_express.order_service.domain.order.infrastructure.client.inventory.dto.InventoryReservationRequest;
import com.early_express.order_service.domain.order.infrastructure.client.inventory.dto.InventoryReservationResponse;
import com.early_express.order_service.domain.order.infrastructure.client.inventory.dto.InventoryRestoreRequest;
import com.early_express.order_service.domain.order.infrastructure.client.inventory.dto.InventoryRestoreResponse;
import com.early_express.order_service.domain.order.infrastructure.client.lastmile.LastMileClient;
import com.early_express.order_service.domain.order.infrastructure.client.lastmile.dto.LastMileDeliveryCreateRequest;
import com.early_express.order_service.domain.order.infrastructure.client.lastmile.dto.LastMileDeliveryCreateResponse;
import com.early_express.order_service.domain.order.infrastructure.client.payment.PaymentClient;
import com.early_express.order_service.domain.order.infrastructure.client.payment.dto.PaymentVerificationRequest;
import com.early_express.order_service.domain.order.infrastructure.client.payment.dto.PaymentVerificationResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Order Saga Orchestrator Service
 *
 * Saga 패턴의 오케스트레이터로 주문 생성 워크플로우를 관리
 *
 * [동기 처리] Step 1~2: 재고 예약 → 결제 검증
 * [비동기 처리] Step 3~7: 경로 계산 → 배송 생성 → 알림 → 추적
 *
 * 실패 시 보상 트랜잭션 실행 (역순)
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OrderSagaOrchestratorService {

    private final OrderCompensationService compensationService;
    private final OrderRepository orderRepository;
    private final OrderSagaRepository sagaRepository;
    private final PaymentEventPublisher paymentEventPublisher;
    private final OrderEventPublisher orderEventPublisher;
    private final NotificationEventPublisher notificationEventPublisher;
    private final TrackingEventPublisher trackingEventPublisher;
    private final PaymentClient paymentClient;
    private final InventoryClient inventoryClient;
    private final HubClient hubClient;
    private final AiClient aiClient;
    private final HubDeliveryClient hubDeliveryClient;
    private final LastMileClient lastMileClient;
    private final ObjectMapper objectMapper;

    /**
     * Order Saga 시작
     *
     * [동기 처리]
     * - Step 1: 재고 예약
     * - Step 2: 결제 검증
     *
     * [비동기 처리]
     * - Step 3~7: 이벤트 발행하여 비동기 처리
     *
     * @param order 생성된 주문
     */
//    @Transactional
//    public void startOrderSaga(Order order) {
//        log.info("=== Order Saga 시작 - orderId: {} ===", order.getIdValue());
//
//        // 1. Saga 생성 및 시작
//        OrderSaga save_saga = OrderSaga.create(order.getId());
//        save_saga.start();
//        OrderSaga saga=sagaRepository.save(save_saga);
//
//        try {
//            // ========== [동기 처리] Step 1: 재고 예약 ==========
//            saga = executeStockReservation(order, saga);
//
//            // ========== [동기 처리] Step 2: 결제 검증 ==========
//            saga = executePaymentVerification(order, saga);
//
//            log.info("=== Order Saga 동기 단계 완료 - orderId: {} ===", order.getIdValue());
//
//            // ========== 이벤트 발행: 비동기 Step 3~7 트리거 ==========
//            publishOrderPaymentVerifiedEvent(order, saga);
//
//            log.info("=== OrderPaymentVerified 이벤트 발행 완료 - orderId: {} ===",
//                    order.getIdValue());
//
//            // ========== [비동기 처리] Step 3~7: 이벤트 발행 ==========
//            // - RouteCalculationRequestedEvent 발행
//            // - Step 3: 경로 계산 (Hub Service + AI Service)
//            // - Step 4: 허브 배송 생성 (조건부)
//            // - Step 5: 업체 배송 생성
//            // - Step 6: 알림 발송 (Best Effort)
//            // - Step 7: 추적 시작 (Best Effort)
//
//        } catch (Exception e) {
//            log.error("Saga 실행 중 오류 발생 - orderId: {}, error: {}",
//                    order.getIdValue(), e.getMessage(), e);
//
//            // 별도 서비스 호출 (새 트랜잭션)
//            // Step 1 실패인지 Step 2 실패인지 구분
//            if (saga.getCurrentStep() == SagaStep.STOCK_RESERVE) {
//                // Step 1 실패: 재고 예약 실패
//                compensationService.startCompensationForStockFailure(
//                        order.getIdValue(),
//                        e.getMessage()
//                );
//            } else {
//                // Step 2 실패: 결제 검증 실패
//                compensationService.startCompensationForPaymentFailure(
//                        order.getIdValue(),
//                        e.getMessage()
//                );
//            }
//
//            throw new SagaException(
//                    OrderErrorCode.SAGA_EXECUTION_FAILED,
//                    "주문 생성 중 오류가 발생했습니다.",
//                    e
//            );
//        }
//    }
    /**
     * 진입점 - 트랜잭션 없음
     */
    public void startOrderSaga(Order order) {
        log.info("=== Order Saga 시작 - orderId: {} ===", order.getIdValue());

        try {
            // 그냥 호출! (외부에서 호출되므로 프록시 통과)
            executeOrderSagaWithTransaction(order);

        } catch (Exception e) {
            log.error("Saga 실행 중 오류 발생 - orderId: {}, error: {}",
                    order.getIdValue(), e.getMessage(), e);

            // 트랜잭션 밖에서 보상 처리
            handleSagaFailure(order, e);

            throw new SagaException(
                    OrderErrorCode.SAGA_EXECUTION_FAILED,
                    "주문 생성 중 오류가 발생했습니다.",
                    e
            );
        }
    }

    /**
     * Saga 실행 - 트랜잭션 관리
     */
    @Transactional
    public void executeOrderSagaWithTransaction(Order order) {
        // Saga 생성 및 Step 실행
        OrderSaga save_saga = OrderSaga.create(order.getId());
        save_saga.start();
        OrderSaga saga = sagaRepository.save(save_saga);

        // ========== [동기 처리] Step 1: 재고 예약 ==========
        saga = executeStockReservation(order, saga);

        // ========== [동기 처리] Step 2: 결제 검증 ==========
        saga = executePaymentVerification(order, saga);

        // ========== 이벤트 발행: 비동기 Step 3~7 트리거 ==========
        publishOrderPaymentVerifiedEvent(order, saga);

        // ========== [비동기 처리] Step 3~7: 이벤트 발행 ==========
        // - RouteCalculationRequestedEvent 발행
        // - Step 3: 경로 계산 (Hub Service + AI Service)
        // - Step 4: 허브 배송 생성 (조건부)
        // - Step 5: 업체 배송 생성
        // - Step 6: 알림 발송 (Best Effort)
        // - Step 7: 추적 시작 (Best Effort)
    }

    /**
     * 보상 처리 - 트랜잭션 밖
     */
    private void handleSagaFailure(Order order, Exception e) {
        try {
            OrderSaga saga = sagaRepository.findByOrderId(order.getId())
                    .orElse(null);

            if (saga == null) {
                log.warn("Saga가 롤백되어 없음, 보상 불필요 - orderId: {}",
                        order.getIdValue());
                return;
            }

            if (saga.getCurrentStep() == SagaStep.STOCK_RESERVE) {
                compensationService.startCompensationForStockFailure(
                        order.getIdValue(), e.getMessage());
            } else {
                compensationService.startCompensationForPaymentFailure(
                        order.getIdValue(), e.getMessage());
            }
        } catch (Exception compensationError) {
            log.error("보상 처리 중 오류 - orderId: {}",
                    order.getIdValue(), compensationError);
        }
    }

    /**
     * Step 1: 재고 예약
     */
    private OrderSaga executeStockReservation(Order order, OrderSaga saga) {
        log.info(">>> Step 1: 재고 예약 시작 - orderId: {}", order.getIdValue());

        order.startStockChecking();
        orderRepository.save(order);

        saga.startStep(SagaStep.STOCK_RESERVE);
        sagaRepository.save(saga);

        try {
            InventoryReservationRequest request = InventoryReservationRequest.of(
                    order.getIdValue(),
                    order.getProductInfo().getProductId(),
                    order.getProductInfo().getQuantity()
            );

            InventoryReservationResponse response = inventoryClient.reserveStock(request);

            if (!response.isAllReserved()) {
                throw new SagaException(
                        OrderErrorCode.SAGA_STEP_FAILED,
                        "재고 예약에 실패했습니다."
                );
            }

            String productHubId = response.getReservedItems().get(0).getHubId();
            order.completeStockReservation(productHubId);
            orderRepository.save(order);

            saga.completeStep(SagaStep.STOCK_RESERVE, response);
            saga =sagaRepository.save(saga);

            log.info(">>> Step 1: 재고 예약 완료 - orderId: {}", order.getIdValue());

            return saga;
        } catch (Exception e) {
            saga.failStep(SagaStep.STOCK_RESERVE, e.getMessage());
            sagaRepository.save(saga);
            throw e;
        }
    }

    /**
     * Step 2: 결제 검증
     */
    private OrderSaga executePaymentVerification(Order order, OrderSaga saga) {
        log.info(">>> Step 2: 결제 검증 시작 - orderId: {}", order.getIdValue());

        order.startPaymentVerification();
        orderRepository.save(order);

        saga.startStep(SagaStep.PAYMENT_VERIFY);
        sagaRepository.save(saga);

        try {
            PaymentVerificationRequest request = PaymentVerificationRequest.of(
                    order.getIdValue(),
                    order.getPgPaymentInfo().getPgProvider(),
                    order.getPgPaymentInfo().getPgPaymentId(),
                    order.getPgPaymentInfo().getPgPaymentKey(),
                    order.getAmountInfo().getTotalAmount(),
                    order.getCompanyInfo().getReceiverCompanyId(),
                    order.getReceiverInfo().getReceiverName(),
                    order.getReceiverInfo().getReceiverEmail(),
                    order.getReceiverInfo().getReceiverPhone(),
                    order.getCompanyInfo().getSupplierCompanyId(),
                    order.getCompanyInfo().getSupplierCompanyId()
            );

            PaymentVerificationResponse response = paymentClient.verifyAndRegisterPayment(request);

            if (!"VERIFIED".equals(response.getStatus())) {
                throw new SagaException(
                        OrderErrorCode.SAGA_STEP_FAILED,
                        "결제 검증에 실패했습니다: " + response.getMessage()
                );
            }

            order.validatePaymentAmount(response.getVerifiedAmount());
            order.completePaymentVerification(response.getPaymentId());
            orderRepository.save(order);

            saga.completeStep(SagaStep.PAYMENT_VERIFY, response);
            saga = sagaRepository.save(saga);

            log.info(">>> Step 2: 결제 검증 완료 - orderId: {}", order.getIdValue());
            return saga;
        } catch (Exception e) {
            saga.failStep(SagaStep.PAYMENT_VERIFY, e.getMessage());
            sagaRepository.save(saga);
            throw e;
        }
    }

    /**
     * 결제 검증 완료 이벤트 발행
     */
    private void publishOrderPaymentVerifiedEvent(Order order, OrderSaga saga) {
        log.info("결제 검증 완료 이벤트 발행 - orderId: {}", order.getIdValue());

        OrderPaymentVerifiedEventData eventData =
                OrderPaymentVerifiedEventData.from(order, saga);

        orderEventPublisher.publishOrderPaymentVerified(eventData);

        log.info("OrderPaymentVerified 이벤트 발행 완료 - orderId: {}", order.getIdValue());
    }

    /**
     * Step 3: 경로 계산 (이벤트 기반)
     * OrderPaymentVerifiedEvent 수신 후 호출
     */
//    @Transactional
//    public void executeRouteCalculation(OrderPaymentVerifiedEvent event) {
//        log.info(">>> Step 3: 경로 계산 시작 - orderId: {}", event.getOrderId());
//
//        // 1. Order 및 Saga 조회
//        Order order = orderRepository.findById(OrderId.from(event.getOrderId()))
//                .orElseThrow(() -> new SagaException(
//                        OrderErrorCode.ORDER_NOT_FOUND,
//                        "주문을 찾을 수 없습니다: " + event.getOrderId()
//                ));
//
//        OrderSaga saga = sagaRepository.findByOrderId(order.getId())
//                .orElseThrow(() -> new SagaException(
//                        OrderErrorCode.SAGA_NOT_FOUND,
//                        "Saga를 찾을 수 없습니다: " + event.getOrderId()
//                ));
//
//        log.info("=== Step 3 시작: 경로 및 시간 계산 - orderId: {}, sagaId: {} ===",
//                order.getIdValue(), saga.getSagaIdValue());
//
//        // 2. Saga Step 시작
//        saga.startStep(SagaStep.ROUTE_CALCULATE);
//        order.startRouteCalculation(); // Order 상태: ROUTE_CALCULATING
//        sagaRepository.save(saga);
//        orderRepository.save(order);
//
//        try {
//            // 3. Hub Service 경로 계산
//            HubRouteCalculationResponse hubResponse = callHubRouteCalculation(order);
//            log.info("Hub 경로 계산 완료 - orderId: {}, originHub: {}, destinationHub: {}, hubs: {}",
//                    order.getIdValue(),
//                    hubResponse.getOriginHubId(),
//                    hubResponse.getDestinationHubId(),
//                    hubResponse.getRouteHubs());
//
//            // 4. Order 도메인 - Hub 정보 업데이트
//            updateOrderWithHubResponse(order, hubResponse);
//            orderRepository.save(order);
//
//            // 5. AI Service 시간 계산
//            AiTimeCalculationResponse aiResponse = callAiTimeCalculation(order, hubResponse);
//            log.info("AI 시간 계산 완료 - orderId: {}, departureDeadline: {}, estimatedDelivery: {}",
//                    order.getIdValue(),
//                    aiResponse.getCalculatedDepartureDeadline(),
//                    aiResponse.getEstimatedDeliveryTime());
//
//            // 6. Order 도메인 - AI 계산 결과 업데이트
//            updateOrderWithAiResponse(order, aiResponse);
//            orderRepository.save(order);
//
//            // 7. Step 완료 처리
//            saga.completeStep(SagaStep.ROUTE_CALCULATE, hubResponse);
//            saga.addStepHistory(SagaStep.ROUTE_CALCULATE, aiResponse); // AI 결과도 히스토리에
//
//            // Order 상태: ROUTE_CALCULATING → DELIVERY_CREATING (다음 Step 대기)
//            order.startDeliveryCreation();
//
//            sagaRepository.save(saga);
//            orderRepository.save(order);
//
//            log.info("<<< Step 3: 경로 및 시간 계산 완료 - orderId: {}, requiresHubDelivery: {}, orderStatus: {}",
//                    event.getOrderId(),
//                    hubResponse.getRequiresHubDelivery(),
//                    order.getStatus().getDescription());
//
//            // 8. 다음 Step 결정 및 트리거
//            triggerNextStep(order, saga, hubResponse);
//
//        } catch (Exception e) {
//            log.error("Step 3: 경로 계산 실패 - orderId: {}, error: {}",
//                    event.getOrderId(), e.getMessage(), e);
//
//            saga.failStep(SagaStep.ROUTE_CALCULATE, e.getMessage());
//            order.fail(); // Order 상태: ROUTE_CALCULATING → FAILED
//            sagaRepository.save(saga);
//            orderRepository.save(order);
//
//            // 보상 트랜잭션 시작!
//            startCompensation(order, saga, e.getMessage());
//
//            throw e;
//        }
//    }
    /**
     * Step 3: 경로 계산 - 진입점 (트랜잭션 없음)
     * OrderPaymentVerifiedEvent 수신 후 호출
     */
    public void executeRouteCalculation(OrderPaymentVerifiedEvent event) {
        log.info(">>> Step 3: 경로 계산 시작 - orderId: {}", event.getOrderId());

        try {
            executeRouteCalculationWithTransaction(event);

        } catch (Exception e) {
            log.error("Step 3: 경로 계산 실패 - orderId: {}, error: {}",
                    event.getOrderId(), e.getMessage(), e);

            // 트랜잭션 밖에서 보상 처리
            handleRouteCalculationFailure(event.getOrderId(), e);

            throw e;
        }
    }

    /**
     * Step 3: 경로 계산 - 트랜잭션 관리
     */
    @Transactional
    public void executeRouteCalculationWithTransaction(OrderPaymentVerifiedEvent event) {
        // 1. Order 및 Saga 조회
        Order order = orderRepository.findById(OrderId.from(event.getOrderId()))
                .orElseThrow(() -> new SagaException(
                        OrderErrorCode.ORDER_NOT_FOUND,
                        "주문을 찾을 수 없습니다: " + event.getOrderId()
                ));

        OrderSaga saga = sagaRepository.findByOrderId(order.getId())
                .orElseThrow(() -> new SagaException(
                        OrderErrorCode.SAGA_NOT_FOUND,
                        "Saga를 찾을 수 없습니다: " + event.getOrderId()
                ));

        log.info("=== Step 3 시작: 경로 및 시간 계산 - orderId: {}, sagaId: {} ===",
                order.getIdValue(), saga.getSagaIdValue());

        // 2. Saga Step 시작
        saga.startStep(SagaStep.ROUTE_CALCULATE);
        order.startRouteCalculation();
        sagaRepository.save(saga);
        orderRepository.save(order);

        // 3. Hub Service 경로 계산
        HubRouteCalculationResponse hubResponse = callHubRouteCalculation(order);
        log.info("Hub 경로 계산 완료 - orderId: {}, originHub: {}, destinationHub: {}, hubs: {}",
                order.getIdValue(),
                hubResponse.getOriginHubId(),
                hubResponse.getDestinationHubId(),
                hubResponse.getRouteHubs());

        // 4. Order 도메인 - Hub 정보 업데이트
        updateOrderWithHubResponse(order, hubResponse);
        orderRepository.save(order);

        // 5. AI Service 시간 계산
        AiTimeCalculationResponse aiResponse = callAiTimeCalculation(order, hubResponse);
        log.info("AI 시간 계산 완료 - orderId: {}, departureDeadline: {}, estimatedDelivery: {}",
                order.getIdValue(),
                aiResponse.getCalculatedDepartureDeadline(),
                aiResponse.getEstimatedDeliveryTime());

        // 6. Order 도메인 - AI 계산 결과 업데이트
        updateOrderWithAiResponse(order, aiResponse);
        orderRepository.save(order);

        // 7. Step 완료 처리
        saga.completeStep(SagaStep.ROUTE_CALCULATE, hubResponse);
        saga.addStepHistory(SagaStep.ROUTE_CALCULATE, aiResponse);

        order.startDeliveryCreation();

        sagaRepository.save(saga);
        orderRepository.save(order);

        log.info("<<< Step 3: 경로 및 시간 계산 완료 - orderId: {}, requiresHubDelivery: {}, orderStatus: {}",
                event.getOrderId(),
                hubResponse.getRequiresHubDelivery(),
                order.getStatus().getDescription());

        // 8. 다음 Step 결정 및 트리거
        triggerNextStep(order, saga, hubResponse);
    }

    /**
     * Step 3 실패 시 보상 처리 - 트랜잭션 밖
     */
    private void handleRouteCalculationFailure(String orderId, Exception e) {
        try {
            Order order = orderRepository.findById(OrderId.from(orderId))
                    .orElse(null);
            OrderSaga saga = sagaRepository.findByOrderId(OrderId.from(orderId))
                    .orElse(null);

            if (saga == null) {
                log.warn("Saga를 찾을 수 없음, 보상 불필요 - orderId: {}", orderId);
                return;
            }

            // Step 실패 처리 (별도 트랜잭션)
            markStepAsFailed(orderId, e.getMessage());

            // 보상 트랜잭션 시작 (별도 트랜잭션)
            compensationService.startCompensationForRouteFailure(orderId, e.getMessage());

        } catch (Exception compensationError) {
            log.error("보상 처리 중 오류 - orderId: {}", orderId, compensationError);
        }
    }

    /**
     * Step 실패 상태 업데이트 - 별도 트랜잭션
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markStepAsFailed(String orderId, String errorMessage) {
        Order order = orderRepository.findById(OrderId.from(orderId)).orElseThrow();
        OrderSaga saga = sagaRepository.findByOrderId(order.getId()).orElseThrow();

        saga.failStep(SagaStep.ROUTE_CALCULATE, errorMessage);
        order.fail();

        sagaRepository.save(saga);
        orderRepository.save(order);
    }

    /**
     * Hub Service 경로 계산 호출
     */
    private HubRouteCalculationResponse callHubRouteCalculation(Order order) {
        log.debug("Hub Service 경로 계산 요청 - orderId: {}, originHub: {}, address: {}",
                order.getIdValue(),
                order.getProductInfo().getProductHubId(),
                order.getReceiverInfo().getDeliveryAddress());

        HubRouteCalculationRequest request = buildHubRouteCalculationRequest(order);

        return hubClient.calculateRoute(request);
    }

    /**
     * AI Service 시간 계산 호출
     */
    private AiTimeCalculationResponse callAiTimeCalculation(
            Order order,
            HubRouteCalculationResponse hubResponse) {

        log.debug("AI Service 시간 계산 요청 - orderId: {}, requestedDelivery: {} {}, distance: {}km",
                order.getIdValue(),
                order.getRequestInfo().getRequestedDeliveryDate(),
                order.getRequestInfo().getRequestedDeliveryTime(),
                hubResponse.getEstimatedDistance());

        AiTimeCalculationRequest request = buildAiTimeCalculationRequest(order, hubResponse);

        AiTimeCalculationResponse response = aiClient.calculateDeliveryTime(request);

        // AI 계산 성공 여부 검증
        if (!response.isSuccessful()) {
            throw new IllegalStateException(
                    "AI 시간 계산 실패: " + response.getErrorMessage()
            );
        }

        // 발송 시한 초과 경고
        if (response.isDepartureDeadlinePassed()) {
            log.warn("발송 시한 초과 - orderId: {}, deadline: {}, 즉시 발송 필요",
                    order.getIdValue(),
                    response.getCalculatedDepartureDeadline());
        }

        return response;
    }

    /**
     * 다음 Step 결정 및 트리거
     * - 허브 배송 필요: Step 4 → Step 5
     * - 허브 배송 불필요: Step 5 직접 실행
     */
    private void triggerNextStep(Order order, OrderSaga saga, HubRouteCalculationResponse hubResponse) {
        boolean requiresHubDelivery = !hubResponse.getOriginHubId()
                .equals(hubResponse.getDestinationHubId());

        if (requiresHubDelivery) {
            log.info("다음 Step: Step 4 (허브 배송 생성) - 허브 간 이동 필요 ({} → {})",
                    hubResponse.getOriginHubId(),
                    hubResponse.getDestinationHubId());

            // Step 4 실행 → 내부에서 Step 5 호출
            executeHubDeliveryCreation(order, saga, hubResponse);

        } else {
            log.info("다음 Step: Step 5 (업체 배송 생성) - 동일 허브 내 배송 ({})",
                    hubResponse.getOriginHubId());

            // Step 4 스킵, Step 5 직접 실행
            executeLastMileDeliveryCreation(order, saga);
        }
    }

    // ==================== Step 4: 허브 배송 생성 ====================

    /**
     * Step 4: 허브 배송 생성
     * 출발 허브 → 도착 허브 간 배송 생성
     */
    @Transactional
    public void executeHubDeliveryCreation(Order order, OrderSaga saga, HubRouteCalculationResponse hubResponse) {
        log.info(">>> Step 4: 허브 배송 생성 시작 - orderId: {}", order.getIdValue());

        saga.startStep(SagaStep.HUB_DELIVERY_CREATE);
        sagaRepository.save(saga);

        try {
            // 1. 허브 배송 생성 요청
            HubDeliveryCreateRequest request = buildHubDeliveryRequest(order, hubResponse);
            HubDeliveryCreateResponse response = hubDeliveryClient.createDelivery(request);

            // 2. 응답 검증
            if (!response.isSuccess()) {
                throw new SagaException(
                        OrderErrorCode.HUB_DELIVERY_CREATION_FAILED,
                        "허브 배송 생성에 실패했습니다: " + response.getMessage()
                );
            }

            // 3. Order 도메인 업데이트 - 허브 배송 ID 저장
            updateOrderWithHubDeliveryId(order, response.getHubDeliveryId());
            orderRepository.save(order);

            // 4. Saga Step 완료
            saga.completeStep(SagaStep.HUB_DELIVERY_CREATE, response);
            sagaRepository.save(saga);

            log.info(">>> Step 4: 허브 배송 생성 완료 - orderId: {}, hubDeliveryId: {}",
                    order.getIdValue(), response.getHubDeliveryId());

            // 5. 다음 Step 실행: Step 5 (업체 배송 생성)
            executeLastMileDeliveryCreation(order, saga);

        } catch (Exception e) {
            log.error("Step 4: 허브 배송 생성 실패 - orderId: {}, error: {}",
                    order.getIdValue(), e.getMessage(), e);

            saga.failStep(SagaStep.HUB_DELIVERY_CREATE, e.getMessage());
            sagaRepository.save(saga);

            // 보상 트랜잭션 시작
            startCompensation(order, saga, e.getMessage());

            throw e;
        }
    }

    // ==================== Step 5: 업체 배송 생성 ====================

    /**
     * Step 5: 업체 배송 생성
     * 도착 허브 → 수령 업체 배송 생성
     */
    @Transactional
    public void executeLastMileDeliveryCreation(Order order, OrderSaga saga) {
        log.info(">>> Step 5: 업체 배송 생성 시작 - orderId: {}", order.getIdValue());

        saga.startStep(SagaStep.LAST_MILE_DELIVERY_CREATE);
        sagaRepository.save(saga);

        try {
            // 1. 업체 배송 생성 요청
            LastMileDeliveryCreateRequest request = buildLastMileDeliveryRequest(order);
            LastMileDeliveryCreateResponse response = lastMileClient.createDelivery(request);

            // 2. 응답 검증
            if (!response.isSuccess()) {
                throw new SagaException(
                        OrderErrorCode.LAST_MILE_DELIVERY_CREATION_FAILED,
                        "업체 배송 생성에 실패했습니다: " + response.getMessage()
                );
            }

            // 3. Order 도메인 업데이트 - 업체 배송 ID 저장
            updateOrderWithLastMileDeliveryId(order, response.getLastMileDeliveryId());
            orderRepository.save(order);

            // 4. Saga Step 완료
            saga.completeStep(SagaStep.LAST_MILE_DELIVERY_CREATE, response);
            sagaRepository.save(saga);

            log.info(">>> Step 5: 업체 배송 생성 완료 - orderId: {}, lastMileDeliveryId: {}",
                    order.getIdValue(), response.getLastMileDeliveryId());

            // 5. 주문 확정 처리
            completeOrderSaga(order, saga);

        } catch (Exception e) {
            log.error("Step 5: 업체 배송 생성 실패 - orderId: {}, error: {}",
                    order.getIdValue(), e.getMessage(), e);

            saga.failStep(SagaStep.LAST_MILE_DELIVERY_CREATE, e.getMessage());
            sagaRepository.save(saga);

            // 보상 트랜잭션 시작
            startCompensation(order, saga, e.getMessage());

            throw e;
        }
    }

    /**
     * Saga 완료 처리
     * 모든 Step 성공 시 주문 확정
     */
    private void completeOrderSaga(Order order, OrderSaga saga) {
        log.info("=== Order Saga 완료 처리 - orderId: {} ===", order.getIdValue());

        // 1. 주문 확정
        order.confirm();
        orderRepository.save(order);

        // 2. Saga 완료
        saga.complete();
        sagaRepository.save(saga);

        log.info("=== Order Saga 완료 - orderId: {}, orderStatus: {} ===",
                order.getIdValue(), order.getStatus().getDescription());

        // 3. Step 6 (알림) 이벤트 발행 - Best Effort
        publishNotificationEvent(order);

        // 4. Step 7 (추적 시작) 이벤트 발행 - Best Effort
        publishTrackingStartEvent(order);
    }

    /**
     * Step 6: 알림 발송 요청 이벤트 발행
     * Best Effort - 실패해도 주문 처리는 계속 진행
     */
    private void publishNotificationEvent(Order order) {
        log.info(">>> Step 6: 알림 발송 이벤트 발행 시작 - orderId: {}", order.getIdValue());

        try {
            // 1. 알림 발송 요청 이벤트 데이터 생성
            NotificationRequestedEventData eventData = NotificationRequestedEventData.of(
                    order.getIdValue(),
                    order.getReceiverInfo().getReceiverName(),
                    order.getReceiverInfo().getReceiverPhone(),
                    order.getReceiverInfo().getReceiverEmail(),
                    order.getOrderNumber().getValue(),
                    order.getAiCalculationResult().getEstimatedDeliveryTime(),
                    order.getReceiverInfo().getDeliveryAddress()
            );

            // 2. 알림 발송 요청 이벤트 발행
            notificationEventPublisher.publishNotificationRequested(eventData);

            log.info(">>> Step 6: 알림 발송 이벤트 발행 완료 - orderId: {}, receiverName: {}, receiverEmail: {}",
                    order.getIdValue(),
                    order.getReceiverInfo().getReceiverName(),
                    order.getReceiverInfo().getReceiverEmail());

        } catch (Exception e) {
            // Best Effort: 알림 실패해도 주문 처리는 계속 진행
            log.error("Step 6: 알림 발송 이벤트 발행 실패 (Best Effort, 주문 처리 계속) - orderId: {}, error: {}",
                    order.getIdValue(), e.getMessage(), e);
        }
    }

    // ==================== Step 7: 추적 시작 (Best Effort) ====================

    /**
     * Step 7: 추적 시작 요청 이벤트 발행
     * Best Effort - 실패해도 주문 처리는 계속 진행
     */
    private void publishTrackingStartEvent(Order order) {
        log.info(">>> Step 7: 추적 시작 이벤트 발행 시작 - orderId: {}", order.getIdValue());

        try {
            // 1. 추적 시작 요청 이벤트 데이터 생성
            TrackingStartRequestedEventData eventData = TrackingStartRequestedEventData.of(
                    order.getIdValue(),
                    order.getOrderNumber().getValue(),
                    order.getDeliveryInfo().getHubDeliveryId(),
                    order.getDeliveryInfo().getLastMileDeliveryId(),
                    order.getProductInfo().getProductHubId(),
                    order.getDestinationHubId(),
                    order.getRoutingHub(),
                    order.getDeliveryInfo().getRequiresHubDelivery(),
                    order.getAiCalculationResult().getEstimatedDeliveryTime()
            );

            // 2. 추적 시작 요청 이벤트 발행
            trackingEventPublisher.publishTrackingStartRequested(eventData);

            log.info(">>> Step 7: 추적 시작 이벤트 발행 완료 - orderId: {}, hubDeliveryId: {}, lastMileDeliveryId: {}",
                    order.getIdValue(),
                    order.getDeliveryInfo().getHubDeliveryId(),
                    order.getDeliveryInfo().getLastMileDeliveryId());

        } catch (Exception e) {
            // Best Effort: 추적 시작 실패해도 주문 처리는 계속 진행
            log.error("Step 7: 추적 시작 이벤트 발행 실패 (Best Effort, 주문 처리 계속) - orderId: {}, error: {}",
                    order.getIdValue(), e.getMessage(), e);
        }
    }

    // ==================== Request Builder 메서드 ====================

    /**
     * Hub Service 경로 계산 요청 빌드
     */
    private HubRouteCalculationRequest buildHubRouteCalculationRequest(Order order) {
        return HubRouteCalculationRequest.of(
                order.getIdValue(),
                order.getProductInfo().getProductHubId(),
                order.getReceiverInfo().getDeliveryAddress(),
                order.getReceiverInfo().getDeliveryAddressDetail()
        );
    }

    /**
     * AI Service 시간 계산 요청 빌드
     */
    private AiTimeCalculationRequest buildAiTimeCalculationRequest(
            Order order,
            HubRouteCalculationResponse hubResponse) {

        return AiTimeCalculationRequest.of(
                order.getIdValue(),
                hubResponse.getOriginHubId(),
                hubResponse.getDestinationHubId(),
                hubResponse.getRouteHubs(),
                hubResponse.getRequiresHubDelivery(),
                hubResponse.getEstimatedDistance(),
                hubResponse.getRouteInfoJson(),
                order.getRequestInfo().getRequestedDeliveryDate(),
                order.getRequestInfo().getRequestedDeliveryTime(),
                order.getReceiverInfo().getDeliveryAddress(),
                order.getReceiverInfo().getDeliveryAddressDetail(),
                order.getProductInfo().getQuantity(),
                order.getRequestInfo().getSpecialInstructions()
        );
    }

    /**
     * 허브 배송 생성 요청 빌드
     */
    private HubDeliveryCreateRequest buildHubDeliveryRequest(Order order, HubRouteCalculationResponse hubResponse) {
        AiCalculationResult aiResult = order.getAiCalculationResult();

        return HubDeliveryCreateRequest.of(
                order.getIdValue(),
                order.getProductInfo().getProductHubId(),      // 출발 허브
                order.getDestinationHubId(),                    // 도착 허브
                hubResponse.getRouteHubs(),                     // HubResponse에서 직접 가져옴
                hubResponse.getRouteInfoJson(),
                formatDateTime(aiResult.getCalculatedDepartureDeadline()),
                formatDateTime(aiResult.getEstimatedDeliveryTime())
        );
    }

    /**
     * 업체 배송 생성 요청 빌드
     */
    private LastMileDeliveryCreateRequest buildLastMileDeliveryRequest(Order order) {
        AiCalculationResult aiResult = order.getAiCalculationResult();

        return LastMileDeliveryCreateRequest.of(
                order.getIdValue(),
                order.getDestinationHubId(),                           // 도착 허브 (배송 출발점)
                order.getCompanyInfo().getReceiverCompanyId(),         // 수령 업체 ID
                order.getReceiverInfo().getDeliveryAddress(),          // 받는 곳 주소
                order.getReceiverInfo().getDeliveryAddressDetail(),    // 상세 주소
                order.getReceiverInfo().getReceiverName(),             // 받는 사람 이름
                order.getReceiverInfo().getReceiverPhone(),            // 받는 사람 전화번호
                formatDateTime(aiResult.getEstimatedDeliveryTime()),   // 예상 도착 시간
                order.getRequestInfo().getSpecialInstructions()        // 특이사항
        );
    }

    /**
     * LocalDateTime을 ISO 8601 문자열로 변환
     */
    private String formatDateTime(java.time.LocalDateTime dateTime) {
        if (dateTime == null) {
            return null;
        }
        return dateTime.format(java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME);
    }

    // ==================== Order 업데이트 메서드 ====================

    /**
     * Hub 응답으로 Order 업데이트
     */
    private void updateOrderWithHubResponse(Order order, HubRouteCalculationResponse hubResponse) {
        // 1. 도착 허브 ID 설정
        order.updateDestinationHubId(hubResponse.getDestinationHubId());

        // 2. 경로 정보 JSON 저장
        order.updateRouteInfo(hubResponse.getRouteInfoJson());

        // 3. 허브 배송 필요 여부 설정
        DeliveryInfo updatedDeliveryInfo = order.getDeliveryInfo()
                .withRequiresHubDelivery(hubResponse.getRequiresHubDelivery());
        order.updateDeliveryInfo(updatedDeliveryInfo);

        log.debug("Order Hub 정보 업데이트 완료 - orderId: {}, destinationHub: {}, requiresHubDelivery: {}",
                order.getIdValue(),
                hubResponse.getDestinationHubId(),
                hubResponse.getRequiresHubDelivery());
    }

    /**
     * AI 응답으로 Order 업데이트
     */
    private void updateOrderWithAiResponse(Order order, AiTimeCalculationResponse aiResponse) {
        // 기존 AiCalculationResult에 AI 계산 결과 추가
        AiCalculationResult updatedResult = order.getAiCalculationResult()
                .withAiCalculation(
                        aiResponse.getCalculatedDepartureDeadline(),
                        aiResponse.getEstimatedDeliveryTime(),
                        aiResponse.getAiMessage()
                );

        // Order 도메인에 AI 계산 결과 반영
        order.updateAiCalculationResult(updatedResult);

        log.debug("Order AI 계산 결과 업데이트 완료 - orderId: {}, departureDeadline: {}, estimatedDelivery: {}",
                order.getIdValue(),
                aiResponse.getCalculatedDepartureDeadline(),
                aiResponse.getEstimatedDeliveryTime());
    }

    /**
     * 허브 배송 ID로 Order 업데이트
     */
    private void updateOrderWithHubDeliveryId(Order order, String hubDeliveryId) {
        DeliveryInfo updatedDeliveryInfo = order.getDeliveryInfo()
                .withHubDeliveryId(hubDeliveryId);

        order.updateDeliveryInfo(updatedDeliveryInfo);

        log.debug("Order 허브 배송 ID 업데이트 완료 - orderId: {}, hubDeliveryId: {}",
                order.getIdValue(), hubDeliveryId);
    }

    /**
     * 업체 배송 ID로 Order 업데이트
     */
    private void updateOrderWithLastMileDeliveryId(Order order, String lastMileDeliveryId) {
        DeliveryInfo updatedDeliveryInfo = order.getDeliveryInfo()
                .withLastMileDeliveryId(lastMileDeliveryId);

        order.updateDeliveryInfo(updatedDeliveryInfo);

        log.debug("Order 업체 배송 ID 업데이트 완료 - orderId: {}, lastMileDeliveryId: {}",
                order.getIdValue(), lastMileDeliveryId);
    }

    // ==================== 보상 트랜잭션 ====================

    /**
     * 보상 트랜잭션 시작
     * REQUIRES_NEW: 호출자 트랜잭션과 독립적으로 실행
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void startCompensation(Order order, OrderSaga saga, String failureReason) {
        log.warn("보상 트랜잭션 시작 - orderId: {}, reason: {}",
                order.getIdValue(), failureReason);

        saga.startCompensation(failureReason);
        sagaRepository.save(saga);

        var completedSteps = saga.getCompletedStepsNeedingCompensation();
        log.info("보상 대상 Step 목록: {}", completedSteps);

        for (SagaStep step : completedSteps.reversed()) {
            try {
                executeCompensationStep(order, saga, step);
            } catch (Exception e) {
                log.error("보상 Step 실패 - step: {}, error: {}",
                        step.getDescription(), e.getMessage(), e);

                saga.failCompensation(step.getCompensationStep(), e.getMessage());
                sagaRepository.save(saga);

                order.fail();
                orderRepository.save(order);

                return;
            }
        }

        saga.completeAllCompensations();
        sagaRepository.save(saga);

        order.compensate();
        orderRepository.save(order);

        log.info("보상 트랜잭션 완료 - orderId: {}", order.getIdValue());
    }

    /**
     * 개별 보상 Step 실행
     *
     * 주의: PAYMENT_CANCEL은 이벤트만 발행하고 완료 처리는 Consumer에서 수행
     */
    private OrderSaga executeCompensationStep(Order order, OrderSaga saga, SagaStep originalStep) {
        SagaStep compensationStep = originalStep.getCompensationStep();

        log.info(">>> 보상 Step 실행 - originalStep: {}, compensationStep: {}",
                originalStep.getDescription(), compensationStep.getDescription());

        saga.executeCompensation(originalStep, compensationStep);
        saga = sagaRepository.save(saga);
        try {
            switch (compensationStep) {
                case PAYMENT_CANCEL -> {
                    // 이벤트만 발행, 완료 처리는 Consumer에서
                    compensatePayment(order, saga);
                    log.info(">>> 결제 취소 이벤트 발행 완료, PaymentRefundedEvent 대기 중");
                    // 여기서 완료 처리하지 않음! Consumer가 처리함
                    return saga;
                }
                case STOCK_RESTORE -> {
                    // 동기 실행 후 즉시 완료 처리
                    compensationService.compensateStock(order, saga);  // ← CompensationService 사용
                }
                case HUB_DELIVERY_CANCEL -> {
                    // 동기 실행 후 즉시 완료 처리
                    compensateHubDelivery(order, saga);
                }
                case LAST_MILE_DELIVERY_CANCEL -> {
                    // 동기 실행 후 즉시 완료 처리
                    compensateLastMileDelivery(order, saga);
                }
                default -> throw new IllegalStateException(
                        "지원하지 않는 보상 Step: " + compensationStep
                );
            }

            // PAYMENT_CANCEL 제외하고 여기서 완료 처리
            saga = sagaRepository.findById(saga.getSagaId()).orElseThrow();
            saga.completeCompensation(compensationStep);
            saga = sagaRepository.save(saga);

            log.info(">>> 보상 Step 완료 - compensationStep: {}",
                    compensationStep.getDescription());

        } catch (Exception e) {
            saga = sagaRepository.findById(saga.getSagaId()).orElseThrow();
            saga.failCompensation(compensationStep, e.getMessage());
            saga = sagaRepository.save(saga);
            throw e;
        }
        return saga;
    }

    /**
     * 결제 취소 (Compensation)
     * 이벤트 기반 처리: RefundRequestedEvent 발행
     */
    private void compensatePayment(Order order, OrderSaga saga) {
        log.info(">>> 결제 취소 시작 (이벤트 발행) - orderId: {}", order.getIdValue());

        try {
            // 1. 보상 데이터 조회
            Object stepData = saga.getCompensationDataForStep(SagaStep.PAYMENT_VERIFY);

            // LinkedHashMap → PaymentVerificationResponse 변환
            PaymentVerificationResponse verifyResponse;
            if (stepData instanceof PaymentVerificationResponse) {
                verifyResponse = (PaymentVerificationResponse) stepData;
            } else {
                verifyResponse = objectMapper.convertValue(stepData, PaymentVerificationResponse.class);
            }

            // 2. 환불 요청 이벤트 데이터 생성
            RefundRequestedEventData eventData = RefundRequestedEventData.of(
                    verifyResponse.getPaymentId(),
                    order.getIdValue(),
                    "주문 생성 실패로 인한 자동 취소"
            );

            // 3. 환불 요청 이벤트 발행 (비동기)
            paymentEventPublisher.publishRefundRequested(eventData);

            log.info(">>> 결제 취소 이벤트 발행 완료 - orderId: {}, paymentId: {}",
                    order.getIdValue(), verifyResponse.getPaymentId());

            // 참고: 실제 환불 처리는 Payment Service에서 수행
            // PaymentRefundedEvent 또는 PaymentRefundFailedEvent를 수신하여 처리 완료

        } catch (Exception e) {
            log.error("결제 취소 이벤트 발행 실패 - orderId: {}, error: {}",
                    order.getIdValue(), e.getMessage(), e);
            throw e;
        }
    }

    /**
     * 재고 복원 (Compensation)
     */
    private void compensateStock(Order order, OrderSaga saga) {
        log.info(">>> 재고 복원 시작 - orderId: {}", order.getIdValue());

        try {
            Object stepData = saga.getCompensationDataForStep(SagaStep.STOCK_RESERVE);
            InventoryReservationResponse reserveResponse = (InventoryReservationResponse) stepData;

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

        } catch (Exception e) {
            log.error("재고 복원 실패 - orderId: {}, error: {}",
                    order.getIdValue(), e.getMessage(), e);
            throw e;
        }
    }

    /**
     * 허브 배송 취소 (Compensation)
     */
    private void compensateHubDelivery(Order order, OrderSaga saga) {
        log.info(">>> 허브 배송 취소 시작 - orderId: {}", order.getIdValue());

        try {
            String hubDeliveryId = order.getDeliveryInfo().getHubDeliveryId();

            if (hubDeliveryId == null || hubDeliveryId.isBlank()) {
                log.info(">>> 허브 배송 ID 없음, 취소 스킵 - orderId: {}", order.getIdValue());
                return;
            }

            HubDeliveryCreateResponse response = hubDeliveryClient.cancelDelivery(hubDeliveryId);

            log.info(">>> 허브 배송 취소 완료 - orderId: {}, hubDeliveryId: {}",
                    order.getIdValue(), hubDeliveryId);

        } catch (Exception e) {
            log.error("허브 배송 취소 실패 - orderId: {}, error: {}",
                    order.getIdValue(), e.getMessage(), e);
            throw e;
        }
    }

    /**
     * 업체 배송 취소 (Compensation)
     */
    private void compensateLastMileDelivery(Order order, OrderSaga saga) {
        log.info(">>> 업체 배송 취소 시작 - orderId: {}", order.getIdValue());

        try {
            String lastMileDeliveryId = order.getDeliveryInfo().getLastMileDeliveryId();

            if (lastMileDeliveryId == null || lastMileDeliveryId.isBlank()) {
                log.info(">>> 업체 배송 ID 없음, 취소 스킵 - orderId: {}", order.getIdValue());
                return;
            }

            LastMileDeliveryCreateResponse response = lastMileClient.cancelDelivery(lastMileDeliveryId);

            log.info(">>> 업체 배송 취소 완료 - orderId: {}, lastMileDeliveryId: {}",
                    order.getIdValue(), lastMileDeliveryId);

        } catch (Exception e) {
            log.error("업체 배송 취소 실패 - orderId: {}, error: {}",
                    order.getIdValue(), e.getMessage(), e);
            throw e;
        }
    }

    // ==================== TODO: Step 6 & 7 ====================

    /**
     * TODO: Step 6 - 알림 발송 완료 이벤트 핸들러 (Best Effort)
     * - NotificationSentEvent 수신
     * - 실패해도 Saga 계속 진행
     */
    // @TransactionalEventListener
    // public void handleNotificationSent(NotificationSentEvent event) { ... }

    /**
     * TODO: Step 7 - 추적 시작 완료 이벤트 핸들러 (Best Effort)
     * - TrackingStartedEvent 수신
     * - 실패해도 Saga 계속 진행
     * - Saga 완료
     */
    // @TransactionalEventListener
    // public void handleTrackingStarted(TrackingStartedEvent event) { ... }
}
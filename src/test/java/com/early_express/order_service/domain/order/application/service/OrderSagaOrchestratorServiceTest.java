package com.early_express.order_service.domain.order.application.service;

import com.early_express.order_service.domain.order.domain.exception.SagaException;
import com.early_express.order_service.domain.order.domain.messaging.notification.NotificationEventPublisher;
import com.early_express.order_service.domain.order.domain.messaging.order.OrderEventPublisher;
import com.early_express.order_service.domain.order.domain.messaging.order.event.OrderPaymentVerifiedEvent;
import com.early_express.order_service.domain.order.domain.messaging.payment.PaymentEventPublisher;
import com.early_express.order_service.domain.order.domain.messaging.tracking.TrackingEventPublisher;
import com.early_express.order_service.domain.order.domain.model.*;
import com.early_express.order_service.domain.order.domain.model.Order;
import com.early_express.order_service.domain.order.domain.model.vo.*;
import com.early_express.order_service.domain.order.domain.repository.OrderRepository;
import com.early_express.order_service.domain.order.domain.repository.OrderSagaRepository;
import com.early_express.order_service.domain.order.infrastructure.client.ai.AiClient;
import com.early_express.order_service.domain.order.infrastructure.client.ai.dto.AiTimeCalculationResponse;
import com.early_express.order_service.domain.order.infrastructure.client.hub.HubClient;
import com.early_express.order_service.domain.order.infrastructure.client.hub.dto.HubRouteCalculationResponse;
import com.early_express.order_service.domain.order.infrastructure.client.hubdelivery.HubDeliveryClient;
import com.early_express.order_service.domain.order.infrastructure.client.hubdelivery.dto.HubDeliveryCreateResponse;
import com.early_express.order_service.domain.order.infrastructure.client.inventory.InventoryClient;
import com.early_express.order_service.domain.order.infrastructure.client.inventory.dto.InventoryReservationResponse;
import com.early_express.order_service.domain.order.infrastructure.client.inventory.dto.InventoryRestoreResponse;
import com.early_express.order_service.domain.order.infrastructure.client.lastmile.LastMileClient;
import com.early_express.order_service.domain.order.infrastructure.client.lastmile.dto.LastMileDeliveryCreateResponse;
import com.early_express.order_service.domain.order.infrastructure.client.payment.PaymentClient;
import com.early_express.order_service.domain.order.infrastructure.client.payment.dto.PaymentVerificationResponse;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("OrderSagaOrchestratorService 통합 테스트")
class OrderSagaOrchestratorServiceTest {

    @Autowired
    private OrderSagaOrchestratorService orchestratorService;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderSagaRepository sagaRepository;

    // 모든 외부 클라이언트와 이벤트 발행자는 Mock으로 처리
    @MockBean
    private PaymentEventPublisher paymentEventPublisher;

    @MockitoBean
    private OrderEventPublisher orderEventPublisher;

    @MockitoBean
    private NotificationEventPublisher notificationEventPublisher;

    @MockitoBean
    private TrackingEventPublisher trackingEventPublisher;

    @MockitoBean
    private PaymentClient paymentClient;

    @MockitoBean
    private InventoryClient inventoryClient;

    @MockitoBean
    private HubClient hubClient;

    @MockitoBean
    private AiClient aiClient;

    @MockitoBean
    private HubDeliveryClient hubDeliveryClient;

    @MockitoBean
    private LastMileClient lastMileClient;

    @BeforeEach
    void setUp() {
        // DB 완전 초기화 (순서 중요: 자식 → 부모)
        sagaRepository.deleteAll();
        orderRepository.deleteAll();

        // Mock 초기화
        reset(paymentEventPublisher, orderEventPublisher, notificationEventPublisher,
                trackingEventPublisher, paymentClient, inventoryClient, hubClient,
                aiClient, hubDeliveryClient, lastMileClient);
    }

    @Test
    @org.junit.jupiter.api.Order(1)
    @DisplayName("Saga 시작 성공 - 재고 예약 및 결제 검증 완료")
    void startOrderSaga_Success() {
        // given
        Order order = createAndSaveUniqueOrder();

        // Mock: 재고 예약 성공 응답
        given(inventoryClient.reserveStock(any())).willReturn(
                InventoryReservationResponse.builder()
                        .reservationId("RES-001")
                        .orderId(order.getIdValue())
                        .allSuccess(true)
                        .reservedItems(List.of(
                                InventoryReservationResponse.ReservedItem.builder()
                                        .productId("PROD-001")
                                        .hubId("HUB-001")
                                        .quantity(10)
                                        .success(true)
                                        .build()
                        ))
                        .build()
        );

        // Mock: 결제 검증 성공 응답
        given(paymentClient.verifyAndRegisterPayment(any())).willReturn(
                PaymentVerificationResponse.builder()
                        .paymentId("PAYMENT-001")
                        .status("VERIFIED")
                        .verifiedAmount(BigDecimal.valueOf(500000))
                        .message("검증 완료")
                        .build()
        );

        // when
        orchestratorService.startOrderSaga(order);

        // then
        verify(inventoryClient, times(1)).reserveStock(any());
        verify(paymentClient, times(1)).verifyAndRegisterPayment(any());
        verify(orderEventPublisher, times(1)).publishOrderPaymentVerified(any());

        // Order와 Saga가 실제 DB에 저장되었는지 확인
        Order savedOrder = orderRepository.findById(order.getId()).orElseThrow();
        assertThat(savedOrder.getStatus()).isEqualTo(OrderStatus.PAYMENT_VERIFIED);

        OrderSaga savedSaga = sagaRepository.findByOrderId(order.getId()).orElseThrow();
        assertThat(savedSaga.getStatus()).isEqualTo(SagaStatus.IN_PROGRESS);
    }

    @Test
    @org.junit.jupiter.api.Order(2)
    @DisplayName("Saga 시작 실패 - 재고 예약 실패 시 보상 트랜잭션 실행")
    void startOrderSaga_Fail_StockReservationFailed() {
        // given
        Order order = createAndSaveUniqueOrder();

        // Mock: 재고 예약 실패 응답
        given(inventoryClient.reserveStock(any())).willReturn(
                InventoryReservationResponse.builder()
                        .reservationId("RES-001")
                        .orderId(order.getIdValue())
                        .allSuccess(false)  // 재고 예약 실패
                        .reservedItems(List.of())
                        .build()
        );

        // when & then
        assertThatThrownBy(() -> orchestratorService.startOrderSaga(order))
                .isInstanceOf(SagaException.class);

        verify(inventoryClient, times(1)).reserveStock(any());
        verify(paymentClient, never()).verifyAndRegisterPayment(any());

        // 보상 트랜잭션이 실행되어 Saga가 저장되었는지 확인
        OrderSaga savedSaga = sagaRepository.findByOrderId(order.getId()).orElseThrow();
        assertThat(savedSaga.getStatus()).isIn(
                SagaStatus.COMPENSATING,
                SagaStatus.COMPENSATED,
                SagaStatus.COMPENSATION_FAILED
        );

        // Order 상태도 확인
        Order savedOrder = orderRepository.findById(order.getId()).orElseThrow();
        assertThat(savedOrder.getStatus()).isIn(
                OrderStatus.COMPENSATED,
                OrderStatus.FAILED
        );
    }

    @Test
    @org.junit.jupiter.api.Order(3)
    @DisplayName("Saga 시작 실패 - 결제 검증 실패 시 재고 보상")
    void startOrderSaga_Fail_PaymentVerificationFailed() {
        // given
        Order order = createAndSaveUniqueOrder();

        // Mock: 재고 예약 성공
        given(inventoryClient.reserveStock(any())).willReturn(
                InventoryReservationResponse.builder()
                        .reservationId("RES-001")
                        .orderId(order.getIdValue())
                        .allSuccess(true)
                        .reservedItems(List.of(
                                InventoryReservationResponse.ReservedItem.builder()
                                        .productId("PROD-001")
                                        .hubId("HUB-001")
                                        .quantity(10)
                                        .success(true)
                                        .build()
                        ))
                        .build()
        );

        // Mock: 결제 검증 실패
        given(paymentClient.verifyAndRegisterPayment(any())).willReturn(
                PaymentVerificationResponse.builder()
                        .paymentId(null)
                        .status("FAILED")  // 검증 실패
                        .message("금액 불일치")
                        .build()
        );

        // Mock: 재고 복원 성공
        given(inventoryClient.restoreStock(any())).willReturn(
                InventoryRestoreResponse.builder()
                        .success(true)
                        .message("재고 복원 완료")
                        .build()
        );

        // when & then
        assertThatThrownBy(() -> orchestratorService.startOrderSaga(order))
                .isInstanceOf(SagaException.class);

        verify(inventoryClient, times(1)).reserveStock(any());
        verify(paymentClient, times(1)).verifyAndRegisterPayment(any());

        // 보상 트랜잭션 실행 확인
        OrderSaga savedSaga = sagaRepository.findByOrderId(order.getId()).orElseThrow();
        assertThat(savedSaga.getStatus()).isIn(
                SagaStatus.COMPENSATING,
                SagaStatus.COMPENSATED
        );

        // 재고 복원 API가 호출되었는지 확인
        verify(inventoryClient, atLeastOnce()).restoreStock(any());

        // Order 상태 확인
        Order savedOrder = orderRepository.findById(order.getId()).orElseThrow();
        assertThat(savedOrder.getStatus()).isEqualTo(OrderStatus.COMPENSATED);
    }

    @Test
    @org.junit.jupiter.api.Order(4)
    @DisplayName("경로 계산 성공")
    void executeRouteCalculation_Success() {
        // given
        Order order = createAndSaveUniqueOrder();

        // Step 1, 2 먼저 실행하여 Saga 생성
        given(inventoryClient.reserveStock(any())).willReturn(
                InventoryReservationResponse.builder()
                        .reservationId("RES-001")
                        .orderId(order.getIdValue())
                        .allSuccess(true)
                        .reservedItems(List.of(
                                InventoryReservationResponse.ReservedItem.builder()
                                        .productId("PROD-001")
                                        .hubId("HUB-001")
                                        .quantity(10)
                                        .success(true)
                                        .build()
                        ))
                        .build()
        );

        given(paymentClient.verifyAndRegisterPayment(any())).willReturn(
                PaymentVerificationResponse.builder()
                        .paymentId("PAYMENT-001")
                        .status("VERIFIED")
                        .verifiedAmount(BigDecimal.valueOf(500000))
                        .message("검증 완료")
                        .build()
        );

        // startOrderSaga로 Saga 생성
        orchestratorService.startOrderSaga(order);

        // Order와 Saga 다시 조회
        order = orderRepository.findById(order.getId()).orElseThrow();
        OrderSaga saga = sagaRepository.findByOrderId(order.getId()).orElseThrow();

        OrderPaymentVerifiedEvent event = OrderPaymentVerifiedEvent.builder()
                .orderId(order.getIdValue())
                .sagaId(saga.getSagaIdValue())
                .productHubId("HUB-001")
                .deliveryAddress("서울시 강남구")
                .deliveryAddressDetail("테헤란로 123")
                .publishedAt(LocalDateTime.now())
                .build();

        // Mock: Hub Service 경로 계산 성공
        given(hubClient.calculateRoute(any())).willReturn(
                HubRouteCalculationResponse.builder()
                        .orderId(order.getIdValue())
                        .originHubId("HUB-001")
                        .destinationHubId("HUB-002")
                        .routeHubs(List.of("HUB-001", "HUB-002"))
                        .requiresHubDelivery(true)
                        .estimatedDistance(50.0)
                        .routeInfoJson("{\"hubs\":[\"HUB-001\",\"HUB-002\"]}")
                        .build()
        );

        // Mock: AI Service 시간 계산 성공
        given(aiClient.calculateDeliveryTime(any())).willReturn(
                AiTimeCalculationResponse.builder()
                        .orderId(order.getIdValue())
                        .calculatedDepartureDeadline(LocalDateTime.now().plusHours(2))
                        .estimatedDeliveryTime(LocalDateTime.now().plusDays(1))
                        .aiMessage("계산 완료")
                        .success(true)
                        .build()
        );

        // Mock: 허브 배송 생성 성공 (Step 4도 자동 실행됨)
        given(hubDeliveryClient.createDelivery(any())).willReturn(
                HubDeliveryCreateResponse.builder()
                        .hubDeliveryId("HUB-DELIVERY-001")
                        .orderId(order.getIdValue())
                        .status("CREATED")
                        .message("생성 완료")
                        .build()
        );

        // Mock: 업체 배송 생성 성공 (Step 5도 자동 실행됨)
        given(lastMileClient.createDelivery(any())).willReturn(
                LastMileDeliveryCreateResponse.builder()
                        .lastMileDeliveryId("LAST-MILE-001")
                        .orderId(order.getIdValue())
                        .assignedDriverId("DRIVER-001")
                        .assignedDriverName("김기사")
                        .status("ASSIGNED")
                        .message("생성 완료")
                        .build()
        );

        // when
        orchestratorService.executeRouteCalculation(event);

        // then
        verify(hubClient, times(1)).calculateRoute(any());
        verify(aiClient, times(1)).calculateDeliveryTime(any());

        // DB에서 Order 상태 확인
        Order updatedOrder = orderRepository.findById(order.getId()).orElseThrow();
        assertThat(updatedOrder.getDestinationHubId()).isEqualTo("HUB-002");
        assertThat(updatedOrder.getAiCalculationResult()).isNotNull();
    }

    @Test
    @org.junit.jupiter.api.Order(5)
    @DisplayName("경로 계산 실패 - Hub 응답 없음 시 보상 트랜잭션")
    void executeRouteCalculation_Fail_NoHubResponse() {
        // given
        Order order = createAndSaveUniqueOrder();

        // Step 1, 2 먼저 실행하여 Saga 생성
        given(inventoryClient.reserveStock(any())).willReturn(
                InventoryReservationResponse.builder()
                        .reservationId("RES-001")
                        .orderId(order.getIdValue())
                        .allSuccess(true)
                        .reservedItems(List.of(
                                InventoryReservationResponse.ReservedItem.builder()
                                        .productId("PROD-001")
                                        .hubId("HUB-001")
                                        .quantity(10)
                                        .success(true)
                                        .build()
                        ))
                        .build()
        );

        given(paymentClient.verifyAndRegisterPayment(any())).willReturn(
                PaymentVerificationResponse.builder()
                        .paymentId("PAYMENT-001")
                        .status("VERIFIED")
                        .verifiedAmount(BigDecimal.valueOf(500000))
                        .message("검증 완료")
                        .build()
        );

        orchestratorService.startOrderSaga(order);

        // Order와 Saga 다시 조회
        order = orderRepository.findById(order.getId()).orElseThrow();
        OrderSaga saga = sagaRepository.findByOrderId(order.getId()).orElseThrow();

        OrderPaymentVerifiedEvent event = OrderPaymentVerifiedEvent.builder()
                .orderId(order.getIdValue())
                .sagaId(saga.getSagaIdValue())
                .productHubId("HUB-001")
                .deliveryAddress("서울시 강남구")
                .deliveryAddressDetail("테헤란로 123")
                .publishedAt(LocalDateTime.now())
                .build();

        // Mock: Hub Service 실패 (null 반환)
        given(hubClient.calculateRoute(any())).willReturn(null);

        // Mock: 보상 API들
        given(inventoryClient.restoreStock(any())).willReturn(
                InventoryRestoreResponse.builder()
                        .success(true)
                        .message("재고 복원 완료")
                        .build()
        );

        // when & then
        assertThatThrownBy(() -> orchestratorService.executeRouteCalculation(event))
                .isInstanceOf(Exception.class);

        verify(hubClient, times(1)).calculateRoute(any());
        verify(aiClient, never()).calculateDeliveryTime(any());

        // 보상 트랜잭션 확인
        OrderSaga updatedSaga = sagaRepository.findByOrderId(order.getId()).orElseThrow();
        assertThat(updatedSaga.getStatus()).isIn(
                SagaStatus.COMPENSATING,
                SagaStatus.COMPENSATED,
                SagaStatus.COMPENSATION_FAILED
        );
    }

    // ==================== Helper Methods ====================

    /**
     * 테스트마다 고유한 Order를 생성하고 DB에 저장합니다.
     * 초기 상태: PENDING
     */
    private Order createAndSaveUniqueOrder() {
        // OrderNumber 형식: ORD-YYYYMMDD-XXX
        String dateStr = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String randomSuffix = String.format("%03d", (int) (Math.random() * 1000));
        String uniqueOrderNumber = String.format("ORD-%s-%s", dateStr, randomSuffix);

        // PG 결제 키는 고유하게
        String uniquePgKey = "PG-PAY-" + System.nanoTime();

        Order order = Order.create(
                OrderNumber.from(uniqueOrderNumber),
                CompanyInfo.of("COMP-001", "HUB-001", "COMP-002", "HUB-002"),
                ProductInfo.of("PROD-001", 10),
                ReceiverInfo.of(
                        "홍길동",
                        "010-1234-5678",
                        "test@example.com",
                        "서울시 강남구",
                        "테헤란로 123",
                        "06234",
                        "문 앞에 놔주세요"
                ),
                RequestInfo.of(
                        LocalDate.now().plusDays(1),
                        LocalTime.of(14, 0),
                        "조심히 배송 부탁드립니다"
                ),
                BigDecimal.valueOf(50000),
                PgPaymentInfo.of("TOSS", uniquePgKey),
                "USER-001"
        );

        // 실제 DB에 저장하여 ID 생성
        return orderRepository.save(order);
    }
}
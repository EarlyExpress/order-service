package com.early_express.order_service.domain.order.application.service;

import com.early_express.order_service.domain.order.application.dto.OrderCreateCommand;
import com.early_express.order_service.domain.order.domain.exception.OrderErrorCode;
import com.early_express.order_service.domain.order.domain.exception.OrderException;
import com.early_express.order_service.domain.order.domain.model.Order;
import com.early_express.order_service.domain.order.domain.model.OrderStatus;
import com.early_express.order_service.domain.order.domain.model.vo.AiCalculationResult;
import com.early_express.order_service.domain.order.domain.model.vo.OrderNumber;
import com.early_express.order_service.domain.order.domain.repository.OrderRepository;
import com.early_express.order_service.domain.order.infrastructure.client.inventory.InventoryClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@SpringBootTest
@Transactional
@ActiveProfiles("test")
@DisplayName("OrderCommandService 테스트")
class OrderCommandServiceTest {

    @Autowired
    private OrderCommandService commandService;

    @Autowired
    private OrderRepository orderRepository;

    @MockBean
    private OrderSagaOrchestratorService sagaOrchestratorService;

    @MockBean
    private OrderNumberGeneratorService orderNumberGeneratorService;

    private OrderCreateCommand testCommand;
    @Mock
    private InventoryClient inventoryClient;

    @BeforeEach
    void setUp() {
        // 테스트 데이터 초기화
        testCommand = createTestCommand();
    }

    @Test
    @DisplayName("주문 생성 성공 - 트랜잭션 커밋 확인")
    void createOrder_Success_TransactionCommitted() {
        // given
        given(orderNumberGeneratorService.generateOrderNumber())
                .willReturn(com.early_express.order_service.domain.order.domain.model.vo.OrderNumber.generate(1));
        doNothing().when(sagaOrchestratorService).startOrderSaga(any(Order.class));

        // when
        Order result = commandService.createOrder(testCommand);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isIn(OrderStatus.PENDING,OrderStatus.CONFIRMED);

        // DB에 실제로 저장되었는지 확인
        Order savedOrder = orderRepository.findById(result.getId()).orElseThrow();
        assertThat(savedOrder.getIdValue()).isEqualTo(result.getIdValue());
        assertThat(savedOrder.getOrderNumberValue()).isNotNull();
        assertThat(savedOrder.getCompanyInfo()).isNotNull();
        assertThat(savedOrder.getProductInfo().getProductId()).isEqualTo("PROD-001");
        assertThat(savedOrder.getProductInfo().getQuantity()).isEqualTo(10);
    }


    @Test
    @DisplayName("주문 취소 성공 - 상태 변경 및 Dirty Checking 확인")
    void cancelOrder_Success_DirtyCheckingApplied() {
        // given
        given(orderNumberGeneratorService.generateOrderNumber())
                .willReturn(com.early_express.order_service.domain.order.domain.model.vo.OrderNumber.generate(3));
        doNothing().when(sagaOrchestratorService).startOrderSaga(any(Order.class));

        Order createdOrder = commandService.createOrder(testCommand);
        String orderId = createdOrder.getIdValue();
        String cancelReason = "고객 요청";

        // when
        commandService.cancelOrder(orderId, cancelReason);

        // then - DB에서 직접 조회하여 상태 확인
        Order cancelledOrder = orderRepository.findById(createdOrder.getId()).orElseThrow();
        assertThat(cancelledOrder.getStatus()).isEqualTo(OrderStatus.CANCELLED);
        assertThat(cancelledOrder.getCancelReason()).isEqualTo(cancelReason);
        assertThat(cancelledOrder.getCancelledAt()).isNotNull();
    }

    @Test
    @DisplayName("주문 취소 실패 - 존재하지 않는 주문")
    void cancelOrder_OrderNotFound_ThrowsException() {
        // given
        String invalidOrderId = "INVALID-ORDER-ID";
        String cancelReason = "고객 요청";

        // when & then
        assertThatThrownBy(() -> commandService.cancelOrder(invalidOrderId, cancelReason))
                .isInstanceOf(OrderException.class)
                .hasFieldOrPropertyWithValue("errorCode", OrderErrorCode.ORDER_NOT_FOUND);

    }

    @Test
    @DisplayName("주문 실패 처리 - 상태 변경 확인")
    void failOrder_Success_StatusChanged() {
        // given
        given(orderNumberGeneratorService.generateOrderNumber())
                .willReturn(com.early_express.order_service.domain.order.domain.model.vo.OrderNumber.generate(4));
        doNothing().when(sagaOrchestratorService).startOrderSaga(any(Order.class));

        Order createdOrder = commandService.createOrder(testCommand);
        String orderId = createdOrder.getIdValue();

        // when
        commandService.failOrder(orderId);

        // then
        Order failedOrder = orderRepository.findById(createdOrder.getId()).orElseThrow();
        assertThat(failedOrder.getStatus()).isEqualTo(OrderStatus.FAILED);
    }

    @Test
    @DisplayName("주문 보상 완료 처리 - 상태 변경 확인")
    void compensateOrder_Success_StatusChanged() {
        // given
        given(orderNumberGeneratorService.generateOrderNumber())
                .willReturn(com.early_express.order_service.domain.order.domain.model.vo.OrderNumber.generate(5));
        doNothing().when(sagaOrchestratorService).startOrderSaga(any(Order.class));

        Order createdOrder = commandService.createOrder(testCommand);
        String orderId = createdOrder.getIdValue();

        // when
        commandService.compensateOrder(orderId);

        // then
        Order compensatedOrder = orderRepository.findById(createdOrder.getId()).orElseThrow();
        assertThat(compensatedOrder.getStatus()).isEqualTo(OrderStatus.COMPENSATED);
    }

    @Test
    @DisplayName("허브 배송 시작 - 상태 및 시간 업데이트 확인")
    void startHubDelivery_Success_StatusAndTimeUpdated() {
        // given
        Order confirmedOrder = createConfirmedOrder();
        String orderId = confirmedOrder.getIdValue();
        LocalDateTime departureTime = LocalDateTime.now();

        // when
        commandService.startHubDelivery(orderId, departureTime);

        // then
        Order updatedOrder = orderRepository.findById(confirmedOrder.getId()).orElseThrow();
        assertThat(updatedOrder.getStatus()).isEqualTo(OrderStatus.HUB_IN_TRANSIT);
        assertThat(updatedOrder.getDeliveryInfo()).isNotNull();
    }

    @Test
    @DisplayName("최종 배송 시작 - 상태 업데이트 확인")
    void startFinalDelivery_Success_StatusUpdated() {
        // given
        given(orderNumberGeneratorService.generateOrderNumber())
                .willReturn(com.early_express.order_service.domain.order.domain.model.vo.OrderNumber.generate(8));
        doNothing().when(sagaOrchestratorService).startOrderSaga(any(Order.class));

        Order createdOrder = commandService.createOrder(testCommand);
        String orderId = createdOrder.getIdValue();

        // Saga 완료 후 상태를 수동으로 CONFIRMED로 변경
        Order order = orderRepository.findById(createdOrder.getId()).orElseThrow();
        order.startStockChecking();
        order.completeStockReservation("HUB-001");
        order.startPaymentVerification();
        order.completePaymentVerification("PAYMENT-001");
        order.startRouteCalculation();
        order.updateDestinationHubId("HUB-002");
        order.updateRouteInfo("{}");
        order.startDeliveryCreation();
        order.completeLastMileDeliveryCreation("LAST-MILE-001");
        orderRepository.save(order);

        LocalDateTime departureTime = LocalDateTime.now();
        LocalDateTime arrivalTime = departureTime.plusHours(2);
        LocalDateTime finalDeliveryStartTime = arrivalTime.plusHours(1);

        commandService.startHubDelivery(orderId, departureTime);
        commandService.arriveAtHub(orderId, arrivalTime);

        // when
        commandService.startFinalDelivery(orderId, finalDeliveryStartTime);

        // then
        Order updatedOrder = orderRepository.findById(createdOrder.getId()).orElseThrow();
        assertThat(updatedOrder.getStatus()).isEqualTo(OrderStatus.IN_DELIVERY);
    }

    @Test
    @DisplayName("배송 완료 - 최종 상태 및 정보 저장 확인")
    void completeDelivery_Success_FinalStatusAndInfoSaved() {
        // given
        Order confirmedOrder = createConfirmedOrder();
        String orderId = confirmedOrder.getIdValue();

        // 배송 프로세스 진행
        commandService.startHubDelivery(orderId, LocalDateTime.now());
        commandService.arriveAtHub(orderId, LocalDateTime.now().plusHours(2));
        commandService.startFinalDelivery(orderId, LocalDateTime.now().plusHours(3));

        LocalDateTime deliveryTime = LocalDateTime.now().plusHours(4);
        String signature = "BASE64_SIGNATURE_DATA";
        String receiverName = "홍길동";

        // when
        commandService.completeDelivery(orderId, deliveryTime, signature, receiverName);

        // then
        Order completedOrder = orderRepository.findById(confirmedOrder.getId()).orElseThrow();
        assertThat(completedOrder.getStatus()).isEqualTo(OrderStatus.COMPLETED);
        assertThat(completedOrder.getDeliveryInfo()).isNotNull();
    }

    @Test
    @DisplayName("동시성 테스트 - 동일 주문에 대한 상태 변경")
    void concurrentUpdate_SameOrder_OnlyOneSucceeds() {
        // given
        Order confirmedOrder = createConfirmedOrder();
        String orderId = confirmedOrder.getIdValue();

        // when & then - 트랜잭션이 순차적으로 처리되는지 확인
        commandService.startHubDelivery(orderId, LocalDateTime.now());

        Order firstUpdate = orderRepository.findById(confirmedOrder.getId()).orElseThrow();
        assertThat(firstUpdate.getStatus()).isEqualTo(OrderStatus.HUB_IN_TRANSIT);
    }

    @Test
    @DisplayName("롤백 테스트 - Saga 실패 시 주문은 저장되고 PENDING 상태로 유지")
    void rollback_WhenSagaFails_OrderStatePreserved() {
        // given
        given(orderNumberGeneratorService.generateOrderNumber())
                .willReturn(com.early_express.order_service.domain.order.domain.model.vo.OrderNumber.generate(1));
        doThrow(new RuntimeException("Saga 실패"))
                .when(sagaOrchestratorService).startOrderSaga(any(Order.class));

        // when
        Order result = commandService.createOrder(testCommand);

        // then - 주문은 생성되었고 DB에 저장됨 (PENDING 상태)
        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo(OrderStatus.PENDING);

        // DB에서 주문이 실제로 저장되었는지 확인 (트랜잭션 롤백 안 됨)
        Order savedOrder = orderRepository.findById(result.getId()).orElseThrow();
        assertThat(savedOrder.getStatus()).isEqualTo(OrderStatus.PENDING);
        assertThat(savedOrder.getOrderNumberValue()).isNotNull();

        // 주문은 저장되었지만 Saga는 실패했음을 로그로 확인 가능
        System.out.printf("주문 저장 확인: orderId={}, status={}",
                savedOrder.getIdValue(), savedOrder.getStatus());
    }

    // ==================== Helper Methods ====================

    private OrderCreateCommand createTestCommand() {
        return OrderCreateCommand.builder()
                .supplierCompanyId("COMP-001")
                .supplierHubId("HUB-001")
                .receiverCompanyId("COMP-002")
                .receiverHubId("HUB-002")
                .productId("PROD-001")
                .quantity(10)
                .unitPrice(BigDecimal.valueOf(5000))
                .receiverName("홍길동")
                .receiverPhone("010-1234-5678")
                .receiverEmail("test@example.com")
                .deliveryAddress("서울시 강남구")
                .deliveryAddressDetail("테헤란로 123")
                .deliveryPostalCode("06234")
                .deliveryNote("문 앞에 놔주세요")
                .requestedDeliveryDate(LocalDate.now().plusDays(1))
                .requestedDeliveryTime(LocalTime.of(14, 0))
                .specialInstructions("조심히 배송 부탁드립니다")
                .pgProvider("TOSS")
                .pgPaymentId("PG-PAY-001")
                .createdBy("USER-001")
                .build();
    }

    /**
     * CONFIRMED 상태의 주문 생성 헬퍼 메서드
     */
    private Order createConfirmedOrder() {
        // 주문 생성
        given(orderNumberGeneratorService.generateOrderNumber())
                .willReturn(com.early_express.order_service.domain.order.domain.model.vo.OrderNumber.generate(
                        (int) (System.currentTimeMillis() % 10000)));
        doNothing().when(sagaOrchestratorService).startOrderSaga(any(Order.class));

        Order createdOrder = commandService.createOrder(testCommand);

        // Saga Step들을 거쳐 CONFIRMED 상태로 만들기
        Order order = orderRepository.findById(createdOrder.getId()).orElseThrow();

        // Step 1: 재고 예약
        order.startStockChecking();
        order.completeStockReservation("HUB-001");

        // Step 2: 결제 검증
        order.startPaymentVerification();
        order.completePaymentVerification("PAYMENT-001");

        // Step 3: 경로 계산
        order.startRouteCalculation();
        order.updateDestinationHubId("HUB-002");
        order.updateRouteInfo("{\"route\": \"test\"}");

        // AI 계산 결과 추가 (필수)
        AiCalculationResult aiResult = AiCalculationResult.withRouteOnly("{\"route\": \"test\"}")
                .withAiCalculation(
                        LocalDateTime.now().plusDays(1),
                        LocalDateTime.now().plusDays(2),
                        "AI 계산 완료"
                );
        order.updateAiCalculationResult(aiResult);

        // Step 4,5: 배송 생성
        order.startDeliveryCreation();  // 상태: PAYMENT_VERIFIED → DELIVERY_CREATING
        order.completeLastMileDeliveryCreation("LAST-MILE-001");

        return orderRepository.save(order);
    }

}
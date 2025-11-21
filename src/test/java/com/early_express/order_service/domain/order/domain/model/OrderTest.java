package com.early_express.order_service.domain.order.domain.model;

import com.early_express.order_service.domain.order.domain.exception.OrderException;
import com.early_express.order_service.domain.order.domain.model.vo.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Order Domain 테스트")
class OrderTest {

    private OrderNumber orderNumber;
    private CompanyInfo companyInfo;
    private ProductInfo productInfo;
    private ReceiverInfo receiverInfo;
    private RequestInfo requestInfo;
    private BigDecimal unitPrice;
    private PgPaymentInfo pgPaymentInfo;
    private String createdBy;

    @BeforeEach
    void setUp() {
        orderNumber = OrderNumber.generate(1);
        companyInfo = CompanyInfo.of("COMP-001", "HUB-001", "COMP-002", "HUB-002");
        productInfo = ProductInfo.of("PROD-001", 5);
        receiverInfo = ReceiverInfo.of(
                "홍길동",
                "010-1234-5678",
                "hong@example.com",
                "서울시 강남구",
                "101호",
                "12345",
                "문 앞에 놓아주세요"
        );
        requestInfo = RequestInfo.of(
                LocalDate.now().plusDays(1),
                LocalTime.of(14, 0),
                "조심히 배송해주세요"
        );
        unitPrice = BigDecimal.valueOf(10000);
        pgPaymentInfo = PgPaymentInfo.of("TOSS", "pay_123456");
        createdBy = "user-001";
    }

    @Test
    @DisplayName("새로운 주문을 생성할 수 있다")
    void createOrder() {
        // when
        Order order = createTestOrder();

        // then
        assertThat(order).isNotNull();
        assertThat(order.getId()).isNotNull();
        assertThat(order.getStatus()).isEqualTo(OrderStatus.PENDING);
        assertThat(order.getOrderNumber()).isEqualTo(orderNumber);
        assertThat(order.getCreatedBy()).isEqualTo(createdBy);
        assertThat(order.getCreatedAt()).isNotNull();
    }

    @Test
    @DisplayName("주문 생성 시 금액 정보가 자동 계산된다")
    void createOrderCalculatesAmount() {
        // when
        Order order = createTestOrder();

        // then
        assertThat(order.getAmountInfo().getUnitPrice()).isEqualByComparingTo(unitPrice);
        assertThat(order.getAmountInfo().getTotalAmount())
                .isEqualByComparingTo(BigDecimal.valueOf(50000)); // 10000 * 5
    }

    @Test
    @DisplayName("주문 생성 시 초기 상태가 설정된다")
    void createOrderInitializesState() {
        // when
        Order order = createTestOrder();

        // then
        assertThat(order.getDeliveryInfo()).isNotNull();
        assertThat(order.getAiCalculationResult()).isNotNull();
        assertThat(order.getDeliveryProgressInfo()).isNotNull();
    }

    // ===== Saga Step 테스트 =====

    @Test
    @DisplayName("재고 확인을 시작할 수 있다")
    void startStockChecking() {
        // given
        Order order = createTestOrder();

        // when
        order.startStockChecking();

        // then
        assertThat(order.getStatus()).isEqualTo(OrderStatus.STOCK_CHECKING);
    }

    @Test
    @DisplayName("재고 예약을 완료할 수 있다")
    void completeStockReservation() {
        // given
        Order order = createTestOrder();
        order.startStockChecking();
        String productHubId = "HUB-001";

        // when
        order.completeStockReservation(productHubId);

        // then
        assertThat(order.getStatus()).isEqualTo(OrderStatus.STOCK_RESERVED);
        assertThat(order.getProductInfo().getProductHubId()).isEqualTo(productHubId);
    }

    @Test
    @DisplayName("재고 예약 시 허브 배송 필요 여부가 결정된다 - 다른 허브")
    void completeStockReservationDifferentHub() {
        // given
        Order order = createTestOrder();
        order.startStockChecking();
        String productHubId = "HUB-003"; // 수령 허브와 다름

        // when
        order.completeStockReservation(productHubId);

        // then
        assertThat(order.requiresHubDelivery()).isTrue();
    }

    @Test
    @DisplayName("재고 예약 시 허브 배송 필요 여부가 결정된다 - 같은 허브")
    void completeStockReservationSameHub() {
        // given
        Order order = createTestOrder();
        order.startStockChecking();
        String productHubId = "HUB-002"; // 수령 허브와 같음

        // when
        order.completeStockReservation(productHubId);

        // then
        assertThat(order.requiresHubDelivery()).isFalse();
    }

    @Test
    @DisplayName("결제 검증을 시작할 수 있다")
    void startPaymentVerification() {
        // given
        Order order = createTestOrder();
        order.startStockChecking();
        order.completeStockReservation("HUB-001");

        // when
        order.startPaymentVerification();

        // then
        assertThat(order.getStatus()).isEqualTo(OrderStatus.PAYMENT_VERIFYING);
    }

    @Test
    @DisplayName("결제 검증을 완료할 수 있다")
    void completePaymentVerification() {
        // given
        Order order = createTestOrder();
        order.startStockChecking();
        order.completeStockReservation("HUB-001");
        order.startPaymentVerification();
        String paymentId = "payment-001";

        // when
        order.completePaymentVerification(paymentId);

        // then
        assertThat(order.getStatus()).isEqualTo(OrderStatus.PAYMENT_VERIFIED);
        assertThat(order.getAmountInfo().getPaymentId()).isEqualTo(paymentId);
    }

    @Test
    @DisplayName("경로 계산을 시작할 수 있다")
    void startRouteCalculation() {
        // given
        Order order = createTestOrder();
        prepareOrderForRouteCalculation(order);

        // when
        order.startRouteCalculation();

        // then
        assertThat(order.getStatus()).isEqualTo(OrderStatus.ROUTE_CALCULATING);
    }

    @Test
    @DisplayName("경로 정보를 저장할 수 있다")
    void saveRouteInfo() {
        // given
        Order order = createTestOrder();
        prepareOrderForRouteCalculation(order);
        order.startRouteCalculation();
        String routeInfoJson = "{\"hubs\":[\"HUB-001\",\"HUB-002\"]}";

        // when
        order.saveRouteInfo(routeInfoJson);

        // then
        assertThat(order.getAiCalculationResult()).isNotNull();
        assertThat(order.getAiCalculationResult().hasRouteInfo()).isTrue();
        assertThat(order.getAiCalculationResult().getRouteInfo()).isEqualTo(routeInfoJson);
    }

    @Test
    @DisplayName("AI 시간 계산을 완료할 수 있다")
    void completeAiCalculation() {
        // given
        Order order = createTestOrder();
        prepareOrderForRouteCalculation(order);
        order.startRouteCalculation();
        order.saveRouteInfo("{\"hubs\":[\"HUB-001\",\"HUB-002\"]}");
        LocalDateTime departureDeadline = LocalDateTime.now().plusHours(2);
        LocalDateTime estimatedDeliveryTime = LocalDateTime.now().plusHours(24);

        // when
        order.completeAiCalculation(departureDeadline, estimatedDeliveryTime, "계산 완료");

        // then
        assertThat(order.getAiCalculationResult()).isNotNull();
        assertThat(order.getAiCalculationResult().isCalculated()).isTrue();
        assertThat(order.getAiCalculationResult().getCalculatedDepartureDeadline())
                .isEqualTo(departureDeadline);
        assertThat(order.requiresHubDelivery()).isTrue(); // 허브가 2개 이상
    }

    @Test
    @DisplayName("허브 배송 생성을 완료할 수 있다")
    void completeHubDeliveryCreation() {
        // given
        Order order = createTestOrder();
        String hubDeliveryId = "HUB-DELIVERY-001";

        // when
        order.completeHubDeliveryCreation(hubDeliveryId);

        // then
        assertThat(order.getDeliveryInfo().getHubDeliveryId()).isEqualTo(hubDeliveryId);
    }

    @Test
    @DisplayName("업체 배송 생성을 완료하면 주문이 확정된다")
    void completeLastMileDeliveryCreation() {
        // given
        Order order = createTestOrder();
        String lastMileDeliveryId = "LAST-MILE-001";

        // when
        order.completeLastMileDeliveryCreation(lastMileDeliveryId);

        // then
        assertThat(order.getStatus()).isEqualTo(OrderStatus.CONFIRMED);
        assertThat(order.getDeliveryInfo().getLastMileDeliveryId()).isEqualTo(lastMileDeliveryId);
    }

    @Test
    @DisplayName("주문을 확정할 수 있다")
    void confirmOrder() {
        // given
        Order order = createTestOrder();
        prepareOrderForConfirmation(order);

        // when
        order.confirm();

        // then
        assertThat(order.getStatus()).isEqualTo(OrderStatus.CONFIRMED);
    }

    // ===== 배송 진행 테스트 =====

    @Test
    @DisplayName("허브 배송을 시작할 수 있다")
    void startHubDelivery() {
        // given
        Order order = createTestOrder();
        prepareOrderForDelivery(order);
        LocalDateTime departureTime = LocalDateTime.now();

        // when
        order.startHubDelivery(departureTime);

        // then
        assertThat(order.getStatus()).isEqualTo(OrderStatus.HUB_IN_TRANSIT);
        assertThat(order.getDeliveryProgressInfo().getActualDepartureTime())
                .isEqualTo(departureTime);
    }

    @Test
    @DisplayName("허브에 도착할 수 있다")
    void arriveAtHub() {
        // given
        Order order = createTestOrder();
        prepareOrderForDelivery(order);
        order.startHubDelivery(LocalDateTime.now());
        LocalDateTime arrivalTime = LocalDateTime.now().plusHours(2);

        // when
        order.arriveAtHub(arrivalTime);

        // then
        assertThat(order.getStatus()).isEqualTo(OrderStatus.HUB_ARRIVED);
        assertThat(order.getDeliveryProgressInfo().getHubArrivalTime())
                .isEqualTo(arrivalTime);
    }

    @Test
    @DisplayName("업체 배송을 준비할 수 있다")
    void prepareLastMileDelivery() {
        // given
        Order order = createTestOrder();

        // when
        order.prepareLastMileDelivery();

        // then
        assertThat(order.getStatus()).isEqualTo(OrderStatus.LAST_MILE_READY);
    }

    @Test
    @DisplayName("최종 배송을 시작할 수 있다")
    void startFinalDelivery() {
        // given
        Order order = createTestOrder();
        LocalDateTime startTime = LocalDateTime.now();

        // when
        order.startFinalDelivery(startTime);

        // then
        assertThat(order.getStatus()).isEqualTo(OrderStatus.IN_DELIVERY);
        assertThat(order.getDeliveryProgressInfo().getFinalDeliveryStartTime())
                .isEqualTo(startTime);
    }

    @Test
    @DisplayName("배송을 완료할 수 있다")
    void completeDelivery() {
        // given
        Order order = createTestOrder();
        order.startFinalDelivery(LocalDateTime.now());
        LocalDateTime completionTime = LocalDateTime.now().plusHours(1);
        String signature = "signature-data";
        String actualReceiverName = "홍길동";

        // when
        order.completeDelivery(completionTime, signature, actualReceiverName);

        // then
        assertThat(order.getStatus()).isEqualTo(OrderStatus.COMPLETED);
        assertThat(order.isCompleted()).isTrue();
        assertThat(order.getDeliveryProgressInfo().getActualDeliveryTime())
                .isEqualTo(completionTime);
    }

    // ===== 취소 및 실패 테스트 =====

    @Test
    @DisplayName("취소 가능한 상태에서 주문을 취소할 수 있다")
    void cancelOrder() {
        // given
        Order order = createTestOrder();
        String cancelReason = "고객 요청";

        // when
        order.cancel(cancelReason);

        // then
        assertThat(order.getStatus()).isEqualTo(OrderStatus.CANCELLED);
        assertThat(order.getCancelReason()).isEqualTo(cancelReason);
        assertThat(order.getCancelledAt()).isNotNull();
    }

    @Test
    @DisplayName("취소 불가능한 상태에서 주문을 취소하면 예외가 발생한다")
    void cannotCancelInDelivery() {
        // given
        Order order = createTestOrder();
        order.startFinalDelivery(LocalDateTime.now());

        // when & then
        assertThatThrownBy(() -> order.cancel("고객 요청"))
                .isInstanceOf(OrderException.class)
                .hasMessageContaining("취소할 수 없습니다");
    }

    @Test
    @DisplayName("주문을 실패 처리할 수 있다")
    void failOrder() {
        // given
        Order order = createTestOrder();

        // when
        order.fail();

        // then
        assertThat(order.getStatus()).isEqualTo(OrderStatus.FAILED);
    }

    @Test
    @DisplayName("주문을 보상 완료 처리할 수 있다")
    void compensateOrder() {
        // given
        Order order = createTestOrder();

        // when
        order.compensate();

        // then
        assertThat(order.getStatus()).isEqualTo(OrderStatus.COMPENSATED);
    }

    // ===== 검증 테스트 =====

    @Test
    @DisplayName("결제 금액을 검증할 수 있다")
    void validatePaymentAmount() {
        // given
        Order order = createTestOrder();
        BigDecimal correctAmount = BigDecimal.valueOf(50000); // 10000 * 5

        // when & then
        assertThatCode(() -> order.validatePaymentAmount(correctAmount))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("결제 금액이 일치하지 않으면 예외가 발생한다")
    void validatePaymentAmountMismatch() {
        // given
        Order order = createTestOrder();
        BigDecimal wrongAmount = BigDecimal.valueOf(30000);

        // when & then
        assertThatThrownBy(() -> order.validatePaymentAmount(wrongAmount))
                .isInstanceOf(OrderException.class)
                .hasMessageContaining("결제 금액이 일치하지 않습니다");
    }

    @Test
    @DisplayName("잘못된 상태에서 작업하면 예외가 발생한다")
    void validateStatusThrowsException() {
        // given
        Order order = createTestOrder();

        // when & then (PENDING 상태에서 결제 검증 시작 불가)
        assertThatThrownBy(() -> order.startPaymentVerification())
                .isInstanceOf(OrderException.class)
                .hasMessageContaining("상태에서만 가능합니다");
    }

    // ===== 조회 메서드 테스트 =====

    @Test
    @DisplayName("취소 가능 여부를 확인할 수 있다")
    void checkCancellable() {
        // given
        Order order = createTestOrder();

        // when & then
        assertThat(order.isCancellable()).isTrue();

        order.startFinalDelivery(LocalDateTime.now());
        assertThat(order.isCancellable()).isFalse();
    }

    @Test
    @DisplayName("주문 ID 문자열을 조회할 수 있다")
    void getIdValue() {
        // given
        Order order = createTestOrder();

        // when
        String idValue = order.getIdValue();

        // then
        assertThat(idValue).isNotNull();
        assertThat(idValue).isNotEmpty();
    }

    @Test
    @DisplayName("주문 번호 문자열을 조회할 수 있다")
    void getOrderNumberValue() {
        // given
        Order order = createTestOrder();

        // when
        String orderNumberValue = order.getOrderNumberValue();

        // then
        assertThat(orderNumberValue).isNotEmpty();
        assertThat(orderNumberValue).isEqualTo(order.getOrderNumber().getValue());
    }

    // ===== Helper Methods =====

    /**
     * 테스트용 Order 생성 (Builder 사용)
     *
     * Order.create()는 id를 null로 설정하므로 테스트에서 사용 불가
     * (실제로는 Entity가 DB에 저장될 때 ID가 생성됨)
     * 따라서 테스트에서는 Builder를 직접 사용하여 ID를 명시적으로 설정
     */
    private Order createTestOrder() {
        return Order.builder()
                .id(OrderId.create()) // 테스트용 ID 생성
                .orderNumber(orderNumber)
                .companyInfo(companyInfo)
                .productInfo(productInfo)
                .deliveryInfo(DeliveryInfo.initial())
                .receiverInfo(receiverInfo)
                .requestInfo(requestInfo)
                .aiCalculationResult(AiCalculationResult.empty())
                .status(OrderStatus.PENDING)
                .amountInfo(AmountInfo.of(unitPrice, productInfo.getQuantity()))
                .pgPaymentInfo(pgPaymentInfo)
                .deliveryProgressInfo(DeliveryProgressInfo.empty())
                .createdBy(createdBy)
                .createdAt(LocalDateTime.now())
                .build();
    }

    private void prepareOrderForRouteCalculation(Order order) {
        order.startStockChecking();
        order.completeStockReservation("HUB-001");
        order.startPaymentVerification();
        order.completePaymentVerification("payment-001");
    }

    private void prepareOrderForConfirmation(Order order) {
        prepareOrderForRouteCalculation(order);
        // PAYMENT_VERIFIED 상태에서 confirm 가능
    }

    private void prepareOrderForDelivery(Order order) {
        prepareOrderForConfirmation(order);
        order.confirm();
    }
}
package com.early_express.order_service.domain.order.application.service;

import com.early_express.order_service.domain.order.domain.exception.OrderErrorCode;
import com.early_express.order_service.domain.order.domain.exception.OrderException;
import com.early_express.order_service.domain.order.domain.model.Order;
import com.early_express.order_service.domain.order.domain.model.OrderStatus;
import com.early_express.order_service.domain.order.domain.model.vo.*;
import com.early_express.order_service.domain.order.domain.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("OrderQueryService 테스트")
class OrderQueryServiceTest {

    @InjectMocks
    private OrderQueryService queryService;

    @Mock
    private OrderRepository orderRepository;

    private Order testOrder;

    @BeforeEach
    void setUp() {
        testOrder = createTestOrder();
    }

    @Test
    @DisplayName("주문 ID로 조회 성공")
    void getOrderById_Success() {
        // given
        String orderId = "ORDER-001";
        given(orderRepository.findById(any(OrderId.class))).willReturn(Optional.of(testOrder));

        // when
        Order result = queryService.getOrderById(orderId);

        // then
        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(testOrder);
        verify(orderRepository, times(1)).findById(any(OrderId.class));
    }

    @Test
    @DisplayName("주문 ID로 조회 실패 - 주문 없음")
    void getOrderById_OrderNotFound() {
        // given
        String orderId = "INVALID-ORDER";
        given(orderRepository.findById(any(OrderId.class))).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> queryService.getOrderById(orderId))
                .isInstanceOf(OrderException.class)
                .hasFieldOrPropertyWithValue("errorCode", OrderErrorCode.ORDER_NOT_FOUND)
                .hasMessageContaining("주문을 찾을 수 없습니다");
    }

    @Test
    @DisplayName("주문 번호로 조회 성공")
    void getOrderByOrderNumber_Success() {
        // given
        String orderNumber = "ORD-20250121-001";
        given(orderRepository.findByOrderNumber(any(OrderNumber.class)))
                .willReturn(Optional.of(testOrder));

        // when
        Order result = queryService.getOrderByOrderNumber(orderNumber);

        // then
        assertThat(result).isNotNull();
        verify(orderRepository, times(1)).findByOrderNumber(any(OrderNumber.class));
    }

    @Test
    @DisplayName("업체별 주문 목록 조회")
    void getOrdersByCompanyId_Success() {
        // given
        String companyId = "COMP-001";
        List<Order> orders = List.of(testOrder);
        given(orderRepository.findByCompanyId(companyId)).willReturn(orders);

        // when
        List<Order> result = queryService.getOrdersByCompanyId(companyId);

        // then
        assertThat(result).hasSize(1);
        assertThat(result).contains(testOrder);
        verify(orderRepository, times(1)).findByCompanyId(companyId);
    }

    @Test
    @DisplayName("상태별 주문 목록 조회")
    void getOrdersByStatus_Success() {
        // given
        OrderStatus status = OrderStatus.CONFIRMED;
        List<Order> orders = List.of(testOrder);
        given(orderRepository.findByStatus(status)).willReturn(orders);

        // when
        List<Order> result = queryService.getOrdersByStatus(status);

        // then
        assertThat(result).hasSize(1);
        verify(orderRepository, times(1)).findByStatus(status);
    }

    @Test
    @DisplayName("배송 ID로 주문 조회 성공")
    void getOrderByLastMileDeliveryId_Success() {
        // given
        String deliveryId = "DELIVERY-001";
        given(orderRepository.findByLastMileDeliveryId(deliveryId))
                .willReturn(Optional.of(testOrder));

        // when
        Order result = queryService.getOrderByLastMileDeliveryId(deliveryId);

        // then
        assertThat(result).isNotNull();
        verify(orderRepository, times(1)).findByLastMileDeliveryId(deliveryId);
    }

    @Test
    @DisplayName("결제 ID로 주문 조회 성공")
    void getOrderByPaymentId_Success() {
        // given
        String paymentId = "PAYMENT-001";
        given(orderRepository.findByPaymentId(paymentId))
                .willReturn(Optional.of(testOrder));

        // when
        Order result = queryService.getOrderByPaymentId(paymentId);

        // then
        assertThat(result).isNotNull();
        verify(orderRepository, times(1)).findByPaymentId(paymentId);
    }

    @Test
    @DisplayName("발송 시한 임박 주문 조회")
    void getOrdersWithUpcomingDeadline_Success() {
        // given
        int hours = 2;
        List<Order> orders = List.of(testOrder);
        given(orderRepository.findOrdersWithUpcomingDeadline(hours)).willReturn(orders);

        // when
        List<Order> result = queryService.getOrdersWithUpcomingDeadline(hours);

        // then
        assertThat(result).hasSize(1);
        verify(orderRepository, times(1)).findOrdersWithUpcomingDeadline(hours);
    }

    @Test
    @DisplayName("발송 시한 초과 주문 조회")
    void getOverdueOrders_Success() {
        // given
        List<Order> orders = List.of(testOrder);
        given(orderRepository.findOverdueOrders()).willReturn(orders);

        // when
        List<Order> result = queryService.getOverdueOrders();

        // then
        assertThat(result).hasSize(1);
        verify(orderRepository, times(1)).findOverdueOrders();
    }

    @Test
    @DisplayName("주문 존재 여부 확인 - 존재함")
    void existsOrder_True() {
        // given
        String orderId = "ORDER-001";
        given(orderRepository.findById(any(OrderId.class))).willReturn(Optional.of(testOrder));

        // when
        boolean result = queryService.existsOrder(orderId);

        // then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("주문 존재 여부 확인 - 존재하지 않음")
    void existsOrder_False() {
        // given
        String orderId = "INVALID-ORDER";
        given(orderRepository.findById(any(OrderId.class))).willReturn(Optional.empty());

        // when
        boolean result = queryService.existsOrder(orderId);

        // then
        assertThat(result).isFalse();
    }

    // ==================== Helper Methods ====================

    private Order createTestOrder() {
        return Order.create(
                OrderNumber.generate(1),
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
                PgPaymentInfo.of("TOSS", "PG-PAY-001"),
                "USER-001"
        );
    }
}
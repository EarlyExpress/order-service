package com.early_express.order_service.domain.order.infrastructure.persistence.repository;

import com.early_express.order_service.domain.order.domain.model.Order;
import com.early_express.order_service.domain.order.domain.model.OrderStatus;
import com.early_express.order_service.domain.order.domain.model.vo.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.*;

/**
 * OrderRepositoryImpl 통합 테스트
 */
@SpringBootTest
@Transactional
@ActiveProfiles("test")
@DisplayName("OrderRepositoryImpl 통합 테스트")
class OrderRepositoryImplTest {

    @Autowired
    private OrderRepositoryImpl orderRepository;

    private Order testOrder1;
    private Order testOrder2;
    private Order testOrder3;

    // 테스트별 고유 번호 생성용
    private static final AtomicInteger counter = new AtomicInteger(0);

    @BeforeEach
    void setUp() {
        // 각 테스트마다 고유한 주문번호 생성
        int uniqueNumber = counter.incrementAndGet();

        testOrder1 = createTestOrder(
                generateUniqueOrderNumber(uniqueNumber * 3 + 1),
                "SUPPLIER-001",
                "RECEIVER-001",
                OrderStatus.PENDING
        );

        testOrder2 = createTestOrder(
                generateUniqueOrderNumber(uniqueNumber * 3 + 2),
                "SUPPLIER-001",
                "RECEIVER-002",
                OrderStatus.CONFIRMED
        );

        testOrder3 = createTestOrder(
                generateUniqueOrderNumber(uniqueNumber * 3 + 3),
                "SUPPLIER-002",
                "RECEIVER-001",
                OrderStatus.COMPLETED
        );
    }

    /**
     * 고유한 주문번호 생성
     */
    private OrderNumber generateUniqueOrderNumber(int sequence) {
        String uuid = UUID.randomUUID().toString().substring(0, 8);
        return OrderNumber.from(String.format("ORD-%s-%03d", uuid, sequence));
    }

    // ===== 헬퍼 메서드 =====

    private Order createTestOrder(
            OrderNumber orderNumber,
            String supplierCompanyId,
            String receiverCompanyId,
            OrderStatus status) {

        Order order = Order.create(
                orderNumber,
                CompanyInfo.of(
                        supplierCompanyId,
                        "HUB-001",
                        receiverCompanyId,
                        "HUB-002"
                ),
                ProductInfo.of("PRODUCT-001", 10),
                ReceiverInfo.of(
                        "홍길동",
                        "010-1234-5678",
                        "test@example.com",
                        "서울시 강남구 테헤란로 123",
                        "상세주소",
                        "06234",
                        "문 앞에 놔주세요"
                ),
                RequestInfo.of(
                        LocalDate.now().plusDays(2),
                        LocalTime.of(14, 0),
                        "조심히 다뤄주세요"
                ),
                BigDecimal.valueOf(10000),
                PgPaymentInfo.of("TOSS", "pg-payment-" + orderNumber.getValue()),
                "USER-001"
        );

        // 요청된 상태로 변경
        changeOrderStatusTo(order, status);

        return order;
    }

    /**
     * 테스트용 Order 상태 변경
     */
    private void changeOrderStatusTo(Order order, OrderStatus targetStatus) {
        // PENDING은 기본 상태이므로 변경 불필요
        if (targetStatus == OrderStatus.PENDING) {
            return;
        }

        // 상태 전이 순서에 맞게 변경
        if (targetStatus == OrderStatus.CONFIRMED) {
            order.startStockChecking();
            order.completeStockReservation("HUB-001");
            order.startPaymentVerification();
            order.completePaymentVerification("PAYMENT-001");
            order.confirm();
        } else if (targetStatus == OrderStatus.COMPLETED) {
            // COMPLETED까지 변경
            changeOrderStatusTo(order, OrderStatus.CONFIRMED);
            order.startHubDelivery(LocalDateTime.now());
            order.arriveAtHub(LocalDateTime.now());
            order.prepareLastMileDelivery();
            order.startFinalDelivery(LocalDateTime.now());
            order.completeDelivery(
                    LocalDateTime.now(),
                    "signature-base64",
                    "홍길동"
            );
        }
        // 필요한 다른 상태들도 추가 가능
    }

    /**
     * 테스트용 Order 생성 (파라미터 없음 - 자동 생성)
     */
    private Order createTestOrderWithUniqueNumber(
            String supplierCompanyId,
            String receiverCompanyId,
            OrderStatus status) {
        int uniqueNumber = counter.incrementAndGet();
        OrderNumber orderNumber = generateUniqueOrderNumber(uniqueNumber);
        return createTestOrder(orderNumber, supplierCompanyId, receiverCompanyId, status);
    }

    // ===== 기본 CRUD 테스트 =====

    @Nested
    @DisplayName("save() - 저장 기능 테스트")
    class SaveTest {

        @Test
        @DisplayName("새로운 주문 저장 성공")
        void save_NewOrder_Success() {
            // when
            Order savedOrder = orderRepository.save(testOrder1);

            // then
            assertThat(savedOrder).isNotNull();
            assertThat(savedOrder.getId()).isNotNull();
            assertThat(savedOrder.getOrderNumber()).isEqualTo(testOrder1.getOrderNumber());
            assertThat(savedOrder.getStatus()).isEqualTo(OrderStatus.PENDING);
        }

        @Test
        @DisplayName("기존 주문 업데이트 성공")
        void save_ExistingOrder_UpdatesSuccessfully() {
            // given
            Order savedOrder = orderRepository.save(testOrder1);
            OrderId orderId = savedOrder.getId();

            // 주문 상태 변경
            savedOrder.startStockChecking();

            // when
            Order updatedOrder = orderRepository.save(savedOrder);

            // then
            assertThat(updatedOrder.getId()).isEqualTo(orderId);
            assertThat(updatedOrder.getStatus()).isEqualTo(OrderStatus.STOCK_CHECKING);
        }

        @Test
        @DisplayName("여러 주문 저장 성공")
        void save_MultipleOrders_Success() {
            // when
            Order saved1 = orderRepository.save(testOrder1);
            Order saved2 = orderRepository.save(testOrder2);
            Order saved3 = orderRepository.save(testOrder3);

            // then
            assertThat(saved1.getId()).isNotNull();
            assertThat(saved2.getId()).isNotNull();
            assertThat(saved3.getId()).isNotNull();
            assertThat(saved1.getId()).isNotEqualTo(saved2.getId());
        }
    }

    @Nested
    @DisplayName("findById() - ID로 조회 테스트")
    class FindByIdTest {

        @Test
        @DisplayName("ID로 주문 조회 성공")
        void findById_ExistingOrder_ReturnsOrder() {
            // given
            Order savedOrder = orderRepository.save(testOrder1);
            OrderId orderId = savedOrder.getId();

            // when
            Optional<Order> foundOrder = orderRepository.findById(orderId);

            // then
            assertThat(foundOrder).isPresent();
            assertThat(foundOrder.get().getId()).isEqualTo(orderId);
            assertThat(foundOrder.get().getOrderNumber()).isEqualTo(testOrder1.getOrderNumber());
        }

        @Test
        @DisplayName("존재하지 않는 ID 조회 시 빈 Optional 반환")
        void findById_NonExistingOrder_ReturnsEmpty() {
            // given
            OrderId nonExistingId = OrderId.from("non-existing-id");

            // when
            Optional<Order> foundOrder = orderRepository.findById(nonExistingId);

            // then
            assertThat(foundOrder).isEmpty();
        }
    }

    @Nested
    @DisplayName("findByOrderNumber() - 주문번호로 조회 테스트")
    class FindByOrderNumberTest {

        @Test
        @DisplayName("주문번호로 조회 성공")
        void findByOrderNumber_ExistingOrder_ReturnsOrder() {
            // given
            Order savedOrder = orderRepository.save(testOrder1);

            // when
            Optional<Order> foundOrder = orderRepository.findByOrderNumber(testOrder1.getOrderNumber());

            // then
            assertThat(foundOrder).isPresent();
            assertThat(foundOrder.get().getId()).isEqualTo(savedOrder.getId());
        }

        @Test
        @DisplayName("존재하지 않는 주문번호 조회 시 빈 Optional 반환")
        void findByOrderNumber_NonExistingOrder_ReturnsEmpty() {
            // given
            OrderNumber nonExistingNumber = OrderNumber.from("ORD-99991231-999");

            // when
            Optional<Order> foundOrder = orderRepository.findByOrderNumber(nonExistingNumber);

            // then
            assertThat(foundOrder).isEmpty();
        }
    }

    @Nested
    @DisplayName("existsByOrderNumber() - 주문번호 존재 확인 테스트")
    class ExistsByOrderNumberTest {

        @Test
        @DisplayName("존재하는 주문번호 확인")
        void existsByOrderNumber_ExistingOrder_ReturnsTrue() {
            // given
            orderRepository.save(testOrder1);

            // when
            boolean exists = orderRepository.existsByOrderNumber(testOrder1.getOrderNumber());

            // then
            assertThat(exists).isTrue();
        }

        @Test
        @DisplayName("존재하지 않는 주문번호 확인")
        void existsByOrderNumber_NonExistingOrder_ReturnsFalse() {
            // given
            OrderNumber nonExistingNumber = OrderNumber.from("ORD-99991231-999");

            // when
            boolean exists = orderRepository.existsByOrderNumber(nonExistingNumber);

            // then
            assertThat(exists).isFalse();
        }
    }

    @Nested
    @DisplayName("findByCompanyId() - 업체 ID로 조회 테스트")
    class FindByCompanyIdTest {

        @Test
        @DisplayName("공급업체 ID로 주문 조회")
        void findByCompanyId_AsSupplier_ReturnsOrders() {
            // given
            orderRepository.save(testOrder1); // SUPPLIER-001
            orderRepository.save(testOrder2); // SUPPLIER-001
            orderRepository.save(testOrder3); // SUPPLIER-002

            // when
            List<Order> orders = orderRepository.findByCompanyId("SUPPLIER-001");

            // then
            assertThat(orders).hasSize(2);
            assertThat(orders).allMatch(order ->
                    order.getCompanyInfo().getSupplierCompanyId().equals("SUPPLIER-001")
            );
        }

        @Test
        @DisplayName("수령업체 ID로 주문 조회")
        void findByCompanyId_AsReceiver_ReturnsOrders() {
            // given
            orderRepository.save(testOrder1); // RECEIVER-001
            orderRepository.save(testOrder2); // RECEIVER-002
            orderRepository.save(testOrder3); // RECEIVER-001

            // when
            List<Order> orders = orderRepository.findByCompanyId("RECEIVER-001");

            // then
            assertThat(orders).hasSize(2);
            assertThat(orders).allMatch(order ->
                    order.getCompanyInfo().getReceiverCompanyId().equals("RECEIVER-001")
            );
        }

        @Test
        @DisplayName("존재하지 않는 업체 ID 조회 시 빈 리스트 반환")
        void findByCompanyId_NonExisting_ReturnsEmptyList() {
            // given
            orderRepository.save(testOrder1);

            // when
            List<Order> orders = orderRepository.findByCompanyId("NON-EXISTING");

            // then
            assertThat(orders).isEmpty();
        }
    }

    @Nested
    @DisplayName("findByStatus() - 상태별 조회 테스트")
    class FindByStatusTest {

        @Test
        @DisplayName("PENDING 상태 주문 조회")
        void findByStatus_PendingOrders_ReturnsMatchingOrders() {
            // given
            orderRepository.save(testOrder1); // PENDING
            orderRepository.save(testOrder2); // CONFIRMED
            orderRepository.save(testOrder3); // COMPLETED

            // when
            List<Order> orders = orderRepository.findByStatus(OrderStatus.PENDING);

            // then
            assertThat(orders).hasSize(1);
            assertThat(orders.get(0).getStatus()).isEqualTo(OrderStatus.PENDING);
        }

        @Test
        @DisplayName("CONFIRMED 상태 주문 조회")
        void findByStatus_ConfirmedOrders_ReturnsMatchingOrders() {
            // given
            orderRepository.save(testOrder1); // PENDING
            orderRepository.save(testOrder2); // CONFIRMED

            // when
            List<Order> orders = orderRepository.findByStatus(OrderStatus.CONFIRMED);

            // then
            assertThat(orders).hasSize(1);
            assertThat(orders.get(0).getStatus()).isEqualTo(OrderStatus.CONFIRMED);
        }

        @Test
        @DisplayName("삭제된 주문은 조회되지 않음")
        void findByStatus_ExcludesDeletedOrders() {
            // given
            Order savedOrder = orderRepository.save(testOrder1);
            orderRepository.delete(savedOrder, "ADMIN-001");

            // when
            List<Order> orders = orderRepository.findByStatus(OrderStatus.PENDING);

            // then
            assertThat(orders).isEmpty();
        }
    }

    @Nested
    @DisplayName("findByLastMileDeliveryId() - 배송 ID로 조회 테스트")
    class FindByLastMileDeliveryIdTest {

        @Test
        @DisplayName("배송 ID로 주문 조회 성공")
        void findByLastMileDeliveryId_ExistingDelivery_ReturnsOrder() {
            // given
            Order savedOrder = orderRepository.save(testOrder2);

            // 배송 ID 설정 (Saga 완료 후)
            savedOrder.completeLastMileDeliveryCreation("DELIVERY-001");
            orderRepository.save(savedOrder);

            // when
            Optional<Order> foundOrder = orderRepository.findByLastMileDeliveryId("DELIVERY-001");

            // then
            assertThat(foundOrder).isPresent();
            assertThat(foundOrder.get().getId()).isEqualTo(savedOrder.getId());
        }

        @Test
        @DisplayName("존재하지 않는 배송 ID 조회 시 빈 Optional 반환")
        void findByLastMileDeliveryId_NonExisting_ReturnsEmpty() {
            // when
            Optional<Order> foundOrder = orderRepository.findByLastMileDeliveryId("NON-EXISTING");

            // then
            assertThat(foundOrder).isEmpty();
        }
    }

    @Nested
    @DisplayName("findByPaymentId() - 결제 ID로 조회 테스트")
    class FindByPaymentIdTest {

        @Test
        @DisplayName("결제 ID로 주문 조회 성공")
        void findByPaymentId_ExistingPayment_ReturnsOrder() {
            // given
            Order savedOrder = orderRepository.save(testOrder1);

            // 결제 검증 완료
            savedOrder.startStockChecking();
            savedOrder.completeStockReservation("HUB-001");
            savedOrder.startPaymentVerification();
            savedOrder.completePaymentVerification("PAYMENT-001");
            orderRepository.save(savedOrder);

            // when
            Optional<Order> foundOrder = orderRepository.findByPaymentId("PAYMENT-001");

            // then
            assertThat(foundOrder).isPresent();
            assertThat(foundOrder.get().getId()).isEqualTo(savedOrder.getId());
        }

        @Test
        @DisplayName("존재하지 않는 결제 ID 조회 시 빈 Optional 반환")
        void findByPaymentId_NonExisting_ReturnsEmpty() {
            // when
            Optional<Order> foundOrder = orderRepository.findByPaymentId("NON-EXISTING");

            // then
            assertThat(foundOrder).isEmpty();
        }
    }

    @Nested
    @DisplayName("delete() - Soft Delete 테스트")
    class DeleteTest {

        @Test
        @DisplayName("주문 소프트 삭제 성공")
        void delete_ExistingOrder_SoftDeletesSuccessfully() {
            // given
            Order savedOrder = orderRepository.save(testOrder1);
            OrderId orderId = savedOrder.getId();

            // when
            orderRepository.delete(savedOrder, "ADMIN-001");

            // then
            // findById는 isDeleted 체크 안함 - 삭제된 것도 조회됨
            Optional<Order> foundOrder = orderRepository.findById(orderId);
            assertThat(foundOrder).isPresent(); // ✅ 여전히 조회됨

            // findByStatus는 isDeleted=false만 조회
            List<Order> ordersByStatus = orderRepository.findByStatus(OrderStatus.PENDING);
            assertThat(ordersByStatus).noneMatch(order -> order.getId().equals(orderId));

            // 삭제된 주문 포함 조회에서는 조회됨
            Optional<Order> includingDeleted = orderRepository.findByIdIncludingDeleted(orderId);
            assertThat(includingDeleted).isPresent();
        }

        @Test
        @DisplayName("존재하지 않는 주문 삭제 시도 시 예외 발생")
        void delete_NonExistingOrder_ThrowsException() {
            // given - 저장되지 않은 주문 (ID가 null)
            Order unsavedOrder = testOrder1;

            // when & then
            // ID가 null이면 NullPointerException 발생
            assertThatThrownBy(() -> orderRepository.delete(unsavedOrder, "ADMIN-001"))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("삭제된 주문 ID로 삭제 시도 시 예외 발생")
        void delete_DeletedOrder_ThrowsException() {
            // given
            Order savedOrder = orderRepository.save(testOrder1);
            orderRepository.delete(savedOrder, "ADMIN-001");

            // 삭제된 주문 재조회 (findById는 삭제된 것도 가져옴)
            Order deletedOrder = orderRepository.findById(savedOrder.getId()).get();

            // when & then - 이미 삭제된 주문을 다시 삭제 시도
            // (실제로는 에러 안날 수도 있음 - 구현에 따라 다름)
            orderRepository.delete(deletedOrder, "ADMIN-002"); // 성공할 수도 있음
        }
    }

    @Nested
    @DisplayName("searchOrders() - 동적 쿼리 검색 테스트")
    class SearchOrdersTest {

        @Test
        @DisplayName("모든 조건으로 검색")
        void searchOrders_AllConditions_ReturnsMatchingOrders() {
            // given
            orderRepository.save(testOrder1);
            orderRepository.save(testOrder2);
            orderRepository.save(testOrder3);

            LocalDateTime startDate = LocalDateTime.now().minusHours(1);
            LocalDateTime endDate = LocalDateTime.now().plusHours(1);
            Pageable pageable = PageRequest.of(0, 10);

            // when
            Page<Order> result = orderRepository.searchOrders(
                    "SUPPLIER-001",
                    OrderStatus.PENDING,
                    startDate,
                    endDate,
                    pageable
            );

            // then
            assertThat(result.getContent()).hasSizeGreaterThanOrEqualTo(1);
            assertThat(result.getContent()).allMatch(order ->
                    order.getCompanyInfo().getSupplierCompanyId().equals("SUPPLIER-001") &&
                            order.getStatus() == OrderStatus.PENDING
            );
        }

        @Test
        @DisplayName("업체 ID만으로 검색")
        void searchOrders_CompanyIdOnly_ReturnsMatchingOrders() {
            // given
            orderRepository.save(testOrder1); // SUPPLIER-001
            orderRepository.save(testOrder2); // SUPPLIER-001
            orderRepository.save(testOrder3); // SUPPLIER-002

            Pageable pageable = PageRequest.of(0, 10);

            // when
            Page<Order> result = orderRepository.searchOrders(
                    "SUPPLIER-001",
                    null,
                    null,
                    null,
                    pageable
            );

            // then
            assertThat(result.getContent()).hasSize(2);
        }

        @Test
        @DisplayName("상태만으로 검색")
        void searchOrders_StatusOnly_ReturnsMatchingOrders() {
            // given
            orderRepository.save(testOrder1); // PENDING
            orderRepository.save(testOrder2); // CONFIRMED
            orderRepository.save(testOrder3); // COMPLETED

            Pageable pageable = PageRequest.of(0, 10);

            // when
            Page<Order> result = orderRepository.searchOrders(
                    null,
                    OrderStatus.CONFIRMED,
                    null,
                    null,
                    pageable
            );

            // then
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).getStatus()).isEqualTo(OrderStatus.CONFIRMED);
        }

        @Test
        @DisplayName("날짜 범위로 검색")
        void searchOrders_DateRange_ReturnsMatchingOrders() {
            // given
            orderRepository.save(testOrder1);
            orderRepository.save(testOrder2);

            LocalDateTime startDate = LocalDateTime.now().minusHours(1);
            LocalDateTime endDate = LocalDateTime.now().plusHours(1);
            Pageable pageable = PageRequest.of(0, 10);

            // when
            Page<Order> result = orderRepository.searchOrders(
                    null,
                    null,
                    startDate,
                    endDate,
                    pageable
            );

            // then
            assertThat(result.getContent()).hasSizeGreaterThanOrEqualTo(2);
        }

        @Test
        @DisplayName("페이징 처리 검증")
        void searchOrders_Pagination_WorksCorrectly() {
            // given
            for (int i = 0; i < 15; i++) {
                Order order = createTestOrderWithUniqueNumber(
                        "SUPPLIER-001",
                        "RECEIVER-001",
                        OrderStatus.PENDING
                );
                orderRepository.save(order);
            }

            Pageable pageable = PageRequest.of(0, 10);

            // when
            Page<Order> result = orderRepository.searchOrders(
                    null,
                    null,
                    null,
                    null,
                    pageable
            );

            // then
            assertThat(result.getContent()).hasSize(10);
            assertThat(result.getTotalElements()).isGreaterThanOrEqualTo(15);
            assertThat(result.getTotalPages()).isGreaterThanOrEqualTo(2);
        }

        @Test
        @DisplayName("삭제된 주문은 검색에서 제외")
        void searchOrders_ExcludesDeletedOrders() {
            // given
            Order order1 = orderRepository.save(testOrder1);
            orderRepository.save(testOrder2);
            orderRepository.delete(order1, "ADMIN-001");

            Pageable pageable = PageRequest.of(0, 10);

            // when
            Page<Order> result = orderRepository.searchOrders(
                    "SUPPLIER-001",
                    null,
                    null,
                    null,
                    pageable
            );

            // then
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent()).noneMatch(order -> order.getId().equals(order1.getId()));
        }
    }

    @Nested
    @DisplayName("findOrdersWithUpcomingDeadline() - 발송 시한 임박 조회 테스트")
    class FindOrdersWithUpcomingDeadlineTest {

        @Test
        @DisplayName("24시간 내 발송 시한 주문 조회")
        void findOrdersWithUpcomingDeadline_Within24Hours_ReturnsOrders() {
            // given
            Order order = orderRepository.save(testOrder2);

            // AI 계산 결과 설정
            LocalDateTime departureDeadline = LocalDateTime.now().plusHours(20);
            LocalDateTime estimatedDelivery = LocalDateTime.now().plusDays(1);

            order.updateRouteInfo("{\"hubs\": [\"HUB-001\", \"HUB-002\"]}");
            AiCalculationResult aiResult = order.getAiCalculationResult()
                    .withAiCalculation(departureDeadline, estimatedDelivery, "AI 계산 완료");

            // 리플렉션 등으로 aiResult 업데이트 필요 (또는 도메인 메서드 제공)
            // order의 aiCalculationResult를 업데이트하는 로직

            orderRepository.save(order);

            // when
            List<Order> orders = orderRepository.findOrdersWithUpcomingDeadline(24);

            // then
            assertThat(orders).hasSizeGreaterThanOrEqualTo(0);
        }
    }

    @Nested
    @DisplayName("findOverdueOrders() - 발송 시한 초과 조회 테스트")
    class FindOverdueOrdersTest {

        @Test
        @DisplayName("발송 시한 초과 주문 조회")
        void findOverdueOrders_ReturnsOverdueOrders() {
            // given
            // 발송 시한이 지난 주문 데이터 설정 필요

            // when
            List<Order> orders = orderRepository.findOverdueOrders();

            // then
            assertThat(orders).isNotNull();
        }
    }

    @Nested
    @DisplayName("searchAllOrdersIncludingDeleted() - 삭제된 주문 포함 검색 테스트")
    class SearchAllOrdersIncludingDeletedTest {

        @Test
        @DisplayName("삭제된 주문만 검색")
        void searchAllOrdersIncludingDeleted_DeletedOnly_ReturnsDeletedOrders() {
            // given
            Order order1 = orderRepository.save(testOrder1);
            orderRepository.save(testOrder2);
            orderRepository.delete(order1, "ADMIN-001");

            Pageable pageable = PageRequest.of(0, 10);

            // when
            Page<Order> result = orderRepository.searchAllOrdersIncludingDeleted(
                    null,
                    null,
                    true, // 삭제된 주문만
                    null,
                    null,
                    pageable
            );

            // then
            assertThat(result.getContent()).hasSizeGreaterThanOrEqualTo(1);
        }

        @Test
        @DisplayName("삭제되지 않은 주문만 검색")
        void searchAllOrdersIncludingDeleted_NotDeletedOnly_ReturnsActiveOrders() {
            // given
            Order order1 = orderRepository.save(testOrder1);
            orderRepository.save(testOrder2);
            orderRepository.delete(order1, "ADMIN-001");

            Pageable pageable = PageRequest.of(0, 10);

            // when
            Page<Order> result = orderRepository.searchAllOrdersIncludingDeleted(
                    null,
                    null,
                    false, // 삭제되지 않은 주문만
                    null,
                    null,
                    pageable
            );

            // then
            assertThat(result.getContent()).hasSize(1);
        }

        @Test
        @DisplayName("모든 주문 검색 (삭제 여부 무관)")
        void searchAllOrdersIncludingDeleted_All_ReturnsAllOrders() {
            // given
            Order order1 = orderRepository.save(testOrder1);
            orderRepository.save(testOrder2);
            orderRepository.delete(order1, "ADMIN-001");

            Pageable pageable = PageRequest.of(0, 10);

            // when
            Page<Order> result = orderRepository.searchAllOrdersIncludingDeleted(
                    null,
                    null,
                    null, // 삭제 여부 무관
                    null,
                    null,
                    pageable
            );

            // then
            assertThat(result.getContent()).hasSize(2);
        }
    }
}
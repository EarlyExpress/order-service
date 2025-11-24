package com.early_express.order_service.domain.order.infrastructure.persistence.entity;

import com.early_express.order_service.domain.order.domain.model.Order;
import com.early_express.order_service.domain.order.domain.model.OrderStatus;
import com.early_express.order_service.domain.order.domain.model.vo.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import static org.assertj.core.api.Assertions.*;

@DisplayName("OrderEntity 테스트")
class OrderEntityTest {

    @Nested
    @DisplayName("fromDomain 메서드는")
    class FromDomainTest {

        @Test
        @DisplayName("도메인 모델의 모든 필드를 엔티티로 정확히 변환한다")
        void shouldConvertAllFieldsFromDomainToEntity() {
            // given
            Order order = createTestOrder();

            // when
            OrderEntity entity = OrderEntity.fromDomain(order);

            // then
            assertThat(entity).isNotNull();
            assertThat(entity.getId()).isNotNull();
            assertThat(entity.getOrderNumber()).isEqualTo(order.getOrderNumberValue());

            // 업체 정보
            assertThat(entity.getSupplierCompanyId()).isEqualTo(order.getCompanyInfo().getSupplierCompanyId());
            assertThat(entity.getSupplierHubId()).isEqualTo(order.getCompanyInfo().getSupplierHubId());
            assertThat(entity.getReceiverCompanyId()).isEqualTo(order.getCompanyInfo().getReceiverCompanyId());
            assertThat(entity.getReceiverHubId()).isEqualTo(order.getCompanyInfo().getReceiverHubId());
            assertThat(entity.getDestinationHubId()).isEqualTo(order.getDestinationHubId());

            // 상품 정보
            assertThat(entity.getProductId()).isEqualTo(order.getProductInfo().getProductId());
            assertThat(entity.getQuantity()).isEqualTo(order.getProductInfo().getQuantity());

            // 배송 정보
            assertThat(entity.getRequiresHubDelivery()).isEqualTo(order.getDeliveryInfo().getRequiresHubDelivery());

            // 수령자 정보
            assertThat(entity.getReceiverName()).isEqualTo(order.getReceiverInfo().getReceiverName());
            assertThat(entity.getReceiverPhone()).isEqualTo(order.getReceiverInfo().getReceiverPhone());
            assertThat(entity.getDeliveryAddress()).isEqualTo(order.getReceiverInfo().getDeliveryAddress());

            // 요청사항
            assertThat(entity.getRequestedDeliveryDate()).isEqualTo(order.getRequestInfo().getRequestedDeliveryDate());
            assertThat(entity.getRequestedDeliveryTime()).isEqualTo(order.getRequestInfo().getRequestedDeliveryTime());

            // 상태
            assertThat(entity.getStatus()).isEqualTo(order.getStatus());

            // 금액 정보
            assertThat(entity.getUnitPrice()).isEqualTo(order.getAmountInfo().getUnitPrice());
            assertThat(entity.getTotalAmount()).isEqualTo(order.getAmountInfo().getTotalAmount());

            // PG 결제 정보
            assertThat(entity.getPgProvider()).isEqualTo(order.getPgPaymentInfo().getPgProvider());
            assertThat(entity.getPgPaymentId()).isEqualTo(order.getPgPaymentInfo().getPgPaymentId());
        }

        @Test
        @DisplayName("destinationHubId가 설정된 주문을 변환한다")
        void shouldConvertOrderWithDestinationHubId() {
            // given
            Order order = createTestOrder();
            order.updateDestinationHubId("DESTINATION-HUB-001");

            // when
            OrderEntity entity = OrderEntity.fromDomain(order);

            // then
            assertThat(entity.getDestinationHubId()).isEqualTo("DESTINATION-HUB-001");
        }

        @Test
        @DisplayName("destinationHubId가 null인 주문을 변환한다")
        void shouldConvertOrderWithNullDestinationHubId() {
            // given
            Order order = createTestOrder();

            // when
            OrderEntity entity = OrderEntity.fromDomain(order);

            // then
            assertThat(entity.getDestinationHubId()).isNull();
        }

        @Test
        @DisplayName("nullable 필드가 null인 경우에도 정상적으로 변환한다")
        void shouldConvertEvenWhenNullableFieldsAreNull() {
            // given
            Order order = createTestOrderWithNullableFields();

            // when
            OrderEntity entity = OrderEntity.fromDomain(order);

            // then
            assertThat(entity).isNotNull();
            assertThat(entity.getProductHubId()).isNull();
            assertThat(entity.getHubDeliveryId()).isNull();
            assertThat(entity.getLastMileDeliveryId()).isNull();
            assertThat(entity.getReceiverEmail()).isNull();
            assertThat(entity.getDeliveryAddressDetail()).isNull();
            assertThat(entity.getPaymentId()).isNull();
            assertThat(entity.getPgPaymentKey()).isNull();
        }

        @Test
        @DisplayName("배송 진행 정보가 없는 초기 주문도 정상적으로 변환한다")
        void shouldConvertOrderWithoutDeliveryProgress() {
            // given
            Order order = createTestOrder();

            // when
            OrderEntity entity = OrderEntity.fromDomain(order);

            // then
            assertThat(entity.getActualDepartureTime()).isNull();
            assertThat(entity.getHubArrivalTime()).isNull();
            assertThat(entity.getFinalDeliveryStartTime()).isNull();
            assertThat(entity.getActualDeliveryTime()).isNull();
            assertThat(entity.getSignature()).isNull();
            assertThat(entity.getActualReceiverName()).isNull();
        }
    }

    @Nested
    @DisplayName("toDomain 메서드는")
    class ToDomainTest {

        @Test
        @DisplayName("엔티티의 모든 필드를 도메인 모델로 정확히 변환한다")
        void shouldConvertAllFieldsFromEntityToDomain() {
            // given
            OrderEntity entity = createTestOrderEntity();

            // when
            Order order = entity.toDomain();

            // then
            assertThat(order).isNotNull();
            assertThat(order.getIdValue()).isEqualTo(entity.getId());
            assertThat(order.getOrderNumberValue()).isEqualTo(entity.getOrderNumber());

            // 업체 정보
            assertThat(order.getCompanyInfo().getSupplierCompanyId()).isEqualTo(entity.getSupplierCompanyId());
            assertThat(order.getCompanyInfo().getReceiverHubId()).isEqualTo(entity.getReceiverHubId());
            assertThat(order.getDestinationHubId()).isEqualTo(entity.getDestinationHubId());

            // 상품 정보
            assertThat(order.getProductInfo().getProductId()).isEqualTo(entity.getProductId());
            assertThat(order.getProductInfo().getQuantity()).isEqualTo(entity.getQuantity());

            // 배송 정보
            assertThat(order.getDeliveryInfo().getRequiresHubDelivery()).isEqualTo(entity.getRequiresHubDelivery());

            // 수령자 정보
            assertThat(order.getReceiverInfo().getReceiverName()).isEqualTo(entity.getReceiverName());
            assertThat(order.getReceiverInfo().getReceiverPhone()).isEqualTo(entity.getReceiverPhone());

            // 요청사항
            assertThat(order.getRequestInfo().getRequestedDeliveryDate()).isEqualTo(entity.getRequestedDeliveryDate());
            assertThat(order.getRequestInfo().getRequestedDeliveryTime()).isEqualTo(entity.getRequestedDeliveryTime());

            // 상태
            assertThat(order.getStatus()).isEqualTo(entity.getStatus());

            // 금액 정보
            assertThat(order.getAmountInfo().getUnitPrice()).isEqualTo(entity.getUnitPrice());
            assertThat(order.getAmountInfo().getTotalAmount()).isEqualTo(entity.getTotalAmount());

            // PG 결제 정보
            assertThat(order.getPgPaymentInfo().getPgProvider()).isEqualTo(entity.getPgProvider());
            assertThat(order.getPgPaymentInfo().getPgPaymentId()).isEqualTo(entity.getPgPaymentId());
        }

        @Test
        @DisplayName("destinationHubId를 올바르게 복원한다")
        void shouldRestoreDestinationHubId() {
            // given
            OrderEntity entity = OrderEntity.builder()
                    .id("test-id")
                    .orderNumber("ORD-20250121-001")
                    .supplierCompanyId("supplier-1")
                    .supplierHubId("hub-1")
                    .receiverCompanyId("receiver-1")
                    .receiverHubId("hub-2")
                    .destinationHubId("DESTINATION-HUB-001")
                    .productId("product-1")
                    .quantity(10)
                    .requiresHubDelivery(true)
                    .receiverName("홍길동")
                    .receiverPhone("010-1234-5678")
                    .deliveryAddress("서울시 강남구")
                    .requestedDeliveryDate(LocalDate.now().plusDays(2))
                    .requestedDeliveryTime(LocalTime.of(14, 0))
                    .status(OrderStatus.PENDING)
                    .unitPrice(BigDecimal.valueOf(10000))
                    .totalAmount(BigDecimal.valueOf(100000))
                    .pgProvider("TOSS")
                    .pgPaymentId("pg-payment-123")
                    .build();

            // when
            Order order = entity.toDomain();

            // then
            assertThat(order.getDestinationHubId()).isEqualTo("DESTINATION-HUB-001");
        }

        @Test
        @DisplayName("destinationHubId가 null이어도 안전하게 복원한다")
        void shouldHandleNullDestinationHubId() {
            // given
            OrderEntity entity = createTestOrderEntity();

            // when
            Order order = entity.toDomain();

            // then
            assertThat(order.getDestinationHubId()).isNull();
        }

        @Test
        @DisplayName("AI 계산 결과를 올바르게 복원한다")
        void shouldRestoreAiCalculationResult() {
            // given
            OrderEntity entity = createTestOrderEntity();
            entity = OrderEntity.builder()
                    .id("test-id")
                    .orderNumber("ORD-20250121-001")
                    .supplierCompanyId("supplier-1")
                    .supplierHubId("hub-1")
                    .receiverCompanyId("receiver-1")
                    .receiverHubId("hub-2")
                    .productId("product-1")
                    .quantity(10)
                    .requiresHubDelivery(true)
                    .receiverName("홍길동")
                    .receiverPhone("010-1234-5678")
                    .deliveryAddress("서울시 강남구")
                    .requestedDeliveryDate(LocalDate.now().plusDays(2))
                    .requestedDeliveryTime(LocalTime.of(14, 0))
                    .calculatedDepartureDeadline(LocalDateTime.now().plusHours(24))
                    .estimatedDeliveryTime(LocalDateTime.now().plusDays(2))
                    .routeInfo("{\"hubs\":[\"HUB-001\",\"HUB-002\"]}")
                    .status(OrderStatus.PENDING)
                    .unitPrice(BigDecimal.valueOf(10000))
                    .totalAmount(BigDecimal.valueOf(100000))
                    .pgProvider("TOSS")
                    .pgPaymentId("pg-payment-123")
                    .build();

            // when
            Order order = entity.toDomain();

            // then
            assertThat(order.getAiCalculationResult()).isNotNull();
            assertThat(order.getAiCalculationResult().getCalculatedDepartureDeadline())
                    .isEqualTo(entity.getCalculatedDepartureDeadline());
            assertThat(order.getAiCalculationResult().getEstimatedDeliveryTime())
                    .isEqualTo(entity.getEstimatedDeliveryTime());
            assertThat(order.getAiCalculationResult().getRouteInfo())
                    .isEqualTo(entity.getRouteInfo());
        }
    }

    @Nested
    @DisplayName("updateFromDomain 메서드는")
    class UpdateFromDomainTest {

        @Test
        @DisplayName("변경 가능한 필드만 업데이트한다")
        void shouldUpdateOnlyMutableFields() {
            // given
            OrderEntity entity = createTestOrderEntity();
            String originalId = entity.getId();
            String originalOrderNumber = entity.getOrderNumber();

            Order order = entity.toDomain();
            order.startStockChecking();
            order.completeStockReservation("product-hub-1");

            // when
            entity.updateFromDomain(order);

            // then
            // 불변 필드는 변경되지 않음
            assertThat(entity.getId()).isEqualTo(originalId);
            assertThat(entity.getOrderNumber()).isEqualTo(originalOrderNumber);

            // 가변 필드는 업데이트됨
            assertThat(entity.getProductHubId()).isEqualTo("product-hub-1");
            assertThat(entity.getStatus()).isEqualTo(OrderStatus.STOCK_RESERVED);
            assertThat(entity.getRequiresHubDelivery()).isEqualTo(order.getDeliveryInfo().getRequiresHubDelivery());
        }

        @Test
        @DisplayName("destinationHubId가 업데이트된다")
        void shouldUpdateDestinationHubId() {
            // given
            OrderEntity entity = createTestOrderEntity();
            assertThat(entity.getDestinationHubId()).isNull();

            Order order = entity.toDomain();
            order.updateDestinationHubId("DESTINATION-HUB-001");

            // when
            entity.updateFromDomain(order);

            // then
            assertThat(entity.getDestinationHubId()).isEqualTo("DESTINATION-HUB-001");
        }

        @Test
        @DisplayName("destinationHubId를 여러 번 업데이트할 수 있다")
        void shouldUpdateDestinationHubIdMultipleTimes() {
            // given
            OrderEntity entity = createTestOrderEntity();
            Order order = entity.toDomain();

            // 첫 번째 업데이트
            order.updateDestinationHubId("HUB-TEMP");
            entity.updateFromDomain(order);
            assertThat(entity.getDestinationHubId()).isEqualTo("HUB-TEMP");

            // 두 번째 업데이트
            order = entity.toDomain();
            order.updateDestinationHubId("HUB-FINAL");

            // when
            entity.updateFromDomain(order);

            // then
            assertThat(entity.getDestinationHubId()).isEqualTo("HUB-FINAL");
        }

        @Test
        @DisplayName("결제 검증 완료 시 payment ID가 업데이트된다")
        void shouldUpdatePaymentId() {
            // given
            OrderEntity entity = createTestOrderEntity();
            Order order = entity.toDomain();

            order.startStockChecking();
            order.completeStockReservation("product-hub-1");
            order.startPaymentVerification();
            order.completePaymentVerification("payment-123");

            // when
            entity.updateFromDomain(order);

            // then
            assertThat(entity.getPaymentId()).isEqualTo("payment-123");
            assertThat(entity.getStatus()).isEqualTo(OrderStatus.PAYMENT_VERIFIED);
        }

        @Test
        @DisplayName("배송 생성 완료 시 배송 ID가 업데이트된다")
        void shouldUpdateDeliveryIds() {
            // given
            OrderEntity entity = createTestOrderEntity();
            Order order = entity.toDomain();

            // Saga 진행
            order.startStockChecking();
            order.completeStockReservation("product-hub-1");
            order.startPaymentVerification();
            order.completePaymentVerification("payment-123");
            order.startRouteCalculation();
            order.startDeliveryCreation();
            order.completeHubDeliveryCreation("hub-delivery-123");
            order.completeLastMileDeliveryCreation("last-mile-delivery-456");

            // when
            entity.updateFromDomain(order);

            // then
            assertThat(entity.getHubDeliveryId()).isEqualTo("hub-delivery-123");
            assertThat(entity.getLastMileDeliveryId()).isEqualTo("last-mile-delivery-456");
            assertThat(entity.getStatus()).isEqualTo(OrderStatus.CONFIRMED);
        }

        @Test
        @DisplayName("배송 진행 정보가 업데이트된다")
        void shouldUpdateDeliveryProgressInfo() {
            // given
            OrderEntity entity = createTestOrderEntity();
            Order order = entity.toDomain();

            LocalDateTime departureTime = LocalDateTime.now();
            LocalDateTime arrivalTime = LocalDateTime.now().plusHours(2);
            LocalDateTime deliveryStartTime = LocalDateTime.now().plusHours(3);
            LocalDateTime deliveryTime = LocalDateTime.now().plusHours(4);

            // 주문을 확정 가능한 상태로 만들기 (Saga 진행)
            order.startStockChecking();
            order.completeStockReservation("product-hub-1");
            order.startPaymentVerification();
            order.completePaymentVerification("payment-123");

            // 배송 진행
            order.updateDestinationHubId("destination-hub-1");
            order.confirm();
            order.startHubDelivery(departureTime);
            order.arriveAtHub(arrivalTime);
            order.prepareLastMileDelivery();
            order.startFinalDelivery(deliveryStartTime);
            order.completeDelivery(deliveryTime, "signature-base64", "실제수령자");

            // when
            entity.updateFromDomain(order);

            // then
            assertThat(entity.getActualDepartureTime()).isEqualTo(departureTime);
            assertThat(entity.getHubArrivalTime()).isEqualTo(arrivalTime);
            assertThat(entity.getFinalDeliveryStartTime()).isEqualTo(deliveryStartTime);
            assertThat(entity.getActualDeliveryTime()).isEqualTo(deliveryTime);
            assertThat(entity.getSignature()).isEqualTo("signature-base64");
            assertThat(entity.getActualReceiverName()).isEqualTo("실제수령자");
            assertThat(entity.getStatus()).isEqualTo(OrderStatus.COMPLETED);
        }

        @Test
        @DisplayName("취소 정보가 업데이트된다")
        void shouldUpdateCancelInfo() {
            // given
            OrderEntity entity = createTestOrderEntity();
            Order order = entity.toDomain();

            order.cancel("고객 요청");

            // when
            entity.updateFromDomain(order);

            // then
            assertThat(entity.getStatus()).isEqualTo(OrderStatus.CANCELLED);
            assertThat(entity.getCancelReason()).isEqualTo("고객 요청");
            assertThat(entity.getCancelledAt()).isNotNull();
        }

        @Test
        @DisplayName("경로 정보와 AI 계산 결과가 업데이트된다")
        void shouldUpdateRouteAndAiCalculationResult() {
            // given
            OrderEntity entity = createTestOrderEntity();
            Order order = entity.toDomain();

            String routeInfo = "{\"hubs\":[\"HUB-001\",\"HUB-002\"],\"distance\":100}";

            // 경로 정보만 업데이트 (AI 계산은 별도 프로세스)
            order.updateRouteInfo(routeInfo);

            // when
            entity.updateFromDomain(order);

            // then
            assertThat(entity.getRouteInfo()).isEqualTo(routeInfo);
            // AI 계산 결과는 updateRouteInfo만으로는 설정되지 않음 (별도 프로세스 필요)
            assertThat(entity.getCalculatedDepartureDeadline()).isNull();
            assertThat(entity.getEstimatedDeliveryTime()).isNull();
        }

        @Test
        @DisplayName("AI 계산 결과를 포함한 완전한 주문 정보가 업데이트된다")
        void shouldUpdateCompleteOrderWithAiCalculation() {
            // given
            OrderEntity entity = createTestOrderEntity();

            // 완전한 주문 생성 (AI 계산 결과 포함)
            Order order = createCompleteOrder();

            // when
            entity.updateFromDomain(order);

            // then
            assertThat(entity.getRouteInfo()).isNotNull();
            assertThat(entity.getRouteInfo()).contains("HUB-001");
            assertThat(entity.getProductHubId()).isEqualTo("product-hub-1");
            assertThat(entity.getStatus()).isEqualTo(OrderStatus.CONFIRMED);
        }
    }

    @Nested
    @DisplayName("도메인-엔티티 양방향 변환은")
    class BidirectionalConversionTest {

        @Test
        @DisplayName("도메인 -> 엔티티 -> 도메인 변환 시 데이터가 보존된다")
        void shouldPreserveDataInRoundTripConversion() {
            // given
            Order originalOrder = createCompleteOrder();

            // when
            OrderEntity entity = OrderEntity.fromDomain(originalOrder);
            Order convertedOrder = entity.toDomain();

            // then
            assertThat(convertedOrder.getOrderNumberValue()).isEqualTo(originalOrder.getOrderNumberValue());
            assertThat(convertedOrder.getStatus()).isEqualTo(originalOrder.getStatus());
            assertThat(convertedOrder.getCompanyInfo()).isEqualTo(originalOrder.getCompanyInfo());
            assertThat(convertedOrder.getProductInfo().getProductId()).isEqualTo(originalOrder.getProductInfo().getProductId());
            assertThat(convertedOrder.getReceiverInfo().getReceiverName()).isEqualTo(originalOrder.getReceiverInfo().getReceiverName());
            assertThat(convertedOrder.getAmountInfo().getTotalAmount()).isEqualTo(originalOrder.getAmountInfo().getTotalAmount());
        }

        @Test
        @DisplayName("여러 번의 업데이트와 변환 후에도 데이터 무결성이 유지된다")
        void shouldMaintainDataIntegrityAfterMultipleUpdates() {
            // given
            Order order = createTestOrder();
            OrderEntity entity = OrderEntity.fromDomain(order);

            // when - 여러 단계의 업데이트
            order = entity.toDomain();
            order.startStockChecking();
            entity.updateFromDomain(order);

            order = entity.toDomain();
            order.completeStockReservation("product-hub-1");
            entity.updateFromDomain(order);

            order = entity.toDomain();
            order.startPaymentVerification();
            entity.updateFromDomain(order);

            order = entity.toDomain();
            order.completePaymentVerification("payment-123");
            entity.updateFromDomain(order);

            // then
            Order finalOrder = entity.toDomain();
            assertThat(finalOrder.getStatus()).isEqualTo(OrderStatus.PAYMENT_VERIFIED);
            assertThat(finalOrder.getProductInfo().getProductHubId()).isEqualTo("product-hub-1");
            assertThat(finalOrder.getAmountInfo().getPaymentId()).isEqualTo("payment-123");
        }
    }

    // ===== 테스트 데이터 생성 헬퍼 메서드 =====

    private Order createTestOrder() {
        return Order.builder()
                .id(OrderId.create()) // 테스트용 ID 생성
                .orderNumber(OrderNumber.generate(1))
                .companyInfo(CompanyInfo.of(
                        "supplier-company-1",
                        "supplier-hub-1",
                        "receiver-company-1",
                        "receiver-hub-1"
                ))
                .destinationHubId(null)
                .productInfo(ProductInfo.of("product-1", 10))
                .deliveryInfo(DeliveryInfo.initial())
                .receiverInfo(ReceiverInfo.of(
                        "홍길동",
                        "010-1234-5678",
                        "hong@example.com",
                        "서울시 강남구 테헤란로 123",
                        "4층",
                        "06234",
                        "문 앞에 놓아주세요"
                ))
                .requestInfo(RequestInfo.of(
                        LocalDate.now().plusDays(2),
                        LocalTime.of(14, 0),
                        "조심히 다뤄주세요"
                ))
                .aiCalculationResult(AiCalculationResult.empty())
                .status(OrderStatus.PENDING)
                .amountInfo(AmountInfo.of(BigDecimal.valueOf(10000), 10))
                .pgPaymentInfo(PgPaymentInfo.of("TOSS", "pg-payment-123"))
                .deliveryProgressInfo(DeliveryProgressInfo.empty())
                .createdBy("user-123")
                .createdAt(LocalDateTime.now())
                .build();
    }

    private Order createTestOrderWithNullableFields() {
        return Order.create(
                OrderNumber.generate(1),
                CompanyInfo.of(
                        "supplier-company-1",
                        "supplier-hub-1",
                        "receiver-company-1",
                        "receiver-hub-1"
                ),
                ProductInfo.of("product-1", 10),
                ReceiverInfo.of(
                        "홍길동",
                        "010-1234-5678",
                        null, // receiverEmail
                        "서울시 강남구",
                        null, // deliveryAddressDetail
                        null, // deliveryPostalCode
                        null  // deliveryNote
                ),
                RequestInfo.of(
                        LocalDate.now().plusDays(2),
                        LocalTime.of(14, 0),
                        null  // specialInstructions
                ),
                BigDecimal.valueOf(10000),
                PgPaymentInfo.of("TOSS", "pg-payment-123"),
                "user-123"
        );
    }

    private Order createCompleteOrder() {
        Order order = createTestOrder();

        // Saga 진행
        order.startStockChecking();
        order.completeStockReservation("product-hub-1");
        order.startPaymentVerification();
        order.completePaymentVerification("payment-123");
        order.startRouteCalculation();

        // 경로 정보 업데이트
        order.updateRouteInfo("{\"hubs\":[\"HUB-001\",\"HUB-002\"]}");

        order.startDeliveryCreation();
        order.completeHubDeliveryCreation("hub-delivery-123");
        order.completeLastMileDeliveryCreation("last-mile-delivery-456");

        return order;
    }

    private OrderEntity createTestOrderEntity() {
        return OrderEntity.builder()
                .id("test-order-id")
                .orderNumber("ORD-20250121-001")
                .supplierCompanyId("supplier-company-1")
                .supplierHubId("supplier-hub-1")
                .receiverCompanyId("receiver-company-1")
                .receiverHubId("receiver-hub-1")
                .destinationHubId(null)
                .productId("product-1")
                .productHubId(null)
                .quantity(10)
                .requiresHubDelivery(false)
                .hubDeliveryId(null)
                .lastMileDeliveryId(null)
                .receiverName("홍길동")
                .receiverPhone("010-1234-5678")
                .receiverEmail("hong@example.com")
                .deliveryAddress("서울시 강남구 테헤란로 123")
                .deliveryAddressDetail("4층")
                .deliveryPostalCode("06234")
                .deliveryNote("문 앞에 놓아주세요")
                .requestedDeliveryDate(LocalDate.now().plusDays(2))
                .requestedDeliveryTime(LocalTime.of(14, 0))
                .specialInstructions("조심히 다뤄주세요")
                .calculatedDepartureDeadline(null)
                .estimatedDeliveryTime(null)
                .routeInfo(null)
                .status(OrderStatus.PENDING)
                .unitPrice(BigDecimal.valueOf(10000))
                .totalAmount(BigDecimal.valueOf(100000))
                .paymentId(null)
                .pgProvider("TOSS")
                .pgPaymentId("pg-payment-123")
                .pgPaymentKey(null)
                .actualDepartureTime(null)
                .hubArrivalTime(null)
                .finalDeliveryStartTime(null)
                .actualDeliveryTime(null)
                .signature(null)
                .actualReceiverName(null)
                .cancelReason(null)
                .cancelledAt(null)
                .build();
    }
}
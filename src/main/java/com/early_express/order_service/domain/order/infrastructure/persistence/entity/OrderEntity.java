package com.early_express.order_service.domain.order.infrastructure.persistence.entity;

import com.early_express.order_service.domain.order.domain.model.Order;
import com.early_express.order_service.domain.order.domain.model.OrderStatus;
import com.early_express.order_service.domain.order.domain.model.vo.*;
import com.early_express.order_service.global.common.utils.UuidUtils;
import com.early_express.order_service.global.infrastructure.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * Order JPA Entity
 * 도메인 모델과 JPA 엔티티 간 변환 담당
 */
@Entity
@Table(name = "p_orders")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OrderEntity extends BaseEntity {

    @Id
    @Column(name = "id", length = 36)
    private String id;

    @Column(name = "order_number", nullable = false, unique = true, length = 50)
    private String orderNumber;

    // ===== 업체 정보 =====
    @Column(name = "supplier_company_id", nullable = false, length = 36)
    private String supplierCompanyId;

    @Column(name = "supplier_hub_id", nullable = false, length = 36)
    private String supplierHubId;

    @Column(name = "receiver_company_id", nullable = false, length = 36)
    private String receiverCompanyId;

    @Column(name = "receiver_hub_id", nullable = false, length = 36)
    private String receiverHubId;

    /**
     * 실제 도착 허브 ID
     * Hub Service가 주소 기반으로 결정한 실제 도착 허브
     */
    @Column(name = "destination_hub_id", length = 36)
    private String destinationHubId;

    // ===== 상품 정보 =====
    @Column(name = "product_id", nullable = false, length = 36)
    private String productId;

    @Column(name = "product_hub_id", length = 36)
    private String productHubId;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    // ===== 배송 정보 =====
    @Column(name = "requires_hub_delivery", nullable = false)
    private Boolean requiresHubDelivery;

    @Column(name = "hub_delivery_id", length = 36)
    private String hubDeliveryId;

    @Column(name = "last_mile_delivery_id", length = 36)
    private String lastMileDeliveryId;

    // ===== 수령자 정보 =====
    @Column(name = "receiver_name", nullable = false, length = 100)
    private String receiverName;

    @Column(name = "receiver_phone", nullable = false, length = 20)
    private String receiverPhone;

    @Column(name = "receiver_email", length = 100)
    private String receiverEmail;

    @Column(name = "delivery_address", nullable = false, length = 500)
    private String deliveryAddress;

    @Column(name = "delivery_address_detail", length = 200)
    private String deliveryAddressDetail;

    @Column(name = "delivery_postal_code", length = 20)
    private String deliveryPostalCode;

    @Column(name = "delivery_note", columnDefinition = "TEXT")
    private String deliveryNote;

    // ===== 요청사항 =====
    @Column(name = "requested_delivery_date", nullable = false)
    private LocalDate requestedDeliveryDate;

    @Column(name = "requested_delivery_time", nullable = false)
    private LocalTime requestedDeliveryTime;

    @Column(name = "special_instructions", columnDefinition = "TEXT")
    private String specialInstructions;

    // ===== AI 계산 결과 =====
    @Column(name = "calculated_departure_deadline")
    private LocalDateTime calculatedDepartureDeadline;

    @Column(name = "estimated_delivery_time")
    private LocalDateTime estimatedDeliveryTime;

    @Column(name = "route_info", columnDefinition = "TEXT")
    private String routeInfo;

    // ===== 상태 =====
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private OrderStatus status;

    // ===== 금액 정보 =====
    @Column(name = "unit_price", nullable = false, precision = 15, scale = 2)
    private BigDecimal unitPrice;

    @Column(name = "total_amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal totalAmount;

    @Column(name = "payment_id", length = 36)
    private String paymentId;

    // ===== PG 결제 정보 =====
    @Column(name = "pg_provider", nullable = false, length = 20)
    private String pgProvider;

    @Column(name = "pg_payment_id", nullable = false, length = 200)
    private String pgPaymentId;

    @Column(name = "pg_payment_key", length = 200)
    private String pgPaymentKey;

    // ===== 배송 진행 정보 =====
    @Column(name = "actual_departure_time")
    private LocalDateTime actualDepartureTime;

    @Column(name = "hub_arrival_time")
    private LocalDateTime hubArrivalTime;

    @Column(name = "final_delivery_start_time")
    private LocalDateTime finalDeliveryStartTime;

    @Column(name = "actual_delivery_time")
    private LocalDateTime actualDeliveryTime;

    @Column(name = "signature", columnDefinition = "TEXT")
    private String signature;

    @Column(name = "actual_receiver_name", length = 100)
    private String actualReceiverName;

    // ===== 취소 정보 =====
    @Column(name = "cancel_reason", columnDefinition = "TEXT")
    private String cancelReason;

    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt;

    @Builder
    private OrderEntity(
            String id,
            String orderNumber,
            String supplierCompanyId,
            String supplierHubId,
            String receiverCompanyId,
            String receiverHubId,
            String destinationHubId,
            String productId,
            String productHubId,
            Integer quantity,
            Boolean requiresHubDelivery,
            String hubDeliveryId,
            String lastMileDeliveryId,
            String receiverName,
            String receiverPhone,
            String receiverEmail,
            String deliveryAddress,
            String deliveryAddressDetail,
            String deliveryPostalCode,
            String deliveryNote,
            LocalDate requestedDeliveryDate,
            LocalTime requestedDeliveryTime,
            String specialInstructions,
            LocalDateTime calculatedDepartureDeadline,
            LocalDateTime estimatedDeliveryTime,
            String routeInfo,
            OrderStatus status,
            BigDecimal unitPrice,
            BigDecimal totalAmount,
            String paymentId,
            String pgProvider,
            String pgPaymentId,
            String pgPaymentKey,
            LocalDateTime actualDepartureTime,
            LocalDateTime hubArrivalTime,
            LocalDateTime finalDeliveryStartTime,
            LocalDateTime actualDeliveryTime,
            String signature,
            String actualReceiverName,
            String cancelReason,
            LocalDateTime cancelledAt) {

        this.id = id;
        this.orderNumber = orderNumber;
        this.supplierCompanyId = supplierCompanyId;
        this.supplierHubId = supplierHubId;
        this.receiverCompanyId = receiverCompanyId;
        this.receiverHubId = receiverHubId;
        this.destinationHubId = destinationHubId;
        this.productId = productId;
        this.productHubId = productHubId;
        this.quantity = quantity;
        this.requiresHubDelivery = requiresHubDelivery;
        this.hubDeliveryId = hubDeliveryId;
        this.lastMileDeliveryId = lastMileDeliveryId;
        this.receiverName = receiverName;
        this.receiverPhone = receiverPhone;
        this.receiverEmail = receiverEmail;
        this.deliveryAddress = deliveryAddress;
        this.deliveryAddressDetail = deliveryAddressDetail;
        this.deliveryPostalCode = deliveryPostalCode;
        this.deliveryNote = deliveryNote;
        this.requestedDeliveryDate = requestedDeliveryDate;
        this.requestedDeliveryTime = requestedDeliveryTime;
        this.specialInstructions = specialInstructions;
        this.calculatedDepartureDeadline = calculatedDepartureDeadline;
        this.estimatedDeliveryTime = estimatedDeliveryTime;
        this.routeInfo = routeInfo;
        this.status = status;
        this.unitPrice = unitPrice;
        this.totalAmount = totalAmount;
        this.paymentId = paymentId;
        this.pgProvider = pgProvider;
        this.pgPaymentId = pgPaymentId;
        this.pgPaymentKey = pgPaymentKey;
        this.actualDepartureTime = actualDepartureTime;
        this.hubArrivalTime = hubArrivalTime;
        this.finalDeliveryStartTime = finalDeliveryStartTime;
        this.actualDeliveryTime = actualDeliveryTime;
        this.signature = signature;
        this.actualReceiverName = actualReceiverName;
        this.cancelReason = cancelReason;
        this.cancelledAt = cancelledAt;
    }

    /**
     * 도메인 모델로부터 엔티티 생성
     */
    public static OrderEntity fromDomain(Order order) {
        return OrderEntity.builder()
                .id(UuidUtils.generate())
                .orderNumber(order.getOrderNumberValue())
                .supplierCompanyId(order.getCompanyInfo().getSupplierCompanyId())
                .supplierHubId(order.getCompanyInfo().getSupplierHubId())
                .receiverCompanyId(order.getCompanyInfo().getReceiverCompanyId())
                .receiverHubId(order.getCompanyInfo().getReceiverHubId())
                .destinationHubId(order.getDestinationHubId())
                .productId(order.getProductInfo().getProductId())
                .productHubId(order.getProductInfo().getProductHubId())
                .quantity(order.getProductInfo().getQuantity())
                .requiresHubDelivery(order.getDeliveryInfo().getRequiresHubDelivery())
                .hubDeliveryId(order.getDeliveryInfo().getHubDeliveryId())
                .lastMileDeliveryId(order.getDeliveryInfo().getLastMileDeliveryId())
                .receiverName(order.getReceiverInfo().getReceiverName())
                .receiverPhone(order.getReceiverInfo().getReceiverPhone())
                .receiverEmail(order.getReceiverInfo().getReceiverEmail())
                .deliveryAddress(order.getReceiverInfo().getDeliveryAddress())
                .deliveryAddressDetail(order.getReceiverInfo().getDeliveryAddressDetail())
                .deliveryPostalCode(order.getReceiverInfo().getDeliveryPostalCode())
                .deliveryNote(order.getReceiverInfo().getDeliveryNote())
                .requestedDeliveryDate(order.getRequestInfo().getRequestedDeliveryDate())
                .requestedDeliveryTime(order.getRequestInfo().getRequestedDeliveryTime())
                .specialInstructions(order.getRequestInfo().getSpecialInstructions())
                .calculatedDepartureDeadline(order.getAiCalculationResult().getCalculatedDepartureDeadline())
                .estimatedDeliveryTime(order.getAiCalculationResult().getEstimatedDeliveryTime())
                .routeInfo(order.getAiCalculationResult().getRouteInfo())
                .status(order.getStatus())
                .unitPrice(order.getAmountInfo().getUnitPrice())
                .totalAmount(order.getAmountInfo().getTotalAmount())
                .paymentId(order.getAmountInfo().getPaymentId())
                .pgProvider(order.getPgPaymentInfo().getPgProvider())
                .pgPaymentId(order.getPgPaymentInfo().getPgPaymentId())
                .pgPaymentKey(order.getPgPaymentInfo().getPgPaymentKey())
                .actualDepartureTime(order.getDeliveryProgressInfo().getActualDepartureTime())
                .hubArrivalTime(order.getDeliveryProgressInfo().getHubArrivalTime())
                .finalDeliveryStartTime(order.getDeliveryProgressInfo().getFinalDeliveryStartTime())
                .actualDeliveryTime(order.getDeliveryProgressInfo().getActualDeliveryTime())
                .signature(order.getDeliveryProgressInfo().getSignature())
                .actualReceiverName(order.getDeliveryProgressInfo().getActualReceiverName())
                .cancelReason(order.getCancelReason())
                .cancelledAt(order.getCancelledAt())
                .build();
    }

    /**
     * 엔티티를 도메인 모델로 변환
     */
    public Order toDomain() {
        return Order.builder()
                .id(OrderId.from(this.id))
                .orderNumber(OrderNumber.from(this.orderNumber))
                .companyInfo(CompanyInfo.of(
                        this.supplierCompanyId,
                        this.supplierHubId,
                        this.receiverCompanyId,
                        this.receiverHubId
                ))
                .destinationHubId(this.destinationHubId)
                .productInfo(ProductInfo.builder()
                        .productId(this.productId)
                        .productHubId(this.productHubId)
                        .quantity(this.quantity)
                        .build())
                .deliveryInfo(DeliveryInfo.builder()
                        .requiresHubDelivery(this.requiresHubDelivery)
                        .hubDeliveryId(this.hubDeliveryId)
                        .lastMileDeliveryId(this.lastMileDeliveryId)
                        .build())
                .receiverInfo(ReceiverInfo.of(
                        this.receiverName,
                        this.receiverPhone,
                        this.receiverEmail,
                        this.deliveryAddress,
                        this.deliveryAddressDetail,
                        this.deliveryPostalCode,
                        this.deliveryNote
                ))
                .requestInfo(RequestInfo.of(
                        this.requestedDeliveryDate,
                        this.requestedDeliveryTime,
                        this.specialInstructions
                ))
                .aiCalculationResult(AiCalculationResult.of(
                        this.calculatedDepartureDeadline,
                        this.estimatedDeliveryTime,
                        this.routeInfo
                ))
                .status(this.status)
                .amountInfo(AmountInfo.builder()
                        .unitPrice(this.unitPrice)
                        .totalAmount(this.totalAmount)
                        .paymentId(this.paymentId)
                        .build())
                .pgPaymentInfo(PgPaymentInfo.builder()
                        .pgProvider(this.pgProvider)
                        .pgPaymentId(this.pgPaymentId)
                        .pgPaymentKey(this.pgPaymentKey)
                        .build())
                .deliveryProgressInfo(DeliveryProgressInfo.builder()
                        .actualDepartureTime(this.actualDepartureTime)
                        .hubArrivalTime(this.hubArrivalTime)
                        .finalDeliveryStartTime(this.finalDeliveryStartTime)
                        .actualDeliveryTime(this.actualDeliveryTime)
                        .signature(this.signature)
                        .actualReceiverName(this.actualReceiverName)
                        .build())
                .createdBy(this.getCreatedBy())
                .createdAt(this.getCreatedAt())
                .cancelReason(this.cancelReason)
                .cancelledAt(this.cancelledAt)
                .build();
    }

    /**
     * 도메인 모델로 엔티티 업데이트
     * Order 도메인의 모든 상태를 엔티티에 반영
     */
    public void updateFromDomain(Order order) {
        // ===== ID 및 주문 번호 =====
        this.id = order.getId().getValue();
        this.orderNumber = order.getOrderNumberValue();

        // ===== 업체 정보 =====
        this.supplierCompanyId = order.getCompanyInfo().getSupplierCompanyId();
        this.supplierHubId = order.getCompanyInfo().getSupplierHubId();
        this.receiverCompanyId = order.getCompanyInfo().getReceiverCompanyId();
        this.receiverHubId = order.getCompanyInfo().getReceiverHubId();

        // ===== 도착 허브 ID =====
        this.destinationHubId = order.getDestinationHubId();

        // ===== 상품 정보 =====
        this.productId = order.getProductInfo().getProductId();
        this.productHubId = order.getProductInfo().getProductHubId();
        this.quantity = order.getProductInfo().getQuantity();

        // ===== 배송 정보 =====
        this.requiresHubDelivery = order.getDeliveryInfo().getRequiresHubDelivery();
        this.hubDeliveryId = order.getDeliveryInfo().getHubDeliveryId();
        this.lastMileDeliveryId = order.getDeliveryInfo().getLastMileDeliveryId();

        // ===== 수령자 정보 =====
        this.receiverName = order.getReceiverInfo().getReceiverName();
        this.receiverPhone = order.getReceiverInfo().getReceiverPhone();
        this.receiverEmail = order.getReceiverInfo().getReceiverEmail();
        this.deliveryAddress = order.getReceiverInfo().getDeliveryAddress();
        this.deliveryAddressDetail = order.getReceiverInfo().getDeliveryAddressDetail();
        this.deliveryPostalCode = order.getReceiverInfo().getDeliveryPostalCode();
        this.deliveryNote = order.getReceiverInfo().getDeliveryNote();

        // ===== 요청사항 =====
        this.requestedDeliveryDate = order.getRequestInfo().getRequestedDeliveryDate();
        this.requestedDeliveryTime = order.getRequestInfo().getRequestedDeliveryTime();
        this.specialInstructions = order.getRequestInfo().getSpecialInstructions();

        // ===== AI 계산 결과 =====
        this.calculatedDepartureDeadline = order.getAiCalculationResult().getCalculatedDepartureDeadline();
        this.estimatedDeliveryTime = order.getAiCalculationResult().getEstimatedDeliveryTime();
        this.routeInfo = order.getAiCalculationResult().getRouteInfo();

        // ===== 상태 =====
        this.status = order.getStatus();

        // ===== 금액 정보 =====
        this.unitPrice = order.getAmountInfo().getUnitPrice();
        this.totalAmount = order.getAmountInfo().getTotalAmount();
        this.paymentId = order.getAmountInfo().getPaymentId();

        // ===== PG 결제 정보 =====
        this.pgProvider = order.getPgPaymentInfo().getPgProvider();
        this.pgPaymentId = order.getPgPaymentInfo().getPgPaymentId();
        this.pgPaymentKey = order.getPgPaymentInfo().getPgPaymentKey();

        // ===== 배송 진행 정보 =====
        this.actualDepartureTime = order.getDeliveryProgressInfo().getActualDepartureTime();
        this.hubArrivalTime = order.getDeliveryProgressInfo().getHubArrivalTime();
        this.finalDeliveryStartTime = order.getDeliveryProgressInfo().getFinalDeliveryStartTime();
        this.actualDeliveryTime = order.getDeliveryProgressInfo().getActualDeliveryTime();
        this.signature = order.getDeliveryProgressInfo().getSignature();
        this.actualReceiverName = order.getDeliveryProgressInfo().getActualReceiverName();

        // ===== 취소 정보 =====
        this.cancelReason = order.getCancelReason();
        this.cancelledAt = order.getCancelledAt();
    }
}
package com.early_express.order_service.domain.order.application.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

/**
 * 주문 생성 Command DTO
 */
@Getter
@Builder
public class OrderCreateCommand {

    // ===== 업체 정보 =====
    private String supplierCompanyId;
    private String supplierHubId;
    private String receiverCompanyId;
    private String receiverHubId;

    // ===== 상품 정보 =====
    private String productId;
    private Integer quantity;
    private BigDecimal unitPrice;

    // ===== 수령자 정보 =====
    private String receiverName;
    private String receiverPhone;
    private String receiverEmail;
    private String deliveryAddress;
    private String deliveryAddressDetail;
    private String deliveryPostalCode;
    private String deliveryNote;

    // ===== 요청사항 =====
    private LocalDate requestedDeliveryDate;
    private LocalTime requestedDeliveryTime;
    private String specialInstructions;

    // ===== PG 결제 정보 =====
    private String pgProvider;
    private String pgPaymentId;
    private String pgPaymentKey;

    // ===== 생성자 =====
    private String createdBy;
}

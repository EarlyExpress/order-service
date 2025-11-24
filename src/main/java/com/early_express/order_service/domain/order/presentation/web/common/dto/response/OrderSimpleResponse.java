package com.early_express.order_service.domain.order.presentation.web.common.dto.response;

import com.early_express.order_service.domain.order.domain.model.Order;
import com.early_express.order_service.domain.order.domain.model.OrderStatus;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 주문 간단 응답 DTO (목록 조회용)
 * 모든 Controller에서 공통으로 사용
 */
@Getter
@Builder
public class OrderSimpleResponse {

    private String orderId;
    private String orderNumber;
    private OrderStatus status;
    private String statusDescription;

    // 업체 정보
    private String supplierCompanyId;
    private String receiverCompanyId;

    // 상품 정보
    private String productId;
    private Integer quantity;

    // 금액 정보
    private BigDecimal totalAmount;

    // 배송 정보
    private String receiverName;
    private LocalDateTime requestedDeliveryDate;

    // 시간 정보
    private LocalDateTime createdAt;

    /**
     * Domain → DTO 변환
     */
    public static OrderSimpleResponse from(Order order) {
        return OrderSimpleResponse.builder()
                .orderId(order.getIdValue())
                .orderNumber(order.getOrderNumberValue())
                .status(order.getStatus())
                .statusDescription(order.getStatus().getDescription())
                .supplierCompanyId(order.getCompanyInfo().getSupplierCompanyId())
                .receiverCompanyId(order.getCompanyInfo().getReceiverCompanyId())
                .productId(order.getProductInfo().getProductId())
                .quantity(order.getProductInfo().getQuantity())
                .totalAmount(order.getAmountInfo().getTotalAmount())
                .receiverName(order.getReceiverInfo().getReceiverName())
                .requestedDeliveryDate(order.getRequestInfo().getRequestedDeliveryDate().atTime(
                        order.getRequestInfo().getRequestedDeliveryTime()
                ))
                .createdAt(order.getCreatedAt())
                .build();
    }
}
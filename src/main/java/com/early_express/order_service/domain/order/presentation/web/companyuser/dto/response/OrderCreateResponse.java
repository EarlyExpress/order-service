package com.early_express.order_service.domain.order.presentation.web.companyuser.dto.response;

import com.early_express.order_service.domain.order.domain.model.Order;
import com.early_express.order_service.domain.order.domain.model.OrderStatus;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 주문 생성 응답 DTO (Company User)
 * 주문 생성 직후 반환되는 간단한 정보
 */
@Getter
@Builder
public class OrderCreateResponse {

    private String orderId;
    private String orderNumber;
    private OrderStatus status;
    private String statusDescription;

    // 금액 정보
    private BigDecimal totalAmount;

    // 배송 예정 정보
    private LocalDateTime requestedDeliveryDate;

    // 생성 시간
    private LocalDateTime createdAt;

    // 메시지
    private String message;

    /**
     * Domain → DTO 변환
     */
    public static OrderCreateResponse from(Order order) {
        return OrderCreateResponse.builder()
                .orderId(order.getIdValue())
                .orderNumber(order.getOrderNumberValue())
                .status(order.getStatus())
                .statusDescription(order.getStatus().getDescription())
                .totalAmount(order.getAmountInfo().getTotalAmount())
                .requestedDeliveryDate(order.getRequestInfo().getRequestedDeliveryDate().atTime(
                        order.getRequestInfo().getRequestedDeliveryTime()
                ))
                .createdAt(order.getCreatedAt())
                .message("주문이 접수되었습니다. 재고 확인 중입니다.")
                .build();
    }
}
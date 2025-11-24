package com.early_express.order_service.domain.order.presentation.web.hubmanager.dto.request;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * 주문 수정 요청 DTO (Hub Manager)
 * - 허브 관리자는 배송 일정 조정 가능
 */
@Getter
@Builder
public class OrderUpdateRequest {

    @NotNull(message = "납품 희망 일자는 필수입니다.")
    @Future(message = "납품 희망 일자는 미래 날짜여야 합니다.")
    private LocalDate requestedDeliveryDate;

    @NotNull(message = "납품 희망 시간은 필수입니다.")
    private LocalTime requestedDeliveryTime;

    private String specialInstructions;

    private String updateReason;
}
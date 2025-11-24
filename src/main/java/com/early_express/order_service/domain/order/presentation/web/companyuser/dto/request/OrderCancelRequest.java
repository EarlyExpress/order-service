package com.early_express.order_service.domain.order.presentation.web.companyuser.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Getter;

/**
 * 주문 취소 요청 DTO (Company User)
 */
@Getter
@Builder
public class OrderCancelRequest {

    @NotBlank(message = "취소 사유는 필수입니다.")
    private String cancelReason;
}

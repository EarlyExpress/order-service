package com.early_express.order_service.domain.order.presentation.web.common.dto.response;

import com.early_express.order_service.domain.order.domain.model.vo.AmountInfo;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

/**
 * 금액 정보 DTO
 */
@Getter
@Builder
public class AmountInfoDto {

    private BigDecimal unitPrice;
    private BigDecimal totalAmount;
    private String paymentId;

    public static AmountInfoDto from(AmountInfo amountInfo) {
        return AmountInfoDto.builder()
                .unitPrice(amountInfo.getUnitPrice())
                .totalAmount(amountInfo.getTotalAmount())
                .paymentId(amountInfo.getPaymentId())
                .build();
    }
}
package com.early_express.order_service.domain.order.presentation.web.common.dto.response;

import com.early_express.order_service.domain.order.domain.model.vo.RequestInfo;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * 요청사항 정보 DTO
 */
@Getter
@Builder
public class RequestInfoDto {

    private LocalDate requestedDeliveryDate;
    private LocalTime requestedDeliveryTime;
    private String specialInstructions;

    public static RequestInfoDto from(RequestInfo requestInfo) {
        return RequestInfoDto.builder()
                .requestedDeliveryDate(requestInfo.getRequestedDeliveryDate())
                .requestedDeliveryTime(requestInfo.getRequestedDeliveryTime())
                .specialInstructions(requestInfo.getSpecialInstructions())
                .build();
    }
}
package com.early_express.order_service.domain.order.presentation.web.common.dto.response;

import com.early_express.order_service.domain.order.domain.model.vo.DeliveryProgressInfo;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 배송 진행 정보 DTO
 */
@Getter
@Builder
public class DeliveryProgressInfoDto {

    private LocalDateTime actualDepartureTime;
    private LocalDateTime hubArrivalTime;
    private LocalDateTime finalDeliveryStartTime;
    private LocalDateTime actualDeliveryTime;
    private String signature;
    private String actualReceiverName;

    public static DeliveryProgressInfoDto from(DeliveryProgressInfo progressInfo) {
        return DeliveryProgressInfoDto.builder()
                .actualDepartureTime(progressInfo.getActualDepartureTime())
                .hubArrivalTime(progressInfo.getHubArrivalTime())
                .finalDeliveryStartTime(progressInfo.getFinalDeliveryStartTime())
                .actualDeliveryTime(progressInfo.getActualDeliveryTime())
                .signature(progressInfo.getSignature())
                .actualReceiverName(progressInfo.getActualReceiverName())
                .build();
    }
}
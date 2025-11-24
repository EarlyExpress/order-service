package com.early_express.order_service.domain.order.presentation.web.common.dto.response;

import com.early_express.order_service.domain.order.domain.model.vo.ReceiverInfo;
import lombok.Builder;
import lombok.Getter;

/**
 * 수령자 정보 DTO
 */
@Getter
@Builder
public class ReceiverInfoDto {

    private String receiverName;
    private String receiverPhone;
    private String receiverEmail;
    private String deliveryAddress;
    private String deliveryAddressDetail;
    private String deliveryPostalCode;
    private String deliveryNote;

    public static ReceiverInfoDto from(ReceiverInfo receiverInfo) {
        return ReceiverInfoDto.builder()
                .receiverName(receiverInfo.getReceiverName())
                .receiverPhone(receiverInfo.getReceiverPhone())
                .receiverEmail(receiverInfo.getReceiverEmail())
                .deliveryAddress(receiverInfo.getDeliveryAddress())
                .deliveryAddressDetail(receiverInfo.getDeliveryAddressDetail())
                .deliveryPostalCode(receiverInfo.getDeliveryPostalCode())
                .deliveryNote(receiverInfo.getDeliveryNote())
                .build();
    }
}
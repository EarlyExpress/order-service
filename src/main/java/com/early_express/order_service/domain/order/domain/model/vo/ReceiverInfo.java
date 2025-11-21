package com.early_express.order_service.domain.order.domain.model.vo;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

/**
 * 수령자 정보 Value Object
 * 수령자 이름, 연락처, 배송 주소 정보를 담음
 */
@Getter
@EqualsAndHashCode
public class ReceiverInfo {

    private final String receiverName;
    private final String receiverPhone;
    private final String receiverEmail; // nullable
    private final String deliveryAddress;
    private final String deliveryAddressDetail; // nullable
    private final String deliveryPostalCode; // nullable
    private final String deliveryNote; // nullable

    @Builder
    private ReceiverInfo(
            String receiverName,
            String receiverPhone,
            String receiverEmail,
            String deliveryAddress,
            String deliveryAddressDetail,
            String deliveryPostalCode,
            String deliveryNote) {

        validateNotNull(receiverName, "수령자 이름");
        validatePhone(receiverPhone);
        validateNotNull(deliveryAddress, "배송 주소");

        this.receiverName = receiverName;
        this.receiverPhone = receiverPhone;
        this.receiverEmail = receiverEmail;
        this.deliveryAddress = deliveryAddress;
        this.deliveryAddressDetail = deliveryAddressDetail;
        this.deliveryPostalCode = deliveryPostalCode;
        this.deliveryNote = deliveryNote;
    }

    public static ReceiverInfo of(
            String receiverName,
            String receiverPhone,
            String receiverEmail,
            String deliveryAddress,
            String deliveryAddressDetail,
            String deliveryPostalCode,
            String deliveryNote) {

        return ReceiverInfo.builder()
                .receiverName(receiverName)
                .receiverPhone(receiverPhone)
                .receiverEmail(receiverEmail)
                .deliveryAddress(deliveryAddress)
                .deliveryAddressDetail(deliveryAddressDetail)
                .deliveryPostalCode(deliveryPostalCode)
                .deliveryNote(deliveryNote)
                .build();
    }

    /**
     * 전체 배송 주소 반환 (주소 + 상세주소)
     */
    public String getFullAddress() {
        if (deliveryAddressDetail != null && !deliveryAddressDetail.trim().isEmpty()) {
            return deliveryAddress + " " + deliveryAddressDetail;
        }
        return deliveryAddress;
    }

    private void validateNotNull(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(fieldName + "는 null이거나 빈 값일 수 없습니다.");
        }
    }

    private void validatePhone(String phone) {
        if (phone == null || phone.trim().isEmpty()) {
            throw new IllegalArgumentException("수령자 연락처는 null이거나 빈 값일 수 없습니다.");
        }

        // 기본적인 전화번호 형식 검증 (010-XXXX-XXXX 등)
        String phonePattern = "^\\d{2,3}-\\d{3,4}-\\d{4}$";
        if (!phone.matches(phonePattern)) {
            throw new IllegalArgumentException("올바른 전화번호 형식이 아닙니다. (예: 010-1234-5678)");
        }
    }
}

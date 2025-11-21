package com.early_express.order_service.domain.order.domain.model.vo;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

@DisplayName("ReceiverInfo vo 테스트")
class ReceiverInfoTest {

    @Test
    @DisplayName("수령자 정보를 생성할 수 있다")
    void createReceiverInfo() {
        // given
        String receiverName = "홍길동";
        String receiverPhone = "010-1234-5678";
        String receiverEmail = "hong@example.com";
        String deliveryAddress = "서울시 강남구";
        String deliveryAddressDetail = "101호";
        String deliveryPostalCode = "12345";
        String deliveryNote = "문 앞에 놓아주세요";

        // when
        ReceiverInfo receiverInfo = ReceiverInfo.of(
                receiverName,
                receiverPhone,
                receiverEmail,
                deliveryAddress,
                deliveryAddressDetail,
                deliveryPostalCode,
                deliveryNote
        );

        // then
        assertThat(receiverInfo.getReceiverName()).isEqualTo(receiverName);
        assertThat(receiverInfo.getReceiverPhone()).isEqualTo(receiverPhone);
        assertThat(receiverInfo.getReceiverEmail()).isEqualTo(receiverEmail);
        assertThat(receiverInfo.getDeliveryAddress()).isEqualTo(deliveryAddress);
        assertThat(receiverInfo.getDeliveryAddressDetail()).isEqualTo(deliveryAddressDetail);
        assertThat(receiverInfo.getDeliveryPostalCode()).isEqualTo(deliveryPostalCode);
        assertThat(receiverInfo.getDeliveryNote()).isEqualTo(deliveryNote);
    }

    @Test
    @DisplayName("전체 배송 주소를 조회할 수 있다")
    void getFullAddress() {
        // given
        ReceiverInfo receiverInfo = ReceiverInfo.of(
                "홍길동",
                "010-1234-5678",
                null,
                "서울시 강남구",
                "101호",
                null,
                null
        );

        // when
        String fullAddress = receiverInfo.getFullAddress();

        // then
        assertThat(fullAddress).isEqualTo("서울시 강남구 101호");
    }

    @Test
    @DisplayName("상세주소가 없으면 기본 주소만 반환한다")
    void getFullAddressWithoutDetail() {
        // given
        ReceiverInfo receiverInfo = ReceiverInfo.of(
                "홍길동",
                "010-1234-5678",
                null,
                "서울시 강남구",
                null,
                null,
                null
        );

        // when
        String fullAddress = receiverInfo.getFullAddress();

        // then
        assertThat(fullAddress).isEqualTo("서울시 강남구");
    }

    @Test
    @DisplayName("수령자 이름이 null이면 예외가 발생한다")
    void validateReceiverNameNull() {
        // when & then
        assertThatThrownBy(() -> ReceiverInfo.of(
                null,
                "010-1234-5678",
                null,
                "서울시 강남구",
                null,
                null,
                null
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("null");
    }

    @Test
    @DisplayName("수령자 연락처가 null이면 예외가 발생한다")
    void validateReceiverPhoneNull() {
        // when & then
        assertThatThrownBy(() -> ReceiverInfo.of(
                "홍길동",
                null,
                null,
                "서울시 강남구",
                null,
                null,
                null
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("수령자 연락처는 null이거나 빈 값일 수 없습니다");
    }

    @Test
    @DisplayName("잘못된 전화번호 형식이면 예외가 발생한다")
    void validatePhoneFormat() {
        // when & then
        assertThatThrownBy(() -> ReceiverInfo.of(
                "홍길동",
                "01012345678", // 하이픈 없음
                null,
                "서울시 강남구",
                null,
                null,
                null
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("올바른 전화번호 형식이 아닙니다");
    }

    @Test
    @DisplayName("배송 주소가 null이면 예외가 발생한다")
    void validateDeliveryAddressNull() {
        // when & then
        assertThatThrownBy(() -> ReceiverInfo.of(
                "홍길동",
                "010-1234-5678",
                null,
                null,
                null,
                null,
                null
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("배송 주소는 null이거나 빈 값일 수 없습니다");
    }

    @Test
    @DisplayName("같은 값을 가진 ReceiverInfo는 동일하다")
    void equalReceiverInfos() {
        // given
        ReceiverInfo receiverInfo1 = ReceiverInfo.of(
                "홍길동", "010-1234-5678", null, "서울시 강남구", null, null, null
        );
        ReceiverInfo receiverInfo2 = ReceiverInfo.of(
                "홍길동", "010-1234-5678", null, "서울시 강남구", null, null, null
        );

        // when & then
        assertThat(receiverInfo1).isEqualTo(receiverInfo2);
        assertThat(receiverInfo1.hashCode()).isEqualTo(receiverInfo2.hashCode());
    }
}
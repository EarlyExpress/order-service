package com.early_express.order_service.domain.order.domain.model.vo;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

@DisplayName("DeliveryInfo vo 테스트")
class DeliveryInfoTest {

    @Test
    @DisplayName("초기 배송 정보를 생성할 수 있다")
    void createInitialDeliveryInfo() {
        // when
        DeliveryInfo deliveryInfo = DeliveryInfo.initial();

        // then
        assertThat(deliveryInfo.needsHubDelivery()).isFalse();
        assertThat(deliveryInfo.hasHubDeliveryId()).isFalse();
        assertThat(deliveryInfo.hasLastMileDeliveryId()).isFalse();
    }

    @Test
    @DisplayName("허브 배송 필요 여부를 설정할 수 있다")
    void setRequiresHubDelivery() {
        // given
        DeliveryInfo deliveryInfo = DeliveryInfo.initial();

        // when
        DeliveryInfo updated = deliveryInfo.withRequiresHubDelivery(true);

        // then
        assertThat(updated.needsHubDelivery()).isTrue();
    }

    @Test
    @DisplayName("허브 배송 ID를 설정할 수 있다")
    void setHubDeliveryId() {
        // given
        DeliveryInfo deliveryInfo = DeliveryInfo.initial();
        String hubDeliveryId = "HUB-DELIVERY-001";

        // when
        DeliveryInfo updated = deliveryInfo.withHubDeliveryId(hubDeliveryId);

        // then
        assertThat(updated.hasHubDeliveryId()).isTrue();
        assertThat(updated.getHubDeliveryId()).isEqualTo(hubDeliveryId);
    }

    @Test
    @DisplayName("업체 배송 ID를 설정할 수 있다")
    void setLastMileDeliveryId() {
        // given
        DeliveryInfo deliveryInfo = DeliveryInfo.initial();
        String lastMileDeliveryId = "LAST-MILE-001";

        // when
        DeliveryInfo updated = deliveryInfo.withLastMileDeliveryId(lastMileDeliveryId);

        // then
        assertThat(updated.hasLastMileDeliveryId()).isTrue();
        assertThat(updated.getLastMileDeliveryId()).isEqualTo(lastMileDeliveryId);
    }

    @Test
    @DisplayName("여러 정보를 순차적으로 설정할 수 있다")
    void setMultipleFields() {
        // given
        DeliveryInfo deliveryInfo = DeliveryInfo.initial();

        // when
        DeliveryInfo updated = deliveryInfo
                .withRequiresHubDelivery(true)
                .withHubDeliveryId("HUB-DELIVERY-001")
                .withLastMileDeliveryId("LAST-MILE-001");

        // then
        assertThat(updated.needsHubDelivery()).isTrue();
        assertThat(updated.hasHubDeliveryId()).isTrue();
        assertThat(updated.hasLastMileDeliveryId()).isTrue();
    }

    @Test
    @DisplayName("requiresHubDelivery가 null이면 false로 처리된다")
    void handleNullRequiresHubDelivery() {
        // when
        DeliveryInfo deliveryInfo = DeliveryInfo.builder()
                .requiresHubDelivery(null)
                .build();

        // then
        assertThat(deliveryInfo.needsHubDelivery()).isFalse();
    }

    @Test
    @DisplayName("같은 값을 가진 DeliveryInfo는 동일하다")
    void equalDeliveryInfos() {
        // given
        DeliveryInfo deliveryInfo1 = DeliveryInfo.initial()
                .withRequiresHubDelivery(true)
                .withHubDeliveryId("HUB-001");
        DeliveryInfo deliveryInfo2 = DeliveryInfo.initial()
                .withRequiresHubDelivery(true)
                .withHubDeliveryId("HUB-001");

        // when & then
        assertThat(deliveryInfo1).isEqualTo(deliveryInfo2);
        assertThat(deliveryInfo1.hashCode()).isEqualTo(deliveryInfo2.hashCode());
    }
}
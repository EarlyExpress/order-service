package com.early_express.order_service.domain.order.domain.model.vo;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.*;

@DisplayName("DeliveryProgressInfo vo 테스트")
class DeliveryProgressInfoTest {

    @Test
    @DisplayName("빈 배송 진행 정보를 생성할 수 있다")
    void createEmptyDeliveryProgressInfo() {
        // when
        DeliveryProgressInfo progressInfo = DeliveryProgressInfo.empty();

        // then
        assertThat(progressInfo.isDeparted()).isFalse();
        assertThat(progressInfo.isArrivedAtHub()).isFalse();
        assertThat(progressInfo.isFinalDeliveryStarted()).isFalse();
        assertThat(progressInfo.isCompleted()).isFalse();
    }

    @Test
    @DisplayName("실제 발송 시간을 기록할 수 있다")
    void recordActualDepartureTime() {
        // given
        DeliveryProgressInfo progressInfo = DeliveryProgressInfo.empty();
        LocalDateTime departureTime = LocalDateTime.now();

        // when
        DeliveryProgressInfo updated = progressInfo.withActualDepartureTime(departureTime);

        // then
        assertThat(updated.isDeparted()).isTrue();
        assertThat(updated.getActualDepartureTime()).isEqualTo(departureTime);
    }

    @Test
    @DisplayName("허브 도착 시간을 기록할 수 있다")
    void recordHubArrivalTime() {
        // given
        DeliveryProgressInfo progressInfo = DeliveryProgressInfo.empty();
        LocalDateTime arrivalTime = LocalDateTime.now();

        // when
        DeliveryProgressInfo updated = progressInfo.withHubArrivalTime(arrivalTime);

        // then
        assertThat(updated.isArrivedAtHub()).isTrue();
        assertThat(updated.getHubArrivalTime()).isEqualTo(arrivalTime);
    }

    @Test
    @DisplayName("최종 배송 시작 시간을 기록할 수 있다")
    void recordFinalDeliveryStartTime() {
        // given
        DeliveryProgressInfo progressInfo = DeliveryProgressInfo.empty();
        LocalDateTime startTime = LocalDateTime.now();

        // when
        DeliveryProgressInfo updated = progressInfo.withFinalDeliveryStartTime(startTime);

        // then
        assertThat(updated.isFinalDeliveryStarted()).isTrue();
        assertThat(updated.getFinalDeliveryStartTime()).isEqualTo(startTime);
    }

    @Test
    @DisplayName("배송 완료 정보를 기록할 수 있다")
    void recordDeliveryCompletion() {
        // given
        DeliveryProgressInfo progressInfo = DeliveryProgressInfo.empty();
        LocalDateTime completionTime = LocalDateTime.now();
        String signature = "base64SignatureData";
        String actualReceiverName = "홍길동";

        // when
        DeliveryProgressInfo updated = progressInfo.withDeliveryCompleted(
                completionTime,
                signature,
                actualReceiverName
        );

        // then
        assertThat(updated.isCompleted()).isTrue();
        assertThat(updated.getActualDeliveryTime()).isEqualTo(completionTime);
        assertThat(updated.getSignature()).isEqualTo(signature);
        assertThat(updated.getActualReceiverName()).isEqualTo(actualReceiverName);
    }

    @Test
    @DisplayName("배송 진행 정보를 순차적으로 기록할 수 있다")
    void recordProgressSequentially() {
        // given
        DeliveryProgressInfo progressInfo = DeliveryProgressInfo.empty();
        LocalDateTime departureTime = LocalDateTime.now();
        LocalDateTime arrivalTime = departureTime.plusHours(2);
        LocalDateTime startTime = arrivalTime.plusHours(1);
        LocalDateTime completionTime = startTime.plusHours(1);

        // when
        DeliveryProgressInfo updated = progressInfo
                .withActualDepartureTime(departureTime)
                .withHubArrivalTime(arrivalTime)
                .withFinalDeliveryStartTime(startTime)
                .withDeliveryCompleted(completionTime, "signature", "홍길동");

        // then
        assertThat(updated.isDeparted()).isTrue();
        assertThat(updated.isArrivedAtHub()).isTrue();
        assertThat(updated.isFinalDeliveryStarted()).isTrue();
        assertThat(updated.isCompleted()).isTrue();
    }

    @Test
    @DisplayName("서명이 없으면 배송 완료로 간주되지 않는다")
    void notCompletedWithoutSignature() {
        // given
        DeliveryProgressInfo progressInfo = DeliveryProgressInfo.empty();
        LocalDateTime completionTime = LocalDateTime.now();

        // when
        DeliveryProgressInfo updated = progressInfo.withDeliveryCompleted(
                completionTime,
                null, // 서명 없음
                "홍길동"
        );

        // then
        assertThat(updated.isCompleted()).isFalse();
    }

    @Test
    @DisplayName("실제 수령자가 없으면 배송 완료로 간주되지 않는다")
    void notCompletedWithoutReceiver() {
        // given
        DeliveryProgressInfo progressInfo = DeliveryProgressInfo.empty();
        LocalDateTime completionTime = LocalDateTime.now();

        // when
        DeliveryProgressInfo updated = progressInfo.withDeliveryCompleted(
                completionTime,
                "signature",
                null // 수령자 없음
        );

        // then
        assertThat(updated.isCompleted()).isFalse();
    }

    @Test
    @DisplayName("같은 값을 가진 DeliveryProgressInfo는 동일하다")
    void equalDeliveryProgressInfos() {
        // given
        LocalDateTime time = LocalDateTime.now();
        DeliveryProgressInfo progressInfo1 = DeliveryProgressInfo.empty()
                .withActualDepartureTime(time);
        DeliveryProgressInfo progressInfo2 = DeliveryProgressInfo.empty()
                .withActualDepartureTime(time);

        // when & then
        assertThat(progressInfo1).isEqualTo(progressInfo2);
        assertThat(progressInfo1.hashCode()).isEqualTo(progressInfo2.hashCode());
    }
}
package com.early_express.order_service.domain.order.domain.model.vo;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.assertj.core.api.Assertions.*;

@DisplayName("RequestInfo vo 테스트")
class RequestInfoTest {

    @Test
    @DisplayName("요청사항을 생성할 수 있다")
    void createRequestInfo() {
        // given
        LocalDate requestedDeliveryDate = LocalDate.now().plusDays(1);
        LocalTime requestedDeliveryTime = LocalTime.of(14, 0);
        String specialInstructions = "조심히 배송해주세요";

        // when
        RequestInfo requestInfo = RequestInfo.of(
                requestedDeliveryDate,
                requestedDeliveryTime,
                specialInstructions
        );

        // then
        assertThat(requestInfo.getRequestedDeliveryDate()).isEqualTo(requestedDeliveryDate);
        assertThat(requestInfo.getRequestedDeliveryTime()).isEqualTo(requestedDeliveryTime);
        assertThat(requestInfo.getSpecialInstructions()).isEqualTo(specialInstructions);
        assertThat(requestInfo.hasSpecialInstructions()).isTrue();
    }

    @Test
    @DisplayName("특별 요청사항이 없을 수 있다")
    void createRequestInfoWithoutSpecialInstructions() {
        // given
        LocalDate requestedDeliveryDate = LocalDate.now().plusDays(1);
        LocalTime requestedDeliveryTime = LocalTime.of(14, 0);

        // when
        RequestInfo requestInfo = RequestInfo.of(
                requestedDeliveryDate,
                requestedDeliveryTime,
                null
        );

        // then
        assertThat(requestInfo.hasSpecialInstructions()).isFalse();
    }

    @Test
    @DisplayName("납품 희망 일자가 null이면 예외가 발생한다")
    void validateRequestedDeliveryDateNull() {
        // when & then
        assertThatThrownBy(() -> RequestInfo.of(
                null,
                LocalTime.of(14, 0),
                null
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("납품 희망 일자는 null일 수 없습니다");
    }

    @Test
    @DisplayName("납품 희망 시간이 null이면 예외가 발생한다")
    void validateRequestedDeliveryTimeNull() {
        // when & then
        assertThatThrownBy(() -> RequestInfo.of(
                LocalDate.now().plusDays(1),
                null,
                null
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("null");
    }

    @Test
    @DisplayName("과거 날짜로 요청하면 예외가 발생한다")
    void validatePastDate() {
        // when & then
        assertThatThrownBy(() -> RequestInfo.of(
                LocalDate.now().minusDays(1),
                LocalTime.of(14, 0),
                null
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("납품 희망 일자는 과거일 수 없습니다");
    }

    @Test
    @DisplayName("당일 배송의 경우 과거 시간으로 요청하면 예외가 발생한다")
    void validatePastTimeForToday() {
        // given
        LocalDate today = LocalDate.now();
        LocalTime pastTime = LocalTime.now().minusHours(1);

        // when & then
        assertThatThrownBy(() -> RequestInfo.of(today, pastTime, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("당일 배송의 경우 현재 시간 이후여야 합니다");
    }

    @Test
    @DisplayName("미래 날짜의 경우 시간 제약이 없다")
    void createRequestInfoForFutureDate() {
        // given
        LocalDate futureDate = LocalDate.now().plusDays(1);
        LocalTime anyTime = LocalTime.of(9, 0);

        // when
        RequestInfo requestInfo = RequestInfo.of(futureDate, anyTime, null);

        // then
        assertThat(requestInfo).isNotNull();
        assertThat(requestInfo.getRequestedDeliveryDate()).isEqualTo(futureDate);
    }

    @Test
    @DisplayName("같은 값을 가진 RequestInfo는 동일하다")
    void equalRequestInfos() {
        // given
        LocalDate date = LocalDate.now().plusDays(1);
        LocalTime time = LocalTime.of(14, 0);
        RequestInfo requestInfo1 = RequestInfo.of(date, time, "테스트");
        RequestInfo requestInfo2 = RequestInfo.of(date, time, "테스트");

        // when & then
        assertThat(requestInfo1).isEqualTo(requestInfo2);
        assertThat(requestInfo1.hashCode()).isEqualTo(requestInfo2.hashCode());
    }
}
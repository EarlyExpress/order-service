package com.early_express.order_service.domain.order.domain.model.vo;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * 요청사항 Value Object
 * 납품 희망 일시 및 특별 요청사항을 담음
 */
@Getter
@EqualsAndHashCode
public class RequestInfo {

    private final LocalDate requestedDeliveryDate; // 납품 희망 일자
    private final LocalTime requestedDeliveryTime; // 납품 희망 시간
    private final String specialInstructions; // 특별 요청사항 (nullable)

    @Builder
    private RequestInfo(
            LocalDate requestedDeliveryDate,
            LocalTime requestedDeliveryTime,
            String specialInstructions) {

        validateNotNull(requestedDeliveryDate, "납품 희망 일자");
        validateNotNull(requestedDeliveryTime, "납품 희망 시간");
        validateDeliveryDateTime(requestedDeliveryDate, requestedDeliveryTime);

        this.requestedDeliveryDate = requestedDeliveryDate;
        this.requestedDeliveryTime = requestedDeliveryTime;
        this.specialInstructions = specialInstructions;
    }

    public static RequestInfo of(
            LocalDate requestedDeliveryDate,
            LocalTime requestedDeliveryTime,
            String specialInstructions) {

        return RequestInfo.builder()
                .requestedDeliveryDate(requestedDeliveryDate)
                .requestedDeliveryTime(requestedDeliveryTime)
                .specialInstructions(specialInstructions)
                .build();
    }

    /**
     * 특별 요청사항이 있는지 확인
     */
    public boolean hasSpecialInstructions() {
        return specialInstructions != null && !specialInstructions.trim().isEmpty();
    }

    private void validateNotNull(Object value, String fieldName) {
        if (value == null) {
            throw new IllegalArgumentException(fieldName + "는 null일 수 없습니다.");
        }
    }

    private void validateDeliveryDateTime(LocalDate date, LocalTime time) {
        // 과거 날짜 검증
        if (date.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("납품 희망 일자는 과거일 수 없습니다.");
        }

        // 당일 배송인 경우 현재 시간 이후여야 함
        if (date.isEqual(LocalDate.now()) && time.isBefore(LocalTime.now())) {
            throw new IllegalArgumentException("당일 배송의 경우 현재 시간 이후여야 합니다.");
        }
    }
}

package com.early_express.order_service.domain.order.domain.model.vo;

import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 주문 번호 Value Object
 * 형식: ORD-YYYYMMDD-XXX (예: ORD-20250115-001)
 */
@Getter
@EqualsAndHashCode
public class OrderNumber {

    private static final String PREFIX = "ORD-";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");

    private final String value;

    private OrderNumber(String value) {
        validateFormat(value);
        this.value = value;
    }

    /**
     * 새로운 주문 번호 생성
     * @param sequenceNumber 일련번호 (001, 002, ...)
     */
    public static OrderNumber generate(int sequenceNumber) {
        String datePart = LocalDateTime.now().format(DATE_FORMATTER);
        String sequencePart = String.format("%03d", sequenceNumber);
        return new OrderNumber(PREFIX + datePart + "-" + sequencePart);
    }

    /**
     * 기존 주문 번호로부터 생성
     */
    public static OrderNumber from(String value) {
        return new OrderNumber(value);
    }

    private void validateFormat(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("주문 번호는 null이거나 빈 값일 수 없습니다.");
        }

        if (!value.startsWith(PREFIX)) {
            throw new IllegalArgumentException("주문 번호는 'ORD-'로 시작해야 합니다.");
        }

        // 기본적인 형식 검증 (ORD-YYYYMMDD-XXX)
        String[] parts = value.split("-");
        if (parts.length != 3) {
            throw new IllegalArgumentException("주문 번호 형식이 올바르지 않습니다. (ORD-YYYYMMDD-XXX)");
        }
    }

    @Override
    public String toString() {
        return value;
    }
}

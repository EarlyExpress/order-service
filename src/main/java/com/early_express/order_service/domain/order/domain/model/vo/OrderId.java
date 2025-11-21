package com.early_express.order_service.domain.order.domain.model.vo;

import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.util.UUID;

/**
 * 주문 ID Value Object
 * 불변 객체로 주문의 고유 식별자를 표현
 */
@Getter
@EqualsAndHashCode
public class OrderId {

    private final String value;

    private OrderId(String value) {
        validateNotNull(value);
        this.value = value;
    }

    /**
     * 새로운 OrderId 생성
     */
    public static OrderId create() {
        return new OrderId(UUID.randomUUID().toString());
    }

    /**
     * 기존 ID로부터 OrderId 생성
     */
    public static OrderId from(String value) {
        return new OrderId(value);
    }

    private void validateNotNull(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("주문 ID는 null이거나 빈 값일 수 없습니다.");
        }
    }

    @Override
    public String toString() {
        return value;
    }
}
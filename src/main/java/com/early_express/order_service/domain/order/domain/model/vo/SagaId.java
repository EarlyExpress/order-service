package com.early_express.order_service.domain.order.domain.model.vo;

import com.early_express.order_service.global.common.utils.UuidUtils;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.util.UUID;

/**
 * Saga ID Value Object
 * 불변 객체로 Saga의 고유 식별자를 표현
 */
@Getter
@EqualsAndHashCode
public class SagaId {

    private final String value;

    private SagaId(String value) {
        validateNotNull(value);
        this.value = value;
    }

    /**
     * 새로운 SagaId 생성
     */
    public static SagaId create() {
        return new SagaId(UuidUtils.generate().toString());
    }

    /**
     * 기존 ID로부터 SagaId 생성
     */
    public static SagaId from(String value) {
        return new SagaId(value);
    }

    private void validateNotNull(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("Saga ID는 null이거나 빈 값일 수 없습니다.");
        }
    }

    @Override
    public String toString() {
        return value;
    }
}

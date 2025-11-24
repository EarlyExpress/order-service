package com.early_express.order_service.domain.order.domain.model.vo;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

@DisplayName("SagaId vo 테스트")
class SagaIdTest {

    @Test
    @DisplayName("새로운 SagaId를 생성할 수 있다")
    void createSagaId() {
        // when
        SagaId sagaId = SagaId.create();

        // then
        assertThat(sagaId).isNotNull();
        assertThat(sagaId.getValue()).isNotEmpty();
    }

    @Test
    @DisplayName("기존 ID로부터 SagaId를 생성할 수 있다")
    void createSagaIdFrom() {
        // given
        String id = "test-saga-id";

        // when
        SagaId sagaId = SagaId.from(id);

        // then
        assertThat(sagaId.getValue()).isEqualTo(id);
    }

    @Test
    @DisplayName("null 값으로 SagaId를 생성하면 예외가 발생한다")
    void validateSagaIdNull() {
        // when & then
        assertThatThrownBy(() -> SagaId.from(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Saga ID는 null이거나 빈 값일 수 없습니다");
    }

    @Test
    @DisplayName("빈 문자열로 SagaId를 생성하면 예외가 발생한다")
    void validateSagaIdEmpty() {
        // when & then
        assertThatThrownBy(() -> SagaId.from(""))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Saga ID는 null이거나 빈 값일 수 없습니다");
    }

    @Test
    @DisplayName("같은 값을 가진 SagaId는 동일하다")
    void equalSagaIds() {
        // given
        String id = "test-saga-id";
        SagaId sagaId1 = SagaId.from(id);
        SagaId sagaId2 = SagaId.from(id);

        // when & then
        assertThat(sagaId1).isEqualTo(sagaId2);
        assertThat(sagaId1.hashCode()).isEqualTo(sagaId2.hashCode());
    }

    @Test
    @DisplayName("toString은 값을 반환한다")
    void toStringReturnsValue() {
        // given
        String id = "test-saga-id";
        SagaId sagaId = SagaId.from(id);

        // when & then
        assertThat(sagaId.toString()).isEqualTo(id);
    }
}
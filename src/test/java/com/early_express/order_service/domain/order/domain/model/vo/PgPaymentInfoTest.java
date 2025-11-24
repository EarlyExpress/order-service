package com.early_express.order_service.domain.order.domain.model.vo;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

@DisplayName("PgPaymentInfo vo 테스트")
class PgPaymentInfoTest {

    @Test
    @DisplayName("PG 결제 정보를 생성할 수 있다")
    void createPgPaymentInfo() {
        // given
        String pgProvider = "TOSS";
        String pgPaymentId = "pay_123456";

        // when
        PgPaymentInfo pgPaymentInfo = PgPaymentInfo.of(pgProvider, pgPaymentId);

        // then
        assertThat(pgPaymentInfo.getPgProvider()).isEqualTo(pgProvider);
        assertThat(pgPaymentInfo.getPgPaymentId()).isEqualTo(pgPaymentId);
        assertThat(pgPaymentInfo.hasPgPaymentKey()).isFalse();
    }

    @Test
    @DisplayName("PG 결제 키를 설정할 수 있다")
    void setPgPaymentKey() {
        // given
        PgPaymentInfo pgPaymentInfo = PgPaymentInfo.of("TOSS", "pay_123456");
        String pgPaymentKey = "key_abcdef";

        // when
        PgPaymentInfo updated = pgPaymentInfo.withPgPaymentKey(pgPaymentKey);

        // then
        assertThat(updated.hasPgPaymentKey()).isTrue();
        assertThat(updated.getPgPaymentKey()).isEqualTo(pgPaymentKey);
    }

    @Test
    @DisplayName("PG사가 null이면 예외가 발생한다")
    void validatePgProviderNull() {
        // when & then
        assertThatThrownBy(() -> PgPaymentInfo.of(null, "pay_123456"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("PG사는 null이거나 빈 값일 수 없습니다");
    }

    @Test
    @DisplayName("PG사가 빈 문자열이면 예외가 발생한다")
    void validatePgProviderEmpty() {
        // when & then
        assertThatThrownBy(() -> PgPaymentInfo.of("", "pay_123456"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("PG사는 null이거나 빈 값일 수 없습니다");
    }

    @Test
    @DisplayName("PG 결제 ID가 null이면 예외가 발생한다")
    void validatePgPaymentIdNull() {
        // when & then
        assertThatThrownBy(() -> PgPaymentInfo.of("TOSS", null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("PG 결제 ID는 null이거나 빈 값일 수 없습니다");
    }

    @Test
    @DisplayName("PG 결제 ID가 빈 문자열이면 예외가 발생한다")
    void validatePgPaymentIdEmpty() {
        // when & then
        assertThatThrownBy(() -> PgPaymentInfo.of("TOSS", ""))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("PG 결제 ID는 null이거나 빈 값일 수 없습니다");
    }

    @Test
    @DisplayName("같은 값을 가진 PgPaymentInfo는 동일하다")
    void equalPgPaymentInfos() {
        // given
        PgPaymentInfo pgPaymentInfo1 = PgPaymentInfo.of("TOSS", "pay_123456");
        PgPaymentInfo pgPaymentInfo2 = PgPaymentInfo.of("TOSS", "pay_123456");

        // when & then
        assertThat(pgPaymentInfo1).isEqualTo(pgPaymentInfo2);
        assertThat(pgPaymentInfo1.hashCode()).isEqualTo(pgPaymentInfo2.hashCode());
    }
}
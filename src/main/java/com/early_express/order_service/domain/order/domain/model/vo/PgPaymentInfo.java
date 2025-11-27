package com.early_express.order_service.domain.order.domain.model.vo;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

/**
 * PG 결제 정보 Value Object
 * PG사, PG 결제 ID, PG 결제 키 정보를 담음
 */
@Getter
@EqualsAndHashCode
public class PgPaymentInfo {

    private final String pgProvider; // PG사 (TOSS, PORTONE 등)
    private final String pgPaymentId; // PG 결제 ID
    private final String pgPaymentKey; // PG 결제 키 (nullable)

    @Builder
    private PgPaymentInfo(
            String pgProvider,
            String pgPaymentId,
            String pgPaymentKey) {

        validateNotNull(pgProvider, "PG사");
        validateNotNull(pgPaymentId, "PG 결제 ID");

        this.pgProvider = pgProvider;
        this.pgPaymentId = pgPaymentId;
        this.pgPaymentKey = pgPaymentKey;
    }

    /**
     * PG 결제 정보 생성
     */
    public static PgPaymentInfo of(String pgProvider, String pgPaymentId, String pgPaymentKey) {
        return PgPaymentInfo.builder()
                .pgProvider(pgProvider)
                .pgPaymentId(pgPaymentId)
                .pgPaymentKey(pgPaymentKey)
                .build();
    }

    /**
     * PG 결제 키 설정
     */
    public PgPaymentInfo withPgPaymentKey(String pgPaymentKey) {
        return PgPaymentInfo.builder()
                .pgProvider(this.pgProvider)
                .pgPaymentId(this.pgPaymentId)
                .pgPaymentKey(pgPaymentKey)
                .build();
    }

    /**
     * PG 결제 키가 설정되었는지 확인
     */
    public boolean hasPgPaymentKey() {
        return pgPaymentKey != null && !pgPaymentKey.trim().isEmpty();
    }

    private void validateNotNull(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(fieldName + "는 null이거나 빈 값일 수 없습니다.");
        }
    }
}

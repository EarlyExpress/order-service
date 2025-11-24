package com.early_express.order_service.domain.order.domain.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Saga Step Enum
 *
 * Forward Steps: 정방향 실행
 * Compensation Steps: 보상 실행 (역순)
 */
@Getter
@RequiredArgsConstructor
public enum SagaStep {

    // ===== Forward Steps (정방향) =====

    /**
     * 재고 예약
     * - 보상: STOCK_RESTORE
     * - 필수: ✓
     */
    STOCK_RESERVE("재고 예약", true, true),

    /**
     * 결제 검증
     * - 보상: PAYMENT_CANCEL
     * - 필수: ✓
     */
    PAYMENT_VERIFY("결제 검증", true, true),

    /**
     * 경로 계산
     * - 보상: 없음 (조회만)
     * - 필수: 조건부 (requiresHubDelivery == true)
     */
    ROUTE_CALCULATE("경로 계산", false, false),

    /**
     * 허브 배송 생성
     * - 보상: HUB_DELIVERY_CANCEL
     * - 필수: 조건부 (requiresHubDelivery == true)
     */
    HUB_DELIVERY_CREATE("허브 배송 생성", true, false),

    /**
     * 업체 배송 생성
     * - 보상: LAST_MILE_DELIVERY_CANCEL
     * - 필수: ✓
     */
    LAST_MILE_DELIVERY_CREATE("업체 배송 생성", true, true),

    /**
     * 알림 발송
     * - 보상: 없음
     * - 필수: ✗ (Best Effort)
     */
    NOTIFICATION_SEND("알림 발송", false, false),

    /**
     * 추적 시작
     * - 보상: 없음
     * - 필수: ✗ (Best Effort)
     */
    TRACKING_START("추적 시작", false, false),

    // ===== Compensation Steps (보상 - 역순) =====

    /**
     * 재고 복원
     */
    STOCK_RESTORE("재고 복원", false, true),

    /**
     * 결제 취소/환불
     */
    PAYMENT_CANCEL("결제 취소", false, true),

    /**
     * 허브 배송 취소
     */
    HUB_DELIVERY_CANCEL("허브 배송 취소", false, false),

    /**
     * 업체 배송 취소
     */
    LAST_MILE_DELIVERY_CANCEL("업체 배송 취소", false, false);

    private final String description;
    private final boolean needsCompensation; // 보상이 필요한지
    private final boolean isMandatory; // 필수 Step인지

    /**
     * Forward Step인지 확인
     */
    public boolean isForwardStep() {
        return this == STOCK_RESERVE
                || this == PAYMENT_VERIFY
                || this == ROUTE_CALCULATE
                || this == HUB_DELIVERY_CREATE
                || this == LAST_MILE_DELIVERY_CREATE
                || this == NOTIFICATION_SEND
                || this == TRACKING_START;
    }

    /**
     * Compensation Step인지 확인
     */
    public boolean isCompensationStep() {
        return this == STOCK_RESTORE
                || this == PAYMENT_CANCEL
                || this == HUB_DELIVERY_CANCEL
                || this == LAST_MILE_DELIVERY_CANCEL;
    }

    /**
     * 대응하는 보상 Step 반환
     */
    public SagaStep getCompensationStep() {
        return switch (this) {
            case STOCK_RESERVE -> STOCK_RESTORE;
            case PAYMENT_VERIFY -> PAYMENT_CANCEL;
            case HUB_DELIVERY_CREATE -> HUB_DELIVERY_CANCEL;
            case LAST_MILE_DELIVERY_CREATE -> LAST_MILE_DELIVERY_CANCEL;
            default -> throw new IllegalStateException(
                    "보상 Step이 없는 Step입니다: " + this.description
            );
        };
    }

    /**
     * 다음 Step 반환
     */
    public SagaStep getNextStep() {
        return switch (this) {
            case STOCK_RESERVE -> PAYMENT_VERIFY;
            case PAYMENT_VERIFY -> ROUTE_CALCULATE;
            case ROUTE_CALCULATE -> HUB_DELIVERY_CREATE;
            case HUB_DELIVERY_CREATE -> LAST_MILE_DELIVERY_CREATE;
            case LAST_MILE_DELIVERY_CREATE -> NOTIFICATION_SEND;
            case NOTIFICATION_SEND -> TRACKING_START;
            default -> throw new IllegalStateException(
                    "다음 Step이 없습니다: " + this.description
            );
        };
    }

    /**
     * 마지막 Step인지 확인
     */
    public boolean isLastStep() {
        return this == TRACKING_START;
    }

    /**
     * Best Effort Step인지 확인 (실패해도 계속 진행)
     */
    public boolean isBestEffort() {
        return this == NOTIFICATION_SEND || this == TRACKING_START;
    }
}

package com.early_express.order_service.domain.order.domain.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 주문 상태 Enum
 *
 * 주문 생성 → Saga 단계 → 배송 진행 → 완료/취소
 */
@Getter
@RequiredArgsConstructor
public enum OrderStatus {

    // ===== 주문 생성 단계 =====
    /**
     * 주문 생성 (PG 결제 ID 수신)
     */
    PENDING("주문 생성"),

    // ===== Saga 실행 단계 (주문 확정까지) =====
    /**
     * 재고 확인 중
     */
    STOCK_CHECKING("재고 확인 중"),

    /**
     * 재고 예약 완료
     */
    STOCK_RESERVED("재고 예약 완료"),

    /**
     * 결제 검증 중
     */
    PAYMENT_VERIFYING("결제 검증 중"),

    /**
     * 결제 검증 완료
     */
    PAYMENT_VERIFIED("결제 검증 완료"),

    /**
     * 경로 계산 중
     */
    ROUTE_CALCULATING("경로 계산 중"),

    /**
     * 배송 생성 중
     */
    DELIVERY_CREATING("배송 생성 중"),

    /**
     * 주문 확정 (Saga 완료)
     */
    CONFIRMED("주문 확정"),

    // ===== 배송 진행 단계 =====
    /**
     * 허브 대기 중
     */
    HUB_WAITING("허브 대기 중"),

    /**
     * 허브 배송 중
     */
    HUB_IN_TRANSIT("허브 배송 중"),

    /**
     * 수령 허브 도착
     */
    HUB_ARRIVED("수령 허브 도착"),

    /**
     * 업체 배송 준비
     */
    LAST_MILE_READY("업체 배송 준비"),

    /**
     * 배송 중
     */
    IN_DELIVERY("배송 중"),

    /**
     * 배송 완료
     */
    COMPLETED("배송 완료"),

    // ===== 예외 상태 =====
    /**
     * 사용자 취소
     */
    CANCELLED("취소됨"),

    /**
     * 시스템 실패
     */
    FAILED("실패"),

    /**
     * 보상 완료 (롤백됨)
     */
    COMPENSATED("보상 완료");

    private final String description;

    /**
     * 취소 가능한 상태인지 확인
     * 배송 시작 전까지만 취소 가능
     */
    public boolean isCancellable() {
        return this == PENDING
                || this == STOCK_CHECKING
                || this == STOCK_RESERVED
                || this == PAYMENT_VERIFYING
                || this == PAYMENT_VERIFIED
                || this == ROUTE_CALCULATING
                || this == DELIVERY_CREATING
                || this == CONFIRMED
                || this == HUB_WAITING;
    }

    /**
     * Saga 진행 중인 상태인지 확인
     */
    public boolean isSagaInProgress() {
        return this == STOCK_CHECKING
                || this == STOCK_RESERVED
                || this == PAYMENT_VERIFYING
                || this == PAYMENT_VERIFIED
                || this == ROUTE_CALCULATING
                || this == DELIVERY_CREATING;
    }

    /**
     * 배송 진행 중인 상태인지 확인
     */
    public boolean isInDelivery() {
        return this == HUB_WAITING
                || this == HUB_IN_TRANSIT
                || this == HUB_ARRIVED
                || this == LAST_MILE_READY
                || this == IN_DELIVERY;
    }

    /**
     * 최종 상태인지 확인 (완료/취소/실패)
     */
    public boolean isFinalState() {
        return this == COMPLETED
                || this == CANCELLED
                || this == FAILED
                || this == COMPENSATED;
    }
}

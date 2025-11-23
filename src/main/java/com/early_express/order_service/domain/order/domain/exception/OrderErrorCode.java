package com.early_express.order_service.domain.order.domain.exception;

import com.early_express.order_service.global.presentation.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Order Service 전용 에러 코드
 *
 * 분류:
 * - ORDER_0xx: 주문 관련 에러
 * - SAGA_1xx: Saga 실행 관련 에러
 * - STOCK_2xx: 재고 관련 에러
 * - DELIVERY_3xx: 배송 관련 에러
 * - EXTERNAL_4xx: 외부 서비스 연동 에러
 * - AI_5xx: AI 서비스 관련 에러
 */
@Getter
@RequiredArgsConstructor
public enum OrderErrorCode implements ErrorCode {

    // ===== 주문 관련 에러 (ORDER_0xx) =====
    ORDER_NOT_FOUND("ORDER_001", "주문을 찾을 수 없습니다.", 404),
    ORDER_CREATION_FAILED("ORDER_002", "주문 생성에 실패했습니다.", 500),
    INVALID_ORDER_STATUS("ORDER_003", "유효하지 않은 주문 상태입니다.", 400),
    ORDER_CANNOT_BE_CANCELLED("ORDER_004", "배송이 시작되어 취소할 수 없습니다.", 422),
    ORDER_CANNOT_BE_MODIFIED("ORDER_005", "주문을 수정할 수 없습니다.", 422),
    ORDER_ACCESS_DENIED("ORDER_006", "해당 주문에 대한 권한이 없습니다.", 403),
    ORDER_AMOUNT_MISMATCH("ORDER_007", "주문 금액이 일치하지 않습니다.", 400),
    DUPLICATE_ORDER_NUMBER("ORDER_008", "이미 존재하는 주문 번호입니다.", 409),

    // ===== Saga 관련 에러 (SAGA_1xx) =====
    SAGA_NOT_FOUND("SAGA_101", "Saga를 찾을 수 없습니다.", 404),
    SAGA_EXECUTION_FAILED("SAGA_102", "Saga 실행에 실패했습니다.", 500),
    SAGA_STEP_FAILED("SAGA_103", "Saga Step 실행에 실패했습니다.", 500),
    SAGA_COMPENSATION_FAILED("SAGA_104", "Saga 보상 트랜잭션에 실패했습니다.", 500),
    SAGA_TIMEOUT("SAGA_105", "Saga 실행 시간이 초과되었습니다.", 504),
    SAGA_STATE_MISMATCH("SAGA_106", "Saga 상태가 일치하지 않습니다.", 409),
    SAGA_ALREADY_COMPLETED("SAGA_107", "이미 완료된 Saga입니다.", 409),
    SAGA_ALREADY_COMPENSATED("SAGA_108", "이미 보상 처리된 Saga입니다.", 409),

    // ===== 재고 관련 에러 (STOCK_2xx) =====
    INSUFFICIENT_STOCK("STOCK_201", "재고가 부족합니다.", 409),
    STOCK_RESERVATION_FAILED("STOCK_202", "재고 예약에 실패했습니다.", 500),
    STOCK_RESTORE_FAILED("STOCK_203", "재고 복원에 실패했습니다.", 500),
    STOCK_INFO_NOT_FOUND("STOCK_204", "재고 정보를 찾을 수 없습니다.", 404),
    INVALID_RESERVATION_ID("STOCK_205", "유효하지 않은 재고 예약 ID입니다.", 400),

    // ===== 배송 관련 에러 (DELIVERY_3xx) =====
    DELIVERY_CREATION_FAILED("DELIVERY_301", "배송 생성에 실패했습니다.", 500),
    HUB_DELIVERY_CREATION_FAILED("DELIVERY_302", "허브 배송 생성에 실패했습니다.", 500),
    LAST_MILE_DELIVERY_CREATION_FAILED("DELIVERY_303", "업체 배송 생성에 실패했습니다.", 500),
    DELIVERY_CANCELLATION_FAILED("DELIVERY_304", "배송 취소에 실패했습니다.", 500),
    ROUTE_NOT_FOUND("DELIVERY_305", "배송 경로를 찾을 수 없습니다.", 404),
    ROUTE_CALCULATION_FAILED("DELIVERY_306", "경로 계산에 실패했습니다.", 500),
    DELIVERY_STATUS_UPDATE_FAILED("DELIVERY_308", "배송 상태 업데이트에 실패했습니다.", 500),
    DELIVERY_NOT_FOUND("DELIVERY_309", "배송 정보를 찾을 수 없습니다.", 404),

    // ===== 외부 서비스 연동 에러 (EXTERNAL_4xx) =====
    INVENTORY_SERVICE_ERROR("EXTERNAL_401", "재고 서비스 연동 중 오류가 발생했습니다.", 502),
    PAYMENT_SERVICE_ERROR("EXTERNAL_402", "결제 서비스 연동 중 오류가 발생했습니다.", 502),
    HUB_SERVICE_ERROR("EXTERNAL_403", "허브 서비스 연동 중 오류가 발생했습니다.", 502),
    AI_SERVICE_ERROR("EXTERNAL_404", "AI 서비스 연동 중 오류가 발생했습니다.", 502),
    HUB_DELIVERY_SERVICE_ERROR("EXTERNAL_405", "허브 배송 서비스 연동 중 오류가 발생했습니다.", 502),
    LAST_MILE_DELIVERY_SERVICE_ERROR("EXTERNAL_406", "업체 배송 서비스 연동 중 오류가 발생했습니다.", 502),
    NOTIFICATION_SERVICE_ERROR("EXTERNAL_407", "알림 서비스 연동 중 오류가 발생했습니다.", 502),
    TRACKING_SERVICE_ERROR("EXTERNAL_408", "추적 서비스 연동 중 오류가 발생했습니다.", 502),
    EXTERNAL_SERVICE_TIMEOUT("EXTERNAL_409", "외부 서비스 응답 시간이 초과되었습니다.", 504),

    // ===== AI 서비스 관련 에러 (AI_5xx) =====
    AI_CALCULATION_FAILED("AI_501", "AI 시간 계산에 실패했습니다.", 500),
    AI_DEADLINE_CALCULATION_FAILED("AI_502", "AI 발송 시한 계산에 실패했습니다.", 500),
    DELIVERY_TIME_NOT_ACHIEVABLE("AI_503", "요청하신 배송 시간을 맞출 수 없습니다.", 422),
    AI_RESPONSE_INVALID("AI_504", "AI 응답 데이터가 유효하지 않습니다.", 500),
    AI_SERVICE_UNAVAILABLE("AI_505", "AI 서비스를 사용할 수 없습니다.", 503),

    // ===== Hub Delivery 서비스 관련 에러 (HUB_DELIVERY_xxx) =====
    HUB_DELIVERY_NOT_FOUND("HUB_DELIVERY_404", "허브 배송을 찾을 수 없습니다.", 404),
    HUB_DELIVERY_ALREADY_EXISTS("HUB_DELIVERY_409", "이미 허브 배송이 존재합니다.", 409),
    HUB_DELIVERY_INVALID_ROUTE("HUB_DELIVERY_422", "유효하지 않은 허브 경로입니다.", 422),

    // ===== Last Mile 서비스 관련 에러 (LAST_MILE_5xx) =====
    LAST_MILE_DELIVERY_NOT_FOUND("LAST_MILE_404", "업체 배송을 찾을 수 없습니다.", 404),
    LAST_MILE_DELIVERY_ALREADY_EXISTS("LAST_MILE_409", "이미 업체 배송이 존재합니다.", 409),
    LAST_MILE_DRIVER_NOT_AVAILABLE("LAST_MILE_422", "배정 가능한 배송 담당자가 없습니다.", 422),
    LAST_MILE_SERVICE_ERROR("LAST_MILE_503", "업체 배송 서비스 오류입니다.", 503);

    private final String code;
    private final String message;
    private final int status;
}
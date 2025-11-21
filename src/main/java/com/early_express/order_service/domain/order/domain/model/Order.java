package com.early_express.order_service.domain.order.domain.model;

import com.early_express.order_service.domain.order.domain.exception.OrderErrorCode;
import com.early_express.order_service.domain.order.domain.exception.OrderException;
import com.early_express.order_service.domain.order.domain.model.vo.*;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Order Aggregate Root
 * 주문의 전체 생명주기를 관리하는 핵심 도메인 모델
 */
@Slf4j
@Getter
public class Order {

    private final OrderId id;
    private final OrderNumber orderNumber;

    // 업체 정보
    private final CompanyInfo companyInfo;

    /**
     * 실제 도착 허브 ID
     * - Hub Service가 주소 기반으로 결정
     * - companyInfo.receiverHubId는 수령 업체의 소속 허브 (초기값)
     * - 실제 배송은 이 destinationHubId로 이동
     */
    private String destinationHubId;

    // 상품 정보
    private ProductInfo productInfo;

    // 배송 정보
    private DeliveryInfo deliveryInfo;

    // 수령자 정보
    private final ReceiverInfo receiverInfo;

    // 요청사항
    private final RequestInfo requestInfo;

    // AI 계산 결과
    private AiCalculationResult aiCalculationResult;

    // 상태
    private OrderStatus status;

    // 금액 정보
    private AmountInfo amountInfo;

    // PG 결제 정보
    private final PgPaymentInfo pgPaymentInfo;

    // 배송 진행 정보
    private DeliveryProgressInfo deliveryProgressInfo;

    // 생성자 정보
    private final String createdBy;
    private final LocalDateTime createdAt;

    // 취소 정보
    private String cancelReason;
    private LocalDateTime cancelledAt;

    @Builder
    private Order(
            OrderId id,
            OrderNumber orderNumber,
            CompanyInfo companyInfo,
            String destinationHubId,
            ProductInfo productInfo,
            DeliveryInfo deliveryInfo,
            ReceiverInfo receiverInfo,
            RequestInfo requestInfo,
            AiCalculationResult aiCalculationResult,
            OrderStatus status,
            AmountInfo amountInfo,
            PgPaymentInfo pgPaymentInfo,
            DeliveryProgressInfo deliveryProgressInfo,
            String createdBy,
            LocalDateTime createdAt,
            String cancelReason,
            LocalDateTime cancelledAt) {

        this.id = id;
        this.orderNumber = orderNumber;
        this.companyInfo = companyInfo;
        this.destinationHubId = destinationHubId;
        this.productInfo = productInfo;
        this.deliveryInfo = deliveryInfo;
        this.receiverInfo = receiverInfo;
        this.requestInfo = requestInfo;
        this.aiCalculationResult = aiCalculationResult;
        this.status = status;
        this.amountInfo = amountInfo;
        this.pgPaymentInfo = pgPaymentInfo;
        this.deliveryProgressInfo = deliveryProgressInfo;
        this.createdBy = createdBy;
        this.createdAt = createdAt;
        this.cancelReason = cancelReason;
        this.cancelledAt = cancelledAt;
    }

    /**
     * 새로운 주문 생성 (팩토리 메서드)
     */
    public static Order create(
            OrderNumber orderNumber,
            CompanyInfo companyInfo,
            ProductInfo productInfo,
            ReceiverInfo receiverInfo,
            RequestInfo requestInfo,
            BigDecimal unitPrice,
            PgPaymentInfo pgPaymentInfo,
            String createdBy) {

        return Order.builder()
                .id(null)
                .orderNumber(orderNumber)
                .companyInfo(companyInfo)
                .destinationHubId(null)
                .productInfo(productInfo)
                .deliveryInfo(DeliveryInfo.initial())
                .receiverInfo(receiverInfo)
                .requestInfo(requestInfo)
                .aiCalculationResult(AiCalculationResult.empty())
                .status(OrderStatus.PENDING)
                .amountInfo(AmountInfo.of(unitPrice, productInfo.getQuantity()))
                .pgPaymentInfo(pgPaymentInfo)
                .deliveryProgressInfo(DeliveryProgressInfo.empty())
                .createdBy(createdBy)
                .createdAt(LocalDateTime.now())
                .build();
    }

    // ===== Saga Step 관련 메서드 =====

    /**
     * Step 1: 재고 예약 완료 처리
     * - 상품 위치 허브 정보 업데이트
     * - 허브 배송 필요 여부 판단
     */
    public void completeStockReservation(String productHubId) {
        validateStatus(OrderStatus.STOCK_CHECKING, "재고 예약");

        // 상품 위치 허브 설정
        this.productInfo = this.productInfo.withProductHubId(productHubId);

        // 허브 배송 필요 여부 판단
        boolean needsHubDelivery = !productHubId.equals(companyInfo.getReceiverHubId());
        this.deliveryInfo = this.deliveryInfo.withRequiresHubDelivery(needsHubDelivery);

        this.status = OrderStatus.STOCK_RESERVED;
    }

    /**
     * Step 2: 결제 검증 완료 처리
     */
    public void completePaymentVerification(String paymentId) {
        validateStatus(OrderStatus.PAYMENT_VERIFYING, "결제 검증");

        this.amountInfo = this.amountInfo.withPaymentId(paymentId);
        this.status = OrderStatus.PAYMENT_VERIFIED;
    }

    /**
     * Step 3: 경로 계산 완료 처리
     */
    /**
     * 도착 허브 ID 업데이트
     * Hub Service가 주소 기반으로 결정한 도착 허브
     */
    public void updateDestinationHubId(String destinationHubId) {
        validateNotNull(destinationHubId, "도착 허브 ID");

        this.destinationHubId = destinationHubId;

        log.info("도착 허브 ID 업데이트 - orderId: {}, destinationHubId: {}",
                this.id.getValue(), destinationHubId);
    }

    /**
     * 경로 정보 업데이트 (Hub Service 응답)
     */
    public void updateRouteInfo(String routeInfoJson) {
        validateNotNull(routeInfoJson, "경로 정보");

        this.aiCalculationResult = AiCalculationResult.withRouteOnly(routeInfoJson);

        log.info("경로 정보 업데이트 - orderId: {}", this.id.getValue());
    }

    /**
     * 배송 정보 업데이트
     */
    public void updateDeliveryInfo(DeliveryInfo deliveryInfo) {
        validateNotNull(deliveryInfo, "배송 정보");

        this.deliveryInfo = deliveryInfo;
    }

    // 검증 헬퍼 메서드
    private void validateNotNull(Object value, String fieldName) {
        if (value == null) {
            throw new OrderException(
                    OrderErrorCode.INVALID_ORDER_STATUS,
                    fieldName + "는 null일 수 없습니다."
            );
        }
    }

    /**
     * Step 4: 허브 배송 생성 완료 처리
     */
    public void completeHubDeliveryCreation(String hubDeliveryId) {
        this.deliveryInfo = this.deliveryInfo.withHubDeliveryId(hubDeliveryId);
    }

    /**
     * Step 5: 업체 배송 생성 완료 처리 (Saga 완료)
     */
    public void completeLastMileDeliveryCreation(String lastMileDeliveryId) {
        this.deliveryInfo = this.deliveryInfo.withLastMileDeliveryId(lastMileDeliveryId);
        this.status = OrderStatus.CONFIRMED;
    }

    /**
     * 주문 확정 (동기 Saga 완료 후)
     * - Saga의 Step 1, 2 완료 후 호출
     * - status를 CONFIRMED로 변경
     */
    public void confirm() {
        // PAYMENT_VERIFIED 또는 DELIVERY_CREATING 상태에서만 확정 가능
        if (this.status != OrderStatus.PAYMENT_VERIFIED &&
                this.status != OrderStatus.DELIVERY_CREATING) {
            throw new OrderException(
                    OrderErrorCode.INVALID_ORDER_STATUS,
                    String.format("주문 확정은 결제 검증 완료 후에만 가능합니다. 현재 상태: %s",
                            this.status.getDescription())
            );
        }

        this.status = OrderStatus.CONFIRMED;
    }

    // ===== 상태 전이 메서드 =====

    /**
     * 재고 확인 시작
     */
    public void startStockChecking() {
        validateStatus(OrderStatus.PENDING, "재고 확인 시작");
        this.status = OrderStatus.STOCK_CHECKING;
    }

    /**
     * 결제 검증 시작
     */
    public void startPaymentVerification() {
        validateStatus(OrderStatus.STOCK_RESERVED, "결제 검증 시작");
        this.status = OrderStatus.PAYMENT_VERIFYING;
    }

    /**
     * 경로 계산 시작
     */
    public void startRouteCalculation() {
        validateStatus(OrderStatus.PAYMENT_VERIFIED, "경로 계산 시작");
        this.status = OrderStatus.ROUTE_CALCULATING;
    }

    /**
     * 배송 생성 시작
     */
    public void startDeliveryCreation() {
        this.status = OrderStatus.DELIVERY_CREATING;
    }

    /**
     * 허브 배송 시작
     */
    public void startHubDelivery(LocalDateTime actualDepartureTime) {
        validateStatus(OrderStatus.CONFIRMED, "허브 배송 시작");

        this.deliveryProgressInfo = this.deliveryProgressInfo
                .withActualDepartureTime(actualDepartureTime);
        this.status = OrderStatus.HUB_IN_TRANSIT;
    }

    /**
     * 허브 도착
     */
    public void arriveAtHub(LocalDateTime hubArrivalTime) {
        validateStatusInDelivery("허브 도착");

        this.deliveryProgressInfo = this.deliveryProgressInfo
                .withHubArrivalTime(hubArrivalTime);
        this.status = OrderStatus.HUB_ARRIVED;
    }

    /**
     * 업체 배송 준비
     */
    public void prepareLastMileDelivery() {
        this.status = OrderStatus.LAST_MILE_READY;
    }

    /**
     * 최종 배송 시작
     */
    public void startFinalDelivery(LocalDateTime finalDeliveryStartTime) {
        this.deliveryProgressInfo = this.deliveryProgressInfo
                .withFinalDeliveryStartTime(finalDeliveryStartTime);
        this.status = OrderStatus.IN_DELIVERY;
    }

    /**
     * 배송 완료
     */
    public void completeDelivery(
            LocalDateTime actualDeliveryTime,
            String signature,
            String actualReceiverName) {

        validateStatus(OrderStatus.IN_DELIVERY, "배송 완료");

        this.deliveryProgressInfo = this.deliveryProgressInfo
                .withDeliveryCompleted(actualDeliveryTime, signature, actualReceiverName);
        this.status = OrderStatus.COMPLETED;
    }

    /**
     * 주문 취소
     */
    public void cancel(String cancelReason) {
        if (!this.status.isCancellable()) {
            throw new OrderException(
                    OrderErrorCode.ORDER_CANNOT_BE_CANCELLED,
                    "현재 주문 상태에서는 취소할 수 없습니다: " + this.status.getDescription()
            );
        }

        this.status = OrderStatus.CANCELLED;
        this.cancelReason = cancelReason;
        this.cancelledAt = LocalDateTime.now();
    }

    /**
     * 주문 실패 처리
     */
    public void fail() {
        this.status = OrderStatus.FAILED;
    }

    /**
     * 주문 보상 완료 처리
     */
    public void compensate() {
        this.status = OrderStatus.COMPENSATED;
    }

    // ===== 검증 메서드 =====

    /**
     * 상태 검증
     */
    private void validateStatus(OrderStatus expectedStatus, String operation) {
        if (this.status != expectedStatus) {
            throw new OrderException(
                    OrderErrorCode.INVALID_ORDER_STATUS,
                    String.format("%s는 %s 상태에서만 가능합니다. 현재 상태: %s",
                            operation,
                            expectedStatus.getDescription(),
                            this.status.getDescription())
            );
        }
    }

    /**
     * 배송 중 상태 검증
     */
    private void validateStatusInDelivery(String operation) {
        if (!this.status.isInDelivery()) {
            throw new OrderException(
                    OrderErrorCode.INVALID_ORDER_STATUS,
                    String.format("%s는 배송 진행 중 상태에서만 가능합니다. 현재 상태: %s",
                            operation,
                            this.status.getDescription())
            );
        }
    }

    /**
     * 결제 금액 검증
     */
    public void validatePaymentAmount(BigDecimal paymentAmount) {
        if (!this.amountInfo.matchesAmount(paymentAmount)) {
            throw new OrderException(
                    OrderErrorCode.ORDER_AMOUNT_MISMATCH,
                    String.format("결제 금액이 일치하지 않습니다. 주문 금액: %s, 결제 금액: %s",
                            this.amountInfo.getTotalAmount(),
                            paymentAmount)
            );
        }
    }

    // ===== 조회 메서드 =====

    /**
     * 허브 배송이 필요한지 확인
     */
    public boolean requiresHubDelivery() {
        return this.deliveryInfo.needsHubDelivery();
    }

    /**
     * 발송 시한이 지났는지 확인
     */
    public boolean isDepartureDeadlinePassed() {
        return this.aiCalculationResult.isDepartureDeadlinePassed();
    }

    /**
     * 취소 가능한 상태인지 확인
     */
    public boolean isCancellable() {
        return this.status.isCancellable();
    }

    /**
     * 배송 완료 여부 확인
     */
    public boolean isCompleted() {
        return this.status == OrderStatus.COMPLETED;
    }

    /**
     * 주문 ID 문자열 반환
     */
    public String getIdValue() {
        return this.id.getValue();
    }

    /**
     * 주문 번호 문자열 반환
     */
    public String getOrderNumberValue() {
        return this.orderNumber.getValue();
    }
}

package com.early_express.order_service.domain.order.presentation.web.companyuser.dto.response;

import com.early_express.order_service.domain.order.domain.model.Order;
import com.early_express.order_service.domain.order.domain.model.OrderStatus;
import com.early_express.order_service.domain.order.presentation.web.common.dto.response.*;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 주문 상세 응답 DTO (Company User)
 * 업체 사용자가 볼 수 있는 주문 상세 정보
 * - 결제 정보는 포함하되 민감한 PG 키는 제외
 * - Saga 정보는 제외
 */
@Getter
@Builder
public class OrderDetailResponse {

    // 기본 정보
    private String orderId;
    private String orderNumber;
    private OrderStatus status;
    private String statusDescription;

    // 업체 정보
    private CompanyInfoDto companyInfo;

    // 상품 정보
    private ProductInfoDto productInfo;

    // 금액 정보
    private AmountInfoDto amountInfo;

    // 수령자 정보
    private ReceiverInfoDto receiverInfo;

    // 요청사항
    private RequestInfoDto requestInfo;

    // 배송 정보 (간단)
    private DeliveryInfoDto deliveryInfo;

    // 배송 진행 정보
    private DeliveryProgressInfoDto deliveryProgressInfo;

    // AI 계산 결과 (예상 시간만)
    private LocalDateTime estimatedDeliveryTime;
    private LocalDateTime calculatedDepartureDeadline;

    // PG 결제 정보 (PG 키는 제외)
    private String pgProvider;
    private String pgPaymentId;

    // 시간 정보
    private LocalDateTime createdAt;

    // 취소 정보
    private String cancelReason;
    private LocalDateTime cancelledAt;

    // 취소 가능 여부
    private Boolean cancellable;

    /**
     * Domain → DTO 변환
     */
    public static OrderDetailResponse from(Order order) {
        return OrderDetailResponse.builder()
                .orderId(order.getIdValue())
                .orderNumber(order.getOrderNumberValue())
                .status(order.getStatus())
                .statusDescription(order.getStatus().getDescription())
                .companyInfo(CompanyInfoDto.from(order.getCompanyInfo()))
                .productInfo(ProductInfoDto.from(order.getProductInfo()))
                .amountInfo(AmountInfoDto.from(order.getAmountInfo()))
                .receiverInfo(ReceiverInfoDto.from(order.getReceiverInfo()))
                .requestInfo(RequestInfoDto.from(order.getRequestInfo()))
                .deliveryInfo(DeliveryInfoDto.from(order.getDeliveryInfo()))
                .deliveryProgressInfo(DeliveryProgressInfoDto.from(order.getDeliveryProgressInfo()))
                .estimatedDeliveryTime(order.getAiCalculationResult().getEstimatedDeliveryTime())
                .calculatedDepartureDeadline(order.getAiCalculationResult().getCalculatedDepartureDeadline())
                .pgProvider(order.getPgPaymentInfo().getPgProvider())
                .pgPaymentId(order.getPgPaymentInfo().getPgPaymentId())
                .createdAt(order.getCreatedAt())
                .cancelReason(order.getCancelReason())
                .cancelledAt(order.getCancelledAt())
                .cancellable(order.isCancellable())
                .build();
    }
}
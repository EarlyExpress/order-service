package com.early_express.order_service.domain.order.presentation.web.master.dto.response;

import com.early_express.order_service.domain.order.domain.model.Order;
import com.early_express.order_service.domain.order.domain.model.OrderSaga;
import com.early_express.order_service.domain.order.domain.model.OrderStatus;
import com.early_express.order_service.domain.order.presentation.web.common.dto.response.*;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 주문 상세 응답 DTO (Master)
 * 마스터 관리자가 볼 수 있는 모든 주문 정보
 * - 모든 도메인 정보 포함
 * - Saga 진행 상태 포함
 * - PG 결제 정보 포함 (키 포함)
 */
@Getter
@Builder
public class MasterOrderDetailResponse {

    // 기본 정보
    private String orderId;
    private String orderNumber;
    private OrderStatus status;
    private String statusDescription;

    // 업체 정보
    private CompanyInfoDto companyInfo;

    // 도착 허브 ID
    private String destinationHubId;

    // 상품 정보
    private ProductInfoDto productInfo;

    // 금액 정보
    private AmountInfoDto amountInfo;

    // 수령자 정보
    private ReceiverInfoDto receiverInfo;

    // 요청사항
    private RequestInfoDto requestInfo;

    // 배송 정보
    private DeliveryInfoDto deliveryInfo;

    // 배송 진행 정보
    private DeliveryProgressInfoDto deliveryProgressInfo;

    // AI 계산 결과
    private AiCalculationResultDto aiCalculationResult;

    // PG 결제 정보 (전체)
    private PgPaymentInfoDto pgPaymentInfo;

    // Saga 정보 (optional)
    private OrderSagaInfoDto sagaInfo;

    // 생성자 정보
    private String createdBy;
    private LocalDateTime createdAt;

    // 취소 정보
    private String cancelReason;
    private LocalDateTime cancelledAt;

    // 상태 플래그
    private Boolean cancellable;
    private Boolean departureDeadlinePassed;
    private Boolean completed;

    /**
     * Domain → DTO 변환
     */
    public static MasterOrderDetailResponse from(Order order) {
        return MasterOrderDetailResponse.builder()
                .orderId(order.getIdValue())
                .orderNumber(order.getOrderNumberValue())
                .status(order.getStatus())
                .statusDescription(order.getStatus().getDescription())
                .companyInfo(CompanyInfoDto.from(order.getCompanyInfo()))
                .destinationHubId(order.getDestinationHubId())
                .productInfo(ProductInfoDto.from(order.getProductInfo()))
                .amountInfo(AmountInfoDto.from(order.getAmountInfo()))
                .receiverInfo(ReceiverInfoDto.from(order.getReceiverInfo()))
                .requestInfo(RequestInfoDto.from(order.getRequestInfo()))
                .deliveryInfo(DeliveryInfoDto.from(order.getDeliveryInfo()))
                .deliveryProgressInfo(DeliveryProgressInfoDto.from(order.getDeliveryProgressInfo()))
                .aiCalculationResult(AiCalculationResultDto.from(order.getAiCalculationResult()))
                .pgPaymentInfo(PgPaymentInfoDto.from(order.getPgPaymentInfo()))
                .createdBy(order.getCreatedBy())
                .createdAt(order.getCreatedAt())
                .cancelReason(order.getCancelReason())
                .cancelledAt(order.getCancelledAt())
                .cancellable(order.isCancellable())
                .departureDeadlinePassed(order.isDepartureDeadlinePassed())
                .completed(order.isCompleted())
                .build();
    }

    /**
     * Domain → DTO 변환 (Saga 정보 포함)
     */
    public static MasterOrderDetailResponse from(Order order, OrderSaga saga) {
        MasterOrderDetailResponse response = from(order);
        return MasterOrderDetailResponse.builder()
                .orderId(response.getOrderId())
                .orderNumber(response.getOrderNumber())
                .status(response.getStatus())
                .statusDescription(response.getStatusDescription())
                .companyInfo(response.getCompanyInfo())
                .destinationHubId(response.getDestinationHubId())
                .productInfo(response.getProductInfo())
                .amountInfo(response.getAmountInfo())
                .receiverInfo(response.getReceiverInfo())
                .requestInfo(response.getRequestInfo())
                .deliveryInfo(response.getDeliveryInfo())
                .deliveryProgressInfo(response.getDeliveryProgressInfo())
                .aiCalculationResult(response.getAiCalculationResult())
                .pgPaymentInfo(response.getPgPaymentInfo())
                .sagaInfo(OrderSagaInfoDto.from(saga))
                .createdBy(response.getCreatedBy())
                .createdAt(response.getCreatedAt())
                .cancelReason(response.getCancelReason())
                .cancelledAt(response.getCancelledAt())
                .cancellable(response.getCancellable())
                .departureDeadlinePassed(response.getDepartureDeadlinePassed())
                .completed(response.getCompleted())
                .build();
    }

    /**
     * PG 결제 정보 DTO (Master용 - 전체 정보)
     */
    @Getter
    @Builder
    public static class PgPaymentInfoDto {
        private String pgProvider;
        private String pgPaymentId;
        private String pgPaymentKey;

        public static PgPaymentInfoDto from(
                com.early_express.order_service.domain.order.domain.model.vo.PgPaymentInfo pgPaymentInfo) {
            return PgPaymentInfoDto.builder()
                    .pgProvider(pgPaymentInfo.getPgProvider())
                    .pgPaymentId(pgPaymentInfo.getPgPaymentId())
                    .pgPaymentKey(pgPaymentInfo.getPgPaymentKey())
                    .build();
        }
    }
}
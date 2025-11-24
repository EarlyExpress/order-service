package com.early_express.order_service.domain.order.presentation.web.hubmanager.dto.response;

import com.early_express.order_service.domain.order.domain.model.Order;
import com.early_express.order_service.domain.order.domain.model.OrderStatus;
import com.early_express.order_service.domain.order.presentation.web.common.dto.response.*;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 주문 상세 응답 DTO (Hub Manager)
 * 허브 관리자가 볼 수 있는 주문 상세 정보
 * - 배송 관련 상세 정보 포함
 * - AI 계산 결과 포함 (경로, 시간)
 * - 결제 정보는 제외
 */
@Getter
@Builder
public class HubOrderDetailResponse {

    // 기본 정보
    private String orderId;
    private String orderNumber;
    private OrderStatus status;
    private String statusDescription;

    // 업체 정보
    private CompanyInfoDto companyInfo;

    // 도착 허브 ID (Hub Service가 결정한 실제 도착 허브)
    private String destinationHubId;

    // 상품 정보
    private ProductInfoDto productInfo;

    // 수령자 정보
    private ReceiverInfoDto receiverInfo;

    // 요청사항
    private RequestInfoDto requestInfo;

    // 배송 정보
    private DeliveryInfoDto deliveryInfo;

    // 배송 진행 정보
    private DeliveryProgressInfoDto deliveryProgressInfo;

    // AI 계산 결과 (전체)
    private AiCalculationResultDto aiCalculationResult;

    // 시간 정보
    private LocalDateTime createdAt;

    // 발송 시한 초과 여부
    private Boolean departureDeadlinePassed;

    /**
     * Domain → DTO 변환
     */
    public static HubOrderDetailResponse from(Order order) {
        return HubOrderDetailResponse.builder()
                .orderId(order.getIdValue())
                .orderNumber(order.getOrderNumberValue())
                .status(order.getStatus())
                .statusDescription(order.getStatus().getDescription())
                .companyInfo(CompanyInfoDto.from(order.getCompanyInfo()))
                .destinationHubId(order.getDestinationHubId())
                .productInfo(ProductInfoDto.from(order.getProductInfo()))
                .receiverInfo(ReceiverInfoDto.from(order.getReceiverInfo()))
                .requestInfo(RequestInfoDto.from(order.getRequestInfo()))
                .deliveryInfo(DeliveryInfoDto.from(order.getDeliveryInfo()))
                .deliveryProgressInfo(DeliveryProgressInfoDto.from(order.getDeliveryProgressInfo()))
                .aiCalculationResult(AiCalculationResultDto.from(order.getAiCalculationResult()))
                .createdAt(order.getCreatedAt())
                .departureDeadlinePassed(order.isDepartureDeadlinePassed())
                .build();
    }
}
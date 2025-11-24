package com.early_express.order_service.domain.order.presentation.web.hubmanager.dto.request;

import com.early_express.order_service.domain.order.application.service.OrderQueryService;
import com.early_express.order_service.domain.order.domain.model.OrderStatus;
import lombok.Builder;
import lombok.Getter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

/**
 * 허브 주문 검색 요청 DTO (Hub Manager)
 */
@Getter
@Builder
public class HubOrderSearchRequest {

    private OrderStatus status;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime startDate;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime endDate;

    /**
     * Request → Query 변환
     *
     * @param hubId 허브 ID (Header에서 전달)
     */
    public OrderQueryService.HubOrderSearchQuery toQuery(String hubId) {
        return new OrderQueryService.HubOrderSearchQuery(
                hubId,
                status,
                startDate,
                endDate
        );
    }
}
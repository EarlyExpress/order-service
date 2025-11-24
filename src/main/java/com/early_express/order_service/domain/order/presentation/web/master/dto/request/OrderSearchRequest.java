package com.early_express.order_service.domain.order.presentation.web.master.dto.request;

import com.early_express.order_service.domain.order.application.service.OrderQueryService;
import com.early_express.order_service.domain.order.domain.model.OrderStatus;
import lombok.Builder;
import lombok.Getter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

/**
 * 주문 검색 요청 DTO (Master)
 */
@Getter
@Builder
public class OrderSearchRequest {

    private String companyId;

    private OrderStatus status;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime startDate;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime endDate;

    private Boolean isDeleted;

    /**
     * Request → Query 변환
     */
    public OrderQueryService.OrderSearchQuery toQuery() {
        return new OrderQueryService.OrderSearchQuery(
                companyId,
                status,
                startDate,
                endDate
        );
    }

    /**
     * Request → Query (Deleted 포함) 변환
     */
    public OrderQueryService.OrderSearchQueryWithDeleted toQueryWithDeleted() {
        return new OrderQueryService.OrderSearchQueryWithDeleted(
                companyId,
                status,
                isDeleted,
                startDate,
                endDate
        );
    }
}

package com.early_express.order_service.domain.order.presentation.web.hubmanager;

import com.early_express.order_service.domain.order.application.service.OrderCommandService;
import com.early_express.order_service.domain.order.application.service.OrderQueryService;
import com.early_express.order_service.domain.order.domain.model.Order;
import com.early_express.order_service.domain.order.presentation.web.common.dto.response.OrderSimpleResponse;
import com.early_express.order_service.domain.order.presentation.web.hubmanager.dto.request.HubOrderSearchRequest;
import com.early_express.order_service.domain.order.presentation.web.hubmanager.dto.request.OrderUpdateRequest;
import com.early_express.order_service.domain.order.presentation.web.hubmanager.dto.response.HubOrderDetailResponse;
import com.early_express.order_service.global.common.dto.PageInfo;
import com.early_express.order_service.global.presentation.dto.ApiResponse;
import com.early_express.order_service.global.presentation.dto.PageResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Hub Manager Order Controller
 * 허브 관리자 주문 관리 API
 */
@Slf4j
@RestController
@RequestMapping("/v1/order/web/hub-manager")
@RequiredArgsConstructor
public class HubManagerOrderController {

    private final OrderQueryService orderQueryService;
    private final OrderCommandService orderCommandService;

    /**
     * 허브 주문 목록 조회 (기본 - 페이징)
     * GET /v1/order/web/hub-manager/hub-orders
     */
    @GetMapping("/hub-orders")
    public ApiResponse<PageResponse<OrderSimpleResponse>> getHubOrders(
            @RequestHeader("X-Hub-Id") String hubId,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable) {

        log.info("허브 주문 목록 조회 - hubId: {}", hubId);

        Page<Order> orderPage = orderQueryService.getOrdersByHubId(hubId, pageable);

        // Domain → DTO 변환
        PageResponse<OrderSimpleResponse> response = PageResponse.of(
                orderPage.getContent().stream()
                        .map(OrderSimpleResponse::from)
                        .toList(),
                PageInfo.of(orderPage)
        );

        return ApiResponse.success(response);
    }

    /**
     * 허브 주문 검색 (필터링 + 페이징)
     * GET /v1/order/web/hub-manager/hub-orders/search
     */
    @GetMapping("/hub-orders/search")
    public ApiResponse<PageResponse<OrderSimpleResponse>> searchHubOrders(
            @RequestHeader("X-Hub-Id") String hubId,
            @ModelAttribute HubOrderSearchRequest request,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable) {

        log.info("허브 주문 검색 - hubId: {}, status: {}, startDate: {}, endDate: {}",
                hubId, request.getStatus(), request.getStartDate(), request.getEndDate());

        Page<Order> orderPage = orderQueryService.searchOrdersByHubId(
                request.toQuery(hubId),
                pageable
        );

        // Domain → DTO 변환
        PageResponse<OrderSimpleResponse> response = PageResponse.of(
                orderPage.getContent().stream()
                        .map(OrderSimpleResponse::from)
                        .toList(),
                PageInfo.of(orderPage)
        );

        return ApiResponse.success(response);
    }

    /**
     * 주문 상세 조회 (허브 관리자 관점)
     * GET /v1/order/web/hub-manager/orders/{id}
     */
    @GetMapping("/orders/{id}")
    public ApiResponse<HubOrderDetailResponse> getOrder(@PathVariable String id) {

        log.info("주문 상세 조회 - orderId: {}", id);

        Order order = orderQueryService.getOrderById(id);

        // Domain → DTO 변환 (허브 관리자 관점)
        HubOrderDetailResponse response = HubOrderDetailResponse.from(order);

        return ApiResponse.success(response);
    }
}
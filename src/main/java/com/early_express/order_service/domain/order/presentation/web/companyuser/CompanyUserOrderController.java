package com.early_express.order_service.domain.order.presentation.web.companyuser;

import com.early_express.order_service.domain.order.application.dto.OrderCreateCommand;
import com.early_express.order_service.domain.order.application.service.OrderCommandService;
import com.early_express.order_service.domain.order.application.service.OrderQueryService;
import com.early_express.order_service.domain.order.domain.model.Order;
import com.early_express.order_service.domain.order.presentation.web.common.dto.response.OrderSimpleResponse;
import com.early_express.order_service.domain.order.presentation.web.companyuser.dto.request.OrderCancelRequest;
import com.early_express.order_service.domain.order.presentation.web.companyuser.dto.request.OrderCreateRequest;
import com.early_express.order_service.domain.order.presentation.web.companyuser.dto.response.OrderCreateResponse;
import com.early_express.order_service.domain.order.presentation.web.companyuser.dto.response.OrderDetailResponse;
import com.early_express.order_service.global.presentation.dto.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Company User Order Controller
 * 업체 사용자 주문 관리 API
 */
@Slf4j
@RestController
@RequestMapping("/v1/order/web/company-user")
@RequiredArgsConstructor
public class CompanyUserOrderController {

    private final OrderCommandService orderCommandService;
    private final OrderQueryService orderQueryService;

    /**
     * 주문 생성
     * POST /v1/order/web/company-user/orders
     */
    @PostMapping("/orders")
    public ApiResponse<OrderCreateResponse> createOrder(
            @Valid @RequestBody OrderCreateRequest request,
            @RequestHeader("X-User-Id") String userId) {

        log.info("주문 생성 요청 - userId: {}, productId: {}",
                userId, request.getProductId());

        OrderCreateCommand command = request.toCommand(userId);
        Order order = orderCommandService.createOrder(command);

        // Domain → DTO 변환
        OrderCreateResponse response = OrderCreateResponse.from(order);

        return ApiResponse.success(response, "주문이 생성되었습니다.");
    }

    /**
     * 내 주문 목록 조회
     * GET /v1/order/web/company-user/my-orders
     */
    @GetMapping("/my-orders")
    public ApiResponse<List<OrderSimpleResponse>> getMyOrders(
            @RequestHeader("X-Company-Id") String companyId) {

        log.info("내 주문 목록 조회 - companyId: {}", companyId);

        List<Order> orders = orderQueryService.getOrdersByCompanyId(companyId);

        // Domain → DTO 변환
        List<OrderSimpleResponse> responses = orders.stream()
                .map(OrderSimpleResponse::from)
                .toList();

        return ApiResponse.success(responses);
    }

    /**
     * 주문 상세 조회
     * GET /v1/order/web/company-user/orders/{id}
     */
    @GetMapping("/orders/{id}")
    public ApiResponse<OrderDetailResponse> getOrder(@PathVariable String id) {

        log.info("주문 상세 조회 - orderId: {}", id);

        Order order = orderQueryService.getOrderById(id);

        // Domain → DTO 변환
        OrderDetailResponse response = OrderDetailResponse.from(order);

        return ApiResponse.success(response);
    }

    /**
     * 주문 취소
     * POST /v1/order/web/company-user/cancel/{id}
     */
    @PostMapping("/cancel/{id}")
    public ApiResponse<Void> cancelOrder(
            @PathVariable String id,
            @Valid @RequestBody OrderCancelRequest request) {

        log.info("주문 취소 요청 - orderId: {}, reason: {}",
                id, request.getCancelReason());

        orderCommandService.cancelOrder(id, request.getCancelReason());

        return ApiResponse.success(null, "주문이 취소되었습니다.");
    }
}
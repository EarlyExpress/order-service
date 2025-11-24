package com.early_express.order_service.domain.order.presentation.web.master;

import com.early_express.order_service.domain.order.application.service.OrderQueryService;
import com.early_express.order_service.domain.order.domain.model.Order;
import com.early_express.order_service.domain.order.domain.model.OrderSaga;
import com.early_express.order_service.domain.order.presentation.web.common.dto.response.OrderSimpleResponse;
import com.early_express.order_service.domain.order.presentation.web.master.dto.request.OrderSearchRequest;
import com.early_express.order_service.domain.order.presentation.web.master.dto.response.MasterOrderDetailResponse;
import com.early_express.order_service.global.common.dto.PageInfo;
import com.early_express.order_service.global.presentation.dto.ApiResponse;
import com.early_express.order_service.global.presentation.dto.PageResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

/**
 * Master Order Controller
 * 관리자 주문 관리 API
 */
@Slf4j
@RestController
@RequestMapping("/v1/order/web/master")
@RequiredArgsConstructor
public class MasterOrderController {

    private final OrderQueryService orderQueryService;
    // private final OrderSagaQueryService orderSagaQueryService; // Saga 조회용 서비스 (필요 시 추가)

    /**
     * 전체 주문 검색 (페이징)
     * GET /v1/order/web/master/orders
     */
    @GetMapping("/orders")
    public ApiResponse<PageResponse<OrderSimpleResponse>> searchOrders(
            @ModelAttribute OrderSearchRequest request,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable) {

        log.info("관리자 주문 검색 - companyId: {}, status: {}",
                request.getCompanyId(), request.getStatus());

        Page<Order> orderPage = orderQueryService.searchOrders(
                request.toQuery(),
                pageable
        );

        // Domain → DTO 변환
        List<OrderSimpleResponse> responses = orderPage.getContent().stream()
                .map(OrderSimpleResponse::from)
                .toList();

        PageResponse<OrderSimpleResponse> response = PageResponse.of(
                responses,
                PageInfo.of(orderPage)
        );

        return ApiResponse.success(response);
    }

    /**
     * 전체 주문 검색 (삭제된 것 포함)
     * GET /v1/order/web/master/orders/all
     */
    @GetMapping("/orders/all")
    public ApiResponse<PageResponse<OrderSimpleResponse>> searchAllOrders(
            @RequestHeader("X-Hub-Id") String hubId,
            @ModelAttribute OrderSearchRequest request,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable) {

        log.info("관리자 전체 주문 검색 (삭제 포함) - isDeleted: {}",
                request.getIsDeleted());

        Page<Order> orderPage = orderQueryService.searchAllOrdersIncludingDeleted(
                request.toQueryWithDeleted(),
                pageable
        );

        // Domain → DTO 변환
        List<OrderSimpleResponse> responses = orderPage.getContent().stream()
                .map(OrderSimpleResponse::from)
                .toList();

        PageResponse<OrderSimpleResponse> response = PageResponse.of(
                responses,
                PageInfo.of(orderPage)
        );

        return ApiResponse.success(response);
    }

    /**
     * 주문 상세 조회 (마스터 관점)
     * GET /v1/order/web/master/orders/{id}
     */
    @GetMapping("/orders/{id}")
    public ApiResponse<MasterOrderDetailResponse> getOrder(
            @RequestHeader("X-Hub-Id") String hubId,
            @PathVariable String id) {

        log.info("관리자 주문 상세 조회 - orderId: {}", id);

        Order order = orderQueryService.getOrderById(id);

        // Saga 정보 조회 (선택적)
        // TODO: OrderSagaQueryService 구현 후 활성화
        // Optional<OrderSaga> saga = orderSagaQueryService.findByOrderId(id);
        //
        // MasterOrderDetailResponse response;
        // if (saga.isPresent()) {
        //     // Saga 정보 포함
        //     response = MasterOrderDetailResponse.from(order, saga.get());
        // } else {
        //     // Saga 정보 없이
        //     response = MasterOrderDetailResponse.from(order);
        // }

        // 현재는 Saga 정보 없이 반환
        MasterOrderDetailResponse response = MasterOrderDetailResponse.from(order);

        return ApiResponse.success(response);
    }

    /**
     * 주문 상세 조회 (삭제된 것 포함)
     * GET /v1/order/web/master/orders/{id}/with-deleted
     */
    @GetMapping("/orders/{id}/with-deleted")
    public ApiResponse<MasterOrderDetailResponse> getOrderIncludingDeleted(
            @RequestHeader("X-Hub-Id") String hubId,
            @PathVariable String id) {

        log.info("관리자 주문 상세 조회 (삭제 포함) - orderId: {}", id);

        Order order = orderQueryService.getOrderByIdIncludingDeleted(id);

        // Domain → DTO 변환
        MasterOrderDetailResponse response = MasterOrderDetailResponse.from(order);

        return ApiResponse.success(response);
    }

    /**
     * 발송 시한 임박 주문 조회
     * GET /v1/order/web/master/orders/upcoming-deadline
     */
    @GetMapping("/orders/upcoming-deadline")
    public ApiResponse<List<OrderSimpleResponse>> getUpcomingDeadlineOrders(
            @RequestHeader("X-Hub-Id") String hubId,
            @RequestParam(defaultValue = "2") int hours) {

        log.info("발송 시한 임박 주문 조회 - hours: {}", hours);

        List<Order> orders = orderQueryService.getOrdersWithUpcomingDeadline(hours);

        // Domain → DTO 변환
        List<OrderSimpleResponse> responses = orders.stream()
                .map(OrderSimpleResponse::from)
                .toList();

        return ApiResponse.success(responses);
    }

    /**
     * 발송 시한 초과 주문 조회
     * GET /v1/order/web/master/orders/overdue
     */
    @GetMapping("/orders/overdue")
    public ApiResponse<List<OrderSimpleResponse>> getOverdueOrders(
            @RequestHeader("X-Hub-Id") String hubId
    ) {

        log.info("발송 시한 초과 주문 조회");

        List<Order> orders = orderQueryService.getOverdueOrders();

        // Domain → DTO 변환
        List<OrderSimpleResponse> responses = orders.stream()
                .map(OrderSimpleResponse::from)
                .toList();

        return ApiResponse.success(responses);
    }

    /**
     * Saga 진행 상태 조회 (상세)
     * GET /v1/order/web/master/orders/{id}/saga
     *
     * Saga 진행 상태를 별도로 조회하고 싶을 때 사용
     */
    // @GetMapping("/orders/{id}/saga")
    // public ApiResponse<OrderSagaInfoDto> getOrderSaga(@PathVariable String id) {
    //     log.info("주문 Saga 상태 조회 - orderId: {}", id);
    //
    //     OrderSaga saga = orderSagaQueryService.findByOrderId(id)
    //             .orElseThrow(() -> new OrderException(
    //                     OrderErrorCode.ORDER_NOT_FOUND,
    //                     "해당 주문의 Saga를 찾을 수 없습니다."
    //             ));
    //
    //     OrderSagaInfoDto response = OrderSagaInfoDto.from(saga);
    //     return ApiResponse.success(response);
    // }
}
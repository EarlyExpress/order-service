package com.early_express.order_service.domain.order.infrastructure.client.payment;

import com.early_express.order_service.domain.order.infrastructure.client.payment.dto.PaymentVerificationRequest;
import com.early_express.order_service.domain.order.infrastructure.client.payment.dto.PaymentVerificationResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * Payment Service Feign Client
 * 결제 서비스와의 동기 통신
 */
@FeignClient(
        name = "payment-service",
//        url = "${client.payment-service.url}",
        configuration = PaymentClientConfig.class
)
public interface PaymentClient {

    /**
     * 결제 검증 및 등록 (Saga Step 2)
     * - PG사 결제 검증 (금액, 상태 등)
     * - Payment 엔티티 생성
     *
     * @param request 결제 검증 요청
     * @return 검증 결과 (paymentId, status, pgTransactionId)
     */
    @PostMapping("/v1/payment/internal/all/verify-and-register")
    PaymentVerificationResponse verifyAndRegisterPayment(
            @RequestBody PaymentVerificationRequest request
    );
}

package com.early_express.order_service.domain.order.infrastructure.client.ai;

import com.early_express.order_service.domain.order.infrastructure.client.ai.dto.AiTimeCalculationRequest;
import com.early_express.order_service.domain.order.infrastructure.client.ai.dto.AiTimeCalculationResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * AI Service Feign Client
 * AI 기반 배송 시간 계산 서비스와의 동기 통신
 */
@FeignClient(
        name = "ai-service",
        url = "${client.ai-service.url}",
        configuration = AiClientConfig.class
)
public interface AiClient {

    /**
     * AI 배송 시간 계산 (Saga Step 3 확장)
     * - Hub Service의 경로 계산 결과를 기반으로 시간 계산
     * - 발송 시한 및 예상 배송 완료 시간 계산
     * - 납품 희망 시간 준수 가능 여부 판단
     *
     * @param request AI 시간 계산 요청 (Hub 경로 + 주문 정보)
     * @return AI 계산 결과 (발송 시한, 예상 도착 시간, 판단 근거)
     */
    @PostMapping("/v1/aiagent/internal/time/calculate")
    AiTimeCalculationResponse calculateDeliveryTime(
            @RequestBody AiTimeCalculationRequest request
    );
}
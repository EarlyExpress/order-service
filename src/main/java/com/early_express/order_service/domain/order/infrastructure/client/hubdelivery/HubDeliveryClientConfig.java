package com.early_express.order_service.domain.order.infrastructure.client.hubdelivery;

import feign.codec.ErrorDecoder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Hub Delivery Client 설정
 * - ErrorDecoder만 개별 정의
 * - 나머지는 글로벌 FeignConfig 사용
 */
@Configuration
public class HubDeliveryClientConfig {

    /**
     * Hub Delivery 전용 에러 디코더
     */
    @Bean
    public ErrorDecoder hubDeliveryErrorDecoder() {
        return new HubDeliveryErrorDecoder();
    }
}
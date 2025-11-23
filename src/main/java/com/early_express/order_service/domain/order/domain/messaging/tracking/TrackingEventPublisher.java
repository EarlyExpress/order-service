package com.early_express.order_service.domain.order.domain.messaging.tracking;

/**
 * Tracking Event Publisher Interface (도메인 레이어)
 * Infrastructure 레이어에서 구현
 */
public interface TrackingEventPublisher {

    /**
     * 추적 시작 요청 이벤트 발행
     * Order Service → Tracking Service
     *
     * @param eventData 추적 시작 요청 이벤트 데이터
     */
    void publishTrackingStartRequested(TrackingStartRequestedEventData eventData);
}
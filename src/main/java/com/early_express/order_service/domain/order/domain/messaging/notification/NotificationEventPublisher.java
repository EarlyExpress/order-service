package com.early_express.order_service.domain.order.domain.messaging.notification;

/**
 * Notification Event Publisher Interface (도메인 레이어)
 * Infrastructure 레이어에서 구현
 */
public interface NotificationEventPublisher {

    /**
     * 알림 발송 요청 이벤트 발행
     * Order Service → Notification Service
     *
     * @param eventData 알림 발송 요청 이벤트 데이터
     */
    void publishNotificationRequested(NotificationRequestedEventData eventData);
}
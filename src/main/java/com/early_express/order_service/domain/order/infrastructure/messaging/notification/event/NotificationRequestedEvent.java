package com.early_express.order_service.domain.order.domain.messaging.notification.event;

import com.early_express.order_service.domain.order.domain.messaging.notification.NotificationRequestedEventData;
import com.early_express.order_service.global.infrastructure.event.base.BaseEvent;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

/**
 * 알림 발송 요청 이벤트 (Kafka 메시지)
 * Order Service → Notification Service
 * Topic: notification-events
 */
@Getter
@SuperBuilder
@NoArgsConstructor
public class NotificationRequestedEvent extends BaseEvent {

    private String orderId;
    private String receiverName;
    private String receiverPhone;
    private String receiverEmail;
    private String orderNumber;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime estimatedDeliveryTime;

    private String deliveryAddress;
    private String notificationType;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime requestedAt;

    /**
     * EventData로부터 Event 생성
     */
    public static NotificationRequestedEvent from(NotificationRequestedEventData data) {
        NotificationRequestedEvent event = NotificationRequestedEvent.builder()
                .orderId(data.getOrderId())
                .receiverName(data.getReceiverName())
                .receiverPhone(data.getReceiverPhone())
                .receiverEmail(data.getReceiverEmail())
                .orderNumber(data.getOrderNumber())
                .estimatedDeliveryTime(data.getEstimatedDeliveryTime())
                .deliveryAddress(data.getDeliveryAddress())
                .notificationType(data.getNotificationType())
                .requestedAt(data.getRequestedAt())
                .build();

        event.initBaseEvent("NOTIFICATION_REQUESTED", "order-service");

        return event;
    }
}
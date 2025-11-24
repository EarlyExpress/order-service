package com.early_express.order_service.domain.order.domain.messaging.notification;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 알림 발송 요청 이벤트 데이터 (도메인)
 * Order Service → Notification Service
 */
@Getter
@Builder
public class NotificationRequestedEventData {

    /**
     * 주문 ID
     */
    private final String orderId;

    /**
     * 수령자 이름
     */
    private final String receiverName;

    /**
     * 수령자 연락처
     */
    private final String receiverPhone;

    /**
     * 수령자 이메일
     */
    private final String receiverEmail;

    /**
     * 주문 번호
     */
    private final String orderNumber;

    /**
     * 예상 배송 완료 시간
     */
    private final LocalDateTime estimatedDeliveryTime;

    /**
     * 배송 주소
     */
    private final String deliveryAddress;

    /**
     * 알림 유형
     * ORDER_CONFIRMED: 주문 확정
     */
    private final String notificationType;

    /**
     * 요청 시간
     */
    private final LocalDateTime requestedAt;

    /**
     * 알림 발송 요청 이벤트 데이터 생성
     */
    public static NotificationRequestedEventData of(
            String orderId,
            String receiverName,
            String receiverPhone,
            String receiverEmail,
            String orderNumber,
            LocalDateTime estimatedDeliveryTime,
            String deliveryAddress) {

        return NotificationRequestedEventData.builder()
                .orderId(orderId)
                .receiverName(receiverName)
                .receiverPhone(receiverPhone)
                .receiverEmail(receiverEmail)
                .orderNumber(orderNumber)
                .estimatedDeliveryTime(estimatedDeliveryTime)
                .deliveryAddress(deliveryAddress)
                .notificationType("ORDER_CONFIRMED")
                .requestedAt(LocalDateTime.now())
                .build();
    }
}
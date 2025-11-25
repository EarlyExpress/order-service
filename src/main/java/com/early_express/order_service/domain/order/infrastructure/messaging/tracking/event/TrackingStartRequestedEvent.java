package com.early_express.order_service.domain.order.domain.messaging.tracking.event;

import com.early_express.order_service.domain.order.domain.messaging.tracking.TrackingStartRequestedEventData;
import com.early_express.order_service.global.infrastructure.event.base.BaseEvent;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

/**
 * 추적 시작 요청 이벤트 (Kafka 메시지)
 * Order Service → Tracking Service
 * Topic: tracking-events
 */
@Getter
@SuperBuilder
@NoArgsConstructor
public class TrackingStartRequestedEvent extends BaseEvent {

    private String orderId;
    private String orderNumber;
    private String hubDeliveryId;
    private String lastMileDeliveryId;
    private String originHubId;
    private String destinationHubId;
    private String routingHub;
    private Boolean requiresHubDelivery;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime estimatedDeliveryTime;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime requestedAt;

    /**
     * EventData로부터 Event 생성
     */
    public static TrackingStartRequestedEvent from(TrackingStartRequestedEventData data) {
        TrackingStartRequestedEvent event = TrackingStartRequestedEvent.builder()
                .orderId(data.getOrderId())
                .orderNumber(data.getOrderNumber())
                .hubDeliveryId(data.getHubDeliveryId())
                .lastMileDeliveryId(data.getLastMileDeliveryId())
                .originHubId(data.getOriginHubId())
                .destinationHubId(data.getDestinationHubId())
                .routingHub(data.getRoutingHub())
                .requiresHubDelivery(data.getRequiresHubDelivery())
                .estimatedDeliveryTime(data.getEstimatedDeliveryTime())
                .requestedAt(data.getRequestedAt())
                .build();

        event.initBaseEvent("TRACKING_START_REQUESTED", "order-service");

        return event;
    }
}
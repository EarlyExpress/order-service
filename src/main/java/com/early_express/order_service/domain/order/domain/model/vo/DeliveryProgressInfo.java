package com.early_express.order_service.domain.order.domain.model.vo;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 배송 진행 정보 Value Object
 * 실제 배송 진행 과정의 시간 정보를 담음
 */
@Getter
@EqualsAndHashCode
public class DeliveryProgressInfo {

    private final LocalDateTime actualDepartureTime; // 실제 발송 시간
    private final LocalDateTime hubArrivalTime; // 허브 도착 시간
    private final LocalDateTime finalDeliveryStartTime; // 최종 배송 시작 시간
    private final LocalDateTime actualDeliveryTime; // 실제 배송 완료 시간
    private final String signature; // 서명 (Base64)
    private final String actualReceiverName; // 실제 수령자

    @Builder
    private DeliveryProgressInfo(
            LocalDateTime actualDepartureTime,
            LocalDateTime hubArrivalTime,
            LocalDateTime finalDeliveryStartTime,
            LocalDateTime actualDeliveryTime,
            String signature,
            String actualReceiverName) {

        this.actualDepartureTime = actualDepartureTime;
        this.hubArrivalTime = hubArrivalTime;
        this.finalDeliveryStartTime = finalDeliveryStartTime;
        this.actualDeliveryTime = actualDeliveryTime;
        this.signature = signature;
        this.actualReceiverName = actualReceiverName;
    }

    /**
     * 초기 상태 (배송 진행 전)
     */
    public static DeliveryProgressInfo empty() {
        return DeliveryProgressInfo.builder().build();
    }

    /**
     * 실제 발송 시간 기록
     */
    public DeliveryProgressInfo withActualDepartureTime(LocalDateTime actualDepartureTime) {
        return DeliveryProgressInfo.builder()
                .actualDepartureTime(actualDepartureTime)
                .hubArrivalTime(this.hubArrivalTime)
                .finalDeliveryStartTime(this.finalDeliveryStartTime)
                .actualDeliveryTime(this.actualDeliveryTime)
                .signature(this.signature)
                .actualReceiverName(this.actualReceiverName)
                .build();
    }

    /**
     * 허브 도착 시간 기록
     */
    public DeliveryProgressInfo withHubArrivalTime(LocalDateTime hubArrivalTime) {
        return DeliveryProgressInfo.builder()
                .actualDepartureTime(this.actualDepartureTime)
                .hubArrivalTime(hubArrivalTime)
                .finalDeliveryStartTime(this.finalDeliveryStartTime)
                .actualDeliveryTime(this.actualDeliveryTime)
                .signature(this.signature)
                .actualReceiverName(this.actualReceiverName)
                .build();
    }

    /**
     * 최종 배송 시작 시간 기록
     */
    public DeliveryProgressInfo withFinalDeliveryStartTime(LocalDateTime finalDeliveryStartTime) {
        return DeliveryProgressInfo.builder()
                .actualDepartureTime(this.actualDepartureTime)
                .hubArrivalTime(this.hubArrivalTime)
                .finalDeliveryStartTime(finalDeliveryStartTime)
                .actualDeliveryTime(this.actualDeliveryTime)
                .signature(this.signature)
                .actualReceiverName(this.actualReceiverName)
                .build();
    }

    /**
     * 배송 완료 정보 기록
     */
    public DeliveryProgressInfo withDeliveryCompleted(
            LocalDateTime actualDeliveryTime,
            String signature,
            String actualReceiverName) {

        return DeliveryProgressInfo.builder()
                .actualDepartureTime(this.actualDepartureTime)
                .hubArrivalTime(this.hubArrivalTime)
                .finalDeliveryStartTime(this.finalDeliveryStartTime)
                .actualDeliveryTime(actualDeliveryTime)
                .signature(signature)
                .actualReceiverName(actualReceiverName)
                .build();
    }

    /**
     * 발송이 시작되었는지 확인
     */
    public boolean isDeparted() {
        return actualDepartureTime != null;
    }

    /**
     * 허브에 도착했는지 확인
     */
    public boolean isArrivedAtHub() {
        return hubArrivalTime != null;
    }

    /**
     * 최종 배송이 시작되었는지 확인
     */
    public boolean isFinalDeliveryStarted() {
        return finalDeliveryStartTime != null;
    }

    /**
     * 배송이 완료되었는지 확인
     */
    public boolean isCompleted() {
        return actualDeliveryTime != null && signature != null && actualReceiverName != null;
    }
}

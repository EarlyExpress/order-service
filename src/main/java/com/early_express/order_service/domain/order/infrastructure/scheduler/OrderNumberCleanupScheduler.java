package com.early_express.order_service.domain.order.infrastructure.scheduler;

import com.early_express.order_service.domain.order.application.service.OrderNumberGeneratorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 주문번호 카운터 정리 스케줄러
 *
 * 매일 자정에 이전 날짜의 카운터를 메모리에서 제거하여
 * 메모리 누수를 방지합니다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OrderNumberCleanupScheduler {

    private final OrderNumberGeneratorService orderNumberGeneratorService;

    /**
     * 매일 자정 1시에 이전 날짜 카운터 정리
     * Cron: 초 분 시 일 월 요일
     */
    @Scheduled(cron = "0 0 1 * * *")
    public void cleanupOldCounters() {
        log.info("주문번호 카운터 정리 시작");

        try {
            orderNumberGeneratorService.cleanupOldCounters();
            log.info("주문번호 카운터 정리 완료");
        } catch (Exception e) {
            log.error("주문번호 카운터 정리 실패", e);
        }
    }

    /**
     * 매시간 현재 카운터 상태 로깅 (모니터링용)
     * Cron: 매시간 0분
     */
    @Scheduled(cron = "0 0 * * * *")
    public void logCurrentStatus() {
        try {
            int currentSequence = orderNumberGeneratorService.getCurrentSequence();
            int remainingCapacity = orderNumberGeneratorService.getRemainingCapacity();

            log.info("주문번호 생성 현황 - 현재: {}, 남은 용량: {}",
                    currentSequence, remainingCapacity);

            // 용량 부족 경고 (남은 개수가 50개 이하)
            if (remainingCapacity <= 50) {
                log.warn("⚠️ 일일 주문번호 생성 용량 부족 - 남은 용량: {}", remainingCapacity);
            }

        } catch (Exception e) {
            log.error("주문번호 상태 로깅 실패", e);
        }
    }
}
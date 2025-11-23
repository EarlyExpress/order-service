package com.early_express.order_service.domain.order.application.service;

import com.early_express.order_service.domain.order.domain.model.vo.OrderNumber;
import com.early_express.order_service.domain.order.domain.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 주문 번호 생성 서비스
 *
 * 형식: ORD-YYYYMMDD-XXX (예: ORD-20250121-001)
 *
 * 단일 서버 환경을 가정하여 ConcurrentHashMap으로 중복 방지
 * - Key: 날짜 (YYYYMMDD)
 * - Value: AtomicInteger (일련번호 카운터)
 *
 * 참고: 분산 환경에서는 Redis INCR 또는 DB Sequence 사용 필요
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OrderNumberGeneratorService {

    private final OrderRepository orderRepository;

    /**
     * 날짜별 주문번호 카운터
     * Key: YYYYMMDD (예: "20250121")
     * Value: AtomicInteger (일련번호)
     */
    private final ConcurrentHashMap<String, AtomicInteger> dailyCounters = new ConcurrentHashMap<>();

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final String PREFIX = "ORD-";

    /**
     * 새로운 주문 번호 생성
     *
     * @return 생성된 주문 번호
     */
    public OrderNumber generateOrderNumber() {
        String today = LocalDate.now().format(DATE_FORMATTER);

        // 오늘 날짜의 카운터 가져오기 (없으면 새로 생성)
        AtomicInteger counter = dailyCounters.computeIfAbsent(today, key -> {
            log.info("새로운 날짜의 주문번호 카운터 초기화 - date: {}", key);
            return new AtomicInteger(0);
        });

        // 일련번호 증가 및 주문번호 생성
        int sequenceNumber;
        OrderNumber orderNumber;

        do {
            // 카운터 증가 (1부터 시작)
            sequenceNumber = counter.incrementAndGet();

            // 주문번호 생성
            orderNumber = OrderNumber.generate(sequenceNumber);

            // DB에 중복 체크 (안전장치)
            if (!orderRepository.existsByOrderNumber(orderNumber)) {
                break;
            }

            // 중복이면 다시 시도
            log.warn("주문번호 중복 발견, 재시도 - orderNumber: {}", orderNumber.getValue());

        } while (sequenceNumber < 999); // 최대 999개까지

        if (sequenceNumber >= 999) {
            log.error("일일 주문번호 한도 초과 - date: {}, count: {}", today, sequenceNumber);
            throw new IllegalStateException("일일 주문번호 생성 한도를 초과했습니다.");
        }

        log.info("주문번호 생성 완료 - orderNumber: {}, sequence: {}",
                orderNumber.getValue(), sequenceNumber);

        return orderNumber;
    }

    /**
     * 특정 날짜의 카운터 초기화
     * (일일 배치 작업 등에서 사용)
     *
     * @param date 초기화할 날짜
     */
    public void resetDailyCounter(LocalDate date) {
        String dateKey = date.format(DATE_FORMATTER);
        dailyCounters.remove(dateKey);
        log.info("주문번호 카운터 초기화 - date: {}", dateKey);
    }

    /**
     * 이전 날짜의 카운터 정리
     * (메모리 관리를 위해 주기적으로 호출)
     */
    public void cleanupOldCounters() {
        String today = LocalDate.now().format(DATE_FORMATTER);

        dailyCounters.keySet().removeIf(dateKey -> {
            if (!dateKey.equals(today)) {
                log.info("이전 날짜 카운터 삭제 - date: {}", dateKey);
                return true;
            }
            return false;
        });
    }

    /**
     * 현재 일련번호 조회 (디버깅/모니터링용)
     *
     * @return 오늘의 현재 일련번호
     */
    public int getCurrentSequence() {
        String today = LocalDate.now().format(DATE_FORMATTER);
        AtomicInteger counter = dailyCounters.get(today);
        return counter != null ? counter.get() : 0;
    }

    /**
     * 특정 날짜의 생성 가능한 주문 수 확인
     *
     * @return 남은 주문 생성 가능 수
     */
    public int getRemainingCapacity() {
        int currentSequence = getCurrentSequence();
        return Math.max(0, 999 - currentSequence);
    }
}

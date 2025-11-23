package com.early_express.order_service.domain.order.application.service;

import com.early_express.order_service.domain.order.domain.model.vo.OrderNumber;
import com.early_express.order_service.domain.order.domain.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
@DisplayName("OrderNumberGeneratorService 테스트")
class OrderNumberGeneratorServiceTest {

    @InjectMocks
    private OrderNumberGeneratorService generatorService;

    @Mock
    private OrderRepository orderRepository;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");

    @BeforeEach
    void setUp() {
        generatorService.cleanupOldCounters();
    }

    @Test
    @DisplayName("주문 번호 생성 성공")
    void generateOrderNumber_Success() {
        // given
        given(orderRepository.existsByOrderNumber(any(OrderNumber.class))).willReturn(false);

        // when
        OrderNumber orderNumber = generatorService.generateOrderNumber();

        // then
        assertThat(orderNumber).isNotNull();
        assertThat(orderNumber.getValue()).startsWith("ORD-");
        assertThat(orderNumber.getValue()).contains(LocalDate.now().format(DATE_FORMATTER));
        assertThat(orderNumber.getValue()).endsWith("-001"); // 첫 번째 주문
    }

    @Test
    @DisplayName("연속된 주문 번호 생성 - 일련번호 증가")
    void generateOrderNumber_SequentialNumbers() {
        // given
        given(orderRepository.existsByOrderNumber(any(OrderNumber.class))).willReturn(false);

        // when
        OrderNumber first = generatorService.generateOrderNumber();
        OrderNumber second = generatorService.generateOrderNumber();
        OrderNumber third = generatorService.generateOrderNumber();

        // then
        assertThat(first.getValue()).endsWith("-001");
        assertThat(second.getValue()).endsWith("-002");
        assertThat(third.getValue()).endsWith("-003");
    }

    @Test
    @DisplayName("주문 번호 중복 발생 시 재시도")
    void generateOrderNumber_RetryOnDuplicate() {
        // given
        given(orderRepository.existsByOrderNumber(any(OrderNumber.class)))
                .willReturn(true)  // 첫 번째 시도: 중복
                .willReturn(false); // 두 번째 시도: 성공

        // when
        OrderNumber orderNumber = generatorService.generateOrderNumber();

        // then
        assertThat(orderNumber).isNotNull();
        assertThat(orderNumber.getValue()).endsWith("-002"); // 중복으로 인해 2번으로 생성됨
    }

    @Test
    @DisplayName("현재 일련번호 조회")
    void getCurrentSequence_Success() {
        // given
        given(orderRepository.existsByOrderNumber(any(OrderNumber.class))).willReturn(false);

        generatorService.generateOrderNumber();
        generatorService.generateOrderNumber();
        generatorService.generateOrderNumber();

        // when
        int currentSequence = generatorService.getCurrentSequence();

        // then
        assertThat(currentSequence).isEqualTo(3);
    }

    @Test
    @DisplayName("남은 생성 가능 주문 수 확인")
    void getRemainingCapacity_Success() {
        // given
        given(orderRepository.existsByOrderNumber(any(OrderNumber.class))).willReturn(false);

        generatorService.generateOrderNumber();
        generatorService.generateOrderNumber();

        // when
        int remaining = generatorService.getRemainingCapacity();

        // then
        assertThat(remaining).isEqualTo(997); // 999 - 2
    }

    @Test
    @DisplayName("날짜별 카운터 초기화")
    void resetDailyCounter_Success() {
        // given
        given(orderRepository.existsByOrderNumber(any(OrderNumber.class))).willReturn(false);

        generatorService.generateOrderNumber();
        generatorService.generateOrderNumber();

        assertThat(generatorService.getCurrentSequence()).isEqualTo(2);

        // when
        generatorService.resetDailyCounter(LocalDate.now());

        // then
        assertThat(generatorService.getCurrentSequence()).isEqualTo(0);
    }

    @Test
    @DisplayName("이전 날짜 카운터 정리")
    void cleanupOldCounters_Success() {
        // given
        given(orderRepository.existsByOrderNumber(any(OrderNumber.class))).willReturn(false);

        generatorService.generateOrderNumber();

        // when
        generatorService.cleanupOldCounters();

        // then
        // 오늘 카운터는 유지되어야 함
        assertThat(generatorService.getCurrentSequence()).isEqualTo(1);
    }

    @Test
    @DisplayName("일일 주문번호 한도 초과 예외")
    void generateOrderNumber_ExceedsLimit() {
        // given
        given(orderRepository.existsByOrderNumber(any(OrderNumber.class))).willReturn(true);

        // when & then
        assertThatThrownBy(() -> {
            for (int i = 0; i < 1000; i++) {
                generatorService.generateOrderNumber();
            }
        }).isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("일일 주문번호 생성 한도를 초과했습니다");
    }

    @Test
    @DisplayName("주문 번호 형식 검증")
    void generateOrderNumber_ValidFormat() {
        // given
        given(orderRepository.existsByOrderNumber(any(OrderNumber.class))).willReturn(false);

        // when
        OrderNumber orderNumber = generatorService.generateOrderNumber();

        // then
        String value = orderNumber.getValue();
        assertThat(value).matches("ORD-\\d{8}-\\d{3}"); // ORD-YYYYMMDD-XXX 형식
    }
}
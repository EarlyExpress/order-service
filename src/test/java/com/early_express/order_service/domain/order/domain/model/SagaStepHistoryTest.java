package com.early_express.order_service.domain.order.domain.model;

import com.early_express.order_service.domain.order.domain.model.vo.SagaId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

@DisplayName("SagaStepHistory domain 테스트")
class SagaStepHistoryTest {

    @Test
    @DisplayName("새로운 Step History를 생성할 수 있다")
    void createStepHistory() {
        // given
        SagaId sagaId = SagaId.create();
        SagaStep step = SagaStep.STOCK_RESERVE;

        // when
        SagaStepHistory history = SagaStepHistory.create(sagaId, step);

        // then
        assertThat(history).isNotNull();
        assertThat(history.getSagaIdValue()).isEqualTo(sagaId.getValue());
        assertThat(history.getStep()).isEqualTo(step);
        assertThat(history.getStatus()).isEqualTo(StepStatus.PENDING);
        assertThat(history.getRetryCount()).isEqualTo(0);
    }

    @Test
    @DisplayName("Step을 시작할 수 있다")
    void startStep() {
        // given
        SagaStepHistory history = SagaStepHistory.create(SagaId.create(), SagaStep.STOCK_RESERVE);
        Object requestData = "requestData";

        // when
        history.start(requestData);

        // then
        assertThat(history.getStatus()).isEqualTo(StepStatus.IN_PROGRESS);
        assertThat(history.getRequest()).isNotNull();
    }

    @Test
    @DisplayName("Step을 완료할 수 있다")
    void completeStep() {
        // given
        SagaStepHistory history = SagaStepHistory.create(SagaId.create(), SagaStep.STOCK_RESERVE);
        history.start("request");
        Object responseData = "responseData";

        // when
        history.complete(responseData);

        // then
        assertThat(history.getStatus()).isEqualTo(StepStatus.SUCCESS);
        assertThat(history.isSuccessful()).isTrue();
        assertThat(history.getResponse()).isNotNull();
        assertThat(history.getCompletedAt()).isNotNull();
    }

    @Test
    @DisplayName("Step이 실패할 수 있다")
    void failStep() {
        // given
        SagaStepHistory history = SagaStepHistory.create(SagaId.create(), SagaStep.STOCK_RESERVE);
        history.start("request");
        String errorMessage = "재고 부족";

        // when
        history.fail(errorMessage);

        // then
        assertThat(history.getStatus()).isEqualTo(StepStatus.FAILED);
        assertThat(history.isFailed()).isTrue();
        assertThat(history.getErrorMessage()).isEqualTo(errorMessage);
        assertThat(history.getCompletedAt()).isNotNull();
    }

    @Test
    @DisplayName("Step을 보상 완료 처리할 수 있다")
    void compensateStep() {
        // given
        SagaStepHistory history = SagaStepHistory.create(SagaId.create(), SagaStep.STOCK_RESTORE);
        history.start("request");
        history.complete("response");

        // when
        history.compensated();

        // then
        assertThat(history.getStatus()).isEqualTo(StepStatus.COMPENSATED);
    }

    @Test
    @DisplayName("재시도 횟수를 증가시킬 수 있다")
    void incrementRetryCount() {
        // given
        SagaStepHistory history = SagaStepHistory.create(SagaId.create(), SagaStep.STOCK_RESERVE);

        // when
        history.incrementRetryCount();
        history.incrementRetryCount();

        // then
        assertThat(history.getRetryCount()).isEqualTo(2);
    }

    @Test
    @DisplayName("최대 재시도 횟수 초과 여부를 확인할 수 있다")
    void checkMaxRetriesExceeded() {
        // given
        SagaStepHistory history = SagaStepHistory.create(SagaId.create(), SagaStep.STOCK_RESERVE);
        int maxRetries = 3;

        // when
        history.incrementRetryCount();
        history.incrementRetryCount();
        history.incrementRetryCount();

        // then
        assertThat(history.hasExceededMaxRetries(maxRetries)).isTrue();
    }

    @Test
    @DisplayName("최대 재시도 횟수 이내인지 확인할 수 있다")
    void checkMaxRetriesNotExceeded() {
        // given
        SagaStepHistory history = SagaStepHistory.create(SagaId.create(), SagaStep.STOCK_RESERVE);
        int maxRetries = 3;

        // when
        history.incrementRetryCount();
        history.incrementRetryCount();

        // then
        assertThat(history.hasExceededMaxRetries(maxRetries)).isFalse();
    }

    @Test
    @DisplayName("null 데이터로 완료해도 예외가 발생하지 않는다")
    void completeWithNullData() {
        // given
        SagaStepHistory history = SagaStepHistory.create(SagaId.create(), SagaStep.STOCK_RESERVE);
        history.start(null);

        // when & then
        assertThatCode(() -> history.complete(null)).doesNotThrowAnyException();
        assertThat(history.isSuccessful()).isTrue();
    }

    @Test
    @DisplayName("복잡한 객체도 JSON으로 변환된다")
    void handleComplexObject() {
        // given
        SagaStepHistory history = SagaStepHistory.create(SagaId.create(), SagaStep.STOCK_RESERVE);
        var requestData = new TestData("test", 123);

        // when
        history.start(requestData);

        // then
        assertThat(history.getRequest()).isNotNull();
    }

    // 테스트용 데이터 클래스
    static class TestData {
        private final String name;
        private final int value;

        TestData(String name, int value) {
            this.name = name;
            this.value = value;
        }

        public String getName() { return name; }
        public int getValue() { return value; }
    }
}
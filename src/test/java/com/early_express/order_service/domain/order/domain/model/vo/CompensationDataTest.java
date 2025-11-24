package com.early_express.order_service.domain.order.domain.model.vo;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;

@DisplayName("CompensationData vo 테스트")
class CompensationDataTest {

    @Test
    @DisplayName("빈 보상 데이터를 생성할 수 있다")
    void createEmptyCompensationData() {
        // when
        CompensationData data = CompensationData.empty();

        // then
        assertThat(data.isEmpty()).isTrue();
        assertThat(data.getData()).isEmpty();
    }

    @Test
    @DisplayName("기존 Map으로부터 보상 데이터를 생성할 수 있다")
    void createFromMap() {
        // given
        Map<String, Object> map = new HashMap<>();
        map.put("step1", "data1");
        map.put("step2", "data2");

        // when
        CompensationData data = CompensationData.from(map);

        // then
        assertThat(data.isEmpty()).isFalse();
        assertThat(data.hasStepData("step1")).isTrue();
        assertThat(data.getStepData("step1")).isEqualTo("data1");
    }

    @Test
    @DisplayName("JSON 문자열로부터 보상 데이터를 생성할 수 있다")
    void createFromJson() {
        // given
        String json = "{\"step1\":\"data1\",\"step2\":\"data2\"}";

        // when
        CompensationData data = CompensationData.fromJson(json);

        // then
        assertThat(data.isEmpty()).isFalse();
        assertThat(data.hasStepData("step1")).isTrue();
        assertThat(data.getStepData("step1")).isEqualTo("data1");
    }

    @Test
    @DisplayName("잘못된 JSON으로 생성하면 예외가 발생한다")
    void createFromInvalidJson() {
        // given
        String invalidJson = "invalid json";

        // when & then
        assertThatThrownBy(() -> CompensationData.fromJson(invalidJson))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("보상 데이터 JSON 파싱 실패");
    }

    @Test
    @DisplayName("Step 데이터를 추가할 수 있다")
    void addStepData() {
        // given
        CompensationData data = CompensationData.empty();
        String stepName = "STOCK_RESERVE";
        Object stepData = Map.of("productId", "PROD-001", "quantity", 5);

        // when
        CompensationData updated = data.addStepData(stepName, stepData);

        // then
        assertThat(updated.hasStepData(stepName)).isTrue();
        assertThat(updated.getStepData(stepName)).isEqualTo(stepData);
    }

    @Test
    @DisplayName("여러 Step 데이터를 추가할 수 있다")
    void addMultipleStepData() {
        // given
        CompensationData data = CompensationData.empty();

        // when
        CompensationData updated = data
                .addStepData("step1", "data1")
                .addStepData("step2", "data2")
                .addStepData("step3", "data3");

        // then
        assertThat(updated.hasStepData("step1")).isTrue();
        assertThat(updated.hasStepData("step2")).isTrue();
        assertThat(updated.hasStepData("step3")).isTrue();
    }

    @Test
    @DisplayName("Step 데이터를 조회할 수 있다")
    void getStepData() {
        // given
        CompensationData data = CompensationData.empty()
                .addStepData("STOCK_RESERVE", "stockData");

        // when
        Object stepData = data.getStepData("STOCK_RESERVE");

        // then
        assertThat(stepData).isEqualTo("stockData");
    }

    @Test
    @DisplayName("없는 Step 데이터를 조회하면 null을 반환한다")
    void getNonExistentStepData() {
        // given
        CompensationData data = CompensationData.empty();

        // when
        Object stepData = data.getStepData("NON_EXISTENT");

        // then
        assertThat(stepData).isNull();
    }

    @Test
    @DisplayName("Step 데이터 존재 여부를 확인할 수 있다")
    void checkStepDataExists() {
        // given
        CompensationData data = CompensationData.empty()
                .addStepData("step1", "data1");

        // when & then
        assertThat(data.hasStepData("step1")).isTrue();
        assertThat(data.hasStepData("step2")).isFalse();
    }

    @Test
    @DisplayName("보상 데이터를 JSON으로 변환할 수 있다")
    void convertToJson() {
        // given
        CompensationData data = CompensationData.empty()
                .addStepData("step1", "data1");

        // when
        String json = data.toJson();

        // then
        assertThat(json).contains("step1");
        assertThat(json).contains("data1");
    }

    @Test
    @DisplayName("JSON 변환 후 다시 파싱하면 동일한 데이터를 얻는다")
    void jsonRoundTrip() {
        // given
        CompensationData original = CompensationData.empty()
                .addStepData("step1", "data1")
                .addStepData("step2", "data2");

        // when
        String json = original.toJson();
        CompensationData parsed = CompensationData.fromJson(json);

        // then
        assertThat(parsed.getStepData("step1")).isEqualTo("data1");
        assertThat(parsed.getStepData("step2")).isEqualTo("data2");
    }

    @Test
    @DisplayName("데이터 복사본을 반환하여 불변성을 보장한다")
    void ensureImmutability() {
        // given
        CompensationData data = CompensationData.empty()
                .addStepData("step1", "data1");

        // when
        Map<String, Object> dataMap = data.getData();
        dataMap.put("step2", "data2"); // 외부에서 수정 시도

        // then
        assertThat(data.hasStepData("step2")).isFalse(); // 원본은 변경되지 않음
    }

    @Test
    @DisplayName("같은 값을 가진 CompensationData는 동일하다")
    void equalCompensationData() {
        // given
        CompensationData data1 = CompensationData.empty().addStepData("step1", "data1");
        CompensationData data2 = CompensationData.empty().addStepData("step1", "data1");

        // when & then
        assertThat(data1).isEqualTo(data2);
        assertThat(data1.hashCode()).isEqualTo(data2.hashCode());
    }
}
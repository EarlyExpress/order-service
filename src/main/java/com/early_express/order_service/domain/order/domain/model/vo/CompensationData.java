package com.early_express.order_service.domain.order.domain.model.vo;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

/**
 * 보상 데이터 Value Object
 * Saga Step별 보상 트랜잭션에 필요한 데이터를 JSON 형태로 관리
 */
@Getter
@EqualsAndHashCode
public class CompensationData {

    private static final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    private final Map<String, Object> data;

    private CompensationData(Map<String, Object> data) {
        this.data = new HashMap<>(data);
    }

    /**
     * 빈 보상 데이터 생성
     */
    public static CompensationData empty() {
        return new CompensationData(new HashMap<>());
    }

    /**
     * 기존 데이터로부터 생성
     */
    public static CompensationData from(Map<String, Object> data) {
        return new CompensationData(data);
    }

    /**
     * JSON 문자열로부터 생성
     */
    public static CompensationData fromJson(String json) {
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> data = objectMapper.readValue(json, Map.class);
            return new CompensationData(data);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("보상 데이터 JSON 파싱 실패", e);
        }
    }

    /**
     * Step별 보상 데이터 추가
     */
    public CompensationData addStepData(String stepName, Object stepData) {
        Map<String, Object> newData = new HashMap<>(this.data);
        newData.put(stepName, stepData);
        return new CompensationData(newData);
    }

    /**
     * Step별 보상 데이터 조회
     */
    public Object getStepData(String stepName) {
        return this.data.get(stepName);
    }

    /**
     * Step 데이터 존재 여부 확인
     */
    public boolean hasStepData(String stepName) {
        return this.data.containsKey(stepName);
    }

    /**
     * JSON 문자열로 변환
     */
    public String toJson() {
        try {
            return objectMapper.writeValueAsString(this.data);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("보상 데이터 JSON 변환 실패", e);
        }
    }

    /**
     * 데이터 복사본 반환 (불변성 보장)
     */
    public Map<String, Object> getData() {
        return new HashMap<>(this.data);
    }

    /**
     * 비어있는지 확인
     */
    public boolean isEmpty() {
        return this.data.isEmpty();
    }
}

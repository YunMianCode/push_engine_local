package com.qunar.marketing.mkt.pub.alg.tfserving.push.engine.server.service.utils.domain;

import lombok.Builder;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;
@Data
@Builder
public class PredictResult {
    private Map<String, float[][]> results;
    private boolean success;
    private String errorMessage;
    private Map<String, Object> metadata;

//    Map<String, float[][]> getVectors() {
//        return results;
//    }
//
//    void setVectors(Map<String, float[][]> results) {
//        this.results = results;
//    }

    public static PredictResult success() {
        return PredictResult.builder().success(true).build();
    }
    public static PredictResult success(Map<String, float[][]> results) {
        return PredictResult.builder().success(true).results(results).build();
    }

    public static PredictResult failure(String errorMessage) {
        return PredictResult.builder()
                .success(false)
                .errorMessage(errorMessage)
                .build();
    }

    public void addMetadata(String key, Object value) {
        if (metadata == null) {
            metadata = new HashMap<>();
        }
        metadata.put(key, value);
    }

}

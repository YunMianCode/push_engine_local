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

    /**
     * 构建一个无结果数据的成功预测结果
     * <p>补充说明：用于预测成功但不需要返回结果矩阵的场景
     * @return 标记为成功的 PredictResult 实例
     */
    public static PredictResult success() {
        return PredictResult.builder().success(true).build();
    }

    /**
     * 构建一个携带预测结果矩阵的成功预测结果
     * <p>补充说明：将模型输出的多维结果矩阵封装进返回对象
     * @param results 预测结果矩阵，key 为输出名称，value 为对应的浮点二维数组
     * @return 标记为成功并包含结果数据的 PredictResult 实例
     */
    public static PredictResult success(Map<String, float[][]> results) {
        return PredictResult.builder().success(true).results(results).build();
    }

    /**
     * 构建一个失败的预测结果
     * <p>补充说明：用于预测过程中发生错误时返回，携带错误信息以便上层处理
     * @param errorMessage 失败原因描述
     * @return 标记为失败并携带错误信息的 PredictResult 实例
     */
    public static PredictResult failure(String errorMessage) {
        return PredictResult.builder()
                .success(false)
                .errorMessage(errorMessage)
                .build();
    }

    /**
     * 向元数据中追加一条键值对信息
     * <p>补充说明：metadata 为空时惰性初始化 HashMap，用于存放预测过程中的附加信息（如耗时、模型版本等）
     * @param key 元数据键
     * @param value 元数据值
     */
    public void addMetadata(String key, Object value) {
        if (metadata == null) {
            metadata = new HashMap<>();
        }
        metadata.put(key, value);
    }

}

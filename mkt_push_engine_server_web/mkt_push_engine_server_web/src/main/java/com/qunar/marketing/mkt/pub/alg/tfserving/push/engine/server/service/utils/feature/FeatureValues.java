package com.qunar.marketing.mkt.pub.alg.tfserving.push.engine.server.service.utils.feature;

import lombok.Data;

import java.util.Map;

@Data
public class FeatureValues {
    private Map<String, float[][]> floatFeatureValues;
    private Map<String, int[][]> intFeatureValues;
    private Map<String, String[][]>  stringFeatureValues;
}

package com.qunar.marketing.mkt.pub.alg.tfserving.push.engine.server.service.utils.domain;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class PredictParam {
    private String modelKey;

    private float[][] matrix;

    private Map<String, float[][]> subMatrices;

    private Map<String, String[][]> subStringMatrices;

    private Map<String , int[][]> subIntMatrices;

    private int batchSize;

    private List<String> outputs;

    //private TfMatrices tensorMatrix;

    private boolean classifyMatrix;

    private boolean isDebug = false;
}

package com.qunar.marketing.mkt.pub.alg.tfserving.push.engine.server.service.utils.model;

import java.util.List;
import java.util.Map;
import lombok.Data;

@Data
public class ModelServerContext {
    private String modelKey;

    private float[][] matrix;

    private Map<String, float[][]> subMatrices;

    private Map<String, int[][]> subIntMatrices;

    private Map<String, String[][]> subStringMatrices;

    private int batchSize;

    private List<String> outputs;

    //private TfMatrices tensorMatrix;

    private boolean classifyMatrix;

    private boolean isDebug = false;

}

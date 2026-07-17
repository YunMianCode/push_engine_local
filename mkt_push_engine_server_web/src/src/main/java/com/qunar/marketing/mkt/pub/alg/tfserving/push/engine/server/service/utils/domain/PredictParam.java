package com.qunar.marketing.mkt.pub.alg.tfserving.push.engine.server.service.utils.domain;

import lombok.Data;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Data
public class PredictParam {
    private String modelKey;
    private float[][] matrix;
    private Map<String, float[][]> subMatrices;
    private Map<String, int[][]> subIntMatrices;
    private Map<String, String[][]> subStringMatrices;
    private int batchSize;
    private List<String> outputs;
    private boolean classifyMatrix;
    private boolean isDebug = false;

    @Override
    public String toString() {
        return "PredictParam{" +
                "modelKey='" + modelKey + '\'' +
                ", matrix=" + deepToString(matrix) +
                ", subMatrices=" + deepToStringMap(subMatrices) +
                ", subIntMatrices=" + deepToStringIntMap(subIntMatrices) +
                ", subStringMatrices=" + deepToStringStringMap(subStringMatrices) +
                ", batchSize=" + batchSize +
                ", outputs=" + outputs +
                ", classifyMatrix=" + classifyMatrix +
                ", isDebug=" + isDebug +
                '}';
    }

    private String deepToString(float[][] array) {
        if (array == null) return "null";
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < array.length; i++) {
            if (i > 0) sb.append(", ");
            sb.append(Arrays.toString(array[i]));
        }
        return sb.append("]").toString();
    }

    private String deepToStringMap(Map<String, float[][]> map) {
        if (map == null) return "null";
        StringBuilder sb = new StringBuilder("{");
        boolean first = true;
        for (Map.Entry<String, float[][]> entry : map.entrySet()) {
            if (!first) sb.append(", ");
            first = false;
            sb.append(entry.getKey()).append("=").append(deepToString(entry.getValue()));
        }
        return sb.append("}").toString();
    }

    private String deepToStringIntMap(Map<String, int[][]> map) {
        if (map == null) return "null";
        StringBuilder sb = new StringBuilder("{");
        boolean first = true;
        for (Map.Entry<String, int[][]> entry : map.entrySet()) {
            if (!first) sb.append(", ");
            first = false;
            int[][] value = entry.getValue();
            StringBuilder valSb = new StringBuilder("[");
            for (int i = 0; i < value.length; i++) {
                if (i > 0) valSb.append(", ");
                valSb.append(Arrays.toString(value[i]));
            }
            sb.append(entry.getKey()).append("=").append(valSb.append("]"));
        }
        return sb.append("}").toString();
    }

    private String deepToStringStringMap(Map<String, String[][]> map) {
        if (map == null) return "null";
        StringBuilder sb = new StringBuilder("{");
        boolean first = true;
        for (Map.Entry<String, String[][]> entry : map.entrySet()) {
            if (!first) sb.append(", ");
            first = false;
            String[][] value = entry.getValue();
            StringBuilder valSb = new StringBuilder("[");
            for (int i = 0; i < value.length; i++) {
                if (i > 0) valSb.append(", ");
                valSb.append(Arrays.toString(value[i]));
            }
            sb.append(entry.getKey()).append("=").append(valSb.append("]"));
        }
        return sb.append("}").toString();
    }
}
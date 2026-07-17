package com.qunar.marketing.mkt.pub.alg.tfserving.push.engine.server.service.utils.Predictors.tf;

import lombok.Data;
import org.tensorflow.proto.framework.DataType;

import java.util.List;

@Data
public class TensorModelConfig {
    private List<InputConfig> inputs;
    private OutPutConfig outputs;

    @Data
    public static class InputConfig {
        private String name;
        private List<String> features;
        private DataType dtype;
    }
    @Data
    public static class OutPutConfig {
        private String name;
        private DataType dtype;
    }
}

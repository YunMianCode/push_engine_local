package com.qunar.marketing.mkt.pub.alg.tfserving.push.engine.server.service.utils.Predictors.tf;

import lombok.Data;

import java.util.List;

@Data
public class FeatureGroupConfig {
    private List<FeatureGroup> features;

    @Data
    public static class FeatureGroup {
        // 字段名与 features.yml 的全小写键名保持一致，避免 SnakeYAML 严格 bean 映射因大小写/拼写不一致抛 ConstructorException
        private String featuregroups;
        private List<String> featuresnames;
        private List<Integer> featuresdtype;    }
}

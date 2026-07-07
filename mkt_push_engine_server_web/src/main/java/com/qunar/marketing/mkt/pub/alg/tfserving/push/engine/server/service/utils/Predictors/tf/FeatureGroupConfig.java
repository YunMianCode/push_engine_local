package com.qunar.marketing.mkt.pub.alg.tfserving.push.engine.server.service.utils.Predictors.tf;

import lombok.Data;

import java.util.List;

@Data
public class FeatureGroupConfig {
    private List<FeatureGroup> features;

    @Data
    public static class FeatureGroup {
        private String featuregroups;
        private List<String> featuresnames;
        private List<Integer> featuresdtype;    }
}

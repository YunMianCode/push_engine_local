package com.qunar.marketing.mkt.pub.alg.tfserving.push.engine.server.service.utils.domain;

import com.qunar.marketing.mkt.pub.alg.tfserving.push.engine.server.service.utils.Predictors.tf.TensorModelConfig;
import lombok.Builder;
import lombok.Data;
import org.tensorflow.Signature;
import org.tensorflow.proto.framework.DataType;

import java.util.List;
import java.util.Map;

@Data
@Builder
public class ModelConfig {
    // 模型文件路径
    private String modelPath;

    // 模型类型
    private ModelType modelType;

    // 模型名称
    private String modelName;

    // 模型版本
    private String modelVersion;

    // 输入名称映射（业务名称 -> 模型实际输入名称）
    private Map<String, String> inputNameMappings;

    // 输出名称映射（业务名称 -> 模型实际输出名称）
    private Map<String, String> outputNameMappings;

    private Map<String, Signature.TensorDescription> inputMap;  // 解析模型signature 得到

    private Map<String, Signature.TensorDescription> outputMap;  // 解析模型signature 得到

    // 输入形状信息（输入名称 -> 形状数组）
    private Map<String, long[]> inputShapes; // 解析模型signature 得到

    private Map<String, DataType> dataTypes; // 解析模型signature 得到

    private Map<String, long[]> outputShapes; // 解析模型signature 得到

    // 框架特定配置参数
    private Map<String, Object> frameworkSpecificParams;

    private TensorModelConfig tensorModelConfig;  // 模型输入输出tensor -> features的映射关系

    private Map<String, List<String>> inputToFeaturesMap; // 模型输入->features的映射关系

    private Map<String, List<String>> featureskey;  // 从redis中读取的模型需要的feature（原始的）featuregroup->featurename

    private Map<String, List<Integer>> featuresDtype; // 从redis中读取的模型需要的feature（原始的）featuregroup->featurename 类型

    private List<String> outputNames; // tensor.xml 中配置的输出 or request 传递来的。

    // 是否启用GPU
    private boolean useGpu;
}

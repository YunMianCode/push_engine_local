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

    // ==================== 基础元信息（Model.getInstance 加载时设置）====================

    private String modelPath;

    private ModelType modelType;

    private String modelName;

    private String modelVersion;

    /** 是否启用 GPU（预留开关 */
    private boolean useGpu;

    // ==================== 模型签名信息（TFPredictor.initModelConfig 解析 SavedModel signature 得到）====================

    private Map<String, Signature.TensorDescription> inputMap;

    private Map<String, Signature.TensorDescription> outputMap;

    /** 输入张量形状：张量名（signature.name 去掉冒号后缀） -> 形状数组，用于预热构造 mock 张量与一致性校验 */
    private Map<String, long[]> inputShapes;

    private Map<String, long[]> outputShapes;

    private Map<String, DataType> dataTypes;

    // ==================== tensor.yml 配置（输入张量 -> 特征 映射 + 输出名）====================

    /** tensor.yml 解析出的原始配置对象 */
    private TensorModelConfig tensorModelConfig;

    private Map<String, List<String>> inputToFeaturesMap;

    /** tensor.yml 解析出的输出张量名列表 */
    private List<String> outputNames;

    // ==================== features.yml 配置（特征组 -> 特征名/类型，用于从特征平台拉取业务特征）====================

    /** userid,特征名列表 */
    private Map<String, List<String>> featuresKey;

    private Map<String, List<Integer>> featuresDtype;

    // ==================== 预留字段（当前实现未填充/未使用）====================

    /** 输入名称映射（业务名称 -> 模型实际输入名称），预留字段，当前实现未填充 */
    private Map<String, String> inputNameMappings;

    /** 输出名称映射（业务名称 -> 模型实际输出名称），预留字段，当前实现未填充 */
    private Map<String, String> outputNameMappings;

    /** 框架特定配置参数，预留字段，当前实现未填充 */
    private Map<String, Object> frameworkSpecificParams;
}

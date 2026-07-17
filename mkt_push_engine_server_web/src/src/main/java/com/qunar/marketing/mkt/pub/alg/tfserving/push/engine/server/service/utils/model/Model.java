package com.qunar.marketing.mkt.pub.alg.tfserving.push.engine.server.service.utils.model;

import com.alibaba.fastjson.JSONObject;
import com.qunar.marketing.mkt.pub.alg.tfserving.push.engine.server.service.utils.Predictors.IPredictor;
import com.qunar.marketing.mkt.pub.alg.tfserving.push.engine.server.service.utils.Predictors.onnx.OnnxPredictor;
import com.qunar.marketing.mkt.pub.alg.tfserving.push.engine.server.service.utils.Predictors.pytorch.PyTorchPredictor;
import com.qunar.marketing.mkt.pub.alg.tfserving.push.engine.server.service.utils.Predictors.tf.TFPredictor;
import com.qunar.marketing.mkt.pub.alg.tfserving.push.engine.server.service.utils.constant.CommonConstants;
import com.qunar.marketing.mkt.pub.alg.tfserving.push.engine.server.service.utils.domain.ModelConfig;
import com.qunar.marketing.mkt.pub.alg.tfserving.push.engine.server.service.utils.domain.PredictParam;
import com.qunar.marketing.mkt.pub.alg.tfserving.push.engine.server.service.utils.domain.PredictResult;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Model {
    private static Logger LOGGER = LoggerFactory.getLogger(Model.class);

    @Getter
    private final ModelConfig modelConfig;

    private final IPredictor predictor;

    /**
     * 构造并加载模型
     * @param modelConfig 模型配置
     */
    private Model(ModelConfig modelConfig) {
        this.modelConfig = modelConfig;
        this.predictor = createPredictor(modelConfig);
        this.predictor.init(this.modelConfig);
    }

    /**
     * 根据模型类型创建对应的预测器
     * @param modelConfig 模型配置
     * @return 对应类型的预测器实例
     * @throws IllegalArgumentException 模型类型不支持时抛出
     */
    private IPredictor createPredictor(ModelConfig modelConfig) {

        LOGGER.info("== createPredictor: modelType={} modelName={}", modelConfig.getModelType(), modelConfig.getModelName());
        switch (modelConfig.getModelType()) {
            case ONNX:
                return new OnnxPredictor();
            case PYTORCH:
                return new PyTorchPredictor();
            case TENSORFLOW:
                return new TFPredictor();
            default:
                throw new IllegalArgumentException("Unsupported model type: " + modelConfig.getModelType());
        }
    }

    /**
     * 根据模型名(含版本)创建并加载 Model 实例
     * <p>读取 property.json 构建基础 ModelConfig，创建预测器并完成初始化
     * @param modelNameWithVersion 模型名称加版本标识，格式为 模型名_version_版本号
     * @return 已加载完成的 Model 实例
     * @throws IllegalArgumentException 模型类型不支持或加载失败时抛出
     */
    public static Model getInstance(String modelNameWithVersion) {
        try {
            LOGGER.info("== getInstance(modelNameWithVersion): {}", modelNameWithVersion);
            ModelConfig config = buildBaseConfig(modelNameWithVersion);
            return new Model(config);
        } catch (Exception e) {
            LOGGER.error("== load model failed: {}", modelNameWithVersion, e);
            throw new IllegalArgumentException("load model failed: " + modelNameWithVersion, e);
        }
    }

    /**
     * 模型预热
     * <p>补充说明：委托给内部 predictor 执行预热操作，提前初始化资源以减少首次预测耗时
     */
    public void warmUp() {
        this.predictor.warmUp();
    }

    /**
     * 执行模型预测
     * <p>补充说明：委托给内部 predictor 执行实际预测
     *
     * @param predictParam 预测参数
     * @return 预测结果
     */
    public PredictResult predict(PredictParam predictParam) {
        // predictParam 含特征矩阵，toString 开销大，仅 DEBUG 打印全量；INFO 只打轻量字段
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("predict(param): {}", predictParam.toString());
        } else {
            LOGGER.info("predict: modelKey={} batchSize={}", predictParam.getModelKey(), predictParam.getBatchSize());
        }
        return this.predictor.predict(predictParam);
    }

    /**
     * 读取 property.json 并构建 ModelConfig 基础字段
     * @param modelNameWithVersion 模型名称加版本标识，格式为 模型名_version_版本号
     * @return 含基础字段的 ModelConfig
     * @throws IOException 读取 property.json 失败时抛出
     */
    private static ModelConfig buildBaseConfig(String modelNameWithVersion) throws IOException {
        PropertyConfig propertyConfig = readProperty(modelNameWithVersion);
        String[] modelNames = modelNameWithVersion.split("_version_");
        return ModelConfig.builder()
                .modelType(propertyConfig.toModelType())
                .modelName(modelNames[0])
                .modelVersion(modelNames[1])
                .modelPath(CommonConstants.MODEL_DIR + "/" + modelNameWithVersion)
                .build();
    }

    /**
     * 读取模型 property.json 配置并反序列化为 PropertyConfig
     *
     * @param propertyPath 模型路径（不含 property.json 文件名）
     * @return property.json 对应的 PropertyConfig
     * @throws IOException 读取或解析配置文件时发生异常
     */
    private static PropertyConfig readProperty(String propertyPath) throws IOException {
        propertyPath = CommonConstants.MODEL_DIR + File.separator + propertyPath + File.separator + "property.json";
        LOGGER.info("== property.json Path: {}", propertyPath);
        String jsonContent = new String(Files.readAllBytes(Paths.get(propertyPath)));
        PropertyConfig propertyConfig = JSONObject.parseObject(jsonContent, PropertyConfig.class);
        LOGGER.info("== property.json modelType {}", propertyConfig.getModelType());
        return propertyConfig;
    }
}

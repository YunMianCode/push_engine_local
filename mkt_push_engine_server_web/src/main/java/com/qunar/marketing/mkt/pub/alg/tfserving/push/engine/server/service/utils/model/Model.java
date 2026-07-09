package com.qunar.marketing.mkt.pub.alg.tfserving.push.engine.server.service.utils.model;

import com.alibaba.fastjson.JSONObject;
import com.qunar.marketing.mkt.pub.alg.tfserving.push.engine.server.api.Constants;
import com.qunar.marketing.mkt.pub.alg.tfserving.push.engine.server.service.model.ModelManager;
import com.qunar.marketing.mkt.pub.alg.tfserving.push.engine.server.service.utils.Predictors.IPredictor;
import com.qunar.marketing.mkt.pub.alg.tfserving.push.engine.server.service.utils.Predictors.onnx.OnnxPredictor;
import com.qunar.marketing.mkt.pub.alg.tfserving.push.engine.server.service.utils.Predictors.pytorch.PyTorchPredictor;
import com.qunar.marketing.mkt.pub.alg.tfserving.push.engine.server.service.utils.Predictors.tf.TFPredictor;
import com.qunar.marketing.mkt.pub.alg.tfserving.push.engine.server.service.utils.constant.CommonConstants;
import com.qunar.marketing.mkt.pub.alg.tfserving.push.engine.server.service.utils.domain.ModelConfig;
import com.qunar.marketing.mkt.pub.alg.tfserving.push.engine.server.service.utils.domain.ModelType;
import com.qunar.marketing.mkt.pub.alg.tfserving.push.engine.server.service.utils.domain.PredictParam;
import com.qunar.marketing.mkt.pub.alg.tfserving.push.engine.server.service.utils.domain.PredictResult;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public class Model {
    private static Logger LOGGER = LoggerFactory.getLogger(Model.class);

    @Getter
    private ModelConfig modelConfig;

    private IPredictor predictor;

    /**
     * 创建模型实例
     * @param modelNameWithVersion 模型名称及版本，格式：{modelName}_version_{version}
     * @return 模型实例
     * @throws IllegalArgumentException 不支持的模型类型
     */
    public static Model getInstance(String modelNameWithVersion) {
        try {
            LOGGER.info("getInstance(modelNameWithVersion): {}",  modelNameWithVersion);
            Model model = new Model();
            ModelConfig config = ModelConfig.builder().build();
            Map<String, Object> configMap = readProperty(modelNameWithVersion);
            String[] modelNames = modelNameWithVersion.split("_version_");
            config.setModelType((ModelType) configMap.get("modelType"));
            LOGGER.info("getInstance(modelNameWithVersion):ModelType {}", config.getModelType());

            config.setModelName(modelNames[0]);
            config.setModelVersion(modelNames[1]);
            config.setModelPath(CommonConstants.MODEL_DIR + "/" + modelNameWithVersion);
            model.loadModel(config);
            return model;
        } catch (Exception e) {
            e.printStackTrace();
            throw new IllegalArgumentException("Unsupported model type: " + modelNameWithVersion);
        }
    }

    /**
     * 加载模型配置并初始化预测器
     * @param modelConfig 模型配置
     */
    private void loadModel(ModelConfig modelConfig) {
        this.modelConfig = modelConfig;
        this.predictor = createPredictor(modelConfig);
        this.predictor.init(this.modelConfig);
    }

    /**
     * 根据模型类型创建预测器
     * @param modelConfig 模型配置
     * @return 预测器实例
     * @throws IllegalArgumentException 不支持的模型类型
     */
    private IPredictor createPredictor(ModelConfig modelConfig) {

        LOGGER.info("createPredictor(modelConfig): {}",  modelConfig.toString());
        switch (modelConfig.getModelType()) {
            case ONNX:
                IPredictor onnx = new OnnxPredictor();
                return onnx;
            case PYTORCH:
                IPredictor pytorch = new PyTorchPredictor();
                return pytorch;
            case TENSORFLOW:
                IPredictor tensorflow = new TFPredictor();
                return tensorflow;
            default:
                throw new IllegalArgumentException("Unsupported model type: " + modelConfig.getModelType());
        }
    }

    /**
     * 执行模型预测
     * @param predictParam 预测参数
     * @return 预测结果
     */
    public PredictResult predict(PredictParam predictParam) {
        LOGGER.info("predict(modelConfig): {}",  predictParam.toString());
        return this.predictor.predict(predictParam);
    }

    /**
     * 模型预热
     */
    public void warmUp() {
        this.predictor.warmUp();
    }

//    public void setModelFeature()

    /**
     * 读取模型属性配置文件
     * @param propertyPath 属性文件路径
     * @return 属性配置映射
     * @throws IOException 文件读取失败
     */
    private static  Map<String, Object> readProperty(String propertyPath) throws IOException {

        propertyPath = CommonConstants.MODEL_DIR + File.separator + propertyPath + File.separator + "property.json";
        LOGGER.info("readProperty(String propertyPath): {}",  propertyPath);
        String jsonContent = new String(Files.readAllBytes(Paths.get(propertyPath)));
        JSONObject jsonObject = JSONObject.parseObject(jsonContent);
        Map<String, Object> properties = new HashMap<>();
        for (Map.Entry<String, Object> entry : jsonObject.entrySet()) {
            String key = entry.getKey();
            if (key.equals("modelType")) {
                String value = (String)(entry.getValue());
                LOGGER.info("readProperty(String propertyPath): value {}",  value);
                switch (value) {
                    case "onnx":
                        properties.put("modelType", ModelType.ONNX);
                        break;
                    case "pytorch":
                        properties.put("modelType", ModelType.PYTORCH);
                        break;
                    case "tensorflow":
                        properties.put("modelType", ModelType.TENSORFLOW);
                        break;
                    default:
                        properties.put("modelType", ModelType.UNKNOWN);
                        break;
                }
            } else {
                properties.put(entry.getKey(), entry.getValue());
            }
        }
        return properties;
    }

    private static ModelType getModelType(String stringModelType) {
        switch (stringModelType) {
            case "onnx":
                return ModelType.ONNX;
            case "pytorch":
                return ModelType.PYTORCH;
            case "tensorflow":
                return ModelType.TENSORFLOW;
            default:
                return ModelType.UNKNOWN;
        }
    }

}

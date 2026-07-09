package com.qunar.marketing.mkt.pub.alg.tfserving.push.engine.server.service.utils.Predictors;

import com.qunar.marketing.mkt.pub.alg.tfserving.push.engine.server.service.utils.domain.ModelConfig;
import com.qunar.marketing.mkt.pub.alg.tfserving.push.engine.server.service.utils.domain.PredictResult;
import com.qunar.marketing.mkt.pub.alg.tfserving.push.engine.server.service.utils.domain.PredictParam;

/**
 * 预测器接口
 * <p>定义模型预测的通用接口，支持TensorFlow、ONNX、PyTorch等多种模型类型
 */
public interface IPredictor {

    /**
     * 执行预测
     * @param param 预测参数
     * @return 预测结果
     */
    public PredictResult predict(PredictParam param);

    /**
     * 模型预热
     * <p>提前加载模型到内存，避免首次预测延迟
     */
    public void warmUp();

    /**
     * 初始化预测器
     * @param modelConfig 模型配置
     */
    public void init(ModelConfig modelConfig);

    /**
     * 释放资源
     */
    public void close();
}

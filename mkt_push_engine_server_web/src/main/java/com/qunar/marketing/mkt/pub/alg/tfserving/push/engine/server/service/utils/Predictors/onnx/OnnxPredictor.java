package com.qunar.marketing.mkt.pub.alg.tfserving.push.engine.server.service.utils.Predictors.onnx;

import com.qunar.marketing.mkt.pub.alg.tfserving.push.engine.server.service.utils.Predictors.IPredictor;
import com.qunar.marketing.mkt.pub.alg.tfserving.push.engine.server.service.utils.domain.ModelConfig;
import com.qunar.marketing.mkt.pub.alg.tfserving.push.engine.server.service.utils.domain.PredictParam;
import com.qunar.marketing.mkt.pub.alg.tfserving.push.engine.server.service.utils.domain.PredictResult;

/**
 * ONNX预测器
 * <p>ONNX模型的预测器实现（预留）
 */
public class OnnxPredictor implements IPredictor {

    /**
     * 执行ONNX模型预测
     * @param predictParam 预测参数
     * @return 预测结果
     */
    @Override
    public PredictResult predict(PredictParam predictParam) {
        return null;
    }

    /**
     * 初始化ONNX预测器
     * @param modelConfig 模型配置
     */
    @Override
    public void init(ModelConfig modelConfig) {

    }

    /**
     * 释放ONNX资源
     */
    @Override
    public void close() {

    }

    /**
     * ONNX模型预热
     */
    @Override
    public void warmUp()
    {

    }
}

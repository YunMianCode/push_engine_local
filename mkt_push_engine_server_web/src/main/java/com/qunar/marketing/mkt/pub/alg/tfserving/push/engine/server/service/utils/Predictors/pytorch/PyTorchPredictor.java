package com.qunar.marketing.mkt.pub.alg.tfserving.push.engine.server.service.utils.Predictors.pytorch;

import com.qunar.marketing.mkt.pub.alg.tfserving.push.engine.server.service.utils.Predictors.IPredictor;
import com.qunar.marketing.mkt.pub.alg.tfserving.push.engine.server.service.utils.domain.ModelConfig;
import com.qunar.marketing.mkt.pub.alg.tfserving.push.engine.server.service.utils.domain.PredictParam;
import com.qunar.marketing.mkt.pub.alg.tfserving.push.engine.server.service.utils.domain.PredictResult;

/**
 * PyTorch预测器
 * <p>PyTorch模型的预测器实现（预留）
 */
public class PyTorchPredictor implements IPredictor {

    /**
     * 执行PyTorch模型预测
     * @param predictParam 预测参数
     * @return 预测结果
     */
    @Override
    public PredictResult predict(PredictParam predictParam) {
        return null;
    }

    /**
     * 初始化PyTorch预测器
     * @param modelConfig 模型配置
     */
    @Override
    public void init(ModelConfig modelConfig) {

    }

    /**
     * 释放PyTorch资源
     */
    @Override
    public void close() {

    }

    /**
     * PyTorch模型预热
     */
    @Override
    public void warmUp() {
    }
}

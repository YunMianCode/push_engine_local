package com.qunar.marketing.mkt.pub.alg.tfserving.push.engine.server.service.utils.Predictors.onnx;

import com.qunar.marketing.mkt.pub.alg.tfserving.push.engine.server.service.utils.Predictors.IPredictor;
import com.qunar.marketing.mkt.pub.alg.tfserving.push.engine.server.service.utils.domain.ModelConfig;
import com.qunar.marketing.mkt.pub.alg.tfserving.push.engine.server.service.utils.domain.PredictParam;
import com.qunar.marketing.mkt.pub.alg.tfserving.push.engine.server.service.utils.domain.PredictResult;

public class OnnxPredictor implements IPredictor {

    /**
     * 执行 ONNX 模型预测
     * <p>补充说明：当前为空实现，待接入 ONNX Runtime 推理逻辑
     * @param predictParam 预测输入参数
     * @return 预测结果（当前未实现，固定返回 null）
     */
    @Override
    public PredictResult predict(PredictParam predictParam) {
        return null;
    }

    /**
     * 初始化 ONNX 预测器
     * <p>补充说明：当前为空实现，待补充模型加载与会话构建逻辑
     * @param modelConfig 模型配置
     */
    @Override
    public void init(ModelConfig modelConfig) {

    }

    /**
     * 释放 ONNX 预测器资源
     * <p>补充说明：当前为空实现，待补充会话与模型句柄关闭逻辑
     */
    @Override
    public void close() {

    }

    /**
     * ONNX 模型预热
     * <p>补充说明：当前为空实现，待补充预热推理逻辑
     */
    @Override
    public void warmUp()
    {

    }
}

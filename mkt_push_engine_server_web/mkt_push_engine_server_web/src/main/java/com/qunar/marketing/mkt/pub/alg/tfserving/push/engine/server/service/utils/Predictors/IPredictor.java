package com.qunar.marketing.mkt.pub.alg.tfserving.push.engine.server.service.utils.Predictors;

import com.qunar.marketing.mkt.pub.alg.tfserving.push.engine.server.service.utils.domain.ModelConfig;
import com.qunar.marketing.mkt.pub.alg.tfserving.push.engine.server.service.utils.domain.PredictResult;
import com.qunar.marketing.mkt.pub.alg.tfserving.push.engine.server.service.utils.domain.PredictParam;

public interface IPredictor {

    /**
     * 执行模型预测
     * <p>补充说明：各实现类（如 TFPredictor、OnnxPredictor 等）根据自身推理引擎加载输入参数并输出预测结果
     * @param param 预测输入参数，包含特征矩阵等推理所需数据
     * @return 预测结果，包含输出张量转换后的数值及元数据
     */
    public PredictResult predict(PredictParam param);

    /**
     * 模型预热
     * <p>补充说明：在正式预测前用模拟数据执行一次或多次推理，使引擎完成图编译与内存预分配，降低首请求延迟
     */
    public void warmUp();

    /**
     * 初始化预测器
     * <p>补充说明：加载模型文件、构建推理会话、解析模型签名与配置，通常在预测器实例创建后、预热前调用
     * @param modelConfig 模型配置，包含模型路径、输入输出映射等信息
     */
    public void init(ModelConfig modelConfig);

    /**
     * 释放预测器资源
     * <p>补充说明：关闭推理会话、释放模型句柄等底层资源，防止内存泄漏，通常在预测器生命周期结束时调用
     */
    public void close();
}

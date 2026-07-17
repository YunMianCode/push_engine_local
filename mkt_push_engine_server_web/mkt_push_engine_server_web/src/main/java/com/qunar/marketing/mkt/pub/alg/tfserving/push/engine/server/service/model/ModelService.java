package com.qunar.marketing.mkt.pub.alg.tfserving.push.engine.server.service.model;


import com.qunar.marketing.mkt.pub.alg.tfserving.push.engine.server.api.AlgoInnerRequest;
import com.qunar.marketing.mkt.pub.alg.tfserving.push.engine.server.service.utils.feature.FeaturePlatformServer;
import com.qunar.marketing.mkt.pub.alg.tfserving.push.engine.server.service.utils.feature.FeatureValues;
import com.qunar.marketing.mkt.pub.alg.tfserving.push.engine.server.service.utils.model.Model;
import com.qunar.marketing.mkt.pub.alg.tfserving.push.engine.server.service.utils.model.ModelServerContext;
import com.qunar.marketing.mkt.pub.alg.tfserving.push.engine.server.service.utils.domain.PredictParam;
import com.qunar.marketing.mkt.pub.alg.tfserving.push.engine.server.service.utils.domain.PredictResult;
import org.springframework.stereotype.Service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Resource;
import java.util.*;

//模型服务，负责模型预测的上下文构建和模型计算调用
@Service
public class ModelService {
    private static final Logger LOGGER = LoggerFactory.getLogger(ModelService.class);

    @Resource
    private FeaturePlatformServer featurePlatformServer;

    /**
     * 根据算法请求与已取好的模型构建模型服务上下文
     * @param request 算法内部请求，包含 modelKey、userId、itemIds、batchSize 等
     * @param model   已通过 modelKey 取好的模型对象
     * @return 填充完毕的模型服务上下文
     */
    public ModelServerContext buildContext(AlgoInnerRequest request, Model model) {
        ModelServerContext context = new ModelServerContext();
        Map<String, List<String>> FG2features = model.getModelConfig().getFeaturesKey();
        Map<String, List<Integer>> FG2featuresD = model.getModelConfig().getFeaturesDtype();
        // FG2features/FG2featuresD 为模型级常量，启动期已在 TFPredictor.loadFeaturesYml 打印一次，请求级不再重复打印
        String user_id = request.getUserId();
        List<String> item_ids = request.getItemIds();
        FeatureValues featureValues = featurePlatformServer.getFeatureValues(FG2features, FG2featuresD, user_id, item_ids);
        context.setBatchSize(request.getBatchSize());
        context.setModelKey(request.getModelKey());
        context.setOutputs(model.getModelConfig().getOutputNames());
        LOGGER.debug("== setOutputs {}", model.getModelConfig().getOutputNames());
        context.setSubIntMatrices(featureValues.getIntFeatureValues());
        context.setSubStringMatrices(featureValues.getStringFeatureValues());
        context.setSubMatrices(featureValues.getFloatFeatureValues());
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("== ModelServerContext info {}", context);
        }
        return context;
    }

    /**
     * 执行模型预测计算
     * @param modelServerContext 模型服务上下文，包含模型键、特征矩阵、输出配置等
     * @param model              已通过 modelKey 取好的模型对象
     * @return 模型预测结果
     */
    public PredictResult modelCalculate(ModelServerContext modelServerContext, Model model) {
        PredictParam predictParam = createPredictParamFromContext(modelServerContext);
        PredictResult predictResult = model.predict(predictParam);

        // 可以打印日志
        // 可以添加监控

        return predictResult;
    }

    /**
     * 将模型服务上下文转换为预测参数
     * @param context 模型服务上下文
     * @return 构建完成的预测参数对象
     */
    private PredictParam createPredictParamFromContext(ModelServerContext context) {
        PredictParam predictParam = new PredictParam();
        predictParam.setModelKey(context.getModelKey());
        predictParam.setSubMatrices(context.getSubMatrices());
        predictParam.setSubIntMatrices(context.getSubIntMatrices());
        predictParam.setSubStringMatrices(context.getSubStringMatrices());
        predictParam.setDebug(context.isDebug());
        predictParam.setClassifyMatrix(context.isClassifyMatrix());
        predictParam.setOutputs(context.getOutputs());
        if (context.isClassifyMatrix()) {
            predictParam.setBatchSize(context.getBatchSize());
        } else {
            int batchSize = 1;
            if (!context.getSubMatrices().isEmpty()) {
                Set<String> keys = context.getSubMatrices().keySet();
                String firstKey = keys.iterator().next();
                batchSize = context.getSubMatrices().get(firstKey).length;
            }
            predictParam.setBatchSize(batchSize);
        }
        return predictParam;
    }
}

package com.qunar.marketing.mkt.pub.alg.tfserving.push.engine.server.service.model;


import com.qunar.marketing.mkt.pub.alg.tfserving.push.engine.server.api.AlgoInnerRequest;
import com.qunar.marketing.mkt.pub.alg.tfserving.push.engine.server.service.utils.feature.FeaturePlatformServer;
import com.qunar.marketing.mkt.pub.alg.tfserving.push.engine.server.service.utils.feature.FeatureValues;
import com.qunar.marketing.mkt.pub.alg.tfserving.push.engine.server.service.utils.model.Model;
import com.qunar.marketing.mkt.pub.alg.tfserving.push.engine.server.service.utils.model.ModelServerContext;
import com.qunar.marketing.mkt.pub.alg.tfserving.push.engine.server.service.utils.domain.PredictParam;
import com.qunar.marketing.mkt.pub.alg.tfserving.push.engine.server.service.utils.domain.PredictResult;
import org.bouncycastle.math.raw.Mod;
import org.springframework.stereotype.Service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ModelService {
    private static final Logger LOGGER = LoggerFactory.getLogger(ModelService.class);

    @Resource
    private ModelManager modelManager;

    @Resource
    private FeaturePlatformServer featurePlatformServer;

    public Model getModel(String modelName) {
        return modelManager.getModel(modelName, -1);
    }

    public Model getModelFromKey(String modelKey) {
        return modelManager.getModelFromKey(modelKey);
    }

    public Set<String> getModelNames() {
        return modelManager.modelContainer.keySet();
    }

    public ModelServerContext buildContext(AlgoInnerRequest request) {

        ModelServerContext context = new ModelServerContext();
        //Model model = modelManager.getModel(request.getModelKey(), -1);
        Model model = modelManager.getModelFromKey(request.getModelKey());

        // 获得模型需要的特征名
        Map<String, List<String>> FG2features = model.getModelConfig().getFeatureskey();
        Map<String, List<Integer>> FG2featuresD = model.getModelConfig().getFeaturesDtype();
        LOGGER.info("FG2features {}", FG2features.toString());
        LOGGER.info("FG2featuresD {}", FG2featuresD.toString());

        // 相应的key
        String user_id = request.getUserId();
        List<String> item_ids = request.getItemIds();


        // 获得特征
        FeatureValues featureValues = featurePlatformServer.getFeatureValues(FG2features, FG2featuresD, user_id, item_ids);


        // 设置context
        context.setBatchSize(request.getBatchSize());
        context.setModelKey(request.getModelKey());
        //context.setOutputs(model.getModelConfig().getOutputMap().keySet().stream().collect(Collectors.toList()));
        context.setOutputs(model.getModelConfig().getOutputNames());
        LOGGER.info("====setOutputs {}", model.getModelConfig().getOutputNames());
        //context.setOutputs(new ArrayList<>(model.getModelConfig().getOutputMap().keySet()));
        context.setIntSubMatrices(featureValues.getIntFeatureValues());
        context.setStringSubMatrices(featureValues.getStringFeatureValues());
        context.setSubMatrices(featureValues.getFloatFeatureValues());
        LOGGER.info("=====context {}", context.toString());

        return context;
    }

    public PredictResult modelCalculate(ModelServerContext modelServerContext) {
        //Model model = modelManager.getModel(modelServerContext.getModelKey(), -1);
        Model model = modelManager.getModelFromKey(modelServerContext.getModelKey());

        PredictParam  predictParam = createPredictParamFromContext(modelServerContext);

        PredictResult predictResult = model.predict(predictParam);

        // 可以打印日志
        // 可以添加监控

        return predictResult;
    }
    private PredictParam createPredictParamFromContext(ModelServerContext context) {
        PredictParam predictParam = new PredictParam();
        predictParam.setModelKey(context.getModelKey());
        predictParam.setSubMatrices(context.getSubMatrices());
        predictParam.setSubIntMatrices(context.getIntSubMatrices());
        predictParam.setSubStringMatrices(context.getStringSubMatrices());
        predictParam.setDebug(context.isDebug());
        predictParam.setClassifyMatrix(context.isClassifyMatrix());
        predictParam.setOutputs(context.getOutputs());
        if(context.isClassifyMatrix()) {
            predictParam.setBatchSize(context.getBatchSize());
//            predictParam.setOutputList(context.getOutputs());
//            predictParam.setDoubleMatrices(context.getTensorMatrix().getDoubleMatrices());
//            predictParam.setLongMatrices(context.getTensorMatrix().getLongMatrices());
//            predictParam.setIntMatrices(context.getTensorMatrix().getIntMatrices());
//            predictParam.setFloatMatrices(context.getTensorMatrix().getFloatMatrices());
//            predictParam.setStringMatrices(context.getTensorMatrix().getStringMatrices());
        }else {
            //predictParam.setMatrix(context.getMatrix());
//            Optional<float[][]> anyValue = context.getSubMatrices().values().stream().findAny();
//
//            // 输出值（若存在）
//            int batchSize = 1;
//            anyValue.ifPresent((value) -> {
//                batchSize = value.length;
//                }
//            );
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

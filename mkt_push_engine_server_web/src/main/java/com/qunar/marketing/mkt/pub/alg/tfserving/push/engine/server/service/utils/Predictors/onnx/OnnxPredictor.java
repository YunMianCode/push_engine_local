package com.qunar.marketing.mkt.pub.alg.tfserving.push.engine.server.service.utils.Predictors.onnx;

import com.qunar.marketing.mkt.pub.alg.tfserving.push.engine.server.service.utils.Predictors.IPredictor;
import com.qunar.marketing.mkt.pub.alg.tfserving.push.engine.server.service.utils.domain.ModelConfig;
import com.qunar.marketing.mkt.pub.alg.tfserving.push.engine.server.service.utils.domain.PredictParam;
import com.qunar.marketing.mkt.pub.alg.tfserving.push.engine.server.service.utils.domain.PredictResult;

public class OnnxPredictor implements IPredictor {

    @Override
    public PredictResult predict(PredictParam predictParam) {
        return null;
    }

    @Override
    public void init(ModelConfig modelConfig) {

    }

    @Override
    public void close() {

    }

    @Override
    public void warmUp()
    {

    }
}

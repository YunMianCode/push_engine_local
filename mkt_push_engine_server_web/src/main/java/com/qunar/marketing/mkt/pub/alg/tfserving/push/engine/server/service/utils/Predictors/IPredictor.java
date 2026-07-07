package com.qunar.marketing.mkt.pub.alg.tfserving.push.engine.server.service.utils.Predictors;

import com.qunar.marketing.mkt.pub.alg.tfserving.push.engine.server.service.utils.domain.ModelConfig;
import com.qunar.marketing.mkt.pub.alg.tfserving.push.engine.server.service.utils.domain.PredictResult;
import com.qunar.marketing.mkt.pub.alg.tfserving.push.engine.server.service.utils.domain.PredictParam;

public interface IPredictor {

    public PredictResult predict(PredictParam param);

    public void warmUp();

    public void init(ModelConfig modelConfig);

    public void close();
}

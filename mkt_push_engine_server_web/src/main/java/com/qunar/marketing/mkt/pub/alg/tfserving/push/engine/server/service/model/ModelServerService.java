package com.qunar.marketing.mkt.pub.alg.tfserving.push.engine.server.service.model;

import com.qunar.flight.qmonitor.QMonitor;
import com.qunar.marketing.mkt.pub.alg.tfserving.push.engine.server.api.AlgoInnerRequest;
import com.qunar.marketing.mkt.pub.alg.tfserving.push.engine.server.api.AlgoInnerResponse;
import com.qunar.marketing.mkt.pub.alg.tfserving.push.engine.server.api.Constants;
import com.qunar.marketing.mkt.pub.alg.tfserving.push.engine.server.service.utils.exception.ModelServerException;
import com.qunar.marketing.mkt.pub.alg.tfserving.push.engine.server.service.utils.exception.ParamException;
import com.qunar.marketing.mkt.pub.alg.tfserving.push.engine.server.service.utils.model.ModelServerContext;
import com.qunar.marketing.mkt.pub.alg.tfserving.push.engine.server.service.utils.domain.PredictResult;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
@Slf4j
public class ModelServerService {

    @Resource
    private ModelService modelService;

    public void predict(AlgoInnerRequest request, AlgoInnerResponse response) throws ModelServerException {
        // 1. 校验参数
        checkRequest(request);

        // 2. 校验context, 主要是获得特征
        ModelServerContext context = modelService.buildContext(request);

        // 3. 预测
        PredictResult predictResult = modelService.modelCalculate(context);

        // 4. 封装结果
        packageResponse(response, predictResult);

    }

    private void checkRequest(AlgoInnerRequest request) throws ModelServerException {
        log.info("checkRequest {}", request.getModelKey());
        checkModelExist(request.getModelKey());
    }

    public ModelServerContext buildContext(AlgoInnerRequest request) throws ModelServerException {
        ModelServerContext context = new ModelServerContext();

        return context;
    }

    private void packageResponse(AlgoInnerResponse response, PredictResult predictResult) {
        response.setVectors(predictResult.getResults());
        response.setCode(Constants.C_SUCCESS);
    }

    private void checkModelExist(String modelKey) {
        if (StringUtils.isEmpty(modelKey)) {
            throw new ParamException("param check failed!");
        }
        log.info("checkModelExist {}", modelService.getModelNames());
        if (null == modelService.getModelFromKey(modelKey)) {
            log.info("model check failed!" + modelKey);
            QMonitor.recordOne("param.check.model.info.not.found");
            throw new ParamException("no such model!");
        }
    }
}

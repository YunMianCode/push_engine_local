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

    /**
     * 执行模型预测
     * @param request 预测请求
     * @param response 预测响应（输出参数）
     * @throws ModelServerException 模型服务异常
     */
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

    /**
     * 校验请求参数
     * @param request 预测请求
     * @throws ModelServerException 参数校验失败
     */
    private void checkRequest(AlgoInnerRequest request) throws ModelServerException {
        log.info("checkRequest {}", request.getModelKey());
        checkModelExist(request.getModelKey());
    }

    /**
     * 构建模型服务上下文
     * @param request 预测请求
     * @return 模型服务上下文
     * @throws ModelServerException 上下文构建失败
     */
    public ModelServerContext buildContext(AlgoInnerRequest request) throws ModelServerException {
        ModelServerContext context = new ModelServerContext();

        return context;
    }

    /**
     * 封装预测响应
     * @param response 响应对象
     * @param predictResult 预测结果
     */
    private void packageResponse(AlgoInnerResponse response, PredictResult predictResult) {
        response.setVectors(predictResult.getResults());
        response.setCode(Constants.C_SUCCESS);
    }

    /**
     * 检查模型是否存在
     * @param modelKey 模型Key
     * @throws ParamException 模型不存在
     */
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

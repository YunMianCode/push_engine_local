package com.qunar.marketing.mkt.pub.alg.tfserving.push.engine.server.service.model;

import com.qunar.marketing.mkt.pub.alg.tfserving.push.engine.server.api.AlgoInnerRequest;
import com.qunar.marketing.mkt.pub.alg.tfserving.push.engine.server.api.AlgoInnerResponse;
import com.qunar.marketing.mkt.pub.alg.tfserving.push.engine.server.api.Constants;
import com.qunar.marketing.mkt.pub.alg.tfserving.push.engine.server.service.utils.exception.ModelServerException;
import com.qunar.marketing.mkt.pub.alg.tfserving.push.engine.server.service.utils.exception.ParamException;
import com.qunar.marketing.mkt.pub.alg.tfserving.push.engine.server.service.utils.model.Model;
import com.qunar.marketing.mkt.pub.alg.tfserving.push.engine.server.service.utils.model.ModelServerContext;
import com.qunar.marketing.mkt.pub.alg.tfserving.push.engine.server.service.utils.domain.PredictResult;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
@Slf4j
//模型编排，编排模型预测的主流程
public class ModelServerService {

    @Resource
    private ModelService modelService;

    @Resource
    private ModelManager modelManager;

    /**
     * 执行算法预测主流程
     * @param request 算法预测请求参数
     * @param response 算法预测响应对象，由本方法填充结果
     * @throws ModelServerException 参数校验或上下文构建过程中发生异常时抛出
     */
    public void predict(AlgoInnerRequest request, AlgoInnerResponse response) throws ModelServerException {
        // 1. 校验参数
        checkRequest(request);
        // 2. 获取模型
        Model model = modelManager.getModelFromKey(request.getModelKey());
        // 3. 构建特征上下文
        ModelServerContext context = modelService.buildContext(request, model);
        // 4. 预测
        PredictResult predictResult = modelService.modelCalculate(context, model);
        // 5. 封装结果
        packageResponse(response, predictResult);
    }

    /**
     * 校验预测请求参数
     * @param request 算法预测请求参数
     * @throws ParamException modelKey 为空时抛出
     */
    private void checkRequest(AlgoInnerRequest request) {
        log.info("== checkRequest: modelKey={}", request.getModelKey());
        checkModelExist(request.getModelKey());
    }

    /**
     * 校验 modelKey 非空
     * @param modelKey 模型 key
     * @throws ParamException modelKey 为空时抛出
     */
    private void checkModelExist(String modelKey) {
        if (StringUtils.isEmpty(modelKey)) {
            throw new ParamException("param check failed!");
        }
    }

    /**
     * 封装预测结果到响应对象
     * @param response 待填充的算法预测响应对象
     * @param predictResult 模型计算得到的结果
     * @throws ModelServerException 预测失败时抛出
     */
    private void packageResponse(AlgoInnerResponse response, PredictResult predictResult) throws ModelServerException {
        if (predictResult == null || !predictResult.isSuccess()) {
            String errorMsg = predictResult == null ? "predict result is null" : predictResult.getErrorMessage();
            log.error("== model predict failed: {}", errorMsg);
            throw new ModelServerException("model predict failed: " + errorMsg);
        }
        response.setVectors(predictResult.getResults());
        response.setCode(Constants.C_SUCCESS);
    }

}

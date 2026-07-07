package com.qunar.marketing.mkt.pub.alg.tfserving.push.engine.server.service.provider;

import com.qunar.marketing.mkt.pub.alg.tfserving.push.engine.server.api.*;
import com.qunar.marketing.mkt.pub.alg.tfserving.push.engine.server.service.model.ModelServerService;
import com.qunar.marketing.mkt.pub.alg.tfserving.push.engine.server.service.utils.ThreadMXBeanUtils;
import com.qunar.marketing.mkt.pub.alg.tfserving.push.engine.server.service.utils.constant.CommonConstants;
import com.qunar.marketing.mkt.pub.alg.tfserving.push.engine.server.service.utils.exception.ModelServerException;
import com.qunar.marketing.mkt.pub.alg.tfserving.push.engine.server.service.utils.exception.ParamException;
import org.apache.dubbo.config.annotation.DubboService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Resource;
import java.util.concurrent.CompletableFuture;

@DubboService
public class ApacheDubboFutureAsyncServiceImpl implements ApacheDubboFutureAsyncService {
    private static final Logger LOG = LoggerFactory.getLogger(ApacheDubboFutureAsyncServiceImpl.class);

    @Resource
    private ModelServerService modelServerService;

    @Override
    public CompletableFuture<Blog> select(int id) {
        return CompletableFuture.completedFuture(new Blog(1, "CompletableFuture"));
    }

    @Override
    public CompletableFuture<AlgoInnerResponse> predict(AlgoInnerRequest request) {
        long startCpuTime = 0L;
        boolean needCalCpuTime = false;
        if (request.isSetCpuTimeFlag() && request.getCpuTimeFlag() == 1) {
            startCpuTime = ThreadMXBeanUtils.INSTANCE.currentThreadCpuTimeMicros();
            needCalCpuTime = true;
        }
        AlgoInnerResponse response = new AlgoInnerResponse();

        try {
            modelServerService.predict(request, response);
            if (needCalCpuTime) {
                long cpuTime = ThreadMXBeanUtils.INSTANCE.currentThreadCpuTimeMicros() - startCpuTime;
                response.setCpuTime(cpuTime);
            }
        } catch (ParamException e) {
            response.setCode(Constants.C_PARAM_ERROR);
            response.setMsg(e.getMessage());
            if (needCalCpuTime) response.setCpuTime(-1L);
            LOG.error("param error:request:{}", request, e);
        } catch (ModelServerException e) {
            response.setCode(Constants.C_MODEL_ERROR);
            response.setMsg(e.getMessage());
            if (needCalCpuTime) response.setCpuTime(-1L);
            LOG.error("model calculate error, request:{}", request, e);
        } catch (Exception e) {
            response.setCode(Constants.C_INTERNAL_ERROR);
            response.setMsg(CommonConstants.INNER_ERROR_MESSAGE);
            if (needCalCpuTime) response.setCpuTime(-1L);
            LOG.error("inner error, request:{}", request.getModelKey(), e);
        } finally {

        }

        return CompletableFuture.completedFuture(response);
    }
}

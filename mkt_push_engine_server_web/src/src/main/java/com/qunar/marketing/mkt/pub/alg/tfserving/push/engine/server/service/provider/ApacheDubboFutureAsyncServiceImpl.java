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

    /**
     * 异步查询示例接口，返回固定 Blog
     * <p>演示用 CompletableFuture 包装返回值，id 参数当前未参与逻辑
     * @param id 博客ID
     * @return 已完成的 CompletableFuture，内含固定构造的 Blog
     */
    @Override
    public CompletableFuture<Blog> select(int id) {
        return CompletableFuture.completedFuture(new Blog(1, "CompletableFuture"));
    }

    /**
     * 异步执行模型预测
     * @param request 算法内部请求
     * @return 已完成的 CompletableFuture，内含预测响应
     */
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
            long startTime = System.currentTimeMillis();
            LOG.debug("== predict start: modelKey={}", request.getModelKey());
            modelServerService.predict(request, response);
            if (needCalCpuTime) {
                long cpuTime = ThreadMXBeanUtils.INSTANCE.currentThreadCpuTimeMicros() - startCpuTime;
                response.setCpuTime(cpuTime);
            }
            LOG.info("== predict done: modelKey={} time={}ms", request.getModelKey(), System.currentTimeMillis() - startTime);
        } catch (ParamException e) {
            response.setCode(Constants.C_PARAM_ERROR);
            response.setMsg(e.getMessage());
            if (needCalCpuTime) response.setCpuTime(-1L);
            LOG.error("== predict param error: modelKey={}", request.getModelKey(), e);
            LOG.debug("== param error request: {}", request, e);
        } catch (ModelServerException e) {
            response.setCode(Constants.C_MODEL_ERROR);
            response.setMsg(e.getMessage());
            if (needCalCpuTime) response.setCpuTime(-1L);
            LOG.error("== predict model error: modelKey={}", request.getModelKey(), e);
            LOG.debug("== model error request: {}", request, e);
        } catch (Exception e) {
            response.setCode(Constants.C_INTERNAL_ERROR);
            response.setMsg(CommonConstants.INNER_ERROR_MESSAGE);
            if (needCalCpuTime) response.setCpuTime(-1L);
            LOG.error("== predict inner error: modelKey={}", request.getModelKey(), e);
        } finally {

        }

        return CompletableFuture.completedFuture(response);
    }
}

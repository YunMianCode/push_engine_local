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
import qunar.tc.dubbo.async.AsyncImpl;

import javax.annotation.Resource;

@AsyncImpl(DubboBetterAsyncService.class)
@DubboService(version = "1.0.0")
public class DubboBetterAsyncServiceImpl implements DubboBetterAsyncService {
    private static final Logger LOGGER = LoggerFactory.getLogger(DubboBetterAsyncServiceImpl.class);

    @Resource
    private ModelServerService modelServerService;

    /**
     * 异步长任务查询示例接口，返回固定 Blog
     * @param blogId 博客ID
     * @return 固定内容的 Blog 对象
     */
    @Override
    public Blog longTaskSelect(Integer blogId) {
        return new Blog(blogId, "better async");
    }

    /**
     * 同步执行模型预测（better async 实现）
     * @param request 算法内部请求
     * @return 预测响应
     */
    @Override
    public AlgoInnerResponse predict(AlgoInnerRequest request) {

        long startCpuTime = 0L;
        boolean needCalCpuTime = false;
        if (request.isSetCpuTimeFlag() && request.getCpuTimeFlag() == 1) {
            startCpuTime = ThreadMXBeanUtils.INSTANCE.currentThreadCpuTimeMicros();
            needCalCpuTime = true;
        }
        AlgoInnerResponse response = new AlgoInnerResponse();

        try {
            long startTime = System.currentTimeMillis();
            LOGGER.debug("== predict start: modelKey={}", request.getModelKey());
            modelServerService.predict(request, response);
            if (needCalCpuTime) {
                long cpuTime = ThreadMXBeanUtils.INSTANCE.currentThreadCpuTimeMicros() - startCpuTime;
                response.setCpuTime(cpuTime);
            }
            LOGGER.info("== predict done: modelKey={} time={}ms", request.getModelKey(), System.currentTimeMillis() - startTime);
        } catch (ParamException e) {
            response.setCode(Constants.C_PARAM_ERROR);
            response.setMsg(e.getMessage());
            if (needCalCpuTime) response.setCpuTime(-1L);
            LOGGER.error("== predict param error: modelKey={}", request.getModelKey(), e);
            LOGGER.debug("== param error request: {}", request, e);
        } catch (ModelServerException e) {
            response.setCode(Constants.C_MODEL_ERROR);
            response.setMsg(e.getMessage());
            if (needCalCpuTime) response.setCpuTime(-1L);
            LOGGER.error("== predict model error: modelKey={}", request.getModelKey(), e);
            LOGGER.debug("== model error request: {}", request, e);
        } catch (Exception e) {
            response.setCode(Constants.C_INTERNAL_ERROR);
            response.setMsg(CommonConstants.INNER_ERROR_MESSAGE);
            if (needCalCpuTime) response.setCpuTime(-1L);
            LOGGER.error("== predict inner error: modelKey={}", request.getModelKey(), e);
        } finally {

        }
        return  response;
    }
}

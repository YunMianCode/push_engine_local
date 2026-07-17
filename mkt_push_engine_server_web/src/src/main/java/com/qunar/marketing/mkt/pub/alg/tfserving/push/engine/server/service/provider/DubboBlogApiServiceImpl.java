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

/**
 * 普通dubbo调用接口
 * provider端实现
 */
@DubboService(version = "1.0.0")
public class DubboBlogApiServiceImpl implements DubboBlogApiService {
    private static final Logger LOG = LoggerFactory.getLogger(DubboBlogApiServiceImpl.class);

    @Resource
    private ModelServerService modelServerService;


    /**
     * 普通同步 dubbo 查询示例接口
     * @param blogId 博客ID
     * @return 构造的 Blog 对象
     */
    @Override
    public Blog select(Integer blogId) {
        Blog blog = new Blog(blogId, "DubboBlogApiService");
        blog.setContent("调用Dubbo框架同步接口");
        return blog;
    }

    /**
     * 同步执行模型预测（普通 dubbo 实现）
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
            LOG.debug("== predict start: modelKey={}", request.getModelKey());
            modelServerService.predict(request, response);
            if (needCalCpuTime) {
                long cpuTime = ThreadMXBeanUtils.INSTANCE.currentThreadCpuTimeMicros() - startCpuTime;
                response.setCpuTime(cpuTime);
            }
            LOG.info("== predict done: modelKey={} time={}ms", request.getModelKey(), System.currentTimeMillis() - startTime);
            return response;
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
        return  response;
    }
}

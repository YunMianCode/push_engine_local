package com.qunar.marketing.mkt.pub.alg.tfserving.push.engine.server.service.provider;

import com.qunar.marketing.mkt.pub.alg.tfserving.push.engine.server.api.*;
import com.qunar.marketing.mkt.pub.alg.tfserving.push.engine.server.service.model.ModelServerService;
import com.qunar.marketing.mkt.pub.alg.tfserving.push.engine.server.service.utils.ThreadMXBeanUtils;
import com.qunar.marketing.mkt.pub.alg.tfserving.push.engine.server.service.utils.constant.CommonConstants;
import com.qunar.marketing.mkt.pub.alg.tfserving.push.engine.server.service.utils.exception.ModelServerException;
import com.qunar.marketing.mkt.pub.alg.tfserving.push.engine.server.service.utils.exception.ParamException;
import org.apache.dubbo.config.annotation.DubboService;
import org.bouncycastle.math.ec.rfc8032.Ed25519;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import qunar.tc.dubbo.async.AsyncImpl;

import javax.annotation.Resource;

/**
 * Dubbo异步服务实现
 * <p>提供模型预测和Blog查询的异步服务能力
 */
@AsyncImpl(DubboBetterAsyncService.class)
@DubboService(version = "1.0.0")
public class DubboBetterAsyncServiceImpl implements DubboBetterAsyncService {
    private static final Logger LOGGER = LoggerFactory.getLogger(DubboBetterAsyncServiceImpl.class);

    @Resource
    private ModelServerService modelServerService;

    /**
     * 长任务查询Blog
     * @param blogId BlogID
     * @return Blog对象
     */
    @Override
    public Blog longTaskSelect(Integer blogId) {
        return new Blog(blogId, "better async");
    }

    /**
     * 执行模型预测
     * <p>支持CPU耗时统计，根据请求参数选择是否计算CPU时间
     * @param request 预测请求
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
            modelServerService.predict(request, response);
            if (needCalCpuTime) {
                long cpuTime = ThreadMXBeanUtils.INSTANCE.currentThreadCpuTimeMicros() - startCpuTime;
                response.setCpuTime(cpuTime);
            }
            LOGGER.info("predict time: {}", System.currentTimeMillis() - startTime);
        } catch (ParamException e) {
            response.setCode(Constants.C_PARAM_ERROR);
            response.setMsg(e.getMessage());
            if (needCalCpuTime) response.setCpuTime(-1L);
            LOGGER.error("param error:request:{}", request, e);
        } catch (ModelServerException e) {
            response.setCode(Constants.C_MODEL_ERROR);
            response.setMsg(e.getMessage());
            if (needCalCpuTime) response.setCpuTime(-1L);
            LOGGER.error("model calculate error, request:{}", request, e);
        } catch (Exception e) {
            response.setCode(Constants.C_INTERNAL_ERROR);
            response.setMsg(CommonConstants.INNER_ERROR_MESSAGE);
            if (needCalCpuTime) response.setCpuTime(-1L);
            LOGGER.error("inner error, request:{}", request.getModelKey(), e);
        } finally {

        }
        return  response;
    }
}

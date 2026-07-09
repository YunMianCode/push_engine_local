package com.qunar.marketing.mkt.pub.alg.tfserving.push.engine.server.api;

/**
 * Dubbo同步服务接口
 * <p>提供Blog查询和模型预测的同步Dubbo调用能力
 */
public interface DubboBlogApiService {

    /**
     * 查询Blog（同步）
     * @param blogId BlogID
     * @return Blog对象
     */
    Blog select(Integer blogId);

    /**
     * 执行模型预测（同步）
     * @param request 预测请求
     * @return 预测响应
     */
    AlgoInnerResponse predict(AlgoInnerRequest request);
}

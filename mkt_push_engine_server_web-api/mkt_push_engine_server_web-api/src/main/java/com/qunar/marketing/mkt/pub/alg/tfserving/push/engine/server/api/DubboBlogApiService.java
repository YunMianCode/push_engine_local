package com.qunar.marketing.mkt.pub.alg.tfserving.push.engine.server.api;

/**
 * 普通dubbo调用接口
 */
public interface DubboBlogApiService {
    /**
     * 根据BlogID同步查询Blog
     * <p>补充说明：普通Dubbo同步RPC调用，返回Blog对象。
     * @param blogId Blog标识
     * @return Blog对象
     */
    Blog select(Integer blogId);
    /**
     * 根据算法请求同步执行预测
     * <p>补充说明：普通Dubbo同步RPC调用，返回算法预测响应。
     * @param request 算法内部请求对象
     * @return 算法内部响应对象
     */
    AlgoInnerResponse predict(AlgoInnerRequest request);
}

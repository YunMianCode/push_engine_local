package com.qunar.marketing.mkt.pub.alg.tfserving.push.engine.server.api;

import qunar.tc.dubbo.async.Async;

/**
 * Dubbo异步服务接口
 * <p>使用Qunar封装的Dubbo异步调用，参考wiki：https://wiki.corp.qunar.com/confluence/display/devwiki/Better+Async
 */
@Async
public interface DubboBetterAsyncService {

    /**
     * 长任务查询Blog
     * @param blogId BlogID
     * @return Blog对象
     */
    Blog longTaskSelect(Integer blogId);

    /**
     * 执行模型预测
     * @param request 预测请求
     * @return 预测响应
     */
    AlgoInnerResponse predict(AlgoInnerRequest request);
}

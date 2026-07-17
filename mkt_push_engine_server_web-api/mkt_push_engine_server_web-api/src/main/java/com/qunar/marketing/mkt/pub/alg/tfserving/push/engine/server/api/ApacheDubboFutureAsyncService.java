package com.qunar.marketing.mkt.pub.alg.tfserving.push.engine.server.api;

import java.util.concurrent.CompletableFuture;

/**
* @author tcdev
* @date 2021/9/23 12:00
* @description 使用dubbo官方的CompletableFuture进行异步调用的接口
*/
public interface ApacheDubboFutureAsyncService {
    /**
     * 根据ID异步查询Blog
     * <p>补充说明：基于Dubbo官方CompletableFuture实现的异步RPC调用，调用方通过返回的Future获取结果。
     * @param id Blog标识
     * @return 携带Blog的CompletableFuture
     */
    CompletableFuture<Blog> select(int id);
    /**
     * 根据算法请求异步执行预测
     * <p>补充说明：基于Dubbo官方CompletableFuture实现的异步RPC调用，调用方通过返回的Future获取预测响应。
     * @param request 算法内部请求对象
     * @return 携带AlgoInnerResponse的CompletableFuture
     */
    CompletableFuture<AlgoInnerResponse> predict(AlgoInnerRequest request);
}

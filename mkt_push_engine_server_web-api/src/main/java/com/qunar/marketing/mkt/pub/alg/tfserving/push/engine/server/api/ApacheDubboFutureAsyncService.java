package com.qunar.marketing.mkt.pub.alg.tfserving.push.engine.server.api;

import java.util.concurrent.CompletableFuture;

/**
 * Dubbo Future异步服务接口
 * <p>使用Dubbo官方的CompletableFuture进行异步调用
 */
public interface ApacheDubboFutureAsyncService {

    /**
     * 查询Blog（Future异步）
     * @param id BlogID
     * @return CompletableFuture异步结果
     */
    CompletableFuture<Blog> select(int id);

    /**
     * 执行模型预测（Future异步）
     * @param request 预测请求
     * @return CompletableFuture异步结果
     */
    CompletableFuture<AlgoInnerResponse> predict(AlgoInnerRequest request);
}

package com.qunar.marketing.mkt.pub.alg.tfserving.push.engine.server.api;

import java.util.concurrent.CompletableFuture;

/**
* @author tcdev
* @date 2021/9/23 12:00
* @description 使用dubbo官方的CompletableFuture进行异步调用的接口
*/
public interface ApacheDubboFutureAsyncService {
    CompletableFuture<Blog> select(int id);
    CompletableFuture<AlgoInnerResponse> predict(AlgoInnerRequest request);
}

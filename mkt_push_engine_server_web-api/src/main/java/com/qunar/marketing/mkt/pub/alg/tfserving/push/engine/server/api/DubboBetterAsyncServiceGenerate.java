package com.qunar.marketing.mkt.pub.alg.tfserving.push.engine.server.api;
import com.google.common.util.concurrent.ListenableFuture;
import java.util.concurrent.CompletableFuture;

/**
*  这个类在编译时会自动生成，参考wiki: https://wiki.corp.qunar.com/confluence/display/devwiki/Better+Async
*/
@javax.annotation.Generated("qunar.tc.dubbo.async.processor.AsyncAnnotationProcessor")
@qunar.tc.dubbo.async.AsyncImpl(com.qunar.marketing.mkt.pub.alg.tfserving.push.engine.server.api.DubboBetterAsyncService.class)
public interface DubboBetterAsyncServiceGenerate extends DubboBetterAsyncService {
    ListenableFuture<com.qunar.marketing.mkt.pub.alg.tfserving.push.engine.server.api.Blog> longTaskSelectAsync(java.lang.Integer blogId);
    CompletableFuture<com.qunar.marketing.mkt.pub.alg.tfserving.push.engine.server.api.Blog> longTaskSelectNewQAsync(java.lang.Integer blogId);
    ListenableFuture<com.qunar.marketing.mkt.pub.alg.tfserving.push.engine.server.api.AlgoInnerResponse> predictAsync(com.qunar.marketing.mkt.pub.alg.tfserving.push.engine.server.api.AlgoInnerRequest request);
    CompletableFuture<com.qunar.marketing.mkt.pub.alg.tfserving.push.engine.server.api.AlgoInnerResponse> predictNewQAsync(com.qunar.marketing.mkt.pub.alg.tfserving.push.engine.server.api.AlgoInnerRequest request);
}
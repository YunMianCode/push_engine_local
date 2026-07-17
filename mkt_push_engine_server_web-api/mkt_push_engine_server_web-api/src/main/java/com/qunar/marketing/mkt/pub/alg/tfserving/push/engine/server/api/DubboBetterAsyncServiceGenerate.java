package com.qunar.marketing.mkt.pub.alg.tfserving.push.engine.server.api;
import com.google.common.util.concurrent.ListenableFuture;
import java.util.concurrent.CompletableFuture;

/**
*  这个类在编译时会自动生成，参考wiki: https://wiki.corp.qunar.com/confluence/display/devwiki/Better+Async
*/
@javax.annotation.Generated("qunar.tc.dubbo.async.processor.AsyncAnnotationProcessor")
@qunar.tc.dubbo.async.AsyncImpl(com.qunar.marketing.mkt.pub.alg.tfserving.push.engine.server.api.DubboBetterAsyncService.class)
public interface DubboBetterAsyncServiceGenerate extends DubboBetterAsyncService {
    /**
     * 异步查询Blog（ListenableFuture版本）
     * <p>补充说明：由编译期自动生成的异步方法，基于Guava ListenableFuture返回longTaskSelect的异步结果。
     * @param blogId Blog标识
     * @return 携带Blog的ListenableFuture
     */
    ListenableFuture<com.qunar.marketing.mkt.pub.alg.tfserving.push.engine.server.api.Blog> longTaskSelectAsync(java.lang.Integer blogId);
    /**
     * 异步查询Blog（CompletableFuture版本）
     * <p>补充说明：由编译期自动生成的异步方法，基于CompletableFuture返回longTaskSelect的异步结果。
     * @param blogId Blog标识
     * @return 携带Blog的CompletableFuture
     */
    CompletableFuture<com.qunar.marketing.mkt.pub.alg.tfserving.push.engine.server.api.Blog> longTaskSelectNewQAsync(java.lang.Integer blogId);
    /**
     * 异步执行预测（ListenableFuture版本）
     * <p>补充说明：由编译期自动生成的异步方法，基于Guava ListenableFuture返回predict的异步结果。
     * @param request 算法内部请求对象
     * @return 携带AlgoInnerResponse的ListenableFuture
     */
    ListenableFuture<com.qunar.marketing.mkt.pub.alg.tfserving.push.engine.server.api.AlgoInnerResponse> predictAsync(com.qunar.marketing.mkt.pub.alg.tfserving.push.engine.server.api.AlgoInnerRequest request);
    /**
     * 异步执行预测（CompletableFuture版本）
     * <p>补充说明：由编译期自动生成的异步方法，基于CompletableFuture返回predict的异步结果。
     * @param request 算法内部请求对象
     * @return 携带AlgoInnerResponse的CompletableFuture
     */
    CompletableFuture<com.qunar.marketing.mkt.pub.alg.tfserving.push.engine.server.api.AlgoInnerResponse> predictNewQAsync(com.qunar.marketing.mkt.pub.alg.tfserving.push.engine.server.api.AlgoInnerRequest request);
}
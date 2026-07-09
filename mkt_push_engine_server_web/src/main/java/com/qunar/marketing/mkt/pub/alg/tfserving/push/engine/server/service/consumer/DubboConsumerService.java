package com.qunar.marketing.mkt.pub.alg.tfserving.push.engine.server.service.consumer;

import com.qunar.marketing.mkt.pub.alg.tfserving.push.engine.server.api.Blog;

import com.qunar.marketing.mkt.pub.alg.tfserving.push.engine.server.api.AlgoInnerResponse;

import com.qunar.marketing.mkt.pub.alg.tfserving.push.engine.server.api.AlgoInnerRequest;

import java.util.concurrent.CompletableFuture;

/**
 * Dubbo服务消费者接口
 * <p>封装Dubbo同步/异步调用方式，提供Blog查询和模型预测服务
 */
public interface DubboConsumerService {

    /**
     * 同步调用Blog查询
     * @param blogId BlogID
     * @return Blog对象
     */
    Blog call(Integer blogId);

    /**
     * BetterAsync异步调用Blog查询（方式一）
     * @param blogId BlogID
     * @return Blog对象
     */
    Blog callBetterAsync1(Integer blogId);

    /**
     * BetterAsync异步调用Blog查询（方式二）
     * @param blogId BlogID
     * @return Blog对象
     */
    Blog callBetterAsync2(Integer blogId);

    /**
     * Future异步调用Blog查询
     * @param blobId BlogID
     * @return CompletableFuture异步结果
     */
    CompletableFuture<Blog> apacheDubboFutureAsyncService(Integer blobId);

    /**
     * 同步调用模型预测
     * @param request 预测请求
     * @return 预测响应
     */
    AlgoInnerResponse callAlgo(AlgoInnerRequest request);

    /**
     * BetterAsync异步调用模型预测（方式一）
     * @param request 预测请求
     * @return 预测响应
     */
    AlgoInnerResponse callAlgoAsync1(AlgoInnerRequest request);

    /**
     * BetterAsync异步调用模型预测（方式二）
     * @param request 预测请求
     * @return 预测响应
     */
    AlgoInnerResponse callAlgoAsync2(AlgoInnerRequest request);

    /**
     * Future异步调用模型预测
     * @param request 预测请求
     * @return CompletableFuture异步结果
     */
    CompletableFuture<AlgoInnerResponse> apacheDubboAlogFutureAsync(AlgoInnerRequest request);
}

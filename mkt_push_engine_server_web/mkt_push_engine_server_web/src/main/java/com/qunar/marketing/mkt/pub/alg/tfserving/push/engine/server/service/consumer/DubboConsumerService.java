package com.qunar.marketing.mkt.pub.alg.tfserving.push.engine.server.service.consumer;

import com.qunar.marketing.mkt.pub.alg.tfserving.push.engine.server.api.Blog;

import com.qunar.marketing.mkt.pub.alg.tfserving.push.engine.server.api.AlgoInnerResponse;

import com.qunar.marketing.mkt.pub.alg.tfserving.push.engine.server.api.AlgoInnerRequest;

import java.util.concurrent.CompletableFuture;

public interface DubboConsumerService {
    /**
     * 根据 blogId 同步调用 Dubbo 服务查询 Blog 信息
     * @param blogId Blog 主键 ID
     * @return 查询到的 Blog 对象
     */
    Blog call(Integer blogId);

    /**
     * 基于 ListenableFuture 异步调用 Dubbo 服务查询 Blog 信息并同步等待结果
     * @param blogId Blog 主键 ID
     * @return 查询到的 Blog 对象
     */
    Blog callBetterAsync1(Integer blogId);

    /**
     * 基于 CompletableFuture 异步调用 Dubbo 服务查询 Blog 信息并同步等待结果
     * @param blogId Blog 主键 ID
     * @return 查询到的 Blog 对象
     */
    Blog callBetterAsync2(Integer blogId);

    /**
     * 通过 Apache Dubbo 原生 Future 方式异步调用服务查询 Blog 信息
     * @param blobId Blog 主键 ID
     * @return 包装 Blog 结果的 CompletableFuture
     */
    CompletableFuture<Blog> apacheDubboFutureAsyncService(Integer blobId);

    /**
     * 根据 AlgoInnerRequest 同步调用 Dubbo 服务进行算法推理
     * @param request 算法推理请求参数
     * @return 算法推理响应结果
     */
    AlgoInnerResponse callAlgo(AlgoInnerRequest request);

    /**
     * 基于 ListenableFuture 异步调用 Dubbo 服务进行算法推理并同步等待结果
     * @param request 算法推理请求参数
     * @return 算法推理响应结果
     */
    AlgoInnerResponse callAlgoAsync1(AlgoInnerRequest request);

    /**
     * 基于 CompletableFuture 异步调用 Dubbo 服务进行算法推理并同步等待结果
     * @param request 算法推理请求参数
     * @return 算法推理响应结果
     */
    AlgoInnerResponse callAlgoAsync2(AlgoInnerRequest request);

    /**
     * 通过 Apache Dubbo 原生 Future 方式异步调用服务进行算法推理
     * @param request 算法推理请求参数
     * @return 包装算法推理响应结果的 CompletableFuture
     */
    CompletableFuture<AlgoInnerResponse> apacheDubboAlogFutureAsync(AlgoInnerRequest request);
}

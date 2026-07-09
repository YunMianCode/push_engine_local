package com.qunar.marketing.mkt.pub.alg.tfserving.push.engine.server.service.consumer.impl;

import com.google.common.util.concurrent.ListenableFuture;
import com.qunar.marketing.mkt.pub.alg.tfserving.push.engine.server.api.*;
import com.qunar.marketing.mkt.pub.alg.tfserving.push.engine.server.service.consumer.DubboConsumerService;
import org.apache.dubbo.rpc.RpcContext;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

/**
 * Dubbo服务消费者实现
 */
@Service
public class DubboConsumerServiceImpl implements DubboConsumerService {

    @Resource
    private DubboBlogApiService dubboBlogApiService;

    @Resource
    private DubboBetterAsyncServiceGenerate dubboBetterAsyncServiceGenerate;

    @Resource
    private ApacheDubboFutureAsyncService apacheDubboFutureAsyncService;

    /**
     * 同步调用Blog查询
     * @param blogId BlogID
     * @return Blog对象
     */
    @Override
    public Blog call(final Integer blogId) {
        try {
            return dubboBlogApiService.select(blogId);
        } catch (Exception e) {
            throw e;
        }
    }

    /**
     * BetterAsync异步调用Blog查询（方式一）
     * @param blogId BlogID
     * @return Blog对象
     */
    @Override
    public Blog callBetterAsync1(final Integer blogId) {
        final ListenableFuture<Blog> future = dubboBetterAsyncServiceGenerate.longTaskSelectAsync(blogId);
        try {
            return future.get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * BetterAsync异步调用Blog查询（方式二）
     * @param blogId BlogID
     * @return Blog对象
     */
    @Override
    public Blog callBetterAsync2(final Integer blogId) {
        final CompletableFuture<Blog> future = dubboBetterAsyncServiceGenerate.longTaskSelectNewQAsync(blogId);
        try {
            return future.get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Future异步调用Blog查询
     * @param blogId BlogID
     * @return CompletableFuture异步结果
     */
    @Override
    public CompletableFuture<Blog> apacheDubboFutureAsyncService(final Integer blogId) {
        return apacheDubboFutureAsyncService.select(blogId);
    }

    /**
     * 同步调用模型预测
     * @param request 预测请求
     * @return 预测响应
     */
    @Override
    public AlgoInnerResponse callAlgo(AlgoInnerRequest request) {
        try {
            return dubboBlogApiService.predict(request);
        } catch (Exception e) {
            throw e;
        }
    }

    /**
     * BetterAsync异步调用模型预测（方式一）
     * @param request 预测请求
     * @return 预测响应
     */
    @Override
    public AlgoInnerResponse callAlgoAsync1(AlgoInnerRequest request) {
        final ListenableFuture<AlgoInnerResponse> future = dubboBetterAsyncServiceGenerate.predictAsync(request);
        try {
            return future.get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * BetterAsync异步调用模型预测（方式二）
     * @param request 预测请求
     * @return 预测响应
     */
    @Override
    public AlgoInnerResponse callAlgoAsync2(AlgoInnerRequest request) {
        final CompletableFuture<AlgoInnerResponse> future = dubboBetterAsyncServiceGenerate.predictNewQAsync(request);
        try {
            return future.get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Future异步调用模型预测
     * @param request 预测请求
     * @return CompletableFuture异步结果
     */
    @Override
    public CompletableFuture<AlgoInnerResponse> apacheDubboAlogFutureAsync(AlgoInnerRequest request) {
        return apacheDubboFutureAsyncService.predict(request);
    }
}

package com.qunar.marketing.mkt.pub.alg.tfserving.push.engine.server.service.consumer.impl;

import com.google.common.util.concurrent.ListenableFuture;
import com.qunar.marketing.mkt.pub.alg.tfserving.push.engine.server.api.*;
import com.qunar.marketing.mkt.pub.alg.tfserving.push.engine.server.service.consumer.DubboConsumerService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Dubbo 消费端服务实现
 * @author 郭安
 * @date 2026-07-13
 */
@Slf4j
@Service
public class DubboConsumerServiceImpl implements DubboConsumerService {

    // 异步调用超时时间
    private static final long ASYNC_TIMEOUT_SECONDS = 5;

    private final DubboBlogApiService dubboBlogApiService;
    private final DubboBetterAsyncServiceGenerate dubboBetterAsyncServiceGenerate;
    private final ApacheDubboFutureAsyncService apacheDubboFutureAsyncService;

    @Autowired
    public DubboConsumerServiceImpl(DubboBlogApiService dubboBlogApiService,
                                    DubboBetterAsyncServiceGenerate dubboBetterAsyncServiceGenerate,
                                    ApacheDubboFutureAsyncService apacheDubboFutureAsyncService) {
        this.dubboBlogApiService = dubboBlogApiService;
        this.dubboBetterAsyncServiceGenerate = dubboBetterAsyncServiceGenerate;
        this.apacheDubboFutureAsyncService = apacheDubboFutureAsyncService;
    }

    /**
     * 根据 blogId 同步调用 Dubbo 服务查询 Blog 信息
     * @param blogId Blog 主键 ID
     * @return 查询到的 Blog 对象
     */
    @Override
    public Blog call(final Integer blogId) {
        return dubboBlogApiService.select(blogId);
    }

    /**
     * 基于 ListenableFuture 异步调用 Dubbo 服务查询 Blog 信息并同步等待结果（带超时）
     * @param blogId Blog 主键 ID
     * @return 查询到的 Blog 对象
     * @throws RuntimeException 等待结果过程中发生中断、执行异常或超时时包装抛出
     */
    @Override
    public Blog callBetterAsync1(final Integer blogId) {
        final ListenableFuture<Blog> future = dubboBetterAsyncServiceGenerate.longTaskSelectAsync(blogId);
        try {
            return future.get(ASYNC_TIMEOUT_SECONDS, TimeUnit.SECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            log.error("callBetterAsync1 failed, blogId={}", blogId, e);
            throw new RuntimeException("callBetterAsync1 failed, blogId=" + blogId, e);
        }
    }

    /**
     * 基于 CompletableFuture 异步调用 Dubbo 服务查询 Blog 信息并同步等待结果（带超时）
     * @param blogId Blog 主键 ID
     * @return 查询到的 Blog 对象
     * @throws RuntimeException 等待结果过程中发生中断、执行异常或超时时包装抛出
     */
    @Override
    public Blog callBetterAsync2(final Integer blogId) {
        final CompletableFuture<Blog> future = dubboBetterAsyncServiceGenerate.longTaskSelectNewQAsync(blogId);
        try {
            return future.get(ASYNC_TIMEOUT_SECONDS, TimeUnit.SECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            log.error("callBetterAsync2 failed, blogId={}", blogId, e);
            throw new RuntimeException("callBetterAsync2 failed, blogId=" + blogId, e);
        }
    }

    /**
     * 通过 Apache Dubbo 原生 Future 方式异步调用服务查询 Blog 信息
     * @param blogId Blog 主键 ID
     * @return 包装 Blog 结果的 CompletableFuture
     */
    @Override
    public CompletableFuture<Blog> apacheDubboFutureAsyncService(final Integer blogId) {
        return apacheDubboFutureAsyncService.select(blogId);
    }

    /**
     * 根据 AlgoInnerRequest 同步调用 Dubbo 服务进行算法预测
     * @param request 算法预测请求参数
     * @return 算法预测响应结果
     */
    @Override
    public AlgoInnerResponse callAlgo(AlgoInnerRequest request) {
        return dubboBlogApiService.predict(request);
    }

    /**
     * 基于 ListenableFuture 异步调用 Dubbo 服务进行算法预测并同步等待结果（带超时）
     * @param request 算法预测请求参数
     * @return 算法预测响应结果
     * @throws RuntimeException 等待结果过程中发生中断、执行异常或超时时包装抛出
     */
    @Override
    public AlgoInnerResponse callAlgoAsync1(AlgoInnerRequest request) {
        final ListenableFuture<AlgoInnerResponse> future = dubboBetterAsyncServiceGenerate.predictAsync(request);
        try {
            return future.get(ASYNC_TIMEOUT_SECONDS, TimeUnit.SECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            log.error("callAlgoAsync1 failed, modelKey={}, userId={}", request.getModelKey(), request.getUserId(), e);
            throw new RuntimeException("callAlgoAsync1 failed, modelKey=" + request.getModelKey()
                    + ", userId=" + request.getUserId(), e);
        }
    }

    /**
     * 基于 CompletableFuture 异步调用 Dubbo 服务进行算法预测并同步等待结果（带超时）
     * @param request 算法预测请求参数
     * @return 算法预测响应结果
     * @throws RuntimeException 等待结果过程中发生中断、执行异常或超时时包装抛出
     */
    @Override
    public AlgoInnerResponse callAlgoAsync2(AlgoInnerRequest request) {
        final CompletableFuture<AlgoInnerResponse> future = dubboBetterAsyncServiceGenerate.predictNewQAsync(request);
        try {
            return future.get(ASYNC_TIMEOUT_SECONDS, TimeUnit.SECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            log.error("callAlgoAsync2 failed, modelKey={}, userId={}", request.getModelKey(), request.getUserId(), e);
            throw new RuntimeException("callAlgoAsync2 failed, modelKey=" + request.getModelKey()
                    + ", userId=" + request.getUserId(), e);
        }
    }

    /**
     * 通过 Apache Dubbo 原生 Future 方式异步调用服务进行算法预测
     * @param request 算法预测请求参数
     * @return 包装算法预测响应结果的 CompletableFuture
     */
    @Override
    public CompletableFuture<AlgoInnerResponse> apacheDubboAlogFutureAsync(AlgoInnerRequest request) {
        return apacheDubboFutureAsyncService.predict(request);
    }
}

package com.qunar.marketing.mkt.pub.alg.tfserving.push.engine.server.controller;

import com.qunar.marketing.mkt.pub.alg.tfserving.push.engine.server.api.AlgoInnerRequest;
import com.qunar.marketing.mkt.pub.alg.tfserving.push.engine.server.api.AlgoInnerResponse;
import com.qunar.marketing.mkt.pub.alg.tfserving.push.engine.server.api.Blog;
import com.qunar.marketing.mkt.pub.alg.tfserving.push.engine.server.service.consumer.DubboConsumerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;

import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.async.DeferredResult;

import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;

/**
 * Dubbo 调用测试控制器
 * <p>
 * 提供同步、异步（CompletableFuture、Apache Dubbo Future、DeferredResult）多种 Dubbo
 * 调用方式的测试入口，
 * 覆盖普通 Blog 调用与算法预测（AlgoInnerRequest/AlgoInnerResponse）调用，用于验证
 * DubboConsumerService 的连通性与调用链路。
 *
 * @author 郭安
 * @date 2026-07-13
 */
@RestController
public class TestDubboController {

    private final DubboConsumerService dubboConsumerService;

    @Autowired
    public TestDubboController(DubboConsumerService dubboConsumerService) {
        this.dubboConsumerService = dubboConsumerService;
    }


    @GetMapping("/test-dubbo")
    public ResponseEntity<Blog> testdubbo() {
        return new ResponseEntity<>(dubboConsumerService.call(12), HttpStatus.OK);
    }

    @GetMapping("/test-better-async-dubbo-1")
    public ResponseEntity<Blog> testBetterAsync1() {
        return new ResponseEntity<>(dubboConsumerService.callBetterAsync1(12), HttpStatus.OK);
    }

    @GetMapping("/test-better-async-dubbo-2")
    public ResponseEntity<Blog> testBetterAsync2() {
        return new ResponseEntity<>(dubboConsumerService.callBetterAsync2(12), HttpStatus.OK);
    }

    @GetMapping("/test-future-async-dubbo")
    public DeferredResult<Blog> testApacheDubboFutureAsync() {
        final DeferredResult<Blog> result = new DeferredResult<>();
        final CompletableFuture<Blog> future = dubboConsumerService.apacheDubboFutureAsyncService(12);
        future.whenComplete((r, t) -> {
            if (t != null) {
                result.setErrorResult(t);
                return;
            }
            result.setResult(r);
        });
        return result;
    }

    @GetMapping("/test-predict")
    public ResponseEntity<AlgoInnerResponse> testPredict(@RequestParam String userId) {
        AlgoInnerRequest algoInnerRequest = buildPredictRequest(userId);
        AlgoInnerResponse response = dubboConsumerService.callAlgo(algoInnerRequest);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/test-predict-async-1")
    public ResponseEntity<AlgoInnerResponse> testPredictAsync1(@RequestParam String userId) {
        AlgoInnerRequest algoInnerRequest = buildPredictRequest(userId);
        AlgoInnerResponse response = dubboConsumerService.callAlgoAsync1(algoInnerRequest);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/test-predict-async-2")
    public ResponseEntity<AlgoInnerResponse> testPredictAsync2(@RequestParam String userId) {
        AlgoInnerRequest algoInnerRequest = buildPredictRequest(userId);
        AlgoInnerResponse response = dubboConsumerService.callAlgoAsync2(algoInnerRequest);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/test-predict-future")
    public DeferredResult<AlgoInnerResponse> testPredictFutureAsync(@RequestParam String userId) {
        final DeferredResult<AlgoInnerResponse> result = new DeferredResult<>();
        AlgoInnerRequest algoInnerRequest = buildPredictRequest(userId);
        final CompletableFuture<AlgoInnerResponse> future = dubboConsumerService.apacheDubboAlogFutureAsync(algoInnerRequest);
        future.whenComplete((r, t) -> {
            if (t != null) {
                result.setErrorResult(t);
                return;
            }
            result.setResult(r);
        });
        return result;
    }

    private AlgoInnerRequest buildPredictRequest(String userId) {
        AlgoInnerRequest request = new AlgoInnerRequest();
        request.setCpuTimeFlag(1);
        request.setItemIds(new ArrayList<>());
        request.setBatchSize(1);
        request.setDeviceId("1111111111111");
        // TODO modelKey是否由RTA传入
        request.setModelKey("rta_model_version_1");
        request.setUserId(userId);
        return request;
    }
}

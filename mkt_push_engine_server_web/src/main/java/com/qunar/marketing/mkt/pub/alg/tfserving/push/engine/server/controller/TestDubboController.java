package com.qunar.marketing.mkt.pub.alg.tfserving.push.engine.server.controller;

import com.qunar.marketing.mkt.pub.alg.tfserving.push.engine.server.api.AlgoInnerRequest;
import com.qunar.marketing.mkt.pub.alg.tfserving.push.engine.server.api.AlgoInnerResponse;
import com.qunar.marketing.mkt.pub.alg.tfserving.push.engine.server.api.Blog;
import com.qunar.marketing.mkt.pub.alg.tfserving.push.engine.server.service.consumer.DubboConsumerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.async.DeferredResult;

import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;
/**
 * Dubbo测试控制器
 * <p>提供Dubbo同步/异步调用测试接口，以及模型预测测试接口
 */
@Controller
public class TestDubboController {


    @Autowired
    DubboConsumerService dubboConsumerService;

    /**
     * 测试Dubbo同步调用
     * @return Blog响应
     */
    @GetMapping("/test-dubbo")
    public ResponseEntity<Blog> testdubbo() {
        return new ResponseEntity<>(dubboConsumerService.call(12), HttpStatus.OK);
    }

    /**
     * 测试Dubbo BetterAsync异步调用（方式一）
     * @return Blog响应
     */
    @GetMapping("/test-better-async-dubbo-1")
    public ResponseEntity<Blog> testBetterAsync1() {
        return new ResponseEntity<>(dubboConsumerService.callBetterAsync1(12), HttpStatus.OK);
    }

    /**
     * 测试Dubbo BetterAsync异步调用（方式二）
     * @return Blog响应
     */
    @GetMapping("/test-better-async-dubbo-2")
    public ResponseEntity<Blog> testBetterAsync2() {
        return new ResponseEntity<>(dubboConsumerService.callBetterAsync2(12), HttpStatus.OK);
    }

    /**
     * 测试Dubbo Future异步调用
     * @return DeferredResult异步结果
     */
    @GetMapping("/test-future-async-dubbo")
    @ResponseBody
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

    /**
     * 测试模型预测（同步）
     * @param user 用户ID
     * @return 预测响应
     */
    @GetMapping("/test-predict")
    public ResponseEntity<AlgoInnerResponse> testPredict(@RequestParam String user) {
        AlgoInnerRequest request = new AlgoInnerRequest();
        request.setCpuTimeFlag(1);
        request.setItemIds(new ArrayList<>());
        request.setBatchSize(1);
        request.setDeviceId("1111111111111");
        //支持更多模型，需要优化此处
        request.setModelKey("rta_model_version_1");
        request.setUserId(user);
        AlgoInnerResponse response = dubboConsumerService.callAlgo(request);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * 测试模型预测（异步方式一）
     * @param user 用户ID
     * @return 预测响应
     */
    @GetMapping("/test-predict-async-1")
    public ResponseEntity<AlgoInnerResponse> testPredictAsync1(@RequestParam String user) {
        AlgoInnerRequest request = new AlgoInnerRequest();
        request.setCpuTimeFlag(1);
        request.setItemIds(new ArrayList<>());
        request.setBatchSize(1);
        request.setDeviceId("1111111111111");
        request.setModelKey("rta_model_version_1");
        request.setUserId(user);
        AlgoInnerResponse response = dubboConsumerService.callAlgoAsync1(request);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * 测试模型预测（异步方式二）
     * @param user 用户ID
     * @return 预测响应
     */
    @GetMapping("/test-predict-async-2")
    public ResponseEntity<AlgoInnerResponse> testPredictAsync2(@RequestParam String user) {
        AlgoInnerRequest request = new AlgoInnerRequest();
        request.setCpuTimeFlag(1);
        request.setItemIds(new ArrayList<>());
        request.setBatchSize(1);
        request.setDeviceId("1111111111111");
        request.setModelKey("rta_model_version_1");
        request.setUserId(user);
        AlgoInnerResponse response = dubboConsumerService.callAlgoAsync2(request);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * 测试模型预测（Future异步）
     * @param user 用户ID
     * @return DeferredResult异步结果
     */
    @GetMapping("/test-predict-future")
    public DeferredResult<AlgoInnerResponse> testPredictFutureAsync(@RequestParam String user) {
        AlgoInnerRequest request = new AlgoInnerRequest();
        request.setCpuTimeFlag(1);
        request.setItemIds(new ArrayList<>());
        request.setBatchSize(1);
        request.setDeviceId("1111111111111");
        request.setModelKey("rta_model_version_1");
        request.setUserId(user);

        final DeferredResult<AlgoInnerResponse> result = new DeferredResult<>();
        final CompletableFuture<AlgoInnerResponse> future = dubboConsumerService.apacheDubboAlogFutureAsync(request);
        future.whenComplete((r, t) -> {
            if (t != null) {
                result.setErrorResult(t);
                return;
            }
            result.setResult(r);
        });
        return result;
    }
}

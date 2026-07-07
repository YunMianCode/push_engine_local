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
 * dubbo测试例子
 * 详细wiki:  http://wiki.corp.qunar.com/confluence/pages/viewpage.action?pageId=63243118
 * 访问以下链接测试dubbo
 */
@Controller
public class TestDubboController {


    @Autowired
    DubboConsumerService dubboConsumerService;

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

    @GetMapping("/test-predict")
    public ResponseEntity<AlgoInnerResponse> testPredict(@RequestParam String user) {
        AlgoInnerRequest request = new AlgoInnerRequest();
        request.setCpuTimeFlag(1);
        request.setItemIds(new ArrayList<>());
        request.setBatchSize(1);
        request.setDeviceId("1111111111111");
        request.setModelKey("rta_model_version_1");
        request.setUserId(user);
        AlgoInnerResponse response = dubboConsumerService.callAlgo(request);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

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

package com.qunar.marketing.mkt.pub.alg.tfserving.push.engine.server.controller;

import com.qunar.marketing.mkt.pub.alg.tfserving.push.engine.server.api.AlgoInnerRequest;
import com.qunar.marketing.mkt.pub.alg.tfserving.push.engine.server.api.AlgoInnerResponse;
import com.qunar.marketing.mkt.pub.alg.tfserving.push.engine.server.service.model.ModelServerService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 实时推理控制器
 * <p>提供实时推理的 HTTP 入口
 *
 * @author 郭安
 * @date 2026-07-14
 */
@RestController
@Slf4j
public class RealtimeInferController {

    @Autowired
    private ModelServerService modelServerService;

    /**
     * 小红书实时推理接口
     * @param user 用户标识
     * @return 推理结果
     */
    @GetMapping("/redbook/predict")
    public Map<String, Object> predict(@RequestParam String user) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("user", user);
        result.put("predict", 1.1);
        log.info("realtime infer, user={}, predict=1.1", user);
        return result;
    }

    /**
     * 直连推理（不走 Dubbo）
     * @param userId 用户标识
     * @return 直接推理结果
     */
    @GetMapping("/predict")
    public ResponseEntity<AlgoInnerResponse> directPredict(@RequestParam String userId) {
        AlgoInnerRequest request = new AlgoInnerRequest();
        request.setCpuTimeFlag(1);
        request.setItemIds(new ArrayList<>());
        request.setBatchSize(1);
        request.setDeviceId("1111111111111");
        request.setModelKey("rta_model_version_1");
        request.setUserId(userId);

        AlgoInnerResponse response = new AlgoInnerResponse();
        long start = System.currentTimeMillis();
        try {
            modelServerService.predict(request, response);
        } catch (Exception e) {
            log.error("== direct predict failed, userId={}", userId, e);
        }
        long elapsed = System.currentTimeMillis() - start;
        log.info("== direct-predict total time: {}ms, userId={}", elapsed, userId);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}

package com.qunar.marketing.mkt.pub.alg.tfserving.push.engine.server.controller;

import com.qunar.marketing.mkt.pub.alg.tfserving.push.engine.server.service.utils.aws.AWS3Service;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.context.annotation.Profile;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * OSS 对象存储查询控制器
 * @author 郭安
 * @date 2026-07-14
 */
@RestController
@Slf4j
@Profile({"dev", "test"})
public class OSSController {

    @Resource
    private AWS3Service awS3Service;

    /**
     * 探测单个 OSS 对象是否存在及其元信息
     * @param key 对象 key
     * @return 含 exists/size/lastModified 的元信息
     */
    @GetMapping("/oss/head")
    public Map<String, Object> headObject(@RequestParam String key) {
        return awS3Service.headObject(key);
    }

    /**
     * 批量探测 OSS 对象是否存在
     * <p>不传 keys 时默认探测模型加载链路依赖的关键文件：dictInfo.list 与当前清单引用的模型 zip
     * @param keys 逗号分隔的对象 key 列表，可选
     * @return 各对象元信息列表及总体统计
     */
    @GetMapping("/oss/check")
    public Map<String, Object> checkObjects(@RequestParam(required = false) String keys) {
        Map<String, Object> result = new LinkedHashMap<>();

        List<String> keyList;
        if (keys != null && !keys.trim().isEmpty()) {
            keyList = Arrays.asList(keys.split(","));
        } else {
            // 默认探测模型加载链路依赖的关键文件
            keyList = Arrays.asList("dictInfo.list", "rta_model_version_1.zip");
        }
        result.put("keys", keyList);

        try {
            java.util.List<Map<String, Object>> objects = new java.util.ArrayList<>();
            int existsCount = 0;
            for (String key : keyList) {
                String trimmed = key.trim();
                if (trimmed.isEmpty()) {
                    continue;
                }
                Map<String, Object> item = awS3Service.headObject(trimmed);
                if (Boolean.TRUE.equals(item.get("exists"))) {
                    existsCount++;
                }
                objects.add(item);
            }
            result.put("count", objects.size());
            result.put("existsCount", existsCount);
            result.put("objects", objects);
        } catch (Exception e) {
            result.put("== error", "批量探测OSS对象失败: " + e.getMessage());
            log.error("批量探测OSS对象失败: {}", e.getMessage());
        }

        return result;
    }
}

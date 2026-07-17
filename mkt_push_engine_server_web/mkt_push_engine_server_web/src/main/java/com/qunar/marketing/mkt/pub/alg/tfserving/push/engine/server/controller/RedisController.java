package com.qunar.marketing.mkt.pub.alg.tfserving.push.engine.server.controller;

import com.qunar.redis.storage.Sedis3;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.context.annotation.Profile;

import javax.annotation.Resource;
import java.util.*;

@RestController
@Slf4j
@Profile({"dev", "test"})
public class RedisController {

    @Resource(name = "featureStorge")
    private Sedis3 featureStorge;

    @GetMapping("/redis/insert")
    public Map<String, Object> insertTestData() {
        Map<String, Object> result = new LinkedHashMap<>();

        if (featureStorge == null) {
            result.put("error", "featureStorge连接不可用");
            return result;
        }

        List<String> insertedKeys = new ArrayList<>();
        List<Map<String, Object>> details = new ArrayList<>();
        int successCount = 0;
        int failCount = 0;

        List<Map<String, String>> userProfiles = buildUserProfiles();

        for (int i = 0; i < userProfiles.size(); i++) {
            Map<String, String> profile = userProfiles.get(i);
            String userId = profile.get("userId");
            String userKey = "user:" + userId;

            try {
                for (Map.Entry<String, String> entry : profile.entrySet()) {
                    String field = entry.getKey();
                    String value = entry.getValue();
                    if ("userId".equals(field) || "profile_desc".equals(field)) {
                        continue;
                    }
                    featureStorge.hset(userKey, field, value);
                }

                insertedKeys.add(userKey);
                successCount++;

                Map<String, Object> detail = new LinkedHashMap<>();
                detail.put("key", userKey);
                detail.put("userId", userId);
                detail.put("featureCount", profile.size() - 2);
                detail.put("status", "success");
                detail.put("userProfile", profile.get("profile_desc"));
                details.add(detail);

                log.info("成功写入用户特征: {} ({})", userKey, profile.get("profile_desc"));
            } catch (Exception e) {
                failCount++;
                Map<String, Object> detail = new LinkedHashMap<>();
                detail.put("key", userKey);
                detail.put("userId", userId);
                detail.put("status", "failed");
                detail.put("error", e.getMessage());
                details.add(detail);

                log.error("写入用户特征失败(user={}): {}", userId, e.getMessage());
            }
        }

        result.put("message", "测试数据写入完成");
        result.put("totalCount", userProfiles.size());
        result.put("successCount", successCount);
        result.put("failCount", failCount);
        result.put("insertedKeys", insertedKeys);
        result.put("details", details);
        result.put("queryExample", "/redis/query?userId=1001");

        return result;
    }

    @GetMapping("/redis/query")
    public Map<String, Object> queryUserByUserId(@RequestParam String userId) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("userId", userId);

        String userKey = "user:" + userId;

        if (featureStorge == null) {
            result.put("error", "featureStorge连接不可用");
            return result;
        }

        try {
            Map<String, String> userData = featureStorge.hgetAll(userKey);
            if (!userData.isEmpty()) {
                result.put("key", userKey);
                result.put("featureCount", userData.size());
                result.put("features", userData);
            } else {
                result.put("key", userKey);
                result.put("error", "key不存在");
            }
        } catch (Exception e) {
            result.put("error", "查询失败: " + e.getMessage());
            log.error("查询用户特征失败(user={}): {}", userId, e.getMessage());
        }

        return result;
    }

    private List<Map<String, String>> buildUserProfiles() {
        List<Map<String, String>> profiles = new ArrayList<>();

        Map<String, String> p1 = new HashMap<>();
        p1.put("userId", "1001");
        p1.put("profile_desc", "年轻女性学生-高频旅游用户");
        p1.put("ip", "107110");
        p1.put("device_type", "5");
        p1.put("model", "honor BVL-AN16");
        p1.put("model_detail", "BVL-AN16");
        p1.put("city_level", "4");
        p1.put("city_nature", "4");
        p1.put("install_app_list", "62");
        p1.put("pre_click_count", "-1");
        p1.put("strategy_click_count", "-1");
        p1.put("seed_click_count", "-1");
        p1.put("low_price_click_count", "-1");
        p1.put("age", "4");
        p1.put("gender", "1");
        p1.put("Qunar_install", "1");
        p1.put("consumption_level_id", "1");
        p1.put("jiugongge_click_pv", "-1");
        p1.put("bigsearch_click_count", "-1");
        p1.put("diff_date", "-1");
        p1.put("open_qunar_app", "0");
        profiles.add(p1);

        Map<String, String> p2 = new HashMap<>();
        p2.put("userId", "1002");
        p2.put("profile_desc", "中年男性商务人士-高频出差");
        p2.put("ip", "359183");
        p2.put("device_type", "3");
        p2.put("model", "xiaomi 24031PN0DC");
        p2.put("model_detail", "24031PN0DC");
        p2.put("city_level", "4");
        p2.put("city_nature", "4");
        p2.put("install_app_list", "62");
        p2.put("pre_click_count", "1");
        p2.put("strategy_click_count", "-1");
        p2.put("seed_click_count", "1");
        p2.put("low_price_click_count", "-1");
        p2.put("age", "6");
        p2.put("gender", "1");
        p2.put("Qunar_install", "1");
        p2.put("consumption_level_id", "1");
        p2.put("jiugongge_click_pv", "-1");
        p2.put("bigsearch_click_count", "-1");
        p2.put("diff_date", "0.71");
        p2.put("open_qunar_app", "0");
        profiles.add(p2);

        Map<String, String> p3 = new HashMap<>();
        p3.put("userId", "1003");
        p3.put("profile_desc", "家庭主妇-周末亲子游");
        p3.put("ip", "192.168.1.103");
        p3.put("device_type", "Android");
        p3.put("model", "Xiaomi 12");
        p3.put("model_detail", "Xiaomi,12");
        p3.put("city_level", "2");
        p3.put("city_nature", "guangzhou");
        p3.put("install_app_list", "qunar,meituan,pinduoduo");
        p3.put("pre_click_count", "180");
        p3.put("strategy_click_count", "60");
        p3.put("seed_click_count", "20");
        p3.put("low_price_click_count", "50");
        p3.put("age", "32");
        p3.put("gender", "0");
        p3.put("Qunar_install", "1");
        p3.put("consumption_level_id", "3");
        p3.put("jiugongge_click_pv", "30");
        p3.put("bigsearch_click_count", "5");
        p3.put("diff_date", "10");
        p3.put("open_qunar_app", "0");
        profiles.add(p3);

        Map<String, String> p4 = new HashMap<>();
        p4.put("userId", "1004");
        p4.put("profile_desc", "老年男性-偶尔出游");
        p4.put("ip", "192.168.1.104");
        p4.put("device_type", "Android");
        p4.put("model", "HUAWEI Nova");
        p4.put("model_detail", "HUAWEI,Nova");
        p4.put("city_level", "3");
        p4.put("city_nature", "chengdu");
        p4.put("install_app_list", "qunar");
        p4.put("pre_click_count", "50");
        p4.put("strategy_click_count", "10");
        p4.put("seed_click_count", "5");
        p4.put("low_price_click_count", "8");
        p4.put("age", "65");
        p4.put("gender", "1");
        p4.put("Qunar_install", "1");
        p4.put("consumption_level_id", "4");
        p4.put("jiugongge_click_pv", "5");
        p4.put("bigsearch_click_count", "2");
        p4.put("diff_date", "30");
        p4.put("open_qunar_app", "0");
        profiles.add(p4);

        Map<String, String> p5 = new HashMap<>();
        p5.put("userId", "1005");
        p5.put("profile_desc", "年轻男性程序员-热爱旅行");
        p5.put("ip", "192.168.1.105");
        p5.put("device_type", "iPhone");
        p5.put("model", "iPhone 15 Pro");
        p5.put("model_detail", "iPhone15,3");
        p5.put("city_level", "1");
        p5.put("city_nature", "hangzhou");
        p5.put("install_app_list", "qunar,ctrip,fliggy,tongcheng");
        p5.put("pre_click_count", "650");
        p5.put("strategy_click_count", "280");
        p5.put("seed_click_count", "150");
        p5.put("low_price_click_count", "70");
        p5.put("age", "28");
        p5.put("gender", "1");
        p5.put("Qunar_install", "1");
        p5.put("consumption_level_id", "3");
        p5.put("jiugongge_click_pv", "220");
        p5.put("bigsearch_click_count", "60");
        p5.put("diff_date", "0");
        p5.put("open_qunar_app", "1");
        profiles.add(p5);

        return profiles;
    }
}
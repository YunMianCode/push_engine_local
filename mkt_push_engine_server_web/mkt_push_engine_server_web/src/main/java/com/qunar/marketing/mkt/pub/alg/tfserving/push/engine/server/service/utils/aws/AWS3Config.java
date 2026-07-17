package com.qunar.marketing.mkt.pub.alg.tfserving.push.engine.server.service.utils.aws;

import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;

@Configuration
@PropertySource(value = "classpath:oss.properties", encoding = "UTF-8")
@ConfigurationProperties(prefix = "aws")
@Data
public class AWS3Config {
    private static Logger LOGGER = LoggerFactory.getLogger(AWS3Config.class);
    private Map<String, String> configMap;

    // 接收配置文件中的属性（字段名与配置项对应，支持嵌套）
    private Map<String, String> oss = new HashMap<>();

    /**
     * 初始化 AWS3 配置
     * <p>@PostConstruct 生命周期方法，在 Bean 属性注入完成后、Bean 投入使用前触发；
     * 将 oss.properties 中读取到的 oss 配置赋值给 configMap 并打印日志
     */
    @PostConstruct
    public void init() {
        LOGGER.info("== oss:{}", oss.toString());
        configMap = oss;
        LOGGER.info("== ConfigMap: {}", configMap.toString());
    }

}

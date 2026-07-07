package com.qunar.marketing.mkt.pub.alg.tfserving.push.engine.server.service.utils.aws;

import com.qunar.marketing.mkt.pub.alg.tfserving.push.engine.server.service.model.ModelManager;
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

    @PostConstruct
    public void init() {
        LOGGER.info("oss:{}", oss.toString());
        configMap = oss;
        LOGGER.info("ConfigMap: {}", configMap.toString());
    }

//    public Map<String, String> getConfigMap() {
//        return configMap;
//    }
//    public void setConfigMap(Map<String, String> configMap) {
//        this.configMap = configMap;
//    }
//    public Map<String, String> getOssMap() {
//        return oss;
//    }
//    public void setOssMap(Map<String, String> oss) {
//        this.oss = oss;
//    }


}

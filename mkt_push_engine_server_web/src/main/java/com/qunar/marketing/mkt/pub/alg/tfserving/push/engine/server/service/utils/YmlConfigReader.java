package com.qunar.marketing.mkt.pub.alg.tfserving.push.engine.server.service.utils;

import org.yaml.snakeyaml.Yaml;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Map;

public class YmlConfigReader {
    /**
     * 读取YAML文件并转换为Map
     */
    public static Map<String, Object> readYamlAsMap(String filePath) {
        // 获取YAML文件的输入流
        try (InputStream inputStream = new FileInputStream(filePath)) {
            if (inputStream == null) {
                throw new IllegalArgumentException("找不到YAML文件: " + filePath);
            }

            // 创建YAML解析器
            Yaml yaml = new Yaml();

            // 解析YAML文件并转换为Map
            return yaml.load(inputStream);
        } catch (Exception e) {
            throw new RuntimeException("读取YAML文件失败: " + e.getMessage(), e);
        }
    }
    /**
     * 读取YAML文件并转换为指定的Java对象
     */
    public static <T> T readYamlAsObject(String filePath, Class<T> clazz) {
        try (InputStream inputStream = new FileInputStream(filePath)) {
            if (inputStream == null) {
                throw new IllegalArgumentException("找不到YAML文件: " + filePath);
            }

            Yaml yaml = new Yaml();
            return yaml.loadAs(inputStream, clazz);
        } catch (Exception e) {
            throw new RuntimeException("读取YAML文件失败: " + e.getMessage(), e);
        }
    }

}

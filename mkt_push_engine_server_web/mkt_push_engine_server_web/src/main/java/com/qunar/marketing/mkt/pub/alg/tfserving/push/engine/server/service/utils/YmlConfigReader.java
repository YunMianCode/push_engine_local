package com.qunar.marketing.mkt.pub.alg.tfserving.push.engine.server.service.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;

/**
 * YAML 配置读取工具类
 * 统一使用 UTF-8 编码读取，失败时记录日志并抛出 RuntimeException。
 */
public class YmlConfigReader {

    private static final Logger LOGGER = LoggerFactory.getLogger(YmlConfigReader.class);

    /**
     * 读取YAML文件并转换为Map
     * @param filePath YAML文件的路径
     * @return 解析后的Map，键为String、值为Object；空文件时返回null
     * @throws IllegalArgumentException 当filePath为空时抛出
     * @throws RuntimeException         当YAML文件找不到或读取解析失败时抛出
     */
    public static Map<String, Object> readYamlAsMap(String filePath) {
        checkFilePath(filePath);
        try (InputStream inputStream = new FileInputStream(filePath)) {
            Yaml yaml = new Yaml();
            Map<String, Object> result = yaml.load(
                    new InputStreamReader(inputStream, StandardCharsets.UTF_8));
            LOGGER.info("== 成功读取YAML文件为Map: {}", filePath);
            return result;
        } catch (IOException e) {
            // 文件不存在或IO异常，单独处理，日志更清晰
            LOGGER.error("== YAML文件读取IO失败: {}", filePath, e);
            throw new RuntimeException("读取YAML文件IO失败: " + filePath, e);
        } catch (RuntimeException e) {
            // SnakeYAML 解析失败（如 YAML 语法错误、类型不匹配）会抛 YAMLException
            LOGGER.error("== YAML文件解析失败: {}", filePath, e);
            throw new RuntimeException("解析YAML文件失败: " + filePath, e);
        }
    }

    /**
     * 读取YAML文件并转换为指定的Java对象
     * @param filePath YAML文件的路径
     * @param clazz    目标Java对象的Class类型
     * @param <T>      目标对象类型
     * @return 反序列化后的Java对象
     * @throws IllegalArgumentException 当filePath为空时抛出
     * @throws RuntimeException         当YAML文件找不到或读取解析失败时抛出
     */
    public static <T> T readYamlAsObject(String filePath, Class<T> clazz) {
        checkFilePath(filePath);
        try (InputStream inputStream = Files.newInputStream(Paths.get(filePath))) {
            Yaml yaml = new Yaml();
            T result = yaml.loadAs(
                    new InputStreamReader(inputStream, StandardCharsets.UTF_8), clazz);
            LOGGER.info("== 成功读取YAML文件为对象[{}]: {}", clazz.getSimpleName(), filePath);
            return result;
        } catch (IOException e) {
            LOGGER.error("== YAML文件读取IO失败: {}", filePath, e);
            throw new RuntimeException("读取YAML文件IO失败: " + filePath, e);
        } catch (RuntimeException e) {
            LOGGER.error("== YAML文件解析为对象[{}]失败: {}", clazz.getSimpleName(), filePath, e);
            throw new RuntimeException("解析YAML文件失败: " + filePath + " 目标类型=" + clazz.getSimpleName(), e);
        }
    }

    /**
     * 校验文件路径非空
     */
    private static void checkFilePath(String filePath) {
        if (filePath == null || filePath.trim().isEmpty()) {
            LOGGER.error("== YAML文件路径为空");
            throw new IllegalArgumentException("YAML文件路径不能为空");
        }
    }

}

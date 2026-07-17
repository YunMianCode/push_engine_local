package com.qunar.marketing.mkt.pub.alg.tfserving.push.engine.server.common.config;

import com.qunar.redis.storage.Sedis3;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class RedisConfig {

    /**
     * 创建主特征存储 Sedis3 客户端实例
     * <p>在 Spring 容器启动时作为 Bean 注册，使用 redis.properties 中 featureStorage 前缀的配置参数初始化 Sedis3 客户端，用于访问主特征存储
     * @param namespace 命名空间
     * @param cipher 密码（cipher）
     * @param timeout 连接超时时间（毫秒）
     * @param poolCoreSize 连接池核心大小
     * @param poolMaxSize 连接池最大大小
     * @param zkAddr ZooKeeper 地址
     * @return 初始化完成的 Sedis3 客户端实例
     */
    @Bean(name = "featureStorge")
    public Sedis3 getFeatureStorge(@Value("${redis.featureStorage.namespace}") String namespace,
                                   @Value("${redis.featureStorage.cipher}") String cipher,
                                   @Value("${redis.featureStorage.timeout}") int timeout,
                                   @Value("${redis.featureStorage.poolCoreSize}") int poolCoreSize,
                                   @Value("${redis.featureStorage.poolMaxSize}") int poolMaxSize,
                                   @Value("${redis.featureStorage.zkAddr}") String zkAddr){
        log.info("namespace: {}, cipher: {}, timeout {}, zkA: {}", namespace,cipher,timeout,zkAddr);
        return new Sedis3(namespace, cipher, timeout, poolCoreSize, poolMaxSize, zkAddr);
    }

    /**
     * 创建第二组特征存储 Sedis3 客户端实例
     * <p>在 Spring 容器启动时作为 Bean 注册，使用 redis.properties 中 featureStorage1 前缀的配置参数初始化 Sedis3 客户端，用于访问第二组特征存储
     * @param namespace 命名空间
     * @param cipher 密码（cipher）
     * @param timeout 连接超时时间（毫秒）
     * @param poolCoreSize 连接池核心大小
     * @param poolMaxSize 连接池最大大小
     * @param zkAddr ZooKeeper 地址
     * @return 初始化完成的 Sedis3 客户端实例
     */
    @Bean(name = "featureStorge1")
    public Sedis3 getFeatureStorge1(@Value("${redis.featureStorage1.namespace}") String namespace,
                                    @Value("${redis.featureStorage1.cipher}") String cipher,
                                    @Value("${redis.featureStorage1.timeout}") int timeout,
                                    @Value("${redis.featureStorage1.poolCoreSize}") int poolCoreSize,
                                    @Value("${redis.featureStorage1.poolMaxSize}") int poolMaxSize,
                                    @Value("${redis.featureStorage1.zkAddr}") String zkAddr){
        log.info("namespace: {}, cipher: {}, timeout {}, zkA: {}", namespace,cipher,timeout,zkAddr);
        return new Sedis3(namespace, cipher, timeout, poolCoreSize, poolMaxSize, zkAddr);
    }


    /**
     * 创建第三组特征存储 Sedis3 客户端实例
     * <p>在 Spring 容器启动时作为 Bean 注册，使用 redis.properties 中 featureStorage2 前缀的配置参数初始化 Sedis3 客户端，用于访问第三组特征存储
     * @param namespace 命名空间
     * @param cipher 密码（cipher）
     * @param timeout 连接超时时间（毫秒）
     * @param poolCoreSize 连接池核心大小
     * @param poolMaxSize 连接池最大大小
     * @param zkAddr ZooKeeper 地址
     * @return 初始化完成的 Sedis3 客户端实例
     */
    @Bean(name = "featureStorge2")
    public Sedis3 getFeatureStorge2(@Value("${redis.featureStorage2.namespace}") String namespace,
                                    @Value("${redis.featureStorage2.cipher}") String cipher,
                                    @Value("${redis.featureStorage2.timeout}") int timeout,
                                    @Value("${redis.featureStorage2.poolCoreSize}") int poolCoreSize,
                                    @Value("${redis.featureStorage2.poolMaxSize}") int poolMaxSize,
                                    @Value("${redis.featureStorage2.zkAddr}") String zkAddr){
        log.info("namespace: {}, cipher: {}, timeout {}, zkA: {}", namespace,cipher,timeout,zkAddr);
        return new Sedis3(namespace, cipher, timeout, poolCoreSize, poolMaxSize, zkAddr);
    }



}

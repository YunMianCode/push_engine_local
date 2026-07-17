package com.qunar.marketing.mkt.pub.alg.tfserving.push.engine.server.redis;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import qunar.tc.qclient.redis.RedisAsyncClient;
import qunar.tc.qclient.redis.SimpleRedisAsyncClient;
import qunar.tc.qclient.redis.command.ExtendListenableFuture;
import qunar.tc.qclient.redis.exception.checked.RedisException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.concurrent.ExecutionException;
import javax.annotation.Resource;

/**
 * wiki: http://wiki.corp.qunar.com/confluence/display/devwiki/qclient-redis
 * Redis 连接测试
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
public class RedisTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(RedisTest.class);

    @Resource
    private RedisAsyncClient redisAsyncClient;

    @Resource
    private SimpleRedisAsyncClient simpleRedisAsyncClient;

    /**
     * 验证RedisAsyncClient与SimpleRedisAsyncClient的连接及读写功能
     * <p>
     * 补充说明：先分别建立两个异步Redis客户端连接，通过set写入测试值，再通过get异步获取并打印结果，用于验证qclient-redis的连通性与基本读写能力。
     * 
     * @throws RedisException       当Redis操作发生异常时抛出
     * @throws ExecutionException   当异步Future获取结果时发生异常时抛出
     * @throws InterruptedException 当等待异步结果被中断时抛出
     */
    @Test
    public void redisTest() throws RedisException, ExecutionException, InterruptedException {
        redisAsyncClient.connect();
        simpleRedisAsyncClient.connect();
        redisAsyncClient.set("redisAsyncClient", "redisAsyncClientTest");
        simpleRedisAsyncClient.set("simpleRedisAsyncClient", "simpleRedisAsyncClientTest");
        ExtendListenableFuture<String> asyncClientFuture = redisAsyncClient.get("redisAsyncClient");
        ExtendListenableFuture<String> simpleRedisAsyncFuture = this.simpleRedisAsyncClient
                .get("simpleRedisAsyncClient");
        LOGGER.info("redisAsyncClient result {}", asyncClientFuture.get());
        LOGGER.info("simpleRedisAsyncClient result {}", simpleRedisAsyncFuture.get());

    }
}

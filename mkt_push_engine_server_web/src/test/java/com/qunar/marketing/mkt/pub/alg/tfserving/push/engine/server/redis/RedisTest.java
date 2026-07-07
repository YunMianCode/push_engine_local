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
 * wiki:  http://wiki.corp.qunar.com/confluence/display/devwiki/qclient-redis
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

    @Test
    public void redisTest() throws RedisException, ExecutionException, InterruptedException {
        redisAsyncClient.connect();
        simpleRedisAsyncClient.connect();
        redisAsyncClient.set("redisAsyncClient", "redisAsyncClientTest");
        simpleRedisAsyncClient.set("simpleRedisAsyncClient", "simpleRedisAsyncClientTest");
        ExtendListenableFuture<String> asyncClientFuture = redisAsyncClient.get("redisAsyncClient");
        ExtendListenableFuture<String> simpleRedisAsyncFuture = this.simpleRedisAsyncClient.get("simpleRedisAsyncClient");
        LOGGER.info("redisAsyncClient result {}", asyncClientFuture.get());
        LOGGER.info("simpleRedisAsyncClient result {}", simpleRedisAsyncFuture.get());

    }
}

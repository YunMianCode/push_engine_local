package com.qunar.marketing.mkt.pub.alg.tfserving.push.engine.server.tcdev.factory;

import com.qunar.marketing.mkt.pub.alg.tfserving.push.engine.server.api.ApacheDubboFutureAsyncService;
import com.qunar.marketing.mkt.pub.alg.tfserving.push.engine.server.api.DubboBetterAsyncServiceGenerate;
import com.qunar.marketing.mkt.pub.alg.tfserving.push.engine.server.api.DubboBlogApiService;
import org.apache.dubbo.config.annotation.DubboReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import qunar.tc.spring.cloud.annotation.QunarMesh;
/**
* https://wiki.corp.qunar.com/confluence/display/devwiki/Spring+Cloud+Qunar+Dubbo
*/
@Configuration
public class DubboReferenceBeanFactory {
    private static final Logger LOG = LoggerFactory.getLogger(DubboReferenceBeanFactory.class);

    @DubboReference(check = false, version = "1.0.0", timeout = 5000)
    @QunarMesh(targetAppCode = "jjddd", selector = "beta")
    public DubboBlogApiService dubboBlogApiServiceRef;

    /**
     * 将DubboBlogApiService引用注册为Spring Bean
     * <p>补充说明：直接返回由@DubboReference注入的dubboBlogApiServiceRef字段，使该Dubbo引用可被其他Bean依赖注入；目标应用jjddd、beta环境。
     * @return DubboBlogApiService的Dubbo引用实例
     */
    @Bean(name = "dubboBlogApiService")
    public DubboBlogApiService dubboBlogApiService() {
        return dubboBlogApiServiceRef;
    }

    @DubboReference(version = "1.0.0", check = false)
    @QunarMesh(targetAppCode = "jjddd", selector = "beta")
    public DubboBetterAsyncServiceGenerate dubboBetterAsyncServiceGenerate;

    /**
     * 将DubboBetterAsyncServiceGenerate引用注册为Spring Bean
     * <p>补充说明：直接返回由@DubboReference注入的dubboBetterAsyncServiceGenerate字段，使该异步Dubbo引用可被其他Bean依赖注入；目标应用jjddd、beta环境。
     * @return DubboBetterAsyncServiceGenerate的Dubbo引用实例
     */
    @Bean(name = "dubboBetterAsyncServiceGenerate")
    public DubboBetterAsyncServiceGenerate dubboBetterAsyncServiceGenerate() {
        return dubboBetterAsyncServiceGenerate;
    }

    @DubboReference(check = false)
    @QunarMesh(targetAppCode = "jjddd", selector = "beta")
    public ApacheDubboFutureAsyncService apacheDubboFutureAsyncService;

    /**
     * 将ApacheDubboFutureAsyncService引用注册为Spring Bean
     * <p>补充说明：直接返回由@DubboReference注入的apacheDubboFutureAsyncService字段，使该基于CompletableFuture的异步Dubbo引用可被其他Bean依赖注入；目标应用jjddd、beta环境。
     * @return ApacheDubboFutureAsyncService的Dubbo引用实例
     */
    @Bean("apacheDubboFutureAsyncService")
    public ApacheDubboFutureAsyncService apacheDubboFutureAsyncService() {
        return apacheDubboFutureAsyncService;
    }
}

package com.qunar.marketing.mkt.pub.alg.tfserving.push.engine.server.tcdev.factory;

import com.qunar.marketing.mkt.pub.alg.tfserving.push.engine.server.api.ApacheDubboFutureAsyncService;
import com.qunar.marketing.mkt.pub.alg.tfserving.push.engine.server.api.DubboBetterAsyncServiceGenerate;
import com.qunar.marketing.mkt.pub.alg.tfserving.push.engine.server.api.DubboBlogApiService;
import org.apache.dubbo.config.annotation.DubboReference;
import org.apache.dubbo.config.annotation.Method;
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

    @Bean(name = "dubboBlogApiService")
    public DubboBlogApiService dubboBlogApiService() {
        return dubboBlogApiServiceRef;
    }

    @DubboReference(version = "1.0.0", check = false)
    @QunarMesh(targetAppCode = "jjddd", selector = "beta")
    public DubboBetterAsyncServiceGenerate dubboBetterAsyncServiceGenerate;

    @Bean(name = "dubboBetterAsyncServiceGenerate")
    public DubboBetterAsyncServiceGenerate dubboBetterAsyncServiceGenerate() {
        return dubboBetterAsyncServiceGenerate;
    }

    @DubboReference(check = false)
    @QunarMesh(targetAppCode = "jjddd", selector = "beta")
    public ApacheDubboFutureAsyncService apacheDubboFutureAsyncService;

    @Bean("apacheDubboFutureAsyncService")
    public ApacheDubboFutureAsyncService apacheDubboFutureAsyncService() {
        return apacheDubboFutureAsyncService;
    }
}

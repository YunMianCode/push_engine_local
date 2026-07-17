package com.qunar.marketing.mkt.pub.alg.tfserving.push.engine.server;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.springframework.context.annotation.PropertySource;

@SpringBootApplication
@EnableDubbo(scanBasePackages = "com.qunar.marketing.mkt.pub.alg.tfserving.push.engine.server")
@PropertySource({
        "classpath:redis.properties"
})
public class Application extends SpringBootServletInitializer {

    public static void main(String[] args) {
        org.springframework.boot.SpringApplication.run(Application.class, args);
    }
}

package com.qunar.marketing.mkt.pub.alg.tfserving.push.engine.server.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;


@Controller
public class WelcomeController {
    /**
     * 应用根路径欢迎页跳转
     * @return 视图名称 "welcome"
     * @throws Exception 视图解析或请求处理过程中可能抛出的异常
     */
    @GetMapping("/")
    public String welcome() throws Exception {
        return "welcome";
    }

}
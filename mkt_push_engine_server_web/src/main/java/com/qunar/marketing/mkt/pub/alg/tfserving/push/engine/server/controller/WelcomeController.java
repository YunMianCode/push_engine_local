package com.qunar.marketing.mkt.pub.alg.tfserving.push.engine.server.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;


/**
 * 欢迎页面控制器
 */
@Controller
public class WelcomeController {

    /**
     * 欢迎页面
     * @return 欢迎页面视图
     * @throws Exception 页面渲染异常
     */
    @GetMapping("/")
    public String welcome() throws Exception {
        return "welcome";
    }

}
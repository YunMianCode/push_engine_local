package com.qunar.marketing.mkt.pub.alg.tfserving.push.engine.server.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;


@Controller
public class WelcomeController {
    @GetMapping("/")
    public String welcome() throws Exception {
        return "welcome";
    }

}
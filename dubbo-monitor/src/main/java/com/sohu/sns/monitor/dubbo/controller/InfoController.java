package com.sohu.sns.monitor.dubbo.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * author:jy
 * time:16-11-2下午2:31
 */
@Controller
public class InfoController {
    @RequestMapping(value = "/health")
    @ResponseBody
    public String health() {
        return "up";
    }


    @RequestMapping(value = "/")
    @ResponseBody
    public String index() {
        return "dubbo monitor";
    }
}

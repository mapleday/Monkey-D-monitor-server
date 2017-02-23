package com.sohu.sns.monitor.web.controler;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Created by yw on 2017/2/20.
 */
@Controller
public class SnsWatchController {
    @RequestMapping("/snsWatchUtil")
    public  String snsWatchUtil(){
        return "duty";
    }
}

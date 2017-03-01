package com.sohu.sns.monitor.web.controler;

import com.sohu.sns.monitor.web.service.PersonDutyService.SelectPersonDutyService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * Created by yw on 2017/2/23.
 */
@Controller
public class HealthController {

    @ResponseBody
    @RequestMapping("/health")
    public String Health(){
        SelectPersonDutyService.sendDutyInfo();
        return "health";
    }





}

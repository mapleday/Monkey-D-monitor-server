package com.sohu.sns.monitor.controller;

import com.sohu.sns.monitor.constant.RequestValue;
import com.sohu.sns.monitor.controller.annotation.RequestParams;
import com.sohu.sns.monitor.timer.AppErrorCountProcessor;
import com.sohu.sns.monitor.timer.DiffProcessor;
import com.sohu.sns.monitor.timer.StatLogCollector;
import com.sohu.snscommon.utils.LOGGER;
import com.sohu.snscommon.utils.constant.ModuleEnum;
import com.sohu.snscommon.utils.spring.SpringContextUtil;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Created by Gary on 2015/12/21.
 */

@Component("apiController")
public class ApiController {

    @RequestParams(path = "/monitor/collect_statLog", method = {"get", "post"}, required = {"extra"})
    public void CollectStatLog(Map<String, RequestValue> params) throws Exception {
        Long start = System.currentTimeMillis();
        StatLogCollector statLogCollector = SpringContextUtil.getBean(StatLogCollector.class);
        statLogCollector.handle();
        LOGGER.statLog(ModuleEnum.MONITOR_SERVICE, "Monitor.CollectStatLog", null, null, System.currentTimeMillis()-start, 0,0);
    }

    @RequestParams(path = "/monitor/diff_compare", method = {"get", "post"}, required = {"extra"})
    public void diffCompare(Map<String, RequestValue> params) throws Exception {
        Long start = System.currentTimeMillis();
        DiffProcessor diffProcessor = SpringContextUtil.getBean(DiffProcessor.class);
        diffProcessor.handle();
        LOGGER.statLog(ModuleEnum.MONITOR_SERVICE, "Monitor.CollectStatLog", null, null, System.currentTimeMillis() - start, 0, 0);
    }

    @RequestParams(path = "/monitor/count_app_error",  method = {"get", "post"}, required = {"extra"})
    public void countAppError(Map<String, RequestValue> params) throws Exception {
        Long start = System.currentTimeMillis();
        AppErrorCountProcessor appErrorCountProcessor = SpringContextUtil.getBean(AppErrorCountProcessor.class);
        appErrorCountProcessor.process();
        LOGGER.statLog(ModuleEnum.MONITOR_SERVICE, "Monitor.countAppError", null, null, System.currentTimeMillis() - start, 0, 0);
    }

    @RequestParams(path = "/monitor/test",  method = {"get", "post"}, required = {"extra"})
    public String test(Map<String, RequestValue> params) throws Exception {
        System.out.println("test");
        return "success";
    }
}

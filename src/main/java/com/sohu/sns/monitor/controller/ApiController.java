package com.sohu.sns.monitor.controller;

import com.sohu.sns.monitor.constant.RequestValue;
import com.sohu.sns.monitor.controller.annotation.RequestParams;
import com.sohu.sns.monitor.service.CollectStatLogService;
import com.sohu.sns.monitor.service.CountAppErrorService;
import com.sohu.sns.monitor.service.DiffCompareService;
import com.sohu.sns.monitor.service.Test;
import com.sohu.snscommon.utils.LOGGER;
import com.sohu.snscommon.utils.constant.ModuleEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Created by Gary on 2015/12/21.
 */

@Component("apiController")
public class ApiController {

    private static final String SUCCESS = "success";
    private static final String FAILURE = "failure";

    @Autowired
    private CollectStatLogService collectStatLogService;
    @Autowired
    private DiffCompareService diffCompareService;
    @Autowired
    private CountAppErrorService countAppErrorService;
    @Autowired
    private Test test;

    @RequestParams(path = "/monitor/collect_statLog", method = {"get", "post"}, required = {"extra"})
    public String CollectStatLog(Map<String, RequestValue> params) throws Exception {
        Long start = System.currentTimeMillis();
        try {
            collectStatLogService.handle();
        } catch (Exception e) {
            LOGGER.errorLog(ModuleEnum.MONITOR_SERVICE, "collectStatLog", null, null, e);
            return FAILURE;
        }
        LOGGER.statLog(ModuleEnum.MONITOR_SERVICE, "Monitor.CollectStatLog", null, null, System.currentTimeMillis()-start, 0,0);
        return SUCCESS;
    }

    @RequestParams(path = "/monitor/diff_compare", method = {"get", "post"}, required = {"extra"})
    public String diffCompare(Map<String, RequestValue> params) throws Exception {
        Long start = System.currentTimeMillis();
        try {
            diffCompareService.handle();
        } catch (Exception e) {
            LOGGER.errorLog(ModuleEnum.MONITOR_SERVICE, "diffCompare", null, null, e);
            return FAILURE;
        }
        LOGGER.statLog(ModuleEnum.MONITOR_SERVICE, "Monitor.CollectStatLog", null, null, System.currentTimeMillis() - start, 0, 0);
        return SUCCESS;
    }

    @RequestParams(path = "/monitor/count_app_error",  method = {"get", "post"}, required = {"extra"})
    public String countAppError(Map<String, RequestValue> params) throws Exception {
        Long start = System.currentTimeMillis();
        try {
            countAppErrorService.handle();
        } catch (Exception e) {
            LOGGER.errorLog(ModuleEnum.MONITOR_SERVICE, "countAppError", null, null, e);
            return FAILURE;
        }
        LOGGER.statLog(ModuleEnum.MONITOR_SERVICE, "Monitor.countAppError", null, null, System.currentTimeMillis() - start, 0, 0);
        return SUCCESS;
    }

    @RequestParams(path = "/monitor/test",  method = {"get", "post"}, required = {"extra"})
    public String test(Map<String, RequestValue> params) throws Exception {
        try {
            test.handle();
        } catch (Exception e) {
            e.printStackTrace();
            return FAILURE;
        }
        return SUCCESS;
    }
}

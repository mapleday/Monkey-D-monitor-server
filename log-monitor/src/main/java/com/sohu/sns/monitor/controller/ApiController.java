package com.sohu.sns.monitor.controller;

import com.sohu.sns.monitor.constant.RequestValue;
import com.sohu.sns.monitor.controller.annotation.RequestParams;
import com.sohu.sns.monitor.service.CollectStatLogService;
import com.sohu.sns.monitor.service.CountAppErrorService;
import com.sohu.sns.monitor.service.DiffCompareService;
import com.sohu.sns.monitor.service.RedisCheckService;
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
    private RedisCheckService redisCheckService;

    //收集statlog，分析访问次数是否异常
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

    //唯一名数据和sns缓存数据是否一致
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

    //统计LOGGER.errlog出现次数，根据appid
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

    @RequestParams(path = "/monitor/redis",  method = {"get", "post"}, required = {"extra"})
    public String redisCheck(Map<String, RequestValue> params) throws Exception {
        redisCheckService.checkRedis();
        return SUCCESS;
    }
}

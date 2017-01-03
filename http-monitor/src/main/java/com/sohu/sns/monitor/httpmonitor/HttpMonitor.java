package com.sohu.sns.monitor.httpmonitor;

import com.sohu.sns.monitor.common.module.HttpResource;
import com.sohu.sns.monitor.common.services.NotifyService;
import com.sohu.snscommon.utils.LOGGER;
import com.sohu.snscommon.utils.constant.ModuleEnum;
import com.sohu.snscommon.utils.http.HttpClientUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * author:jy
 * time:16-10-13下午3:33
 * HTTP 接口监控
 */
@Component
public class HttpMonitor {
    @Autowired
    NotifyService notifyService;

    /**
     * 监控
     *
     * @param resource
     * @return
     */
    public MonitorResult monitor(HttpResource resource) {
        HttpClientUtil httpClientUtil = HttpClientUtil.create(HttpMonitor.class.getName(), "monitor", resource.getMonitorTimeOut());
        Map<String, String> header = new HashMap();
        header.put("User-Agent", "sns-monitor httpclient");

        String resouceAddress = resource.getResourceAddress();
        Integer monitorTimes = resource.getMonitorTimes();

        MonitorResult monitorResult = new MonitorResult();
        Set<String> faildReasons = new HashSet();

        monitorResult.setMonitorTimes(monitorTimes);
        for (int i = 0; i < monitorTimes.intValue(); i++) {
            try {
                HttpClientUtil.HttpResult post = httpClientUtil.get(resouceAddress, Collections.<String, String>emptyMap(), header);
                int statusCode = post.getStatusCode();
                if (statusCode != 200) {
                    faildReasons.add(String.valueOf(statusCode));
                    monitorResult.setFailed(true);
                    monitorResult.setResouceAddress(resouceAddress);
                    monitorResult.addFailedTimes();
                    LOGGER.buziLog(ModuleEnum.UTIL, "HttpMonitor.monitor", String.valueOf(resource), String.valueOf(statusCode));
                }
            } catch (Exception e) {
                monitorResult.setFailed(true);
                faildReasons.add(e.getClass().getName());
                monitorResult.setResouceAddress(resouceAddress);
                monitorResult.addFailedTimes();
                LOGGER.buziLog(ModuleEnum.UTIL, "HttpMonitor.monitor", String.valueOf(resource), "is failed");
            }

            if (!faildReasons.isEmpty()) {
                StringBuilder faildReason = new StringBuilder();
                for (String reason : faildReasons) {
                    faildReason.append(reason + "|");
                }
                monitorResult.setFailedReason(faildReason.toString());
            }
        }
        return monitorResult;
    }

    public void notify(MonitorResult monitorResult) {
        String messageFormat = "接口报警：( 接口： %s ) ( 监控次：%s ) ( 错误次数:%s ) ( 错误原因:%s )";
        String msg = String.format(messageFormat, monitorResult.getResouceAddress()
                , monitorResult.getMonitorTimes()
                , monitorResult.getFailedTimes()
                , monitorResult.getFailedReason());
        notifyService.sendAllNotifyPerson(msg);
    }
}

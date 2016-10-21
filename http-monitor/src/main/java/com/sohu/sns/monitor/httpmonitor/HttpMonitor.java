package com.sohu.sns.monitor.httpmonitor;

import com.sohu.sns.monitor.httpmonitor.model.HttpResource;
import com.sohu.snscommon.utils.LOGGER;
import com.sohu.snscommon.utils.constant.ModuleEnum;
import com.sohu.snscommon.utils.http.HttpClientUtil;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * author:jy
 * time:16-10-13下午3:33
 * HTTP 接口监控
 */
@Component
public class HttpMonitor {
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
        StringBuilder faildReason = new StringBuilder();
        monitorResult.setMonitorTimes(monitorTimes);
        for (int i = 0; i < monitorTimes.intValue(); i++) {
            try {
                HttpClientUtil.HttpResult post = httpClientUtil.get(resouceAddress, Collections.<String, String>emptyMap(), header);
                int statusCode = post.getStatusCode();
                if (statusCode != 200) {
                    monitorResult.setFailed(true);
                    faildReason.append(String.valueOf(statusCode)).append("|");
                    monitorResult.setResouceAddress(resouceAddress);
                    monitorResult.addFailedTimes();
                    LOGGER.buziLog(ModuleEnum.UTIL, "HttpMonitor.monitor", String.valueOf(resource), String.valueOf(statusCode));
                }
            } catch (Exception e) {
                monitorResult.setFailed(true);
                faildReason.append(e.getClass().getName()).append("|");
                monitorResult.setResouceAddress(resouceAddress);
                monitorResult.addFailedTimes();
                LOGGER.buziLog(ModuleEnum.UTIL, "HttpMonitor.monitor", String.valueOf(resource), "is failed");
            }
            monitorResult.setFailedReason(faildReason.toString());
        }
        return monitorResult;
    }

    public void notify(String phones, MonitorResult monitorResult) {
        String messageFormat = "接口报警：( 接口： %s ) ( 监控次：%s ) ( 错误次数:%s ) ( 错误原因:%s )";
        String msg = String.format(messageFormat, monitorResult.getResouceAddress()
                , monitorResult.getMonitorTimes()
                , monitorResult.getFailedTimes()
                , monitorResult.getFailedReason());
        NotifyUtils.sendWeixin(phones, msg);
    }
}

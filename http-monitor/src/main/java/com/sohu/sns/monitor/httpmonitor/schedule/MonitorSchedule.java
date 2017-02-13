package com.sohu.sns.monitor.httpmonitor.schedule;

import com.sohu.sns.monitor.common.module.HttpResource;
import com.sohu.sns.monitor.common.services.HttpResourceService;
import com.sohu.sns.monitor.httpmonitor.HttpMonitor;
import com.sohu.sns.monitor.httpmonitor.MonitorResult;
import com.sohu.snscommon.utils.LOGGER;
import com.sohu.snscommon.utils.constant.ModuleEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * author:jy
 * time:16-10-13下午5:52
 * 监控调度
 */

@Component
public class MonitorSchedule {
    @Autowired
    HttpResourceService httpResourceService;
    @Autowired
    HttpMonitor monitor;

    private static ConcurrentHashMap<Integer, Long> taskSchedules = new ConcurrentHashMap();//任务调度
    private static ConcurrentHashMap<Integer, HttpResource> tasks = new ConcurrentHashMap();//所有任务

    private static final ExecutorService monitorExecutorService = Executors.newFixedThreadPool(16);//监控执行线程池

    /**
     * 监控任务调度
     *
     * @throws IOException
     */
    @Scheduled(fixedDelay = 1000l)
    public void schedule() throws IOException {
        LOGGER.buziLog(ModuleEnum.MONITOR_SERVICE, "MonitorSchedule.schedule", "monitor", "");
        final List<HttpResource> resources = httpResourceService.getResources();
        if (resources == null || resources.isEmpty()) {
            return;
        }

        Long taskNextTime = Long.valueOf(System.currentTimeMillis());
        for (HttpResource resource : resources) {
            Integer resourceId = resource.getId();
            tasks.put(resourceId, resource);
            if (!taskSchedules.containsKey(resourceId)) {
                taskSchedules.put(resourceId, taskNextTime);
            }
        }

        if (resources.size() < taskSchedules.size()) {
            //有监控被删除
            for (Integer id : taskSchedules.keySet()) {
                HttpResource httpResource = new HttpResource();
                httpResource.setId(id);
                if (!resources.contains(httpResource)) {
                    taskSchedules.remove(id);
                    tasks.remove(id);
                }
            }
        }

        Long currentTime = Long.valueOf(System.currentTimeMillis());
        for (Iterator<Map.Entry<Integer, Long>> iterator = taskSchedules.entrySet().iterator(); iterator.hasNext(); ) {
            Map.Entry<Integer, Long> next = iterator.next();
            final Integer key = next.getKey();
            final Long value = next.getValue();
            final HttpResource httpResource = tasks.get(key);

            if (currentTime >= value) {
                monitorExecutorService.execute(new Runnable() {
                    @Override
                    public void run() {
                        if (taskSchedules.contains(key)) {
                            taskSchedules.put(key, value + httpResource.getMonitorInterval());
                        }
                        MonitorResult monitorResult = monitor.monitor(httpResource);
                        if (monitorResult == null){
                            return;
                        }
                        if (monitorResult.isFailed() && monitorResult.getFailedTimes() >= httpResource.getAlarmThresholdTimes()) {
                            monitor.notify(monitorResult);
                        }
                    }
                });
            }
        }

    }

}

package com.sohu.sns.monitor.mqtt.tasks;

import com.sohu.sns.monitor.common.dao.mqttMonitor.MqttMonitorDao;
import com.sohu.sns.monitor.common.module.MqttServerAddress;
import com.sohu.sns.monitor.common.services.NotifyService;
import com.sohu.sns.monitor.mqtt.client.NettyClient;
import com.sohu.sns.monitor.mqtt.client.SimpleMqttMessage;
import com.sohu.snscommon.utils.LOGGER;
import com.sohu.snscommon.utils.constant.ModuleEnum;
import io.netty.channel.Channel;
import io.netty.handler.codec.mqtt.MqttSubscribeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * author:jy
 * time:16-11-9上午11:14
 * 定时连接,保证服务新的连接可以建立
 */
@Component
public class ScheduleConn {
    private static AtomicLong monitorTimes = new AtomicLong();
    private static AtomicLong errorTimes = new AtomicLong();

    @Autowired
    MqttMonitorDao mqttMonitorDao;
    @Autowired
    NotifyService notifyService;

    /**
     * 启动任务
     */
    public void start() {
        List<MqttServerAddress> servers = mqttMonitorDao.getServers();
        if (servers == null) {
            return;
        }
        for (MqttServerAddress server : servers) {
            run(server.getServerAddress(), server.getMonitorNum());
        }
    }

    /**
     * 静态内部类
     * 负责监控mqtt服务
     */
    private void run(String server, int connNums) {
        boolean isConnAvalable = true;
        ConcurrentHashMap<String, Integer> errorMessages = new ConcurrentHashMap(connNums);
        for (int i = 0; i < connNums; i++) {
            Channel conn = null;
            try {
                monitorTimes.incrementAndGet();
                conn = NettyClient.conn(server, 180);
                MqttSubscribeMessage subscribe = SimpleMqttMessage.createSubscribe("direct_message", "sns_log_echo", "sns_notification", "sns_task");
                conn.writeAndFlush(subscribe);
            } catch (Exception e) {
                errorTimes.incrementAndGet();
                isConnAvalable = false;
                String errorMessage = e.getMessage();
                if (errorMessages.containsKey(errorMessage)) {
                    Integer integer = errorMessages.get(errorMessage);
                    errorMessages.put(errorMessage, integer + 1);
                } else {
                    errorMessages.put(errorMessage, Integer.valueOf(1));
                }

                LOGGER.errorLog(ModuleEnum.MONITOR_SERVICE, "ScheduleConn.MqttMonitor.run", server, "", e);
            }
            if (conn != null) {
                conn.close();
            }
        }

        if (!isConnAvalable) {
            String message = "mqtt 连接建立异常报警，次数：" + errorTimes.get() + "/" + monitorTimes.get()
                    + " 原因：" + errorMessages.toString();
            notifyService.sendAllNotifyPerson(message);
        }

    }
}



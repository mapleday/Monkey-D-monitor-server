package com.sohu.sns.monitor.mqtt.tasks;

import com.sohu.sns.monitor.common.utils.NotifyUtils;
import com.sohu.sns.monitor.mqtt.client.NettyClient;
import com.sohu.sns.monitor.mqtt.client.SimpleMqttMessage;
import com.sohu.snscommon.utils.LOGGER;
import com.sohu.snscommon.utils.constant.ModuleEnum;
import io.netty.channel.Channel;
import io.netty.handler.codec.mqtt.MqttSubscribeMessage;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * author:jy
 * time:16-11-9上午11:14
 * 定时连接,保证服务新的连接可以建立
 */
public class ScheduleConn {
    private static final ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(2);

    private ScheduleConn() {

    }

    /**
     * 启动任务
     *
     * @param connNums
     * @param server
     */
    public static void start(int connNums, String server) {
        scheduledExecutorService.scheduleAtFixedRate(new MqttMonitor(connNums, server), 1, 60, TimeUnit.SECONDS);
    }

    /**
     * 静态内部类
     * 负责监控mqtt服务
     */
    private static class MqttMonitor implements Runnable {
        private int connNums;
        private String server;

        /**
         * 构造方法
         *
         * @param connNums
         * @param server
         */
        MqttMonitor(int connNums, String server) {
            this.connNums = connNums;
            this.server = server;
        }

        @Override
        public void run() {
            boolean isConnAvalable = true;
            ConcurrentHashMap<String, Integer> errorMessages = new ConcurrentHashMap(connNums);
            for (int i = 0; i < connNums; i++) {
                Channel conn = null;
                try {
                    conn = NettyClient.conn(server, 180);
                    MqttSubscribeMessage subscribe = SimpleMqttMessage.createSubscribe("direct_message", "sns_log_echo", "sns_notification", "sns_task");
                    conn.writeAndFlush(subscribe);
                } catch (Exception e) {
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
                String message = "mqtt 连接建立异常报警：" + errorMessages.toString();
                NotifyUtils.sendWeixin("18910556026",message);
            }

        }
    }


}

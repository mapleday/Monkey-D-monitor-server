package com.sohu.sns.monitor.mqtt.tasks;

import com.sohu.sns.monitor.mqtt.client.NettyClient;
import io.netty.channel.Channel;
import io.netty.handler.codec.mqtt.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * author:jy
 * time:16-11-9上午11:14
 * 定时连接
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
            for (int i = 0; i < connNums; i++) {
                Channel conn = null;
                try {
                    conn = NettyClient.conn(server, 180);
                    subscribe(conn);
                } catch (Exception e) {
                    e.printStackTrace();
                    System.out.println("报警");
                }

                if (conn != null && conn.isActive()) {
                    conn.close();
                } else {
                    System.out.println("报警");
                }
            }
        }

        /**
         * 订阅消息
         *
         * @param channel
         */
        private static void subscribe(Channel channel) {
            MqttFixedHeader fixedHeader = new MqttFixedHeader(MqttMessageType.SUBSCRIBE, false, MqttQoS.AT_LEAST_ONCE, false, 0);
            MqttMessageIdVariableHeader mqttMessageIdVariableHeader = MqttMessageIdVariableHeader.from(1);
            List<MqttTopicSubscription> subscriptions = new ArrayList<MqttTopicSubscription>();
            subscriptions.add(new MqttTopicSubscription("direct_message", MqttQoS.AT_LEAST_ONCE));
            subscriptions.add(new MqttTopicSubscription("sns_log_echo", MqttQoS.AT_LEAST_ONCE));
            subscriptions.add(new MqttTopicSubscription("sns_notification", MqttQoS.AT_LEAST_ONCE));
            subscriptions.add(new MqttTopicSubscription("sns_task", MqttQoS.AT_LEAST_ONCE));
            MqttSubscribePayload mqttSubscribePayload = new MqttSubscribePayload(subscriptions);
            MqttSubscribeMessage mqttSubscribeMessage = new MqttSubscribeMessage(fixedHeader, mqttMessageIdVariableHeader, mqttSubscribePayload);
            channel.writeAndFlush(mqttSubscribeMessage);
        }
    }


}

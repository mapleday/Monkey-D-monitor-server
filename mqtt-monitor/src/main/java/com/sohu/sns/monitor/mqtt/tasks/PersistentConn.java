package com.sohu.sns.monitor.mqtt.tasks;

import com.sohu.sns.monitor.common.utils.NotifyUtils;
import com.sohu.sns.monitor.mqtt.client.NettyClient;
import com.sohu.sns.monitor.mqtt.client.SimpleMqttMessage;
import com.sohu.snscommon.utils.LOGGER;
import com.sohu.snscommon.utils.constant.ModuleEnum;
import io.netty.channel.Channel;
import io.netty.handler.codec.mqtt.MqttSubscribeMessage;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * author:jy
 * time:16-11-9下午2:24
 * 持久连接，保证连接建立之后，应该是长期可用的，不被断开
 */
public class PersistentConn {
    private static final ScheduledExecutorService executorService = Executors.newScheduledThreadPool(1);
    private static List<Channel> conns = new ArrayList();
    private static final int MQTT_HEART_INTERVAL = 180;

    private PersistentConn() {

    }

    public static void start(int connNum, String server) {
        executorService.scheduleAtFixedRate(new ConnMonitor(connNum, server), 1, 30, TimeUnit.SECONDS);
    }

    private static class ConnMonitor implements Runnable {
        private int connNum;
        private String server;

        public ConnMonitor(int connNum, String server) {
            this.connNum = connNum;
            this.server = server;
        }

        @Override
        public void run() {
            synchronized (PersistentConn.class) {

                createConns(connNum, server);

                int errorTimes = 0;
                for (Iterator<Channel> iterator = conns.iterator(); iterator.hasNext(); ) {
                    Channel conn = iterator.next();
                    if (conn.isOpen()){
                        conn.writeAndFlush(SimpleMqttMessage.createPing());
                    }
                    if (conn.isActive()) {
                        conn.writeAndFlush(SimpleMqttMessage.createPing());
                    } else {
                        iterator.remove();
                        errorTimes++;
                    }
                }
                if (errorTimes > 0) {
                    String message = "mqtt 长连接异常断开.次数：" + errorTimes;
                    NotifyUtils.sendAlert("18910556026", message);
                }
            }
        }

        /**
         * 创建一个连接
         *
         * @param server
         * @return
         * @throws InterruptedException
         */
        private static Channel createConn(String server) throws InterruptedException {
            Channel conn = NettyClient.conn(server, MQTT_HEART_INTERVAL);
            MqttSubscribeMessage subscribe = SimpleMqttMessage.createSubscribe("direct_message", "sns_log_echo", "sns_notification", "sns_task");
            conn.writeAndFlush(subscribe);
            conns.add(conn);
            return conn;
        }

        /**
         * 创建一组连接
         *
         * @param connNum
         * @param server
         */
        private static void createConns(int connNum, String server) {
            if (conns.size() < connNum) {
                for (int i = 0; i < connNum - conns.size(); i++) {
                    try {
                        createConn(server);
                    } catch (Exception e) {
                        LOGGER.errorLog(ModuleEnum.MONITOR_SERVICE, "PersistentConn.createConns", server, "", e);
                    }
                }
            }
        }
    }


}

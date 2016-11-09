package com.sohu.sns.monitor.mqtt;

import com.sohu.sns.monitor.mqtt.tasks.PersistentConn;
import com.sohu.sns.monitor.mqtt.tasks.ScheduleConn;

/**
 * Created by jy on 16-9-2.
 * mqtt监控入口类
 */
public class MqttMonitorApp {
    private MqttMonitorApp() {
    }

    public static void main(String[] args) throws Exception {
        int connNum = Integer.parseInt(args[0]);
        String server = args[1];
        ScheduleConn.start(connNum, server);
        PersistentConn.start(2, server);
        System.in.read();
    }

    /**
     * 启动监控
     *
     * @param server 服务器地址
     */
    public static void start(String server) {
        ScheduleConn.start(4, server);
        PersistentConn.start(2, server);
    }


}

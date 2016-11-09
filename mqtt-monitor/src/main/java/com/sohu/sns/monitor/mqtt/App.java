package com.sohu.sns.monitor.mqtt;

import com.sohu.sns.monitor.mqtt.tasks.ScheduleConn;

/**
 * Created by jy on 16-9-2.
 * mqtt监控入口类
 */
public class App {
    private App() {
    }

    
    public static void main(String[] args) throws Exception {
        ScheduleConn.start(Integer.parseInt(args[0]), args[1]);
        System.in.read();
    }


}

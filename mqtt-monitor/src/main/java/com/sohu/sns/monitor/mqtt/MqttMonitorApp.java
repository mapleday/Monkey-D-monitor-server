package com.sohu.sns.monitor.mqtt;

import com.sohu.sns.monitor.mqtt.tasks.PersistentConn;
import com.sohu.sns.monitor.mqtt.tasks.ScheduleConn;
import com.sohu.snscommon.utils.LOGGER;
import com.sohu.snscommon.utils.config.ZkPathConfigure;
import com.sohu.snscommon.utils.constant.ModuleEnum;
import com.sohu.snscommon.utils.zk.SnsDiamonds;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.util.concurrent.CountDownLatch;

/**
 * Created by jy on 16-9-2.
 * mqtt监控入口类
 */
public class MqttMonitorApp {
    private MqttMonitorApp() {
    }

    public static void main(String[] args) throws Exception {
        ZkPathConfigure.ROOT_NODE="/sns";
        SnsDiamonds.setDiamondsEnvBySystem();
        int connNum = Integer.parseInt(args[0]);
        String server = args[1];
        ScheduleConn.start(connNum, server);
        PersistentConn.start(2, server);
        ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("mqttConsumer.xml");
        context.start();
        LOGGER.buziLog(ModuleEnum.MONITOR_SERVICE,"app","main","start ok");
        CountDownLatch cd = new CountDownLatch(1);
        cd.await();
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

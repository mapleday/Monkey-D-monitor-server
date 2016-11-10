package com.sohu.sns.monitor.app;

import com.sohu.sns.monitor.mqtt.MqttMonitorApp;
import com.sohu.snscommon.utils.zk.ZkUtils;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.io.IOException;

/**
 * author:jy
 * time:16-10-14下午3:36
 * 资源启动类
 */
public class APP {
    private APP() {
    }

    public static void main(String[] args) throws IOException {
        ClassPathXmlApplicationContext context = null;
        try {
            ZkUtils.setZkConfigFilePath(args[0]);
            ZkUtils.initZkConfig(args[0]);
            context = new ClassPathXmlApplicationContext("/RedisMonitorContext.xml", "/httpMonitorContext.xml");
            MqttMonitorApp.start("192.168.93.11:80");
            MqttMonitorApp.start("192.168.93.13:8888");
            MqttMonitorApp.start("192.168.93.14:8888");
            System.in.read();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (context != null) {
                context.close();
            }
        }
    }


}

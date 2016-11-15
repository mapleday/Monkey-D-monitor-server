package com.sohu.sns.monitor.app;

import com.sohu.sns.monitor.mqtt.MqttMonitorApp;
import com.sohu.snscommon.utils.LOGGER;
import com.sohu.snscommon.utils.constant.ModuleEnum;
import com.sohu.snscommon.utils.zk.SnsDiamonds;
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
            SnsDiamonds.setDiamondsEnvBySystem();
            context = new ClassPathXmlApplicationContext("/RedisMonitorContext.xml", "/httpMonitorContext.xml");
            MqttMonitorApp.start("192.168.93.11:80");
            MqttMonitorApp.start("cc.sns.sohusce.com:80");
            System.in.read();
        } catch (Exception e) {
            LOGGER.errorLog(ModuleEnum.MONITOR_SERVICE, "Monitor-app.app.main", null, null, e);
        } finally {
            if (context != null) {
                context.close();
            }
        }
    }


}

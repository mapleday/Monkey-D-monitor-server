package com.sohu.sns.monitor;

import com.sohu.sns.monitor.server.LogMessageProcessor;
import com.sohu.sns.monitor.server.MessageProcessor;
import com.sohu.snscommon.utils.zk.ZkUtils;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * Created by morgan on 15/9/22.
 */
public class SnsMonitorLogServer {

    public static void main(String[] args) {
        try {
            ZkUtils.setZkConfigFilePath(args[0]);
            ZkUtils.initZkConfig(args[0]);

            new ClassPathXmlApplicationContext("classpath:monitor/monitor-spring.xml");

            new LogMessageProcessor().start();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}

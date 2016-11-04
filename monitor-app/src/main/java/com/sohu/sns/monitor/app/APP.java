package com.sohu.sns.monitor.app;

import com.sohu.sns.monitor.redis.InitRedisConfig;
import com.sohu.sns.monitor.redis.schedule.RedisMonitorSchedule;
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
//
//    public static void main(String[] args) throws IOException {
//        ClassPathXmlApplicationContext context = null;
//        try {
//            context = new ClassPathXmlApplicationContext("/httpMonitorContext.xml");
//            System.in.read();
//        } finally {
//            if (context != null) {
//                context.close();
//            }
//        }
//    }
    public static void main(String[] args) throws IOException {

        InitRedisConfig.start();
    }


}

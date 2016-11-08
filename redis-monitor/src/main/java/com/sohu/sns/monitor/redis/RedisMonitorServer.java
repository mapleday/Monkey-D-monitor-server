package com.sohu.sns.monitor.redis;

import com.sohu.snscommon.utils.zk.ZkUtils;
import org.springframework.context.support.ClassPathXmlApplicationContext;


/**
 * Created by yzh on 2016/11/1.
 */
public class RedisMonitorServer {

    public static void main(String[] args) {
        ClassPathXmlApplicationContext context = null;
        try {
            ZkUtils.setZkConfigFilePath(args[0]);
            ZkUtils.initZkConfig(args[0]);
            context = new ClassPathXmlApplicationContext("/RedisMonitorContext.xml");
            System.in.read();
        }catch (Exception e){
            e.printStackTrace();
        }
        finally {
            if (context != null) {
                context.close();
            }
        }
    }
}

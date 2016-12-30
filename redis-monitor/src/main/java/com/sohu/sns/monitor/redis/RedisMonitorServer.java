package com.sohu.sns.monitor.redis;

import com.sohu.snscommon.utils.LOGGER;
import com.sohu.snscommon.utils.constant.ModuleEnum;
import com.sohu.snscommon.utils.zk.ZkUtils;
import org.springframework.context.support.ClassPathXmlApplicationContext;


/**
 * Created by yzh on 2016/11/1.
 */
public class RedisMonitorServer {

    private  RedisMonitorServer(){}

    public static void main(String[] args) {
        ClassPathXmlApplicationContext context = null;
        try {
            ZkUtils.setZkConfigFilePath(args[0]);
            ZkUtils.initZkConfig(args[0]);
            context = new ClassPathXmlApplicationContext("/redisMonitorContext.xml");
            System.in.read();
        }catch (Exception e){
            LOGGER.errorLog(ModuleEnum.MONITOR_SERVICE, "RedisMonitorServer", null, null, e);
        }
        finally {
            if (context != null) {
                context.close();
            }
        }
    }
}

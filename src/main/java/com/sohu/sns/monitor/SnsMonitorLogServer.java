package com.sohu.sns.monitor;

import com.sohu.sns.monitor.config.MySqlDBConfig;
import com.sohu.sns.monitor.server.LogMessageProcessor;
import com.sohu.sns.monitor.thread.ErrorLogProcessor;
import com.sohu.snscommon.dbcluster.service.impl.MysqlClusterServiceImpl;
import com.sohu.snscommon.utils.spring.SpringContextUtil;
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

            MysqlClusterServiceImpl bean = SpringContextUtil.getBean(MysqlClusterServiceImpl.class);
            bean.init(new MySqlDBConfig());

            new LogMessageProcessor().start();
            new Thread(new ErrorLogProcessor()).start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

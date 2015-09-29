package com.sohu.sns.monitor;

import com.sohu.sns.monitor.server.MessageProcessor;
import com.sohu.sns.monitor.server.config.MonitorDBConfig;
import com.sohu.snscommon.dbcluster.service.MysqlClusterService;
import com.sohu.snscommon.utils.spring.SpringContextUtil;
import com.sohu.snscommon.utils.zk.ZkUtils;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * Created by morgan on 15/9/22.
 */
public class SnsMonitorServer {

    public static void main(String[] args) {
        try {
            ZkUtils.setZkConfigFilePath(args[0]);
            ZkUtils.initZkConfig(args[0]);

            new ClassPathXmlApplicationContext("classpath:monitor/monitor-spring.xml");

            MysqlClusterService bean = SpringContextUtil.getBean(MysqlClusterService.class);
            bean.init(new MonitorDBConfig());

            new MessageProcessor().start();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}

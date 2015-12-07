package com.sohu.sns.monitor;

import com.sohu.sns.monitor.config.MySqlDBConfig;
import com.sohu.sns.monitor.server.ApiStatusProfessor;
import com.sohu.sns.monitor.server.LogMessageProcessor;
import com.sohu.sns.monitor.server.config.UNameMysqlClusterService;
import com.sohu.sns.monitor.thread.ErrorLogProcessor;
import com.sohu.sns.monitor.timer.DiffProcessor;
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

            /** 唯一名线上数据库数据源*/
            UNameMysqlClusterService uNameBean = SpringContextUtil.getBean(UNameMysqlClusterService.class);
            uNameBean.init(null);

            DiffProcessor processor = SpringContextUtil.getBean(DiffProcessor.class);
            processor.handle();

            new LogMessageProcessor().start();  //接收错误日志

            new ApiStatusProfessor().start();   //接收api使用情况日志

            new Thread(new ErrorLogProcessor(bean)).start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

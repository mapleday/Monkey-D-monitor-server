package com.sohu.sns.monitor;

import com.sohu.sns.monitor.config.MySqlDBConfig;
import com.sohu.sns.monitor.server.ApiStatusProfessor;
import com.sohu.sns.monitor.server.LogMessageProcessor;
import com.sohu.sns.monitor.thread.ErrorLogProcessor;
import com.sohu.snscommon.dbcluster.service.impl.MysqlClusterServiceImpl;
import com.sohu.snscommon.utils.spring.SpringContextUtil;
import com.sohu.snscommon.utils.zk.ZkUtils;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.Random;

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

            new LogMessageProcessor().start();  //接收错误日志

            /*下面的方法保证一台实例运行*/
            int flag = new Random().nextInt(10000);
            JdbcTemplate readJdbcTemplate = bean.getReadJdbcTemplate(null);
            JdbcTemplate writeJdbcTemplate = bean.getWriteJdbcTemplate(null);
            writeJdbcTemplate.update("update api_status set status = ? where id = 1", flag);
            Thread.currentThread().sleep(180000);
            if(flag == readJdbcTemplate.queryForObject("select status from api_status where id = 1", Integer.class)) {
                new ApiStatusProfessor().start();   //接收api使用情况日志
            }

            new Thread(new ErrorLogProcessor(bean)).start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

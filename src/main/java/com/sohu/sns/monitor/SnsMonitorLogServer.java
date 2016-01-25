package com.sohu.sns.monitor;

import com.sohu.sns.monitor.config.MySqlDBConfig;
import com.sohu.sns.monitor.server.LogMessageProcessor;
import com.sohu.sns.monitor.server.config.UNameMysqlClusterService;
import com.sohu.sns.monitor.thread.ErrorLogProcessor;
import com.sohu.sns.monitor.util.HttpApiServer;
import com.sohu.snscommon.dbcluster.service.impl.MysqlClusterServiceImpl;
import com.sohu.snscommon.utils.LOGGER;
import com.sohu.snscommon.utils.constant.ModuleEnum;
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

            /** 唯一名线上数据库数据源**/
            UNameMysqlClusterService uNameBean = SpringContextUtil.getBean(UNameMysqlClusterService.class);
            uNameBean.init(null);

            new LogMessageProcessor().start();  //接收错误日志
            new Thread(new ErrorLogProcessor(bean)).start();
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            new HttpApiServer(Integer.parseInt(args[1])).run();
        } catch (Exception e) {
            LOGGER.errorLog(ModuleEnum.MONITOR_SERVICE, "SnsMonitorLogServer.main", null, "SnsMonitorLogServer api start up failure !", e);
        }
    }
}

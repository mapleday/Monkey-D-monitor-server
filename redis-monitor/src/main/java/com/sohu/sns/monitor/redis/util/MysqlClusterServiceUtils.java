package com.sohu.sns.monitor.redis.util;

import com.sohu.sns.monitor.redis.config.MySqlDBConfig;
import com.sohu.snscommon.dbcluster.config.ClusterChangedPostProcessor;
import com.sohu.snscommon.dbcluster.config.MysqlClusterConfig;
import com.sohu.snscommon.dbcluster.service.MysqlClusterService;
import com.sohu.snscommon.dbcluster.service.exception.MysqlClusterException;
import com.sohu.snscommon.dbcluster.service.impl.MysqlClusterServiceImpl;
import com.sohu.snscommon.utils.LOGGER;
import com.sohu.snscommon.utils.constant.ModuleEnum;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * Created by wangzhenya on 16-11-4.
 */
public class MysqlClusterServiceUtils {

    private  MysqlClusterServiceUtils(){

    }

    private static MysqlClusterService mysqlClusterService;


    public static void init() {
        try {
            System.out.println("MysqlClusterServiceUtils 初始化");
            MysqlClusterConfig config = new MySqlDBConfig();
            mysqlClusterService =new MysqlClusterServiceImpl(config, ClusterChangedPostProcessor.NOTHING_PROCESSOR);
            System.out.println("MysqlClusterServiceUtils 初始化完成");
        } catch (Exception e) {
            LOGGER.errorLog(ModuleEnum.UTIL, "MysqlClusterServiceUtils.init", null, null, e);

            e.printStackTrace();
        }
    }

    public static JdbcTemplate getWriteJdbcTemplate() throws MysqlClusterException {
        return mysqlClusterService.getWriteJdbcTemplate("");
    }


    public static JdbcTemplate getReadJdbcTemplate() throws MysqlClusterException {
        return mysqlClusterService.getReadJdbcTemplate("");
    }

    public static void main(String args[]) {
        MysqlClusterServiceUtils.init();
    }
}

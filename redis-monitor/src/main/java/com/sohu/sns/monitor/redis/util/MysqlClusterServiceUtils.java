package com.sohu.sns.monitor.redis.util;

import com.sohu.sns.monitor.redis.config.MySqlDBConfig;
import com.sohu.snscommon.dbcluster.config.ClusterChangedPostProcessor;
import com.sohu.snscommon.dbcluster.config.MysqlClusterConfig;
import com.sohu.snscommon.dbcluster.service.MysqlClusterService;
import com.sohu.snscommon.dbcluster.service.exception.MysqlClusterException;
import com.sohu.snscommon.dbcluster.service.impl.MysqlClusterServiceImpl;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * Created by wangzhenya on 16-11-4.
 */
public class MysqlClusterServiceUtils {

    private static MysqlClusterService mysqlClusterService;


    public static void init() {
        try {
            MysqlClusterConfig config = new MySqlDBConfig();
            mysqlClusterService =new MysqlClusterServiceImpl(config, ClusterChangedPostProcessor.NOTHING_PROCESSOR);
//            mysqlClusterService.init(config);
        } catch (Exception e) {
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

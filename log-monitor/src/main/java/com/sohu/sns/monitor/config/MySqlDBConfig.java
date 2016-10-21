package com.sohu.sns.monitor.config;

import com.sohu.snscommon.dbcluster.config.MysqlClusterConfig;
import com.sohu.snscommon.utils.config.ZkPathConfigure;
import com.sohu.snscommon.utils.zk.SnsDiamonds;

/**
 * Created by morgan on 15/7/1.
 */
public class MySqlDBConfig extends MysqlClusterConfig {

    @Override
    public String getDBTableConfig() {
        return SnsDiamonds.getZkData(getDBTableConfigZkPath());
    }

    @Override
    public String getDBTableConfigZkPath() {
        return ZkPathConfigure.ROOT_NODE + "/sns_metric/metric_db_table";
    }

    @Override
    public String getReadDataSourcePrefix() {
        return "readDataSource_";
    }

    @Override
    public String getWriteDataSourcePrefix() {
        return "writeDataSource_";
    }

    @Override
    public String getDBClusterConfig() {
        return SnsDiamonds.getZkData(getDBClusterZkConfigPath());
    }

    @Override
    public String getDBClusterZkConfigPath() {
        return ZkPathConfigure.ROOT_NODE + "/sns_metric/metric_db";
    }
}

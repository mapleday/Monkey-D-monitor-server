package com.sohu.sns.monitor.server.config;

import com.sohu.snscommon.dbcluster.config.MysqlClusterConfig;
import com.sohu.snscommon.utils.config.ZkPathConfigure;
import com.sohu.snscommon.utils.zk.SnsDiamonds;

/**
 * Created by morgan on 15/9/25.
 */
public class MonitorDBConfig extends MysqlClusterConfig {
    @Override
    public String getDBTableConfig() {
        return SnsDiamonds.getZkData(getDBTableConfigZkPath());
    }

    @Override
    public String getDBTableConfigZkPath() {
        return ZkPathConfigure.ROOT_NODE + "/monitor/monitor_db_table";
    }

    @Override
    public String getReadDataSourcePrefix() {
        return "read-";
    }

    @Override
    public String getWriteDataSourcePrefix() {
        return "write-";
    }

    @Override
    public String getDBClusterConfig() {
        return SnsDiamonds.getZkData(getDBClusterZkConfigPath());
    }

    @Override
    public String getDBClusterZkConfigPath() {
        return ZkPathConfigure.ROOT_NODE + "/monitor/db_config";
    }
}

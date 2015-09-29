package com.sohu.sns.monitor.server.config;

import com.sohu.snscommon.dbcluster.config.DBClusterConfigFactory;
import com.sohu.snscommon.utils.LOGGER;
import com.sohu.snscommon.utils.config.ZkPathConfigure;
import com.sohu.snscommon.utils.constant.ModuleEnum;
import com.sohu.snscommon.utils.zk.ZkUtils;
import org.springframework.stereotype.Component;

/**
 * Created by morgan on 15/9/25.
 */
public class MonitorDBConfig extends DBClusterConfigFactory {
    @Override
    public String getDBClusterConfig() {
        ZkUtils zkUtils = new ZkUtils();
        try {
            zkUtils.connect(ZkPathConfigure.ZOOKEEPER_SERVERS,
                    ZkPathConfigure.ZOOKEEPER_AUTH_USER,
                    ZkPathConfigure.ZOOKEEPER_AUTH_PASSWORD,
                    ZkPathConfigure.ZOOKEEPER_TIMEOUT);
            byte[] data = zkUtils.getData(ZkPathConfigure.ROOT_NODE + "/monitor/db_config");
            if (data != null) {
                return new String(data, "UTF-8");
            }
        } catch (Exception e) {
            LOGGER.errorLog(ModuleEnum.METRIC, "MonitorDBConfig.getDBClusterConfig", null, null, e);
        }
        return null;
    }
}

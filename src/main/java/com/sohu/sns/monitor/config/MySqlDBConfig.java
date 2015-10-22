package com.sohu.sns.monitor.config;

import com.sohu.snscommon.dbcluster.config.DBClusterConfigFactory;
import com.sohu.snscommon.utils.LOGGER;
import com.sohu.snscommon.utils.config.ZkPathConfigure;
import com.sohu.snscommon.utils.constant.ModuleEnum;
import com.sohu.snscommon.utils.zk.ZkUtils;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Created by morgan on 15/7/1.
 */
public class MySqlDBConfig extends DBClusterConfigFactory {

    @Override
    public String getDBClusterConfig() {
        ZkUtils zkUtils = new ZkUtils();
        try {
            zkUtils.connect(ZkPathConfigure.ZOOKEEPER_SERVERS,
                    ZkPathConfigure.ZOOKEEPER_AUTH_USER,
                    ZkPathConfigure.ZOOKEEPER_AUTH_PASSWORD,
                    ZkPathConfigure.ZOOKEEPER_TIMEOUT);
            byte[] data = zkUtils.getData(ZkPathConfigure.ROOT_NODE + "/sns_metric/metric_db");
            if (data != null) {
                return new String(data, "UTF-8");
            }
        } catch (Exception e) {
            LOGGER.errorLog(ModuleEnum.METRIC,"MetricDBConfig.getConfig",null,null,e);
        }
        return null;
    }
}

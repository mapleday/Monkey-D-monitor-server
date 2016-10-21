package com.sohu.sns.monitor.server.config;

import com.sohu.snscommon.dbcluster.config.DBTableNumConfig;
import org.springframework.stereotype.Component;

/**
 * Created by morgan on 15/9/25.
 */
@Component
public class MonitorDBNumConfig extends DBTableNumConfig {

    @Override
    public int getTableNumPerDB() {
        return 1;
    }
}

package com.sohu.sns.monitor.httpmonitor.service;

import com.sohu.sns.monitor.httpmonitor.model.HttpResource;
import com.sohu.snscommon.dbcluster.service.MysqlClusterService;
import com.sohu.snscommon.dbcluster.service.exception.MysqlClusterException;
import com.sohu.snscommon.utils.LOGGER;
import com.sohu.snscommon.utils.constant.ModuleEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.ParameterizedBeanPropertyRowMapper;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Created by yzh on 2016/11/15.
 */
@Component
public class HttpResourceService {
    private final static String QUERY_RESOURCES ="SELECT id as id," +
            "            resource_name as resourceName," +
            "            resource_address as resourceAddress," +
            "            monitor_time_out as monitorTimeOut," +
            "            monitor_time_interval as monitorInterval," +
            "            monitor_times as monitorTimes," +
            "            alarm_threshold_times as alarmThresholdTimes" +
            "        FROM" +
            "          t_monitor_http_resource t" +
            "        WHERE" +
            "          t.status=1";
    @Autowired
    private MysqlClusterService mysqlClusterService;

    public List<HttpResource> getResources(){
        List<HttpResource> list = null;
        try {
            JdbcTemplate readTemplate= mysqlClusterService.getReadJdbcTemplate("");
            RowMapper<HttpResource> rm = ParameterizedBeanPropertyRowMapper.newInstance(HttpResource.class);
            list = readTemplate.query(QUERY_RESOURCES,rm);
            LOGGER.buziLog(ModuleEnum.MONITOR_SERVICE,"HttpResourceService.getResources","Got HttpResource...", "");
        } catch (MysqlClusterException e) {
            LOGGER.errorLog(ModuleEnum.MONITOR_SERVICE, "HttpResourceService.getResources", null, null, e);
        }
        return list;
    }
}

package com.sohu.sns.monitor.server.dao;

import com.sohu.sns.monitor.model.MethodLog;
import com.sohu.sns.monitor.model.MonitorUrl;
import com.sohu.snscommon.dbcluster.service.MysqlClusterService;
import com.sohu.snscommon.dbcluster.service.exception.MysqlClusterException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementSetter;
import org.springframework.stereotype.Component;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

/**
 * Created by morgan on 15/9/24.
 */
@Component("monitorDAO")
public class MonitorUrlDAO {

    @Autowired
    MysqlClusterService mysqlClusterService;

    public void saveMonitorUrl(List<MonitorUrl> monitorUrlList) {

    }

    public void saveMonitorUrl(final MonitorUrl monitorUrl) throws MysqlClusterException {
        if (monitorUrl == null) {
            return;
        }
        JdbcTemplate writeJdbcTemplate = mysqlClusterService.getWriteJdbcTemplate("");
        String sql = "insert into url_tarce_log(url_trace_id, url, app_id, " +
                "instance_id, consume_time, begin_time, end_time," +
                "method_count, params, result_length, has_exception)" +
                "values(?,?,?,?,?,?, ?,?,?, ?, ?);";
        writeJdbcTemplate.update(sql, new PreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps) throws SQLException {
                ps.setString(1, monitorUrl.getUrlTraceId());
                ps.setString(2, monitorUrl.getUrl());
                ps.setString(3, monitorUrl.getAppId());
                ps.setString(4, monitorUrl.getInstanceId());
                ps.setInt(5, monitorUrl.getConsumeTime());
                ps.setLong(6, monitorUrl.getBeginTime());
                ps.setLong(7, monitorUrl.getEndTime());
                ps.setInt(8, monitorUrl.getMethodCount());
                if (monitorUrl.getParams() == null) {
                    ps.setString(9, "");
                } else {
                    ps.setString(9, monitorUrl.getParams());
                }
                if (monitorUrl.getResultLength() == null) {
                    ps.setInt(10, 0);
                } else {
                    ps.setInt(10, monitorUrl.getResultLength());
                }

                ps.setShort(11, monitorUrl.getHasException());
            }
        });
    }

    public void saveMethodLog(final List<MethodLog> methodLogList) throws MysqlClusterException {
        if (methodLogList.isEmpty()) {
            return;
        }
        JdbcTemplate writeJdbcTemplate = mysqlClusterService.getWriteJdbcTemplate("");
        String sql = "INSERT INTO `method_trace_log`" +
                "(" +
                "`method_trace_id`," +
                "`class_name`," +
                "`method_name`," +
                "`consume_time`," +
                "`begin_time`," +
                "`end_time`," +
                "`url_trace_id`," +
                "`param`," +
                "`result`)" +
                "VALUES" +
                "( ?, ?, ?, ?, ?, ?, ?, ?, ?)";


        writeJdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                if (methodLogList.get(i).getMethodTraceId() != null) {
                    ps.setString(1, methodLogList.get(i).getMethodTraceId());
                } else {
                    ps.setString(1, "");
                }
                if (methodLogList.get(i).getClassName() != null) {
                    ps.setString(2, methodLogList.get(i).getClassName());
                } else {
                    ps.setString(2, "");
                }
                if (methodLogList.get(i).getMethodName() != null) {
                    ps.setString(3, methodLogList.get(i).getMethodName());
                } else {
                    ps.setString(3, "");
                }
                if (methodLogList.get(i).getConsumeTime() != null) {
                    ps.setInt(4, methodLogList.get(i).getConsumeTime());
                } else {
                    ps.setInt(4, 0);
                }
                if (methodLogList.get(i).getBeginTime() != null) {
                    ps.setLong(5, methodLogList.get(i).getBeginTime());
                } else {
                    ps.setLong(5, System.currentTimeMillis());
                }
                if (methodLogList.get(i).getEndTime() != null) {
                    ps.setLong(6, methodLogList.get(i).getEndTime());
                } else {
                    ps.setLong(6, System.currentTimeMillis());
                }
                ps.setString(7, methodLogList.get(i).getUrlTraceId());
                if (methodLogList.get(i).getParam() != null) {
                    ps.setString(8, methodLogList.get(i).getParam());
                } else {
                    ps.setString(8, "");
                }

                ps.setString(9, "");
            }

            @Override
            public int getBatchSize() {
                return methodLogList.size();
            }

        });
    }

}

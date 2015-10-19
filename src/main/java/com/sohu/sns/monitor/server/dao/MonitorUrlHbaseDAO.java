package com.sohu.sns.monitor.server.dao;

import com.google.common.base.Optional;
import com.sohu.sns.monitor.constant.MethodLogColumn;
import com.sohu.sns.monitor.constant.TableConstant;
import com.sohu.sns.monitor.constant.URLLogColumn;
import com.sohu.sns.monitor.model.MethodLog;
import com.sohu.sns.monitor.model.MonitorUrl;
import com.sohu.sns.monitor.util.HBaseTableUtil;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.util.Bytes;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by morgan on 15/10/14.
 */
@Repository("monitorUrlHBaseDAO")
public class MonitorUrlHBaseDAO {

    public void saveMonitorUrlLog(MonitorUrl monitorUrl) throws IOException {

        String tableName = TableConstant.getDailyUrlLogTable(
                Optional.fromNullable(monitorUrl.getBeginTime()).or(System.currentTimeMillis()));
        HBaseTableUtil.createTable(tableName, TableConstant.LOG_COL_FML);

        Put put = new Put(Bytes.toBytes(monitorUrl.getUrlTraceId()));

        put.add(Bytes.toBytes(TableConstant.LOG_COL_FML), Bytes.toBytes(URLLogColumn.urlTraceId), Bytes.toBytes(monitorUrl.getUrlTraceId()));
        put.add(Bytes.toBytes(TableConstant.LOG_COL_FML), Bytes.toBytes(URLLogColumn.url), Bytes.toBytes(monitorUrl.getUrl()));
        put.add(Bytes.toBytes(TableConstant.LOG_COL_FML), Bytes.toBytes(URLLogColumn.appId), Bytes.toBytes(monitorUrl.getAppId()));
        put.add(Bytes.toBytes(TableConstant.LOG_COL_FML), Bytes.toBytes(URLLogColumn.instanceId), Bytes.toBytes(monitorUrl.getInstanceId()));
        put.add(Bytes.toBytes(TableConstant.LOG_COL_FML), Bytes.toBytes(URLLogColumn.consumeTime), Bytes.toBytes(monitorUrl.getConsumeTime()));
        put.add(Bytes.toBytes(TableConstant.LOG_COL_FML), Bytes.toBytes(URLLogColumn.beginTime), Bytes.toBytes(monitorUrl.getBeginTime()));
        put.add(Bytes.toBytes(TableConstant.LOG_COL_FML), Bytes.toBytes(URLLogColumn.endTime), Bytes.toBytes(monitorUrl.getEndTime()));
        put.add(Bytes.toBytes(TableConstant.LOG_COL_FML), Bytes.toBytes(URLLogColumn.methodCount), Bytes.toBytes(monitorUrl.getMethodCount()));
        if (monitorUrl.getParams() == null) {
            put.add(Bytes.toBytes(TableConstant.LOG_COL_FML), Bytes.toBytes(URLLogColumn.params), Bytes.toBytes(""));
        } else {
            put.add(Bytes.toBytes(TableConstant.LOG_COL_FML), Bytes.toBytes(URLLogColumn.params), Bytes.toBytes(monitorUrl.getParams()));
        }
        if (monitorUrl.getResultLength() == null) {
            put.add(Bytes.toBytes(TableConstant.LOG_COL_FML), Bytes.toBytes(URLLogColumn.resultLength), Bytes.toBytes(0));
        } else {
            put.add(Bytes.toBytes(TableConstant.LOG_COL_FML), Bytes.toBytes(URLLogColumn.resultLength), Bytes.toBytes(monitorUrl.getResultLength()));
        }
        put.add(Bytes.toBytes(TableConstant.LOG_COL_FML), Bytes.toBytes(URLLogColumn.hasException), Bytes.toBytes(monitorUrl.getHasException()));

        HBaseTableUtil.savePut(tableName, put);

    }

    public void saveMethodUrlLog(List<MethodLog> methodLogList, Long time) throws IOException {
        if (methodLogList == null || methodLogList.isEmpty()) {
            return;
        }
        String tableName = TableConstant.getDailyMethodLogTable(Optional.fromNullable(time).or(System.currentTimeMillis()));
        HBaseTableUtil.createTable(tableName, TableConstant.LOG_COL_FML);
        List<Put> putList = new ArrayList<Put>(methodLogList.size());
        for (MethodLog methodLog : methodLogList) {
            Put put = new Put(Bytes.toBytes(methodLog.getMethodTraceId()));
            if (methodLog.getMethodTraceId() != null) {
                put.add(Bytes.toBytes(TableConstant.LOG_COL_FML), Bytes.toBytes(MethodLogColumn.methodTraceId), Bytes.toBytes(methodLog.getMethodTraceId()));
            } else {
                put.add(Bytes.toBytes(TableConstant.LOG_COL_FML), Bytes.toBytes(MethodLogColumn.methodTraceId), Bytes.toBytes(""));
            }
            if (methodLog.getClassName() != null) {
                put.add(Bytes.toBytes(TableConstant.LOG_COL_FML), Bytes.toBytes(MethodLogColumn.className), Bytes.toBytes(methodLog.getClassName()));
            } else {
                put.add(Bytes.toBytes(TableConstant.LOG_COL_FML), Bytes.toBytes(MethodLogColumn.className), Bytes.toBytes(""));
            }
            if (methodLog.getMethodName() != null) {
                put.add(Bytes.toBytes(TableConstant.LOG_COL_FML), Bytes.toBytes(MethodLogColumn.methodName), Bytes.toBytes(methodLog.getMethodName()));
            } else {
                put.add(Bytes.toBytes(TableConstant.LOG_COL_FML), Bytes.toBytes(MethodLogColumn.methodName), Bytes.toBytes(""));
            }
            if (methodLog.getConsumeTime() != null) {
                put.add(Bytes.toBytes(TableConstant.LOG_COL_FML), Bytes.toBytes(MethodLogColumn.consumeTime), Bytes.toBytes(methodLog.getConsumeTime()));
            } else {
                put.add(Bytes.toBytes(TableConstant.LOG_COL_FML), Bytes.toBytes(MethodLogColumn.consumeTime), Bytes.toBytes(0));
            }
            if (methodLog.getBeginTime() != null) {
                put.add(Bytes.toBytes(TableConstant.LOG_COL_FML), Bytes.toBytes(MethodLogColumn.beginTime), Bytes.toBytes(methodLog.getBeginTime()));
            } else {
                put.add(Bytes.toBytes(TableConstant.LOG_COL_FML), Bytes.toBytes(MethodLogColumn.beginTime), Bytes.toBytes(0));
            }
            if (methodLog.getEndTime() != null) {
                put.add(Bytes.toBytes(TableConstant.LOG_COL_FML), Bytes.toBytes(MethodLogColumn.endTime), Bytes.toBytes(methodLog.getEndTime()));
            } else {
                put.add(Bytes.toBytes(TableConstant.LOG_COL_FML), Bytes.toBytes(MethodLogColumn.endTime), Bytes.toBytes(0));
            }
            put.add(Bytes.toBytes(TableConstant.LOG_COL_FML), Bytes.toBytes(MethodLogColumn.urlTraceId), Bytes.toBytes(methodLog.getUrlTraceId()));
            if (methodLog.getParam() != null) {
                put.add(Bytes.toBytes(TableConstant.LOG_COL_FML), Bytes.toBytes(MethodLogColumn.param), Bytes.toBytes(methodLog.getParam()));
            } else {
                put.add(Bytes.toBytes(TableConstant.LOG_COL_FML), Bytes.toBytes(MethodLogColumn.param), Bytes.toBytes(""));
            }
            if (methodLog.getResult() != null) {
                put.add(Bytes.toBytes(TableConstant.LOG_COL_FML), Bytes.toBytes(MethodLogColumn.result), Bytes.toBytes(methodLog.getResult()));
            } else {
                put.add(Bytes.toBytes(TableConstant.LOG_COL_FML), Bytes.toBytes(MethodLogColumn.result), Bytes.toBytes(""));
            }

            putList.add(put);
        }


        HBaseTableUtil.savePutList(tableName, putList);
    }

}

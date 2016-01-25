package com.sohu.sns.monitor.service.ServiceImpl;

import com.sohu.sns.monitor.service.DelErrorLogsService;
import com.sohu.sns.monitor.util.DateUtil;
import com.sohu.snscommon.dbcluster.service.MysqlClusterService;
import com.sohu.snscommon.utils.LOGGER;
import com.sohu.snscommon.utils.constant.ModuleEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by Gary on 2016/1/25.
 */
@Component
public class DelErrorLogsServiceImpl implements DelErrorLogsService {

    @Autowired
    private MysqlClusterService mysqlClusterService;

    private final ExecutorService processor = Executors.newFixedThreadPool(1);

    private static final Integer DELETE_COUNT = 1000;
    private static final String QUERY_COUNT = "select count(1) from error_logs where updateTime < ?";
    private static final String DELETE_RECORD = "delete from error_logs where updateTime < ? limit ?";

    @Override
    public void deleteRecord() {

        processor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    Date date = DateUtil.getBeginDate(-2);
                    JdbcTemplate readJdbcTemplate = mysqlClusterService.getReadJdbcTemplate(null);
                    JdbcTemplate writeJdbcTemplate = mysqlClusterService.getWriteJdbcTemplate(null);
                    Integer result = readJdbcTemplate.queryForObject(QUERY_COUNT, Integer.class, date);
                    double count = Math.ceil(result.doubleValue() / DELETE_COUNT.doubleValue());
                    for (int i = 0; i < count; i++) {
                        writeJdbcTemplate.update(DELETE_RECORD, date, DELETE_COUNT);
                    }
                    LOGGER.buziLog(ModuleEnum.MONITOR_SERVICE, "delete.errorLogs", null, result.toString());
                } catch (Exception e) {
                    LOGGER.errorLog(ModuleEnum.MONITOR_SERVICE, "delete.errorLogs", null, null, e);
                    e.printStackTrace();
                }
            }
        });
    }
}

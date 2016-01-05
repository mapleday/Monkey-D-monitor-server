package com.sohu.sns.monitor.service.ServiceImpl;

import com.sohu.sns.monitor.service.CollectStatLogService;
import com.sohu.sns.monitor.timer.StatLogCollector;
import com.sohu.snscommon.dbcluster.service.exception.MysqlClusterException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.text.ParseException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by Gary on 2015/12/23.
 */
@Component
public class CollectStatLogServiceImpl implements CollectStatLogService {

    private final ExecutorService processor = Executors.newFixedThreadPool(1);

    @Autowired
    private StatLogCollector statLogCollector;

    @Override
    public void handle() throws Exception {
        processor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    statLogCollector.handle();
                } catch (MysqlClusterException e) {
                    e.printStackTrace();
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
        });
    }
}

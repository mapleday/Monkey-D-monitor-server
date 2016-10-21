package com.sohu.sns.monitor.service.ServiceImpl;

import com.sohu.sns.monitor.service.CollectStatLogService;
import com.sohu.sns.monitor.timer.StatLogVisitAnalyzer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by Gary on 2015/12/23.
 */
@Component
public class CollectStatLogServiceImpl implements CollectStatLogService {

    private final ExecutorService processor = Executors.newFixedThreadPool(1);

    @Autowired
    private StatLogVisitAnalyzer statLogVisitAnalyzer;

    @Override
    public void handle() throws Exception {
        processor.execute(new Runnable() {
            @Override
            public void run() {
                statLogVisitAnalyzer.handle();
            }
        });
    }
}

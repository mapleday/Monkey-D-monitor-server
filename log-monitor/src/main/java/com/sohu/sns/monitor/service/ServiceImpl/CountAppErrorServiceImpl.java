package com.sohu.sns.monitor.service.ServiceImpl;

import com.sohu.sns.monitor.service.CountAppErrorService;
import com.sohu.sns.monitor.timer.AppErrorCountProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by Gary on 2015/12/23.
 */
@Component
public class CountAppErrorServiceImpl implements CountAppErrorService {

    private ExecutorService processor = Executors.newFixedThreadPool(1);

    @Autowired
    private AppErrorCountProcessor appErrorCountProcessor;

    @Override
    public void handle() throws Exception {
        processor.execute(new Runnable() {
            @Override
            public void run() {
                appErrorCountProcessor.process();
            }
        });
    }
}

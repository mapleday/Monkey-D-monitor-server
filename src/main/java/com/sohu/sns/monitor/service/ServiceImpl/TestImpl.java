package com.sohu.sns.monitor.service.ServiceImpl;

import com.sohu.sns.monitor.service.Test;
import org.springframework.stereotype.Component;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by Gary on 2015/12/23.
 */

@Component
public class TestImpl implements Test {

    private final ExecutorService processor = Executors.newFixedThreadPool(1);

    @Override
    public void handle() throws Exception {
        processor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.currentThread().sleep(20000);
                    System.out.println("test");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
    }
}

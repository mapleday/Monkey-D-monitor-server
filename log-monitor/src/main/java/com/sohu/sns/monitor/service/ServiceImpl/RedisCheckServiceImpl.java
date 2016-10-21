package com.sohu.sns.monitor.service.ServiceImpl;

import com.sohu.sns.monitor.service.RedisCheckService;
import com.sohu.sns.monitor.timer.RedisDataCheckProfessor;
import com.sohu.snscommon.utils.LOGGER;
import com.sohu.snscommon.utils.constant.ModuleEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by Gary Chan on 2016/4/15.
 */
@Component
public class RedisCheckServiceImpl implements RedisCheckService {

    private final ExecutorService processor = Executors.newFixedThreadPool(1);

    @Autowired
    private RedisDataCheckProfessor redisDataCheckProfessor;

    @Override
    public void checkRedis() {
        processor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    redisDataCheckProfessor.handle();
                } catch (Exception e) {
                    LOGGER.errorLog(ModuleEnum.MONITOR_SERVICE, "RedisCheckServiceImpl.checkRedis", null, null, e);
                    e.printStackTrace();
                }
            }
        });
    }
}

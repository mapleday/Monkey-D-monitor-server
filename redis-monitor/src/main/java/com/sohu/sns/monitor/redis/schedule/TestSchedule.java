package com.sohu.sns.monitor.redis.schedule;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * author:jy
 * time:16-11-15下午4:28
 */
@Component
public class TestSchedule {

    @Scheduled(fixedRate = 10000)
    public void test() {
        System.out.println("test schedule");
    }
}

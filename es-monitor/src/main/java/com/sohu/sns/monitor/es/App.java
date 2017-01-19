package com.sohu.sns.monitor.es;

import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.util.concurrent.CountDownLatch;

/**
 * author:jy
 * time:17-1-18下午5:57
 */
public class App {
    public static void main(String[] args) throws InterruptedException {
        ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("esMonitorContext.xml");
        context.start();
        CountDownLatch cd = new CountDownLatch(1);
        cd.await();
    }
}

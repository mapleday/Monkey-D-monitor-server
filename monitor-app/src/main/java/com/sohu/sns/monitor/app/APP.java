package com.sohu.sns.monitor.app;

import com.sohu.snscommon.utils.LOGGER;
import com.sohu.snscommon.utils.constant.ModuleEnum;
import com.sohu.snscommon.utils.log.CustomRolloverFileOutputStream;
import com.sohu.snscommon.utils.zk.SnsDiamonds;
import com.sohu.snscommon.utils.zk.ZkUtils;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.io.IOException;
import java.io.PrintStream;
import java.util.TimeZone;
import java.util.concurrent.CountDownLatch;

/**
 * author:jy
 * time:16-10-14下午3:36
 * 资源启动类
 */
public class APP {
    private APP() {
    }

    public static void main(String[] args) throws IOException {
        ClassPathXmlApplicationContext context = null;
        try {
            if (args.length >= 2) {
                sysout(args[1]);
            }
            ZkUtils.setZkConfigFilePath(args[0]);
            ZkUtils.initZkConfig(args[0]);
            SnsDiamonds.setDiamondsEnvBySystem();
            context = new ClassPathXmlApplicationContext("applicationContext.xml");
            System.out.println("start ok !!!!!!");
            System.in.read();
            System.out.println("in !!!!!!");
            CountDownLatch countDownLatch = new CountDownLatch(1);
            countDownLatch.await();
            System.out.println("countDownLatch !!!!!!");
        } catch (Exception e) {
            LOGGER.errorLog(ModuleEnum.MONITOR_SERVICE, "Monitor-app.app.main", null, null, e);
        } finally {
            System.out.println("finally !!!!!!");
            if (context != null) {
                context.close();
            }
        }
    }

    private static void sysout(String appId) throws IOException {
        String logDir = System.getProperty("logdir", "/opt/logs/");
        String out = logDir + "/stdout_" + appId + ".log";
        String err = logDir + "/stderr_" + appId + ".log";
        System.setOut(new PrintStream(new CustomRolloverFileOutputStream(out, true, 31, TimeZone.getDefault(), "yyyy-MM-dd", null), false, "utf-8"));
        System.setErr(new PrintStream(new CustomRolloverFileOutputStream(err, true, 31, TimeZone.getDefault(), "yyyy-MM-dd", null), false, "utf-8"));

        System.out.println("appId is " + appId);
    }


}

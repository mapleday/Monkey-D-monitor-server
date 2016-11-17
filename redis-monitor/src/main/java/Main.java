import com.sohu.sns.monitor.redis.config.ZkPathConfig;
import com.sohu.sns.monitor.redis.util.DateUtil;
import com.sohu.sns.monitor.redis.util.ZkLockUtil;
import com.sohu.snscommon.utils.config.ZkPathConfigure;
import com.sohu.snscommon.utils.zk.SnsDiamonds;
import com.sohu.snscommon.utils.zk.ZkUtils;

import java.util.List;

/**
 * Created by yzh on 2016/11/10.
 */
public class Main {
    public static void main(String[] args) {

        try {
            ZkUtils.setZkConfigFilePath(args[0]);
            ZkUtils.initZkConfig(args[0]);
            ZkUtils zk = new ZkUtils();

            zk.connect(ZkPathConfigure.ZOOKEEPER_SERVERS, ZkPathConfigure.ZOOKEEPER_AUTH_USER,
                    ZkPathConfigure.ZOOKEEPER_AUTH_PASSWORD, ZkPathConfigure.ZOOKEEPER_TIMEOUT);
            SnsDiamonds.setDiamondsEnvBySystem();

            List<String> list= zk.getZooKeeper().getChildren(ZkPathConfig.LAST_EMAIL_TIME,false);
            System.out.println(list);
            for (int i = 0; i < 10; i++) {
                new Timer().start();
            }

           // new ZkLockUtil().test(ZkPathConfig.LAST_EMAIL_TIME_SUB);

            zk.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

class Timer extends Thread {
    public Timer() {

    }

    @Override
    public void run() {
        try {
            System.out.println(currentThread().getId());
            if(new ZkLockUtil().getLock(3540000L, ZkPathConfig.LAST_EMAIL_TIME,ZkPathConfig.LAST_EMAIL_TIME_SUB)){
                System.out.println(currentThread().getId()+" get lock");
            }
            System.out.println("xx");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

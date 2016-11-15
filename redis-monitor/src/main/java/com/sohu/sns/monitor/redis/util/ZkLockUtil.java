package com.sohu.sns.monitor.redis.util;

import com.sohu.sns.monitor.redis.config.ZkPathConfig;
import com.sohu.snscommon.utils.LOGGER;
import com.sohu.snscommon.utils.config.ZkPathConfigure;
import com.sohu.snscommon.utils.constant.ModuleEnum;
import com.sohu.snscommon.utils.zk.ZkUtils;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooDefs;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

/**
 * Created by yzh on 2016/11/10.
 */
public class ZkLockUtil {
    private String selfPath;
    private String waitPath;

    //private final static long DELAY_TIME = 3540000L;
    private final static long DELAY_TIME = 60000L;

    public boolean getLock() throws KeeperException, InterruptedException, IOException{
        ZkUtils zk = new ZkUtils();

        zk.connect(ZkPathConfigure.ZOOKEEPER_SERVERS, ZkPathConfigure.ZOOKEEPER_AUTH_USER,
                ZkPathConfigure.ZOOKEEPER_AUTH_PASSWORD, ZkPathConfigure.ZOOKEEPER_TIMEOUT);
        selfPath = zk.create(ZkPathConfig.LAST_TIME_SUB, null, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL);
        if (checkMinPath(zk)) {
            Long lastTime = Long.parseLong(new String(zk.getData(ZkPathConfig.LAST_TIME)));
            Long curTime = System.currentTimeMillis();
            System.out.println(curTime-lastTime);
            if(curTime-lastTime>=DELAY_TIME) {
                zk.setData(ZkPathConfig.LAST_TIME, curTime.toString().getBytes(), -1);
                zk.delete(selfPath);
                zk.close();
                return true;
            }
            zk.delete(selfPath);
            zk.close();
            return false;
        }
        zk.delete(selfPath);
        zk.close();
        return false;
    }
    private boolean checkMinPath(ZkUtils zk) throws KeeperException, InterruptedException{
        List<String> subNodes = zk.getZooKeeper().getChildren(ZkPathConfig.LAST_TIME,false);

        Collections.sort(subNodes);
        System.out.println(selfPath);
        System.out.println(subNodes);
        int index = subNodes.indexOf(selfPath.substring(ZkPathConfig.LAST_TIME.length()+1));
        switch (index){
            case -1:
                return false;
            case 0:
                return true;
            default:
                waitPath = ZkPathConfig.LAST_TIME+"/"+subNodes.get(index-1);
                if(!zk.exists(waitPath)){
                    return checkMinPath(zk);
                }else {
                    return false;
                }

        }

    }
}

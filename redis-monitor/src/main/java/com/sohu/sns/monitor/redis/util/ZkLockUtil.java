package com.sohu.sns.monitor.redis.util;

import com.sohu.sns.monitor.redis.config.ZkPathConfig;
import com.sohu.snscommon.utils.LOGGER;
import com.sohu.snscommon.utils.config.ZkPathConfigure;
import com.sohu.snscommon.utils.constant.ModuleEnum;
import com.sohu.snscommon.utils.zk.ZkUtils;
import org.apache.zookeeper.*;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

/**
 * Created by yzh on 2016/11/10.
 */
public class ZkLockUtil {
    private String selfPath;
    private String waitPath;

    /**
     *
     * @param delayTime
     * @param node
     * @param subNode
     * @return
     * @throws KeeperException
     * @throws InterruptedException
     * @throws IOException
     */
    public boolean getLock(long delayTime,String node,String subNode) throws KeeperException, InterruptedException, IOException{
        ZkUtils zk = new ZkUtils();

        zk.connect(ZkPathConfigure.ZOOKEEPER_SERVERS, ZkPathConfigure.ZOOKEEPER_AUTH_USER,
                ZkPathConfigure.ZOOKEEPER_AUTH_PASSWORD, ZkPathConfigure.ZOOKEEPER_TIMEOUT);
        selfPath = zk.create(subNode, null, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL);
        LOGGER.buziLog(ModuleEnum.UTIL,"ZkLockUtil.getLock","ephemeral_sequential_node"+selfPath,null);
        if (checkMinPath(zk,node)) {
            Long lastTime = Long.parseLong(new String(zk.getData(node)));
            Long curTime = System.currentTimeMillis();
            LOGGER.buziLog(ModuleEnum.UTIL,"ZkLockUtil.getLock","curTime-lastTime ="+(curTime-lastTime),null);
            if(curTime-lastTime>=delayTime) {
                zk.setData(node, curTime.toString().getBytes(), -1);
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
    private boolean checkMinPath(ZkUtils zk,String node) throws KeeperException, InterruptedException{
        List<String> subNodes = zk.getZooKeeper().getChildren(node,false);
        Collections.sort(subNodes);
        int index = subNodes.indexOf(selfPath.substring(node.length()+1));
        switch (index){
            case -1:
                return false;
            case 0:
                return true;
            default:
                waitPath = node+"/"+subNodes.get(index-1);
                if(!zk.exists(waitPath)){
                    return checkMinPath(zk,node);
                }else {
                    return false;
                }
        }
    }
}
